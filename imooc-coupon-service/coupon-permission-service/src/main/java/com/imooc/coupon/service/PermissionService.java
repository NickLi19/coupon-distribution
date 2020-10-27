package com.imooc.coupon.service;

import com.imooc.coupon.constant.RoleEnum;
import com.imooc.coupon.dao.PathRepository;
import com.imooc.coupon.dao.RolePathMappingRepository;
import com.imooc.coupon.dao.RoleRepository;
import com.imooc.coupon.dao.UserRoleMappingRepository;
import com.imooc.coupon.entity.Path;
import com.imooc.coupon.entity.Role;
import com.imooc.coupon.entity.RolePathMapping;
import com.imooc.coupon.entity.UserRoleMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PermissionService {
    private final PathRepository pathRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final RolePathMappingRepository rolePathMappingRepository;

    @Autowired
    public PermissionService(PathRepository pathRepository, RoleRepository roleRepository,
                             UserRoleMappingRepository userRoleMappingRepository,
                             RolePathMappingRepository rolePathMappingRepository) {
        this.pathRepository = pathRepository;
        this.roleRepository = roleRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
        this.rolePathMappingRepository = rolePathMappingRepository;
    }

    public Boolean checkPermission(Long userId, String uri, String httpMethod) {
        UserRoleMapping userRoleMapping = userRoleMappingRepository.findByUserId(userId);
        if(userRoleMapping == null) {
            log.error("userId not exist is UserRoleMapping: {}", userId);
            return false;
        }

        Optional<Role> role = roleRepository.findById(userRoleMapping.getRoleId());
        if(!role.isPresent()) {
            log.error("roleId not exist in Role: {}", userRoleMapping.getRoleId());
            return false;
        }

        if(role.get().getRoleTag().equals(RoleEnum.SUPER_ADMIN.name())) {
            return true;
        }

        Path path = pathRepository.findByPathPatternAndHttpMethod(uri, httpMethod);
        if(path != null) {
            return true;
        }
        RolePathMapping rolePathMapping = rolePathMappingRepository.
                findByRoleIdAndPathId(role.get().getId(), path.getId());
        return rolePathMapping != null;
    }
}
