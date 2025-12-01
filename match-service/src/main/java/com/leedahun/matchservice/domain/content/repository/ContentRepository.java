package com.leedahun.matchservice.domain.content.repository;

import com.leedahun.matchservice.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

    // 중복 저장 방지를 위한 URL 존재 여부 확인
    boolean existsBySourceIdAndOriginalUrl(Long sourceId, String originalUrl);

}