package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {

    /** {@link com.imooc.coupon.constant.GoodsType} **/
    private Integer type;
    private Double price;
    private Integer count;

}
