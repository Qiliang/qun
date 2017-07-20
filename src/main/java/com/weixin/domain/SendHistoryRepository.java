package com.weixin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendHistoryRepository extends JpaRepository<SendHistory, Long> {

    List<SendHistory> findByUser(String user);
    List<SendHistory> findByOperator(String operator);

}