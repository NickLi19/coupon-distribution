package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IAsyncService;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {

    private final IAsyncService asyncService;

    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {

        if(!request.validate()) {
            throw new CouponException("BuildTemplate Param Is Not Valid!");
        }

        if(templateDao.findByName(request.getName()) != null) {
            throw new CouponException("Exist Same Name Template");
        }

        CouponTemplate template = requestToTemplate(request);
        template = templateDao.save(template);
        asyncService.asyncConstructCouponByTemplate(template);

        return template;
    }

    private CouponTemplate requestToTemplate(TemplateRequest request) {
        return new CouponTemplate(request.getName(), request.getLogo(), request.getDesc(), request.getCategory(),
                                  request.getProductLine(), request.getCount(), request.getUserId(), request.getTarget(),
                                  request.getRule());
    }
}
