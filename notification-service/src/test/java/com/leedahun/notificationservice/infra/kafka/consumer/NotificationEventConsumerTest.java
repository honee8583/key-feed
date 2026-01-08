package com.leedahun.notificationservice.infra.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.notificationservice.domain.notification.service.NotificationEventBuffer;
import com.leedahun.notificationservice.domain.notification.service.NotificationService;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import com.leedahun.notificationservice.infra.kafka.exception.KafkaMessageProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationEventBuffer notificationEventBuffer;

    @Test
    @DisplayName("성공: 유효한 JSON 메시지를 수신하면 DTO로 변환 후 서비스와 버퍼에 전달한다")
    void consume_Success() throws JsonProcessingException {
        // given
        String message = "{\"title\":\"Test Notification\"}";
        NotificationEventDto eventDto = NotificationEventDto.builder()
                .title("Test Notification")
                .build();

        when(objectMapper.readValue(message, NotificationEventDto.class))
                .thenReturn(eventDto);

        // when
        consumer.consume(message);

        // then
        verify(notificationService, times(1)).send(eventDto);
        verify(notificationEventBuffer, times(1)).add(eventDto);
    }

    @Test
    @DisplayName("실패: JSON 파싱 중 에러가 발생하면 KafkaMessageProcessingException을 던진다")
    void consume_Fail_JsonError() throws JsonProcessingException {
        // given
        String invalidMessage = "Invalid JSON";

        when(objectMapper.readValue(eq(invalidMessage), eq(NotificationEventDto.class)))
                .thenThrow(mock(JsonProcessingException.class));

        // when & then
        assertThatThrownBy(() -> consumer.consume(invalidMessage))
                .isInstanceOf(KafkaMessageProcessingException.class);

        verify(notificationService, never()).send(any());
        verify(notificationEventBuffer, never()).add(any());
    }

    @Test
    @DisplayName("실패: 서비스 로직 실행 중 에러가 발생하면 KafkaMessageProcessingException을 던진다")
    void consume_Fail_ServiceError() throws JsonProcessingException {
        // given
        String message = "{}";
        NotificationEventDto eventDto = new NotificationEventDto();

        when(objectMapper.readValue(message, NotificationEventDto.class))
                .thenReturn(eventDto);

        doThrow(new RuntimeException("Service Error"))
                .when(notificationService).send(eventDto);

        // when & then
        assertThatThrownBy(() -> consumer.consume(message))
                .isInstanceOf(KafkaMessageProcessingException.class);

        verify(notificationEventBuffer, never()).add(any());
    }
}