package com.leedahun.feedservice.domain.feed.service.impl;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.infra.client.UserInternalApiClient;
import com.leedahun.feedservice.infra.client.dto.SourceResponseDto;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.entity.Content;
import com.leedahun.feedservice.domain.feed.repository.ContentRepository;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.leedahun.feedservice.common.message.ErrorMessage.IDENTITY_SERVICE_REQUEST_FAIL;
import static com.leedahun.feedservice.common.message.ErrorMessage.USER_SOURCE_REQUEST_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserInternalApiClient userInternalApiClient;
    private final ContentRepository contentRepository;

    @Override
    public Map<Long, String> fetchUserSourceMapping(Long userId) {
        try {
            List<SourceResponseDto> userSources = userInternalApiClient.getUserSources(userId);
            if (CollectionUtils.isEmpty(userSources)) {
                return Collections.emptyMap();
            }

            return userSources.stream()
                    .filter(source -> StringUtils.hasText(source.getUserDefinedName()))
                    .collect(Collectors.toMap(
                            SourceResponseDto::getSourceId,
                            SourceResponseDto::getUserDefinedName,
                            (existing, replacement) -> existing
                    ));
        } catch (FeignException e) {
            log.error("Identity Service 호출 실패. userId: {}, status: {}, error: {}", userId, e.status(), e.getMessage());
            throw new InternalApiRequestException(IDENTITY_SERVICE_REQUEST_FAIL.getMessage());
        } catch (Exception e) {
            log.error("소스 목록 조회 중 예상치 못한 오류 발생. userId: {}", userId, e);
            throw new InternalServerProcessingException(USER_SOURCE_REQUEST_FAIL.getMessage());
        }
    }

    // TODO user_source 엔티티 추가
    @Override
    public CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeeds(Long userId,
                                                                           Map<Long, String> sourceMapping,
                                                                           Long lastId,
                                                                           int size) {

        if (isEmptySourceMapping(sourceMapping)) {
            return CommonPageResponse.empty();
        }

        List<Content> contents = fetchContents(sourceMapping.keySet(), lastId, size);

        boolean hasNext = contents.size() > size;
        List<Content> pagedContents = hasNext ? contents.subList(0, size) : contents;

        List<ContentFeedResponseDto> feeds = toFeedDtos(pagedContents, sourceMapping);
        enrichWithBookmarks(feeds, userId);

        return CommonPageResponse.<ContentFeedResponseDto>builder()
                .content(feeds)
                .hasNext(hasNext)
                .nextCursorId(getNextCursorId(hasNext, pagedContents))
                .build();
    }

    @Override
    public List<ContentFeedResponseDto> getContentsByIds(List<String> contentIds) {
        List<Long> longIds = contentIds.stream()
                .map(this::parseContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return contentRepository.findAllById(longIds).stream()
                .map(ContentFeedResponseDto::from)
                .collect(Collectors.toList());
    }

    private boolean isEmptySourceMapping(Map<Long, String> sourceMapping) {
        return sourceMapping == null || sourceMapping.isEmpty();
    }

    private List<Content> fetchContents(Set<Long> sourceIds, Long lastId, int size) {
        Pageable pageable = buildPageable(size);
        return searchContents(new ArrayList<>(sourceIds), lastId, pageable);
    }

    private List<ContentFeedResponseDto> toFeedDtos(List<Content> contents, Map<Long, String> sourceMapping) {
        return contents.stream()
                .map(content -> ContentFeedResponseDto.from(content, sourceMapping))
                .collect(Collectors.toList());
    }

    private void enrichWithBookmarks(List<ContentFeedResponseDto> feeds, Long userId) {
        if (userId == null || feeds.isEmpty()) {
            return;
        }

        List<String> contentIds = extractContentIds(feeds);
        if (contentIds.isEmpty()) return;

        try {
            Map<String, Long> bookmarkMap = userInternalApiClient.getBookmarkedContentIds(userId, contentIds);
            if (bookmarkMap == null) {
                return;
            }

            feeds.forEach(feed -> {
                if (feed.getContentId() != null) {
                    feed.setBookmarkId(bookmarkMap.get(feed.getContentId()));
                }
            });
        } catch (Exception e) {
            log.error("북마크 정보 조회 실패. userId: {}", userId, e);
        }
    }

    private List<String> extractContentIds(List<ContentFeedResponseDto> feeds) {
        return feeds.stream()
                .map(ContentFeedResponseDto::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long parseContentId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.warn("잘못된 contentId 형식. id: {}", id);
            return null;
        }
    }

    private Pageable buildPageable(int size) {
        return PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id"));
    }

    private List<Content> searchContents(List<Long> sourceIds, Long lastId, Pageable pageable) {
        if (lastId == null) {
            return contentRepository.findBySourceIdIn(sourceIds, pageable);
        }
        return contentRepository.findBySourceIdInAndIdBefore(sourceIds, lastId, pageable);
    }

    private Long getNextCursorId(boolean hasNext, List<Content> contents) {
        if (hasNext && !contents.isEmpty()) {
            return contents.get(contents.size() - 1).getId();
        }
        return null;
    }

}
