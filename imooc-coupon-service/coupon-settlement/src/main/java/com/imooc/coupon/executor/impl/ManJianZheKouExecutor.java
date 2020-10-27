package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    @Override
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        log.debug("Check ManJian And ZheKou Is Match Or Not!");
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(JSON.parseObject(
                    ct.getTemplate().getRule().getUsage().getGoodsType(),
                    List.class
            ));
        });
        return CollectionUtils.isEmpty(CollectionUtils.subtract(goodsType, templateGoodsType));
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if(probability != null) {
            log.debug("ManJian And ZheKou Template Is Not Match To GoodsType!");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zheKou = null;
        for(SettlementInfo.CouponAndTemplateInfo ct: settlement.getCouponAndTemplateInfos()) {
            if(CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.MANJIAN) {
                manJian = ct;
            }
            else {
                zheKou = ct;
            }
        }
        assert manJian != null;
        assert zheKou != null;

        if(!isTemplateCanShared(manJian, zheKou)) {
            log.debug("Current ManJian And ZheKou Can Not Shared!");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manJianBase = (double) manJian.getTemplate().getRule().getDiscount().getBase();
        double manJianQuata = (double) manJian.getTemplate().getRule().getDiscount().getQuota();

        double targetSum = goodsSum;
        if(targetSum >= manJianBase) {
            targetSum -= manJianQuata;
            ctInfos.add(manJian);
        }

        double zheKouQuota = (double) zheKou.getTemplate().getRule().getDiscount().getQuota();
        targetSum *= zheKouQuota * 1.0 / 100;
        ctInfos.add(zheKou);

        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(retain2Decimals(Math.max(targetSum, minCost())));

        log.debug("Use ManJian And ZheKou Coupon Make Goods Cost From {} To {}", goodsSum, settlement.getCost());

        return settlement;
    }

    @SuppressWarnings("all")
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                                        SettlementInfo.CouponAndTemplateInfo zheKou) {
        String manjianKey = manJian.getTemplate().getKey() + String.format("%04d", manJian.getTemplate().getId());
        String zhekouKey = zheKou.getTemplate().getKey() + String.format("%04d", zheKou.getTemplate().getId());
        List<String> allSharedKeysForManjian = new ArrayList<>();
        allSharedKeysForManjian.add(manjianKey);
        allSharedKeysForManjian.addAll(JSON.parseObject(
                manJian.getTemplate().getRule().getWeight(), List.class
        ));
        List<String> allSharedKeysForZhekou = new ArrayList<>();
        allSharedKeysForZhekou.add(zhekouKey);
        allSharedKeysForZhekou.addAll(JSON.parseObject(
                zheKou.getTemplate().getRule().getWeight(), List.class
        ));
        return CollectionUtils.isSubCollection(Arrays.asList(manjianKey, zhekouKey), allSharedKeysForManjian) ||
                CollectionUtils.isSubCollection(Arrays.asList(manjianKey, zhekouKey), allSharedKeysForZhekou);
    }
}
