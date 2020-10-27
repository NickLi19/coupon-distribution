package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final CouponDao couponDao;
    private final IRedisService redisService;
    private final TemplateClient templateClient;
    private final SettlementClient settlementClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService,
                           TemplateClient templateClient,
                           SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        List<Coupon> curCached = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;
        if(CollectionUtils.isNotEmpty(curCached)) {
            log.debug("coupon cache is not empty: {}, {}", userId, status);
            preTarget = curCached;
        } else {
            log.debug("coupon cache is empty, get coupon from db: {}, {}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));
            if(CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("current user do not have coupon: {}, {}", userId, status);
                return dbCoupons;
            }
            Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(dbCoupons.stream()
                    .map(Coupon::getTemplateId).collect(Collectors.toList())).getData();
            dbCoupons.forEach(dc -> dc.setTemplateSDK(id2TemplateSDK.get(dc.getTemplateId())));
            preTarget = dbCoupons;
            redisService.addCouponToCache(userId, preTarget, status);
        }
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());
        if(CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            if(CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add Expired Coupons To Cache From FindCouponByStatus: {}, {}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());
                //Send to Kafka which write back data to database
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                        CouponStatus.EXPIRED.getCode(),
                        classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList())
                )));
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();
        log.debug("Find All Template(From TemplateClient) Count: {}", templateSDKS.size());
        templateSDKS = templateSDKS.stream().filter(
                t -> t.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());
        log.info("Find Usable Template Count: {}", templateSDKS.size());

        // key: TemplateId
        // value: left: Template Coupon Limitation, right: CouponTemplateSDK
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(t -> limit2Template.put(t.getId(), Pair.of(t.getRule().getLimitation(), t)));
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());
        log.debug("Current User Has Usable Coupons: {}, {}", userId, userUsableCoupons.size());
        // key: TemplateId
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        limit2Template.forEach((k, v) -> {
            int limitation = v.getKey();
            CouponTemplateSDK templateSDK = v.getRight();
            if(templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2Template = templateClient
                .findIds2TemplateSDK(Collections.singletonList(request.getTemplateSDK().getId())).getData();
        if(id2Template.size() <= 0) {
            log.error("Can Not Acquire Template From TemplateClient: {}", request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                request.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        if(templateId2Coupons.containsKey(request.getTemplateSDK().getId()) &&
                templateId2Coupons.get(request.getTemplateSDK().getId()).size()
                        >= request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed Template Assign Limitation: {}", request.getTemplateSDK().getId());
            throw new CouponException("Excceed Template Assign Limitation");
        }
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
        if(StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Acquire Coupon Code: {}", request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(), couponCode, CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        redisService.addCouponToCache(request.getUserId(), Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode());
        return newCoupon;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        // there is no coupon, return total cost of goods
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        if(CollectionUtils.isEmpty(ctInfos)) {
            log.info("Empty Coupons For Settle.");
            double goodsSum = 0.0;
            for(GoodsInfo gi: info.getGoodsInfos()) {
                goodsSum += gi.getPrice() * gi.getCount();
            }
            info.setCost(retain2Decimals(goodsSum));
        }
        List<Coupon> coupons = findCouponsByStatus(info.getUserId(), CouponStatus.USABLE.getCode());
        Map<Integer, Coupon> id2Coupon = coupons.stream().collect(Collectors.toMap(Coupon::getId, Function.identity()));
        if(MapUtils.isEmpty(id2Coupon) || // Some problems
                CollectionUtils.isSubCollection(ctInfos.stream()
                        .map(SettlementInfo.CouponAndTemplateInfo::getId)
                        .collect(Collectors.toList()), id2Coupon.keySet())) {
            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream().
                    map(SettlementInfo.CouponAndTemplateInfo::getId).
                    collect(Collectors.toList()));
            log.error("User Coupon Has Some Problems, It's not SubCollection Of Coupons!");
            throw new CouponException("User Coupon Has Some Problems, It's not SubCollection Of Coupons!");
        }
        log.debug("Current Settlement Coupons Is User's {}", ctInfos.size());
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));
        SettlementInfo processedInfo = settlementClient.computeRule(info).getData();
        if(processedInfo.getEmploy() && CollectionUtils.isNotEmpty(processedInfo.getCouponAndTemplateInfos())) {
            log.info("Settle User Coupon: {}, {}", info.getUserId(), JSON.toJSONString(settleCoupons));
            redisService.addCouponToCache(info.getUserId(), settleCoupons, CouponStatus.USED.getCode());
            kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(CouponStatus.USED.getCode(),
                    settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList()))));
        }
        return processedInfo;
    }

    private double retain2Decimals(double value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
