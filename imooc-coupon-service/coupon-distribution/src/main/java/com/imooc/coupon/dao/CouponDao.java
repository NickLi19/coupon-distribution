package com.imooc.coupon.dao;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponDao extends JpaRepository<Coupon, Integer> {
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
    List<Coupon> findAllByUserId(Long userId);
}
