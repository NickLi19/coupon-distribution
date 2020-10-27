package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CouponCategory {
    MANJIAN("Based off coupon", "001"),
    ZHEKOU("Discount coupon", "002"),
    LIJIAN("Off coupon", "003");

    private String description;
    private String code;

    public static CouponCategory of(String code) {
        Objects.requireNonNull(code);
        return Stream.of(values()).filter(bean -> bean.code.equals(code)).findAny().
                orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}
