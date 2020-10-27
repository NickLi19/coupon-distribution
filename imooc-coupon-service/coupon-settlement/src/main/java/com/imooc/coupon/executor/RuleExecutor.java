package com.imooc.coupon.executor;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.vo.SettlementInfo;

public interface RuleExecutor {
    RuleFlag ruleConfig();
    SettlementInfo computeRule(SettlementInfo settlement);
}
