package com.leedahun.matchservice.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.matchservice.domain.content.service.ContentService;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventConsumer {

    private final ContentService contentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.content}", groupId = "match-service-group")
    public void consume(String message) {
        log.info("Kafka 메시지 수신: {}", message);

        try {
            CrawledContentDto crawledContent = objectMapper.readValue(message, CrawledContentDto.class);
            contentService.saveContent(crawledContent);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            // 필요 시 Dead Letter Queue(DLQ)로 보내거나 재시도 로직 추가
        }
    }
}