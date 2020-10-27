package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.GoodsType;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.executor.ExecuteManager;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecuteManagerTest {
    private Long fakeUserId = 20001L;

    @Autowired
    private ExecuteManager manager;

    @Test
    private void testComputeRule() throws CouponException {
        log.info("ManJian Coupon Executor Test");
        SettlementInfo manjianInfo = fakeManJianCouponSettlement();
        SettlementInfo result = manager.computeRule(manjianInfo);
        log.info("{}", result.getCost());
        log.info("{}", result.getCouponAndTemplateInfos().size());
        log.info("{}", result.getCouponAndTemplateInfos());

        log.info("ZheKou Coupon Executor Test");
        SettlementInfo zhekouInfo = fakeZheKouCouponSettlement();
        SettlementInfo result2 = manager.computeRule(zhekouInfo);
        log.info("{}", result2.getCost());
        log.info("{}", result2.getCouponAndTemplateInfos().size());
        log.info("{}", result2.getCouponAndTemplateInfos());

        log.info("LiJian Coupon Executor Test");
        SettlementInfo lijianInfo = fakeZheKouCouponSettlement();
        SettlementInfo result3 = manager.computeRule(lijianInfo);
        log.info("{}", result3.getCost());
        log.info("{}", result3.getCouponAndTemplateInfos().size());
        log.info("{}", result3.getCouponAndTemplateInfos());

        log.info("ManJian ZheKou Coupon Executor Test");
        SettlementInfo manjianZheKouinfo = fakeZheKouCouponSettlement();
        SettlementInfo result4 = manager.computeRule(manjianZheKouinfo);
        log.info("{}", result4.getCost());
        log.info("{}", result4.getCouponAndTemplateInfos().size());
        log.info("{}", result4.getCouponAndTemplateInfos());
    }

    private SettlementInfo fakeManJianCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));
        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.MANJIAN.getCode());
        templateSDK.setKey("100120190801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(20, 199));
        rule.setUsage(new TemplateRule.Usage("Texas", "Dallas", JSON.
                toJSONString(Arrays.asList(GoodsType.WENYU.getCode(), GoodsType.JIAJU.getCode()))));
        templateSDK.setRule(rule);

        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));
        return info;
    }

    private SettlementInfo fakeZheKouCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(2);
        templateSDK.setCategory(CouponCategory.ZHEKOU.getCode());
        templateSDK.setKey("100220190712");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(85, 1));
        rule.setUsage(new TemplateRule.Usage("Texas", "Dallas",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(), GoodsType.JIAJU.getCode()))));

        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));
        return info;
    }

    private SettlementInfo fakeLiJianCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(3);
        templateSDK.setCategory(CouponCategory.LIJIAN.getCode());
        templateSDK.setKey("200320190712");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setUsage(new TemplateRule.Usage("Texas", "Dallas",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(), GoodsType.JIAJU.getCode()))));
        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));
        return info;
    }

    private SettlementInfo fakeManJinAndZheKouCouponSettlement() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo manjianInfo = new SettlementInfo.CouponAndTemplateInfo();
        manjianInfo.setId(1);
        CouponTemplateSDK manjianTemplate = new CouponTemplateSDK();
        manjianTemplate.setId(1);
        manjianTemplate.setCategory(CouponCategory.MANJIAN.getCode());
        manjianTemplate.setKey("100120190712");

        TemplateRule manjianRule = new TemplateRule();
        manjianRule.setDiscount(new TemplateRule.Discount(20, 199));
        manjianRule.setUsage(new TemplateRule.Usage("Texas", "Dallas",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(), GoodsType.JIAJU.getCode()))));
        manjianRule.setWeight(JSON.toJSONString(Collections.emptyList()));
        manjianTemplate.setRule(manjianRule);
        manjianInfo.setTemplate(manjianTemplate);

        SettlementInfo.CouponAndTemplateInfo zhekouInfo = new SettlementInfo.CouponAndTemplateInfo();
        zhekouInfo.setId(1);
        CouponTemplateSDK zhekouTemplate = new CouponTemplateSDK();
        zhekouTemplate.setId(2);
        zhekouTemplate.setCategory(CouponCategory.ZHEKOU.getCode());
        zhekouTemplate.setKey("100220190712");

        TemplateRule zhekouRule = new TemplateRule();
        zhekouRule.setDiscount(new TemplateRule.Discount(85, 1));
        zhekouRule.setUsage(new TemplateRule.Usage("Texas", "Dallas",
                JSON.toJSONString(Arrays.asList(GoodsType.WENYU.getCode(), GoodsType.JIAJU.getCode()))));
        zhekouRule.setWeight(JSON.toJSONString(Collections.singleton("1001201907120001")));
        zhekouTemplate.setRule(zhekouRule);
        zhekouInfo.setTemplate(zhekouTemplate);

        info.setCouponAndTemplateInfos(Arrays.asList(manjianInfo, zhekouInfo));
        return info;
    }
}
