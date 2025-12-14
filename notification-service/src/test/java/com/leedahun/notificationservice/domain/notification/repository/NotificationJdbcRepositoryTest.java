package com.leedahun.notificationservice.domain.notification.repository;

import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationJdbcRepositoryTest {

    @InjectMocks
    private NotificationJdbcRepository notificationJdbcRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    @DisplayName("Batch Insert 성공 - 데이터가 올바르게 PreparedStatement에 매핑되는지 검증")
    void batchInsert_success() throws SQLException {
        // given
        NotificationEventDto event1 = NotificationEventDto.builder()
                .notificationId(100L)
                .userId(1L)
                .title("Title 1")
                .message("Message 1")
                .originalUrl("http://url1.com")
                .build();

        NotificationEventDto event2 = NotificationEventDto.builder()
                .notificationId(101L)
                .userId(2L)
                .title("Title 2")
                .message("Message 2")
                .originalUrl("http://url2.com")
                .build();

        List<NotificationEventDto> events = List.of(event1, event2);

        // JDBC 실행 결과 모의 (2건 성공)
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1});

        // when
        int[] result = notificationJdbcRepository.batchInsert(events);

        // then
        // 1. 결과값 검증
        assertArrayEquals(new int[]{1, 1}, result);

        // 2. JdbcTemplate에 전달된 BatchPreparedStatementSetter를 캡처(Capture)
        ArgumentCaptor<BatchPreparedStatementSetter> pssCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), pssCaptor.capture());

        BatchPreparedStatementSetter capturedPss = pssCaptor.getValue();

        // 3. Batch Size가 리스트 크기와 같은지 검증
        assertEquals(2, capturedPss.getBatchSize());

        // 4. 내부 매핑 로직 검증 (setValues가 올바르게 호출되는지 수동 실행)
        // 첫 번째 데이터(event1) 매핑 검증
        capturedPss.setValues(preparedStatement, 0);
        verify(preparedStatement).setLong(1, event1.getNotificationId());
        verify(preparedStatement).setLong(2, event1.getUserId());
        verify(preparedStatement).setString(3, event1.getTitle());
        verify(preparedStatement).setString(4, event1.getMessage());
        verify(preparedStatement).setString(5, event1.getOriginalUrl());
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class)); // 날짜는 any로 검증

        // 두 번째 데이터(event2) 매핑 검증을 위해 Mock 초기화 혹은 verify 순서 확인 필요하지만
        // 여기서는 흐름상 첫 번째 데이터 매핑 확인으로 충분하거나, reset 후 진행
        clearInvocations(preparedStatement);

        capturedPss.setValues(preparedStatement, 1);
        verify(preparedStatement).setLong(1, event2.getNotificationId());
        verify(preparedStatement).setString(3, event2.getTitle());
    }

    @Test
    @DisplayName("Batch Insert - 리스트가 비어있으면 DB 호출 없이 빈 배열 반환")
    void batchInsert_empty_list() {
        // given
        List<NotificationEventDto> events = Collections.emptyList();

        // when
        int[] result = notificationJdbcRepository.batchInsert(events);

        // then
        assertEquals(0, result.length);
        // DB 호출이 없어야 함
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Batch Insert - 리스트가 null이면 DB 호출 없이 빈 배열 반환")
    void batchInsert_null_list() {
        // given
        List<NotificationEventDto> events = null;

        // when
        int[] result = notificationJdbcRepository.batchInsert(events);

        // then
        assertEquals(0, result.length);
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }
}