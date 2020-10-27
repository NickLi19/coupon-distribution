package com.imooc.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractExecutor {
    /**
     * Danpin
     * One Coupon
     * */
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        List<Integer> goodsType = settlement.getGoodsInfos().stream().map(GoodsInfo::getType)
                .collect(Collectors.toList());
        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0).getTemplate()
                .getRule().getUsage().getGoodsType(), List.class
        );
        return CollectionUtils.isNotEmpty(CollectionUtils.intersection(goodsType, templateGoodsType));
    }

    /**
     *
     * If goods type not match, then return settlementInfo that doesn't contains coupon
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlement, double goodsSum) {
        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy(settlement);
        if(!isGoodsTypeSatisfy) {
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }
        return null;
    }

    protected double goodsCostSum(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(g -> g.getPrice() * g.getCount()).sum();
    }

    protected double retain2Decimals(double value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    protected double minCost() {
        return 0.1;
    }
}
