package com.leedahun.notificationservice.domain.notification.service;

import com.leedahun.notificationservice.domain.notification.repository.NotificationJdbcRepository;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

    @InjectMocks
    private NotificationBatchService notificationBatchService;

    @Mock
    private NotificationJdbcRepository notificationJdbcRepository;

    @Test
    @DisplayName("배치 저장 성공 - 데이터가 존재할 경우 JDBC 리포지토리가 호출됨")
    void saveBatch_success() {
        // given
        NotificationEventDto event1 = NotificationEventDto.builder()
                .notificationId(1L)
                .title("test")
                .build();
        List<NotificationEventDto> events = List.of(event1);

        when(notificationJdbcRepository.batchInsert(events))
                .thenReturn(new int[]{1});

        // when
        notificationBatchService.saveBatch(events);

        // then
        verify(notificationJdbcRepository, times(1)).batchInsert(events);
    }

    @Test
    @DisplayName("배치 저장 성공 - 결과 배열이 비어있어도 예외가 발생하지 않고 로그 로직이 안전하게 처리됨 (-1)")
    void saveBatch_success_empty_result_array() {
        // given
        NotificationEventDto event = NotificationEventDto.builder().notificationId(1L).build();
        List<NotificationEventDto> events = List.of(event);

        // 빈 배열 반환 설정 (result.length == 0)
        when(notificationJdbcRepository.batchInsert(events))
                .thenReturn(new int[]{});

        // when & then
        // result[0] 접근 시 ArrayIndexOutOfBoundsException이 발생하지 않아야 함
        assertDoesNotThrow(() -> notificationBatchService.saveBatch(events));

        verify(notificationJdbcRepository, times(1)).batchInsert(events);
    }

    @Test
    @DisplayName("배치 저장 무시 - 빈 리스트가 전달되면 저장 로직이 실행되지 않음")
    void saveBatch_empty_list() {
        // given
        List<NotificationEventDto> emptyEvents = Collections.emptyList();

        // when
        notificationBatchService.saveBatch(emptyEvents);

        // then
        verify(notificationJdbcRepository, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("배치 저장 무시 - null이 전달되면 저장 로직이 실행되지 않음")
    void saveBatch_null_list() {
        // given
        List<NotificationEventDto> nullEvents = null;

        // when
        notificationBatchService.saveBatch(nullEvents);

        // then
        verify(notificationJdbcRepository, never()).batchInsert(anyList());
    }
}