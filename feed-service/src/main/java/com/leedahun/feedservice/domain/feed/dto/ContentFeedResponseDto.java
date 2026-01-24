package com.leedahun.feedservice.domain.feed.dto;

import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ContentFeedResponseDto {
    private String contentId;
    private String title;
    private String summary;
    private String sourceName;
    private String originalUrl;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;

    private Long bookmarkId;

    public static ContentFeedResponseDto from(ContentDocument content) {
        return from(content, Collections.emptyMap());
    }

    public static ContentFeedResponseDto from(ContentDocument content, Map<Long, String> sourceMapping) {
        String sourceName = sourceMapping.getOrDefault(content.getSourceId(), content.getSourceName());
        return ContentFeedResponseDto.builder()
                .contentId(content.getId())
                .title(content.getTitle())
                .summary(content.getSummary())
                .sourceName(sourceName)
                .originalUrl(content.getOriginalUrl())
                .thumbnailUrl(content.getThumbnailUrl())
                .publishedAt(content.getPublishedAt())
                .build();
    }
}
