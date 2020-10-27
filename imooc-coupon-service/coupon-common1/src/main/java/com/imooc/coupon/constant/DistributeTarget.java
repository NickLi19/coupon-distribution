package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum DistributeTarget {
    SINGLE("Single User", 1),
    MULTI("Multiple Users", 2);

    private String description;
    private Integer code;

    public static DistributeTarget of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values()).filter(bean -> bean.code.equals(code)).findAny().
                orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}
