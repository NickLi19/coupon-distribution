package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

public interface IUserService {
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
