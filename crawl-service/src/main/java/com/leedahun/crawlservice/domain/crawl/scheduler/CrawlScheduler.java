package com.leedahun.crawlservice.domain.crawl.scheduler;

import com.leedahun.crawlservice.domain.crawl.entity.Source;
import com.leedahun.crawlservice.domain.crawl.repository.SourceRepository;
import com.leedahun.crawlservice.domain.crawl.service.CrawlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {

    private final SourceRepository sourceRepository;
    private final CrawlService crawlService;

    // 10분마다 실행
    @Scheduled(fixedRate = 600000)
    public void scheduleCrawling() {
        log.info("=== 스케줄링 크롤링 작업 시작 ===");

        // 10분 이상 수집되지 않은 소스 찾기
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Source> sources = sourceRepository.findSourcesToCrawl(tenMinutesAgo);

        log.info("크롤링 대상 소스: {}개", sources.size());

        for (Source source : sources) {
            try {
                crawlService.processSource(source);  // 개별 소스 처리는 트랜잭션이 분리되어 있으므로 하나가 실패해도 나머지는 계속 진행됨
            } catch (Exception e) {
                log.error("소스 크롤링 실패 (ID: {}, URL: {}): {}", source.getId(), source.getUrl(), e.getMessage());
            }
        }

        log.info("=== 스케줄링 크롤링 작업 종료 ===");
    }
}