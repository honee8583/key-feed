package com.leedahun.feedservice.domain.feed.service;

import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import java.util.List;

public interface FeedService {

    List<Long> fetchUserSourceIds(Long userId);

    CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeeds(Long userId, List<Long> sourceIds, Long lastPublishedAt, int size);

    List<ContentFeedResponseDto> getContentsByIds(List<String> contentIds);

}
