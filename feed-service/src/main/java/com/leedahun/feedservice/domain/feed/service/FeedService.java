package com.leedahun.feedservice.domain.feed.service;

import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import java.util.List;
import java.util.Map;

public interface FeedService {

    Map<Long, String> fetchUserSourceMapping(Long userId);

    CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeeds(Long userId, Map<Long, String> sourceMapping, Long lastPublishedAt, int size);

    List<ContentFeedResponseDto> getContentsByIds(List<String> contentIds);

}
