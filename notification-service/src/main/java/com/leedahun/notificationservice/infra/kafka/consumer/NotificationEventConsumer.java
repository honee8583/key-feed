package com.leedahun.notificationservice.infra.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.notificationservice.domain.notification.service.NotificationEventBuffer;
import com.leedahun.notificationservice.domain.notification.service.NotificationService;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import com.leedahun.notificationservice.infra.kafka.exception.KafkaMessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationEventBuffer notificationEventBuffer;

    @KafkaListener(
            topics = "${app.kafka.topic.notification}",
            groupId = "${app.kafka.group-id.notification}"
    )
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltTopicSuffix = ".dlt"
    )
    public void consume(String message) {

        log.info("Kafka 메시지 수신: {}", message);

        try {
            NotificationEventDto notificationEvent = objectMapper.readValue(message, NotificationEventDto.class);
            notificationService.send(notificationEvent);
            notificationEventBuffer.add(notificationEvent);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new KafkaMessageProcessingException();
        }
    }

}