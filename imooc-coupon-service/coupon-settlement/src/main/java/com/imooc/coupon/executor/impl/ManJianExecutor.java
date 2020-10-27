package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if(probability != null) {
            log.debug("ManJian Template Is Not MATCH tO GoodsType!");
            return probability;
        }

        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double base = (double) templateSDK.getRule().getDiscount().getBase();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();
        if(goodsSum < base) {
            log.debug("Current Goods Cose Sum < ManJian Coupon Base!");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        settlement.setCost(retain2Decimals(Math.max((goodsSum - quota), minCost())));
        log.debug("Use ManJian Coupon Make Goods Cost From{} to {}", goodsSum, settlement.getCost());

        return settlement;
    }
}
