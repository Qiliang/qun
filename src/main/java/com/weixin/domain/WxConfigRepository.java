package com.weixin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WxConfigRepository extends JpaRepository<WxConfig, Integer> {

    List<WxConfig> findByUsername(String username);

}