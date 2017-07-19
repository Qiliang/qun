package com.weixin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendHistoryDetailRepository extends JpaRepository<SendHistoryDetail, Long> {

    List<SendHistoryDetail> findByHistoryId(String historyId);

}