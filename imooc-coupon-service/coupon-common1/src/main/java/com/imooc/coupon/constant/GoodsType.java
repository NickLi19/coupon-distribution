package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum GoodsType {
    WENYU("Wenyu", 1),
    SHENGXIAN("Shengxian", 2),
    JIAJU("Jiaju", 3),
    OTHERS("Others", 4),
    ALL("All", 5);

    private String description;
    private Integer code;

    public static GoodsType of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}
