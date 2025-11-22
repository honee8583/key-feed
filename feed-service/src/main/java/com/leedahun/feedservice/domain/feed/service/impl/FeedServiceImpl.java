package com.leedahun.feedservice.domain.feed.service.impl;

import static com.leedahun.feedservice.common.message.ErrorMessage.IDENTITY_SERVICE_REQUEST_FAIL;
import static com.leedahun.feedservice.common.message.ErrorMessage.KEYWORD_REQUEST_FAIL;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.client.UserInternalApiClient;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import com.leedahun.feedservice.domain.feed.entity.Content;
import com.leedahun.feedservice.domain.feed.repository.ContentRepository;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final ContentRepository contentRepository;
    private final UserInternalApiClient userInternalApiClient;

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
    public CommonPageResponse<ContentFeedResponseDto> getPersonalizedFeed(List<String> keywords, Long lastId, int size) {
        if (CollectionUtils.isEmpty(keywords)) {
            return CommonPageResponse.<ContentFeedResponseDto>builder()
                    .content(Collections.emptyList())
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        String keywordSearchPattern = getKeywordPattern(keywords);
        List<Content> contents = contentRepository.searchByKeywordsKeyset(
                keywordSearchPattern,
                lastId,
                size + 1
        );

        boolean hasNext = contents.size() > size;
        List<Content> resultList = trimResultList(contents, hasNext, size);
        Long nextCursorId = getNextCursorId(hasNext, resultList);

        List<ContentFeedResponseDto> feeds = resultList.stream()
                .map(ContentFeedResponseDto::from)
                .collect(Collectors.toList());

        return CommonPageResponse.<ContentFeedResponseDto>builder()
                .content(feeds)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .build();
    }

    private List<Content> trimResultList(List<Content> contents, boolean hasNext, int size) {
        List<Content> resultList = contents;
        if (hasNext) {
            resultList = contents.subList(0, size);
        }
        return resultList;
    }

    private Long getNextCursorId(boolean hasNext, List<Content> contents) {
        if (hasNext) {
            return contents.get(contents.size() - 1).getId();
        }
        return null;
    }

    private String getKeywordPattern(List<String> keywords) {
        return keywords.stream()
                .map(this::escapeRegex)
                .collect(Collectors.joining("|"));
    }

    private String escapeRegex(String text) {
        return text.replaceAll("([\\\\.*+?\\[^\\]$(){}=!<>|:\\-])", "\\\\$1");
    }

}
