package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;

public interface IAsyncService {
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
