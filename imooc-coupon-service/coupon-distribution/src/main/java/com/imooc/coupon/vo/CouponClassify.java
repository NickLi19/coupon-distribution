package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {
    private List<Coupon> usable;
    private List<Coupon> used;
    private List<Coupon> expired;
    public static CouponClassify classify(List<Coupon> coupons) {
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());
        coupons.forEach(c -> {
            boolean isTimeExpire;
            long curTime = new Date().getTime();
            if(c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())) {
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline() <= curTime;
            } else {
                isTimeExpire = DateUtils.addDays(c.getAssignTime(), c.getTemplateSDK().getRule()
                        .getExpiration().getGap()).getTime() <= curTime;
            }
            if(c.getStatus() == CouponStatus.USED) {
                used.add(c);
            } else if(c.getStatus() == CouponStatus.EXPIRED || isTimeExpire) {
                expired.add(c);
            } else {
                usable.add(c);
            }
        });
        return new CouponClassify(usable, used, expired);
    }
}
