package com.weixin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MassConfigRepository extends JpaRepository<MassConfig, Integer> {

    List<MassConfig> findByUsername(String username);

}