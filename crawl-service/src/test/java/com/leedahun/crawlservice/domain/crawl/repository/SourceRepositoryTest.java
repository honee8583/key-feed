package com.leedahun.crawlservice.domain.crawl.repository;

import com.leedahun.crawlservice.domain.crawl.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SourceRepositoryTest {

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    @DisplayName("수집 대상 조회 - 한 번도 수집하지 않은 소스(null)와 기준 시간보다 오래된 소스를 조회한다")
    void findSourcesToCrawl_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.minusMinutes(10); // 기준 시간: 10분 전

        Source newSource = Source.builder()
                .url("https://new.com/feed")
                .lastCrawledAt(null)  // 한번도 수집 안함
                .build();
        sourceRepository.save(newSource);

        Source oldSource = Source.builder()
                .url("https://old.com/feed")
                .lastCrawledAt(now.minusMinutes(20)) // 20분전에 수집
                .build();
        sourceRepository.save(oldSource);

        Source recentSource = Source.builder()
                .url("https://recent.com/feed")
                .lastCrawledAt(now.minusMinutes(5)) // 5분전에 수집
                .build();
        sourceRepository.save(recentSource);

        // when
        List<Source> results = sourceRepository.findSourcesToCrawl(targetTime);

        // then
        assertThat(results).hasSize(2); // newSource와 oldSource만 조회되어야 함
        assertThat(results).extracting("url")
                .containsExactlyInAnyOrder("https://new.com/feed", "https://old.com/feed");
    }
}