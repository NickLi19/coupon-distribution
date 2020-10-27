package com.imooc.coupon.dao;

import com.imooc.coupon.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Integer> {
    UserRoleMapping findByUserId(Long userId);
}
