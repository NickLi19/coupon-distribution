package com.imooc.coupon;

import com.imooc.coupon.permission.PermissionClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.CreatePathRequest;
import com.imooc.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PermissionRegistry {

    private PermissionClient permissionClient;
    private String serviceName;

    PermissionRegistry(PermissionClient permissionClient, String serviceName) {
        this.permissionClient = permissionClient;
        this.serviceName = serviceName;
    }

    boolean register(List<PermissionInfo> infoList) {
        if(CollectionUtils.isEmpty(infoList)) {
            return false;
        }
        List<CreatePathRequest.PathInfo> pathInfos = infoList.stream().map(info -> CreatePathRequest.PathInfo.builder()
        .pathPattern(info.getUrl()).httpMethod(info.getMethod()).pathName(info.getDescription()).serviceName(serviceName)
        .opMode(info.getIsRead() ? OpModeEnum.READ.name() : OpModeEnum.WRITE.name()).build()).collect(Collectors.toList());
        CommonResponse<List<Integer>> response = permissionClient.createPath(new CreatePathRequest(pathInfos));

        if(!CollectionUtils.isEmpty(response.getData())) {
            log.info("register path info: {}", response.getData());
            return true;
        }
        return false;
    }
}
