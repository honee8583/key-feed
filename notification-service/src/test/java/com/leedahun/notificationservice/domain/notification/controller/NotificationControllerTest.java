package com.leedahun.notificationservice.domain.notification.controller;

import com.leedahun.notificationservice.common.message.SuccessMessage;
import com.leedahun.notificationservice.common.response.CommonPageResponse;
import com.leedahun.notificationservice.config.SecurityConfig;
import com.leedahun.notificationservice.domain.notification.dto.NotificationResponseDto;
import com.leedahun.notificationservice.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithAnonymousUser
@WebMvcTest(controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("[GET /api/notifications/subscribe] SSE 연결 요청 시 SseEmitter를 반환하며 비동기 처리가 시작된다")
    void subscribe_Success() throws Exception {
        // given
        String lastEventId = "1_1710000000";
        SseEmitter sseEmitter = new SseEmitter(60000L);

        when(notificationService.subscribe(any(), eq(lastEventId))).thenReturn(sseEmitter);

        // when & then
        mockMvc.perform(get("/api/notifications/subscribe")
                        .header("Last-Event-ID", lastEventId)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(notificationService).subscribe(any(), eq(lastEventId));
    }

    @Test
    @DisplayName("[GET /api/notifications] 알림 목록 조회 시 200 OK와 페이징된 결과를 반환한다")
    void getNotifications_Success() throws Exception {
        // given
        int size = 10;
        Long lastId = 50L;

        NotificationResponseDto notificationDto = NotificationResponseDto.builder()
                .id(1L)
                .message("Test Message")
                .build();

        CommonPageResponse<NotificationResponseDto> pageResponse = CommonPageResponse.<NotificationResponseDto>builder()
                .content(List.of(notificationDto))
                .nextCursorId(1L)
                .hasNext(true)
                .build();

        when(notificationService.getNotificationHistory(any(), eq(lastId), eq(size)))
                .thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .param("lastId", String.valueOf(lastId))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].message").value("Test Message"))
                .andExpect(jsonPath("$.data.nextCursorId").value(1L))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        verify(notificationService).getNotificationHistory(any(), eq(lastId), eq(size));
    }

    @Test
    @DisplayName("[GET /api/notifications] 파라미터가 없으면 기본값을 사용하여 조회한다")
    void getNotifications_DefaultParams() throws Exception {
        // given
        int defaultSize = 20;

        CommonPageResponse<NotificationResponseDto> emptyResponse = CommonPageResponse.<NotificationResponseDto>builder()
                .content(Collections.emptyList())
                .nextCursorId(null)
                .hasNext(false)
                .build();

        when(notificationService.getNotificationHistory(any(), eq(null), eq(defaultSize)))
                .thenReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(notificationService).getNotificationHistory(any(), eq(null), eq(defaultSize));
    }
}