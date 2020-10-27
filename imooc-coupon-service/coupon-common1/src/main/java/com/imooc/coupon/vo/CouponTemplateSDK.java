package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateSDK {
    private Integer id;
    private String name;
    private String logo;
    private String desc;
    private String category;
    private Integer productLine;
    private String key;
    private Integer target;
    private TemplateRule rule;
}
