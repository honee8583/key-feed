package com.leedahun.notificationservice.domain.notification.service;

import com.leedahun.notificationservice.common.response.CommonPageResponse;
import com.leedahun.notificationservice.domain.notification.dto.NotificationResponseDto;
import com.leedahun.notificationservice.infra.kafka.dto.NotificationEventDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    SseEmitter subscribe(Long userId, String lastEventId);

    void send(NotificationEventDto notificationEvent);

    CommonPageResponse<NotificationResponseDto> getNotificationHistory(Long userId, Long lastId, int size);

}