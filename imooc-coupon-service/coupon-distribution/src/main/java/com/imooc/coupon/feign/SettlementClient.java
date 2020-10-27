package com.imooc.coupon.feign;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.hystrix.SettlementClientHystrix;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value="eureka-client-coupon-settlement", fallback= SettlementClientHystrix.class)
public interface SettlementClient {

    @RequestMapping(value="/coupon-settlement/settlement/compute", method=RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(@RequestBody SettlementInfo settlement) throws CouponException;
}
