package com.leedahun.matchservice.infra.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDto {
    private Long notificationId;
    private Long userId;
    private Long contentId;
    private String title;
    private String message;
    private String originalUrl;
}