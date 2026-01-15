package com.leedahun.matchservice.domain.content.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.matchservice.infra.client.UserInternalApiClient;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import com.leedahun.matchservice.infra.kafka.exception.KafkaMessageProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTriggerServiceImplTest {

    @InjectMocks
    private NotificationTriggerServiceImpl notificationTriggerService;

    @Mock
    private UserInternalApiClient userInternalApiClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Snowflake snowflake;

    private static final String TOPIC_NAME = "test-notification-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationTriggerService, "notificationTopic", TOPIC_NAME);
    }

    @Test
    @DisplayName("알림 매칭 성공 - 키워드와 소스가 매칭되는 유저가 존재하여 Kafka 메시지를 전송함")
    void matchAndSendNotification_success() throws JsonProcessingException {
        // given
        Long sourceId = 5L;
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title("Spring Boot")
                .summary("MSA Guide")
                .originalUrl("http://example.com")
                .sourceId(sourceId)
                .build();

        List<Long> matchedUserIds = List.of(1L, 2L);

        when(userInternalApiClient.findUserIdsByKeywordsAndSource(anySet(), eq(sourceId)))
                .thenReturn(matchedUserIds);

        when(snowflake.nextId()).thenReturn(100L, 101L);
        when(objectMapper.writeValueAsString(any())).thenReturn("json_string");

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        // 1. 유저 조회 API가 변경된 메서드로 호출되었는지 검증
        verify(userInternalApiClient, times(1))
                .findUserIdsByKeywordsAndSource(anySet(), eq(sourceId));

        // 2. Kafka 전송 검증
        verify(kafkaTemplate, times(2)).send(eq(TOPIC_NAME), anyString());

        // 3. Snowflake 호출 검증
        verify(snowflake, times(2)).nextId();
    }

    @Test
    @DisplayName("알림 매칭 성공 - Title이 null이어도 Summary에서 키워드를 추출하여 정상 동작함")
    void matchAndSendNotification_title_is_null() throws JsonProcessingException {
        // given
        Long sourceId = 5L;
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title(null)
                .summary("Spring Boot Guide")
                .originalUrl("http://example.com")
                .sourceId(sourceId)
                .build();

        List<Long> matchedUserIds = List.of(1L);

        when(userInternalApiClient.findUserIdsByKeywordsAndSource(anySet(), eq(sourceId)))
                .thenReturn(matchedUserIds);

        when(snowflake.nextId()).thenReturn(100L);
        when(objectMapper.writeValueAsString(any())).thenReturn("json_string");

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        verify(userInternalApiClient, times(1))
                .findUserIdsByKeywordsAndSource(anySet(), eq(sourceId));
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), anyString());
    }

    @Test
    @DisplayName("알림 매칭 성공 - Summary가 null이어도 Title에서 키워드를 추출하여 정상 동작함")
    void matchAndSendNotification_summary_is_null() throws JsonProcessingException {
        // given
        Long sourceId = 5L;
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title("Java Programming")
                .summary(null)
                .originalUrl("http://example.com")
                .sourceId(sourceId)
                .build();

        List<Long> matchedUserIds = List.of(1L);

        when(userInternalApiClient.findUserIdsByKeywordsAndSource(anySet(), eq(sourceId)))
                .thenReturn(matchedUserIds);

        when(snowflake.nextId()).thenReturn(100L);
        when(objectMapper.writeValueAsString(any())).thenReturn("json_string");

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        verify(userInternalApiClient, times(1))
                .findUserIdsByKeywordsAndSource(anySet(), eq(sourceId));
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), anyString());
    }

    @Test
    @DisplayName("알림 매칭 실패 - Title과 Summary가 모두 null이면 키워드 없음으로 처리되어 조기 종료")
    void matchAndSendNotification_both_null() {
        // given
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title(null)
                .summary(null)
                .sourceId(1L)
                .build();

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        verify(userInternalApiClient, never()).findUserIdsByKeywordsAndSource(anySet(), anyLong());

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("알림 매칭 실패 - 추출된 키워드가 없음 (2글자 미만 혹은 공백)")
    void matchAndSendNotification_no_keywords() {
        // given
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title("A B")
                .summary("!")
                .sourceId(1L)
                .build();

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        // 변경된 메서드가 호출되지 않아야 함
        verify(userInternalApiClient, never())
                .findUserIdsByKeywordsAndSource(anySet(), anyLong());

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("알림 매칭 실패 - 키워드는 있으나 구독 중인 유저가 없음")
    void matchAndSendNotification_no_users_found() {
        // given
        Long sourceId = 5L;
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title("Java")
                .summary("Programming")
                .sourceId(sourceId)
                .build();

        when(userInternalApiClient.findUserIdsByKeywordsAndSource(anySet(), eq(sourceId)))
                .thenReturn(Collections.emptyList());

        // when
        notificationTriggerService.matchAndSendNotification(contentDto);

        // then
        // 유저 조회 API는 호출되었어야 함
        verify(userInternalApiClient, times(1))
                .findUserIdsByKeywordsAndSource(anySet(), eq(sourceId));

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("예외 발생 - JSON 변환 중 에러 발생 시 커스텀 예외 던짐")
    void matchAndSendNotification_json_processing_exception() throws JsonProcessingException {
        // given
        Long sourceId = 5L;
        CrawledContentDto contentDto = CrawledContentDto.builder()
                .title("Error Test")
                .summary("Summary")
                .sourceId(sourceId)
                .build();

        List<Long> matchedUserIds = List.of(1L);

        when(userInternalApiClient.findUserIdsByKeywordsAndSource(anySet(), eq(sourceId))).thenReturn(matchedUserIds);

        // JSON 변환 시 강제로 예외 발생 설정
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Json Error") {});

        // when & then
        assertThrows(KafkaMessageProcessingException.class, () -> {
            notificationTriggerService.matchAndSendNotification(contentDto);
        });

        // Kafka 전송은 실패했으므로 호출되지 않아야 함 (예외 발생 전)
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}