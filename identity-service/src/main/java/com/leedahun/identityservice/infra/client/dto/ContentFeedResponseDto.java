package com.leedahun.identityservice.infra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentFeedResponseDto {
    private String contentId;
    private String title;
    private String summary;
    private String sourceName;
    private String originalUrl;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;
}