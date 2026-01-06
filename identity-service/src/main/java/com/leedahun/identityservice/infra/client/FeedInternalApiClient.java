package com.leedahun.identityservice.infra.client;

import com.leedahun.identityservice.infra.client.dto.ContentFeedResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "feed-service",
        url = "${feign.client.feed-service.url:}"
)
public interface FeedInternalApiClient {

    @PostMapping("/internal/feeds/contents")
    List<ContentFeedResponseDto> getContentsByIds(@RequestBody List<String> contentIds);

}