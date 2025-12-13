package com.leedahun.notificationservice.domain.notification.controller;

import com.leedahun.notificationservice.common.message.SuccessMessage;
import com.leedahun.notificationservice.common.response.CommonPageResponse;
import com.leedahun.notificationservice.common.response.HttpResponse;
import com.leedahun.notificationservice.domain.notification.dto.NotificationResponseDto;
import com.leedahun.notificationservice.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE 연결
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        log.info("SSE 연결 user: {}, Last-Event-ID: {}", userId, lastEventId);
        return notificationService.subscribe(userId, lastEventId);
    }

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal Long userId,
                                              @RequestParam(value = "lastId", required = false) Long lastId,
                                              @RequestParam(defaultValue = "20") int size) {
        CommonPageResponse<NotificationResponseDto> notificationHistory = notificationService.getNotificationHistory(userId, lastId, size);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, SuccessMessage.READ_SUCCESS.getMessage(), notificationHistory));
    }

}