package com.leedahun.feedservice.domain.feed.controller;

import static com.leedahun.feedservice.common.message.SuccessMessage.READ_SUCCESS;

import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.common.response.HttpResponse;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<?> getMyFeeds(@AuthenticationPrincipal Long userId,
                                        @RequestParam(value = "lastId", required = false) Long lastId,
                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        List<String> keywords = feedService.fetchActiveKeywordNames(userId);
        CommonPageResponse<ContentFeedResponseDto> feeds = feedService.getPersonalizedFeed(keywords, lastId, size);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, READ_SUCCESS.getMessage(), feeds));
    }

}
