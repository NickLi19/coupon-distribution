package com.imooc.coupon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupon_role_path_mapping")
public class RolePathMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Basic
    @Column(name = "path_id", nullable = false)
    private Integer pathId;
}
