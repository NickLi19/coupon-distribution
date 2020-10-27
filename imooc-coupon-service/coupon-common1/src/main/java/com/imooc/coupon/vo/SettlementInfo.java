package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {
    private Long userId;
    private List<GoodsInfo> goodsInfos;
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;
    private Boolean employ;
    private Double cost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponAndTemplateInfo {
        /** Coupon id */
        private Integer id;
        private CouponTemplateSDK template;
    }
}
