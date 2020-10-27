package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {}, {}", userId, status);
        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());

        if(CollectionUtils.isEmpty(couponStrs)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream()
                .map(cs -> JSON.parseObject(cs, Coupon.class))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("all")
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save Empty List To Cache For User: {}, Status: {}", userId, JSON.toJSONString(status));
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);
                    operations.opsForHash().putAll(redisKey, invalidCouponMap);
                });
                return null;
            }
        };
        log.info("Pipeline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon Code: {}, {}, {}", templateId, redisKey, couponCode);
        return couponCode;
    }

    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        log.info("Add Coupon To Cache: {}, {}, {}", userId, JSON.toJSONString(coupons), status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch(couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }

        return result;
    }

    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {
        log.debug("Add Coupon To Cache For Usable");
        Map<String, String> needCachedObject = new HashMap<>();
        coupons.forEach(c -> needCachedObject.put(c.getId().toString(), JSON.toJSONString(c)));
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, needCachedObject);
        log.info("Add {} Coupons To Cache: {}, {}", needCachedObject.size(), userId, redisKey);

        redisTemplate.expire(redisKey, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
        return needCachedObject.size();
    }

    @SuppressWarnings("all")
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException{
        log.debug("Add Coupon To Cache For Used.");
        Map<String, String> needCachedForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> needCachedForUsed.put(c.getId().toString(), JSON.toJSONString(c)));
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal ToCache: {}, {}, {}", userId,
                    JSON.toJSONString(curUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache!");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {
                operations.opsForHash().putAll(redisKeyForUsed, needCachedForUsed);
                operations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());
                operations.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                operations.expire(redisKeyForUsed, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    @SuppressWarnings("all")
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons) throws CouponException {
        log.debug("Add Coupon To Cache For Expired.");
        Map<String, String> needCachedForExpired = new HashMap<>();
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());
        List<Coupon> curExpiredCoupons = getCachedCoupons(userId, CouponStatus.EXPIRED.getCode());
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> needCachedForExpired.put(c.getId().toString(), JSON.toJSONString(c)));
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}", userId,
                    JSON.toJSONString(curUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupon Is Not Equal To Cache.");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {
                operations.opsForHash().putAll(redisKeyForExpired, needCachedForExpired);
                operations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());
                operations.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                operations.expire(redisKeyForExpired, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    private Long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }
}
