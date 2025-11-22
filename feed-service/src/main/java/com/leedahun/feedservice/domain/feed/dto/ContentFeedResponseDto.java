package com.leedahun.feedservice.domain.feed.dto;

import com.leedahun.feedservice.domain.feed.entity.Content;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ContentFeedResponseDto {
    private Long contentId;
    private String title;
    private String summary;
    private String sourceName;
    private String originalUrl;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;

    // TODO
    private boolean isBookmarked;

    public static ContentFeedResponseDto from(Content content) {
        return ContentFeedResponseDto.builder()
                .contentId(content.getId())
                .title(content.getTitle())
                .summary(content.getSummary())
                .sourceName(content.getSourceName())
                .originalUrl(content.getOriginalUrl())
                .thumbnailUrl(content.getThumbnailUrl())
                .publishedAt(content.getPublishedAt())
                .build();
    }
}
