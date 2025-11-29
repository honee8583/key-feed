package com.leedahun.matchservice.infra.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.matchservice.domain.content.service.ContentService;
import com.leedahun.matchservice.infra.kafka.dto.CrawledContentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentEventConsumerTest {

    @InjectMocks
    private ContentEventConsumer contentEventConsumer;

    @Mock
    private ContentService contentService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Kafka 메시지(JSON)를 수신하여 DTO로 변환 후 저장 로직을 호출한다")
    void consume_Success() throws JsonProcessingException {
        // given
        String jsonMessage = "{\"title\":\"Test Title\"}";
        CrawledContentDto dto = CrawledContentDto.builder().build();

        // ObjectMapper가 정상적으로 변환한다고 가정
        when(objectMapper.readValue(jsonMessage, CrawledContentDto.class)).thenReturn(dto);

        // when
        contentEventConsumer.consume(jsonMessage);

        // then
        // ObjectMapper가 호출되었는지 검증
        verify(objectMapper, times(1)).readValue(jsonMessage, CrawledContentDto.class);

        // 변환된 DTO로 저장 서비스가 호출되었는지 검증
        verify(contentService, times(1)).saveContent(dto);
    }

    @Test
    @DisplayName("JSON 파싱 중 에러가 발생하면 저장 로직을 호출하지 않고 예외를 처리한다")
    void consume_JsonParsingError() throws JsonProcessingException {
        // given
        String invalidJson = "{invalid-json}";

        // ObjectMapper가 예외를 던지도록 설정
        when(objectMapper.readValue(eq(invalidJson), eq(CrawledContentDto.class)))
                .thenThrow(new JsonProcessingException("Parsing Error") {});

        // when
        contentEventConsumer.consume(invalidJson);

        // then
        // 저장 로직은 절대 호출되지 않아야 함
        verify(contentService, never()).saveContent(any());
    }

    @Test
    @DisplayName("저장 서비스 로직 수행 중 에러가 발생해도 컨슈머는 중단되지 않고 로그를 남긴다")
    void consume_ServiceError() throws JsonProcessingException {
        // given
        String jsonMessage = "{}";
        CrawledContentDto dto = CrawledContentDto.builder().build();

        when(objectMapper.readValue(jsonMessage, CrawledContentDto.class)).thenReturn(dto);

        // 저장 서비스가 예외를 던지도록 설정
        doThrow(new RuntimeException("DB Error")).when(contentService).saveContent(dto);

        // when
        contentEventConsumer.consume(jsonMessage);

        // then
        // 저장 시도는 했으나 예외 발생
        verify(contentService, times(1)).saveContent(dto);
    }
}