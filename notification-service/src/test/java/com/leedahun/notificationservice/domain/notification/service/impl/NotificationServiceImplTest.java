package com.leedahun.notificationservice.domain.notification.service.impl;

import com.leedahun.notificationservice.common.response.CommonPageResponse;
import com.leedahun.notificationservice.domain.notification.dto.NotificationResponseDto;
import com.leedahun.notificationservice.domain.notification.entity.Notification;
import com.leedahun.notificationservice.domain.notification.repository.NotificationRepository;
import com.leedahun.notificationservice.domain.notification.repository.SseEmitterRepository;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private SseEmitterRepository sseEmitterRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("SSE 구독 성공 - 최초 연결 (LastEventId 없음)")
    void subscribe_success_initial_connection() {
        // given
        Long userId = 1L;
        String lastEventId = "";

        // when
        SseEmitter result = notificationService.subscribe(userId, lastEventId);

        // then
        assertNotNull(result);
        verify(sseEmitterRepository, times(1)).save(anyString(), any(SseEmitter.class));
        verify(notificationRepository, never()).findAllByUserIdAndIdGreaterThan(anyLong(), anyLong());
    }

    @Test
    @DisplayName("SSE 구독 성공 - 재연결 및 유실 데이터 전송 (LastEventId 있음)")
    void subscribe_success_with_last_event_id() {
        // given
        Long userId = 1L;
        String lastEventId = "100";

        List<Notification> missedNotifications = new ArrayList<>();
        missedNotifications.add(createNotification(101L, "놓친 알림 1", userId));
        missedNotifications.add(createNotification(102L, "놓친 알림 2", userId));

        when(notificationRepository.findAllByUserIdAndIdGreaterThan(userId, 100L))
                .thenReturn(missedNotifications);

        // when
        SseEmitter result = notificationService.subscribe(userId, lastEventId);

        // then
        assertNotNull(result);
        verify(sseEmitterRepository, times(1)).save(anyString(), any(SseEmitter.class));
        verify(notificationRepository, times(1)).findAllByUserIdAndIdGreaterThan(userId, 100L);
    }

    @Test
    @DisplayName("SSE 구독 - Last-Event-ID 형식이 잘못된 경우 (NumberFormatException 예외 처리)")
    void subscribe_with_invalid_last_event_id() {
        // given
        Long userId = 1L;
        String invalidLastEventId = "invalid_format_string"; // 숫자가 아닌 문자열

        // when
        SseEmitter result = notificationService.subscribe(userId, invalidLastEventId);

        // then
        assertNotNull(result);

        // Emitter 저장은 정상적으로 수행되어야 함
        verify(sseEmitterRepository, times(1)).save(anyString(), any(SseEmitter.class));

        // 예외가 catch 되었으므로 DB 조회 메서드는 실행되지 않아야 함
        verify(notificationRepository, never()).findAllByUserIdAndIdGreaterThan(anyLong(), anyLong());
    }

    @Test
    @DisplayName("SSE 구독 - 완료(Completion) 및 타임아웃(Timeout) 콜백 동작 확인")
    void subscribe_callbacks_verification() {
        // given
        Long userId = 1L;
        String lastEventId = "";

        // when
        SseEmitter result = notificationService.subscribe(userId, lastEventId);

        // then
        assertNotNull(result);

        // 1. Completion 콜백 검증
        // SseEmitter 내부의 completionCallback 필드를 가져옴 (private 필드 접근)
        Runnable completionCallback = (Runnable) ReflectionTestUtils.getField(result, "completionCallback");
        assertNotNull(completionCallback);

        // 콜백 강제 실행
        completionCallback.run();

        // 리포지토리 삭제 메서드가 호출되었는지 검증 (ID는 내부 생성되므로 startsWith로 확인)
        verify(sseEmitterRepository, times(1)).deleteById(argThat(id -> id.startsWith(userId + "_")));

        // 2. Timeout 콜백 검증
        // SseEmitter 내부의 timeoutCallback 필드를 가져옴
        Runnable timeoutCallback = (Runnable) ReflectionTestUtils.getField(result, "timeoutCallback");
        assertNotNull(timeoutCallback);

        // 콜백 강제 실행
        timeoutCallback.run();

        // 리포지토리 삭제 메서드가 총 2번(Completion 1번 + Timeout 1번) 호출되었는지 확인
        verify(sseEmitterRepository, times(2)).deleteById(argThat(id -> id.startsWith(userId + "_")));
    }

    @Test
    @DisplayName("알림 전송 - 연결된 Emitter가 있을 경우 전송 성공")
    void send_success() {
        // given
        Long userId = 1L;
        NotificationEventDto eventDto = NotificationEventDto.builder()
                .notificationId(200L)
                .userId(userId)
                .title("새 알림")
                .message("테스트 메시지")
                .build();

        SseEmitter mockEmitter = new SseEmitter();
        Map<String, SseEmitter> emitters = Map.of(userId + "_12345", mockEmitter);

        when(sseEmitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId)))
                .thenReturn(emitters);

        // when
        notificationService.send(eventDto);

        // then
        verify(sseEmitterRepository, times(1)).findAllEmitterStartWithByUserId(String.valueOf(userId));
    }

    @Test
    @DisplayName("알림 전송 실패 - IOException 발생 시 Emitter 삭제")
    void send_failure_io_exception() throws IOException {
        // given
        Long userId = 1L;
        String emitterId = userId + "_12345";
        NotificationEventDto eventDto = NotificationEventDto.builder()
                .userId(userId)
                .notificationId(300L)
                .title("전송 실패 테스트")
                .build();

        SseEmitter mockEmitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Map.of(emitterId, mockEmitter);

        when(sseEmitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId)))
                .thenReturn(emitters);

        doThrow(new IOException("Broken pipe")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        // when
        notificationService.send(eventDto);

        // then
        verify(sseEmitterRepository, times(1)).deleteById(emitterId);
    }

    @Test
    @DisplayName("알림 목록 조회 - 첫 페이지 조회 (LastId가 null일 때)")
    void getNotificationHistory_first_page() {
        // given
        Long userId = 1L;
        Long lastId = null;
        int size = 20;

        List<Notification> dbResult = new ArrayList<>();
        for (long i = 21; i >= 1; i--) {
            dbResult.add(createNotification(i, "알림 " + i, userId));
        }

        when(notificationRepository.findFirstPage(eq(userId), any(Pageable.class)))
                .thenReturn(dbResult);

        // when
        CommonPageResponse<NotificationResponseDto> response =
                notificationService.getNotificationHistory(userId, lastId, size);

        // then
        assertTrue(response.isHasNext());
        assertEquals(20, response.getContent().size());
        assertEquals(2L, response.getNextCursorId());

        verify(notificationRepository, times(1)).findFirstPage(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("알림 목록 조회 - 다음 페이지 조회 (LastId가 존재할 때) & 마지막 페이지")
    void getNotificationHistory_next_page_end_of_list() {
        // given
        Long userId = 1L;
        Long lastId = 100L;
        int size = 20;

        List<Notification> dbResult = new ArrayList<>();
        for (long i = 99; i >= 95; i--) {
            dbResult.add(createNotification(i, "알림 " + i, userId));
        }

        when(notificationRepository.findNextPage(eq(userId), eq(lastId), any(Pageable.class)))
                .thenReturn(dbResult);

        // when
        CommonPageResponse<NotificationResponseDto> response =
                notificationService.getNotificationHistory(userId, lastId, size);

        // then
        assertFalse(response.isHasNext());
        assertEquals(5, response.getContent().size());
        assertEquals(95L, response.getNextCursorId());

        verify(notificationRepository, times(1)).findNextPage(eq(userId), eq(lastId), any(Pageable.class));
    }

    @Test
    @DisplayName("알림 목록 조회 - 조회된 데이터가 없는 경우")
    void getNotificationHistory_empty() {
        // given
        Long userId = 1L;
        Long lastId = null;
        int size = 20;

        when(notificationRepository.findFirstPage(eq(userId), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        // when
        CommonPageResponse<NotificationResponseDto> response =
                notificationService.getNotificationHistory(userId, lastId, size);

        // then
        assertTrue(response.getContent().isEmpty());
        assertFalse(response.isHasNext());
        assertNull(response.getNextCursorId());
    }

    private Notification createNotification(Long id, String title, Long userId) {
        return Notification.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .message("메시지 내용")
                .isRead(false)
                .build();
    }
}