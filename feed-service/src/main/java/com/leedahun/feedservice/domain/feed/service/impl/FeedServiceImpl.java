package com.leedahun.feedservice.domain.feed.service.impl;

import static com.leedahun.feedservice.common.message.ErrorMessage.IDENTITY_SERVICE_REQUEST_FAIL;
import static com.leedahun.feedservice.common.message.ErrorMessage.KEYWORD_REQUEST_FAIL;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.client.UserInternalApiClient;
import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import com.leedahun.feedservice.domain.feed.repository.ContentDocumentRepository;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import feign.FeignException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    public List<String> fetchActiveKeywordNames(Long userId) {
        try {
            List<KeywordResponseDto> keywords = userInternalApiClient.getActiveKeywords(userId);

            if (CollectionUtils.isEmpty(keywords)) {
                return Collections.emptyList();
            }

            return keywords.stream()
                    .map(KeywordResponseDto::getName)
                    .collect(Collectors.toList());

        } catch (FeignException e) {
            log.error("키워드를 조회하는데 실패하였습니다. {}: {}", userId, e.getMessage());
            throw new InternalApiRequestException(IDENTITY_SERVICE_REQUEST_FAIL.getMessage());
        } catch (Exception e) {
            log.error("내부 서버 통신에 실패하였습니다. {}: {}", userId, e.getMessage(), e);
            throw new InternalServerProcessingException(KEYWORD_REQUEST_FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeed(List<String> keywords, Long lastPublishedAt, int size) {
        if (CollectionUtils.isEmpty(keywords)) {
            return CommonPageResponse.<ContentFeedResponseDto>builder()
                    .content(Collections.emptyList())
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        String keywordSearchPattern = buildKeywordSearchPattern(keywords);  // multi_match 쿼리에서 공백은 OR 연산자로 동작
        Pageable pageable = buildPageable(size);
        List<ContentDocument> documents = searchDocuments(keywordSearchPattern, lastPublishedAt, pageable);

        boolean hasNext = documents.size() > size;
        List<ContentDocument> resultList = trimResultList(documents, hasNext, size);

        List<ContentFeedResponseDto> feeds = resultList.stream()
                .map(ContentFeedResponseDto::from)
                .collect(Collectors.toList());

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

    private String buildKeywordSearchPattern(List<String> keywords) {
        return String.join(" ", keywords);
    }

    private Pageable buildPageable(int size) {
        return PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    private List<ContentDocument> searchDocuments(String keywordSearchPattern, Long lastId, Pageable pageable) {
        if (lastId == null) {
            return contentDocumentRepository.searchByKeywordsFirstPage(keywordSearchPattern, pageable);
        }

        String lastPublishedAt = convertCursorMillisToEsDate(lastId);
        return contentDocumentRepository.searchByKeywordsAndCursor(keywordSearchPattern, lastPublishedAt, pageable);
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
