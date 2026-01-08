package com.leedahun.crawlservice.domain.crawl.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem {
    private String guid;
    private String title;
    private String link;
    private String summary;
    private String thumbnailUrl;
    private LocalDateTime pubDate;
}
