package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN("Admin"),
    SUPER_ADMIN("Super_admin"),
    CUSTOMER("User");

    private String roleName;
}
