package com.leedahun.feedservice.domain.feed.service;

import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import java.util.List;

public interface FeedService {

    List<String> fetchActiveKeywordNames(Long userId);

    CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeed(List<String> keywords, Long lastId, int size);

}
