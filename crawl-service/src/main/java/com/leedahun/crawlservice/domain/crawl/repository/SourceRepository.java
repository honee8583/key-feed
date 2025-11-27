package com.leedahun.crawlservice.domain.crawl.repository;

import com.leedahun.crawlservice.domain.crawl.entity.Source;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceRepository extends JpaRepository<Source, Long> {

    /**
     * 수집 대상 조회
     * 1. 한 번도 수집하지 않은 소스 (lastCrawledAt IS NULL)
     * 2. 또는 마지막 수집 시간이 기준 시간(targetTime)보다 오래된 소스
     */
    @Query("SELECT s FROM Source s WHERE s.lastCrawledAt IS NULL OR s.lastCrawledAt < :targetTime")
    List<Source> findSourcesToCrawl(@Param("targetTime") LocalDateTime targetTime);

}
