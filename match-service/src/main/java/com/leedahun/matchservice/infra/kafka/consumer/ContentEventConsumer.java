package com.leedahun.matchservice.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.matchservice.domain.content.service.ContentService;
import com.leedahun.matchservice.domain.content.service.NotificationTriggerService;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import com.leedahun.matchservice.infra.kafka.exception.KafkaMessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventConsumer {

    private final ContentService contentService;
    private final NotificationTriggerService notificationTriggerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topic.content}",
            groupId = "match.content.collector"
    )
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltTopicSuffix = ".dlt"
    )
    public void consume(String message) {
        log.info("Kafka 메시지 수신: {}", message);

        try {
            CrawledContentDto crawledContent = objectMapper.readValue(message, CrawledContentDto.class);
            contentService.saveContent(crawledContent);
            notificationTriggerService.matchAndSendNotification(crawledContent);  // 알림 카프카 메시지 전송
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new KafkaMessageProcessingException();
        }
    }

}