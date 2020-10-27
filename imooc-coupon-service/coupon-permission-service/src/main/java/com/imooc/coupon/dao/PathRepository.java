package com.imooc.coupon.dao;

import com.imooc.coupon.entity.Path;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathRepository extends JpaRepository<Path, Integer> {
    List<Path> findAllByServiceName(String serviceName);
    Path findByPathPatternAndHttpMethod(String pathPattern, String httpMethod);
}
