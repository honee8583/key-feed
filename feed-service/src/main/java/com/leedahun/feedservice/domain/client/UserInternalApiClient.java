package com.leedahun.feedservice.domain.client;

import com.leedahun.feedservice.domain.client.dto.SourceResponseDto;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "identity-service",
        url = "${feign.client.identity-service.url:}"
)
public interface UserInternalApiClient {

    @GetMapping("/internal/users/{userId}/keywords")
    List<KeywordResponseDto> getActiveKeywords(@PathVariable("userId") Long userId);

    @GetMapping("/internal/sources/user/{userId}")
    List<SourceResponseDto> getUserSources(@PathVariable("userId") Long userId);

    @PostMapping("/internal/bookmarks/user/{userId}/check")
    Map<String, Long> getBookmarkedContentIds(@PathVariable("userId") Long userId, @RequestBody List<String> contentIds);

}