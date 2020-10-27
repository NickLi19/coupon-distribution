package com.imooc.coupon.executor;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@SuppressWarnings("all")
public class ExecuteManager implements BeanPostProcessor {

    private static Map<RuleFlag, RuleExecutor> executorIndex = new HashMap<>(RuleFlag.values().length);

    public SettlementInfo computeRule(SettlementInfo settlement) throws CouponException {
        SettlementInfo result = null;
        if(settlement.getCouponAndTemplateInfos().size() == 1) {
            CouponCategory category = CouponCategory.of(
                    settlement.getCouponAndTemplateInfos().get(0).getTemplate().getCategory());
            switch (category) {
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(settlement);
                    break;
                case ZHEKOU:
                    result = executorIndex.get(RuleFlag.ZHEKOU).computeRule(settlement);
                    break;
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN).computeRule(settlement);
                    break;
            }
        } else {
            List<CouponCategory> categories = new ArrayList<>(settlement.getCouponAndTemplateInfos().size());
            settlement.getCouponAndTemplateInfos().forEach(ct -> categories.add(CouponCategory.of(
                    ct.getTemplate().getCategory()
            )));
            if(categories.size() != 2) {
                throw new CouponException("Not Support For More Template Category");
            } else {
                if(categories.contains(CouponCategory.MANJIAN) && categories.contains(CouponCategory.ZHEKOU)) {
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlement);
                } else {
                    throw new CouponException("Not Support For Other Template Category");
                }
            }
        }
        return result;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if(executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("There is already an executor for rule flag");
        }

        log.info("Load executor {} for rule flag {}.", executor.getClass(), ruleFlag);
        executorIndex.put(ruleFlag, executor);
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
