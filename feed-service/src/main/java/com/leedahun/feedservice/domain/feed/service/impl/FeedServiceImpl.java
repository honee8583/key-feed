package com.leedahun.feedservice.domain.feed.service.impl;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.client.UserInternalApiClient;
import com.leedahun.feedservice.domain.client.dto.SourceResponseDto;
import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.repository.ContentDocumentRepository;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.leedahun.feedservice.common.message.ErrorMessage.IDENTITY_SERVICE_REQUEST_FAIL;
import static com.leedahun.feedservice.common.message.ErrorMessage.USER_SOURCE_REQUEST_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserInternalApiClient userInternalApiClient;
    private final ContentDocumentRepository contentDocumentRepository;

    private static final DateTimeFormatter ES_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .withZone(ZoneOffset.UTC);

    @Override
    public Map<Long, String> fetchUserSourceMapping(Long userId) {
        try {
            List<SourceResponseDto> userSources = userInternalApiClient.getUserSources(userId);
            if (CollectionUtils.isEmpty(userSources)) {
                return Collections.emptyMap();
            }

            return userSources.stream()
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

    @Override
    public CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeeds(Long userId, Map<Long, String> sourceMapping, Long lastPublishedAt, int size) {
        if (sourceMapping == null || sourceMapping.isEmpty()) {
            return CommonPageResponse.<ContentFeedResponseDto>builder()
                    .content(Collections.emptyList())
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        List<Long> sourceIds = new ArrayList<>(sourceMapping.keySet());

        Pageable pageable = buildPageable(size);
        List<ContentDocument> documents = searchDocuments(sourceIds, lastPublishedAt, pageable);

        boolean hasNext = documents.size() > size;
        List<ContentDocument> resultList = trimResultList(documents, hasNext, size);

        List<ContentFeedResponseDto> feeds = resultList.stream()
                .map(content -> ContentFeedResponseDto.from(content, sourceMapping))
                .collect(Collectors.toList());

        if (userId != null && !feeds.isEmpty()) {
            try {
                List<String> contentIds = feeds.stream()
                        .map(ContentFeedResponseDto::getContentId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // contentIds가 비어있으면 API 호출 스킵
                if (!contentIds.isEmpty()) {
                    Map<String, Long> bookmarkMap = userInternalApiClient.getBookmarkedContentIds(userId, contentIds);

                    // bookmarkMap이 null일 경우 대비 및 feed.getContentId()가 null인 경우 방어
                    if (bookmarkMap != null) {
                        feeds.forEach(feed -> {
                            if (feed.getContentId() != null) {
                                feed.setBookmarkId(bookmarkMap.get(feed.getContentId()));
                            }
                        });
                    }
                }

            } catch (Exception e) {
                log.error("북마크 정보 조회 실패. userId: {}", userId, e);
            }
        }

        Long nextCursorId = getNextPublishedAt(hasNext, resultList);
        return CommonPageResponse.<ContentFeedResponseDto>builder()
                .content(feeds)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .build();
    }

    @Override
    public List<ContentFeedResponseDto> getContentsByIds(List<String> contentIds) {
        Iterable<ContentDocument> contentDocuments = contentDocumentRepository.findAllById(contentIds);

        List<ContentFeedResponseDto> contents = new ArrayList<>();
        for (ContentDocument contentDocument : contentDocuments) {
            contents.add(ContentFeedResponseDto.from(contentDocument));
        }

        return contents;
    }

    private Pageable buildPageable(int size) {
        return PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    private List<ContentDocument> searchDocuments(List<Long> sourceIds, Long lastId, Pageable pageable) {
        if (lastId == null) {
            return contentDocumentRepository.searchBySourceIdsFirstPage(sourceIds, pageable);
        }

        String lastPublishedAt = convertCursorMillisToEsDate(lastId);
        return contentDocumentRepository.searchBySourceIdsAndCursor(sourceIds, lastPublishedAt, pageable);
    }

    private String convertCursorMillisToEsDate(Long cursorMillis) {
        return ES_DATE_FORMATTER.format(Instant.ofEpochMilli(cursorMillis));
    }

    private List<ContentDocument> trimResultList(List<ContentDocument> contents, boolean hasNext, int size) {
        if (hasNext) {
            return contents.subList(0, size);
        }
        return contents;
    }

    private Long getNextPublishedAt(boolean hasNext, List<ContentDocument> contents) {
        if (hasNext && !contents.isEmpty()) {
            return contents.get(contents.size() - 1).getPublishedAt()
                    .atZone(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli();
        }
        return null;
    }

}
