package com.imooc.coupon;

import com.imooc.coupon.annotation.IgnorePermission;
import com.imooc.coupon.annotation.ImoocCouponPermission;
import com.imooc.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class AnnotationScanner {
    private String pathPrefix;
    private static final String IMOOC_COUPON_PKG = "com.imooc.coupon";

    AnnotationScanner(String prefix) {
        this.pathPrefix = trimPath(prefix);
    }

    List<PermissionInfo> scanPermission(Map<RequestMappingInfo, HandlerMethod> mappingMap) {
        List<PermissionInfo> result = new ArrayList<>();
        mappingMap.forEach((mapInfo, method) -> result.addAll(buildPermission(mapInfo, method)));
        return result;
    }

    private List<PermissionInfo> buildPermission(RequestMappingInfo mapInfo, HandlerMethod handlerMethod) {
        Method javaMethod = handlerMethod.getMethod();
        Class baseClass = javaMethod.getDeclaringClass();

        if(!isImmocCouponPackage(baseClass.getName())) {
            log.debug("ignore method: {}", javaMethod.getName());
            return Collections.emptyList();
        }
        IgnorePermission ignorePermission = javaMethod.getAnnotation(IgnorePermission.class);
        if(ignorePermission != null) {
            log.debug("ignore method: {}", javaMethod.getName());
        }

        ImoocCouponPermission couponPermission = javaMethod.getAnnotation(ImoocCouponPermission.class);
        if(couponPermission == null) {
            log.error("lack @ImoocCouponPermission -> {}#{}", javaMethod.getDeclaringClass().getName(),
                    javaMethod.getName());
            return Collections.emptyList();
        }

        Set<String> urlSet = mapInfo.getPatternsCondition().getPatterns();
        boolean isAllMethods = false;
        Set<RequestMethod> methodSet = mapInfo.getMethodsCondition().getMethods();
        if(CollectionUtils.isEmpty(methodSet)) {
            isAllMethods = true;
        }
        List<PermissionInfo> infoList = new ArrayList<>();
        for(String url: urlSet) {
            if(isAllMethods) {
                PermissionInfo info = buildPermissionInfo(HttpMethodEnum.ALL.name(), javaMethod.getName(),
                        this.pathPrefix + url, couponPermission.readOnly(), couponPermission.description(),
                        couponPermission.extra());
                infoList.add(info);
                continue;
            }

            for(RequestMethod method: methodSet) {
                PermissionInfo info = buildPermissionInfo(method.name(), javaMethod.getName(), this.pathPrefix + url,
                        couponPermission.readOnly(), couponPermission.description(), couponPermission.extra());
                infoList.add(info);
                log.info("permission detected {}", info);
            }
        }
        return infoList;
    }

    private PermissionInfo buildPermissionInfo(
        String reqMethod, String javaMethod, String path, boolean readOnly, String desp, String extra
    ) {
        PermissionInfo info = new PermissionInfo();
        info.setMethod(reqMethod);
        info.setUrl(path);
        info.setIsRead(readOnly);
        info.setDescription(StringUtils.isEmpty(desp) ? javaMethod : desp);
        info.setExtra(extra);
        return info;
    }

    private boolean isImmocCouponPackage(String className) {
        return className.startsWith(IMOOC_COUPON_PKG);
    }

    private String trimPath(String path) {
        if(StringUtils.isEmpty(path)) {
            return "";
        }
        if(!path.startsWith("/")) {
            path = "/" + path;
        }
        if(path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }
}
