package com.leedahun.crawlservice.domain.crawl.service;

import com.leedahun.crawlservice.domain.crawl.dto.CrawledContentDto;
import com.leedahun.crawlservice.domain.crawl.dto.FeedItem;
import com.leedahun.crawlservice.domain.crawl.entity.Source;
import com.leedahun.crawlservice.domain.crawl.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private final SourceRepository sourceRepository;
    private final RssFeedParser rssFeedParser;
    private final KafkaTemplate<String, CrawledContentDto> kafkaTemplate;

    private static final String TOPIC_NAME = "content-topic";

    @Transactional
    public void processSource(Source source) {
        log.info("소스 크롤링 시작: {}", source.getUrl());

        // 1. RSS 파싱
        List<FeedItem> items = rssFeedParser.parse(source.getUrl());
        if (items.isEmpty()) {
            updateSourceStatus(source, source.getLastItemHash()); // 시간만 갱신
            return;
        }

        // 2. 새 글 필터링
        String lastHash = source.getLastItemHash();
        List<FeedItem> newItems = new ArrayList<>();

        // RSS는 보통 최신순으로 정렬되어 있으므로 위에서부터 검사
        for (FeedItem item : items) {
            // 이전에 수집한 마지막 글(Hash)을 만나면 중단
            if (item.getGuid().equals(lastHash)) {
                break;
            }
            newItems.add(item);
        }

        if (newItems.isEmpty()) {
            log.info("새로운 글 없음: {}", source.getUrl());
            updateSourceStatus(source, lastHash); // 시간만 갱신 (카프카 전송 x)
            return;
        }

        log.info("새 글 {}개 발견: {}", newItems.size(), source.getUrl());

        // 3. Kafka 발행 (과거->최신)
        for (int i = newItems.size() - 1; i >= 0; i--) {
            FeedItem item = newItems.get(i);

            CrawledContentDto contentDto = CrawledContentDto.builder()
                    .sourceId(source.getId())
                    .title(item.getTitle())
                    .summary(item.getSummary())
                    .originalUrl(item.getLink())
                    .thumbnailUrl(item.getThumbnailUrl())
                    .publishedAt(item.getPubDate())
                    .build();

            kafkaTemplate.send(TOPIC_NAME, contentDto);
        }

        // 4. Source 업데이트 최신화
        String newLatestHash = items.get(0).getGuid();  // 가장 최신글의 hash로 업데이트
        updateSourceStatus(source, newLatestHash);
    }

    private void updateSourceStatus(Source source, String newHash) {
        source.updateLastCrawledAt(LocalDateTime.now());
        if (newHash != null) {
            source.updateLastItemHash(newHash);
        }
        sourceRepository.save(source);
    }
}