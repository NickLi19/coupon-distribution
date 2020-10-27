package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleFlag {

    // one category coupon
    MANJIAN("manjian rule"),
    ZHEKOU("zhekou rule"),
    LIJIAN("lijian rule"),

    // multiple categories coupon
    MANJIAN_ZHEKOU("manjian + zhekou");

    private String description;

}
