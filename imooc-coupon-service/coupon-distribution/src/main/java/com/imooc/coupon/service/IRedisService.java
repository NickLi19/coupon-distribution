package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;

import java.util.List;

public interface IRedisService {
    List<Coupon> getCachedCoupons(Long userId, Integer status);
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);
    String tryToAcquireCouponCodeFromCache(Integer templateId);
    Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException;
}
