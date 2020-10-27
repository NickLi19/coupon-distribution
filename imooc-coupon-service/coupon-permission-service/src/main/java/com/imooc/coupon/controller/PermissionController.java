package com.imooc.coupon.controller;

import com.imooc.coupon.annotation.IngoreResponseAdvice;
import com.imooc.coupon.service.PathService;
import com.imooc.coupon.service.PermissionService;
import com.imooc.coupon.vo.CheckPermissionRequest;
import com.imooc.coupon.vo.CreatePathRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PermissionController {
    private final PathService pathService;
    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PathService pathService, PermissionService permissionService) {
        this.pathService = pathService;
        this.permissionService = permissionService;
    }

    @PostMapping("/create/path")
    public List<Integer> createPath(@RequestBody CreatePathRequest request) {
        log.info("createPath: {}", request.getPathInfos().size());
        return pathService.createPath(request);
    }

    @IngoreResponseAdvice
    @PostMapping("/check/permission")
    public Boolean checkPermission(@RequestBody CheckPermissionRequest request) {
        log.info("checkPermission for args: {}, {}, {}", request.getUserId(), request.getUri(), request.getHttpMethod());
        return permissionService.checkPermission(request.getUserId(), request.getUri(), request.getHttpMethod());
    }
}
