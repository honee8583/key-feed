package com.leedahun.notificationservice.domain.notification.service;

import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventBufferTest {

    @InjectMocks
    private NotificationEventBuffer buffer;

    @Mock
    private NotificationBatchService batchService;

    // 테스트용 배치 사이즈 설정
    private final int BATCH_SIZE = 5;

    @BeforeEach
    void setUp() {
        // @Value("${jdbc.batch.size}") 필드에 값을 주입하기 위해 리플렉션 유틸 사용
        ReflectionTestUtils.setField(buffer, "FLUSH_BATCH_SIZE", BATCH_SIZE);
    }

    @Test
    @DisplayName("이벤트가 추가되고 flush 호출 시 배치 사이즈만큼 저장소로 전달된다.")
    void addAndFlush_Success() {
        // given
        // 배치 사이즈(5)보다 적은 3개의 이벤트를 추가
        for (int i = 0; i < 3; i++) {
            NotificationEventDto event = NotificationEventDto.builder()
                    .notificationId((long) i)
                    .userId(100L + i)
                    .title("Title " + i)
                    .message("Message body " + i)
                    .build();
            buffer.add(event);
        }

        // when
        buffer.flush();

        // then
        ArgumentCaptor<List<NotificationEventDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(batchService, times(1)).saveBatch(captor.capture());

        List<NotificationEventDto> capturedList = captor.getValue();
        assertThat(capturedList).hasSize(3);

        // 첫 번째 요소 검증
        assertThat(capturedList.get(0).getNotificationId()).isEqualTo(0L);
        assertThat(capturedList.get(0).getTitle()).isEqualTo("Title 0");
    }

    @Test
    @DisplayName("큐에 데이터가 배치 사이즈보다 많을 경우, 배치 사이즈만큼만 잘라서 저장한다.")
    void flush_CutByBatchSize() {
        // given
        // 배치 사이즈(5)보다 많은 12개의 이벤트를 추가
        int totalEvents = 12;
        for (int i = 0; i < totalEvents; i++) {
            NotificationEventDto event = NotificationEventDto.builder()
                    .notificationId((long) i)
                    .userId(200L)
                    .title("Bulk Title " + i)
                    .build();
            buffer.add(event);
        }

        // when: 첫 번째 flush 실행
        buffer.flush();

        // then: 배치 사이즈(5)만큼만 저장되었는지 확인
        ArgumentCaptor<List<NotificationEventDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(batchService, times(1)).saveBatch(captor.capture());

        List<NotificationEventDto> firstBatch = captor.getValue();
        assertThat(firstBatch).hasSize(BATCH_SIZE);
        assertThat(firstBatch.get(0).getNotificationId()).isEqualTo(0L); // 순서 보장 확인

        // when: 두 번째 flush 실행 (남은 7개 중 5개 처리)
        buffer.flush();

        // then: 총 2번 호출됨
        verify(batchService, times(2)).saveBatch(anyList());
    }

    @Test
    @DisplayName("큐가 비어있으면 flush를 해도 batchService가 호출되지 않는다.")
    void flush_EmptyQueue() {
        // given
        // 아무것도 add 하지 않음

        // when
        buffer.flush();

        // then
        verify(batchService, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("저장소 저장 중 예외가 발생해도 flush 메서드는 중단되지 않고 로그를 남긴다.")
    void flush_ExceptionHandling() {
        // given
        NotificationEventDto event = NotificationEventDto.builder()
                .title("Error Event")
                .build();
        buffer.add(event);

        // batchService가 예외를 던지도록 설정
        doThrow(new RuntimeException("DB Error")).when(batchService).saveBatch(anyList());

        // when & then
        // 예외가 밖으로 던져지지 않아야 성공 (try-catch 블록 검증)
        try {
            buffer.flush();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("예외가 내부에서 처리되지 않았습니다.");
        }

        // saveBatch는 호출되었어야 함
        verify(batchService, times(1)).saveBatch(anyList());
    }
}