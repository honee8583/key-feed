package com.leedahun.feedservice.domain.feed.service.impl;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.client.UserInternalApiClient;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import com.leedahun.feedservice.domain.feed.entity.Content;
import com.leedahun.feedservice.domain.feed.repository.ContentRepository;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @InjectMocks
    private FeedServiceImpl feedService;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserInternalApiClient userInternalApiClient;

    @Nested
    @DisplayName("활성 키워드 조회 테스트")
    class FetchActiveKeywordNamesTest {

        @Test
        @DisplayName("성공: FeignClient로부터 키워드 목록을 받아와 문자열 리스트로 반환한다")
        void success() {
            // given
            Long userId = 1L;
            List<KeywordResponseDto> keywordDtos = List.of(
                    KeywordResponseDto.builder()
                            .keywordId(1L)
                            .name("Java")
                            .build(),
                    KeywordResponseDto.builder()
                            .keywordId(2L)
                            .name("Spring")
                            .build()
            );
            when(userInternalApiClient.getActiveKeywords(userId)).thenReturn(keywordDtos);

            // when
            List<String> result = feedService.fetchActiveKeywordNames(userId);

            // then
            assertThat(result).hasSize(2).containsExactly("Java", "Spring");
        }

        @Test
        @DisplayName("성공: 조회된 키워드가 없으면 빈 리스트를 반환한다")
        void success_empty() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getActiveKeywords(userId)).thenReturn(Collections.emptyList());

            // when
            List<String> result = feedService.fetchActiveKeywordNames(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패: FeignException 발생 시 InternalApiRequestException을 던진다")
        void fail_feign_exception() {
            // given
            Long userId = 1L;

            // FeignException은 추상클래스이거나 생성자가 복잡할 수 있어 구체적인 예외를 mock으로 생성
            Request request = Request.create(HttpMethod.GET, "url", Map.of(), null, null, null);
            FeignException feignException = new FeignException.NotFound("Not Found", request, null, null);

            when(userInternalApiClient.getActiveKeywords(userId)).thenThrow(feignException);

            // when & then
            assertThatThrownBy(() -> feedService.fetchActiveKeywordNames(userId))
                    .isInstanceOf(InternalApiRequestException.class);
        }

        @Test
        @DisplayName("실패: 기타 예외 발생 시 InternalServerProcessingException을 던진다")
        void fail_general_exception() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getActiveKeywords(userId)).thenThrow(new RuntimeException("Server Error"));

            // when & then
            assertThatThrownBy(() -> feedService.fetchActiveKeywordNames(userId))
                    .isInstanceOf(InternalServerProcessingException.class);
        }
    }

    @Nested
    @DisplayName("개인화 피드 조회 테스트")
    class GetPersonalizedFeedTest {

        @Test
        @DisplayName("성공: 다음 페이지가 있는 경우")
        void success_has_next() {
            // given
            List<String> keywords = List.of("C++", "Java");
            Long lastId = 100L;
            int size = 5;

            // size + 1인 6개의 컨텐츠가 조회된다고 가정 (그래야 hasNext가 true)
            List<Content> mockContents = IntStream.range(0, 6)
                    .mapToObj(i -> createMockContent((long) i, "Content " + i))
                    .toList();

            // 정규식 검증: C++ -> C\+\+|Java 형태로 변환되어야 함
            String expectedPattern = "C\\+\\+|Java";

            when(contentRepository.searchByKeywordsKeyset(eq(expectedPattern), eq(lastId), eq(size + 1)))
                    .thenReturn(mockContents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeed(keywords, lastId, size);

            // then
            assertThat(response.getContent()).hasSize(5);
            assertThat(response.isHasNext()).isTrue();
            assertThat(response.getNextCursorId()).isEqualTo(4L);

            verify(contentRepository).searchByKeywordsKeyset(eq(expectedPattern), eq(lastId), eq(size + 1));
        }

        @Test
        @DisplayName("성공: 다음 페이지가 없는 경우 (hasNext=false)")
        void success_no_next() {
            // given
            List<String> keywords = List.of("Spring");
            Long lastId = null;
            int size = 5;

            // size보다 적은 3개의 컨텐츠 조회
            List<Content> mockContents = IntStream.range(0, 3)
                    .mapToObj(i -> createMockContent((long) i, "Content " + i))
                    .toList();
            when(contentRepository.searchByKeywordsKeyset(anyString(), eq(lastId), eq(size + 1))).thenReturn(mockContents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeed(keywords, lastId, size);

            // then
            assertThat(response.getContent()).hasSize(3);
            assertThat(response.isHasNext()).isFalse();  // 다음페이지가 존재하지 않아야함
            assertThat(response.getNextCursorId()).isNull();
        }
    }

    private Content createMockContent(Long id, String body) {
        return Content.builder()
                .id(id)
                .sourceId(1L)
                .title(body)
                .summary(body)
                .publishedAt(LocalDateTime.now())
                .originalUrl("http://localhost:8080")
                .thumbnailUrl("http://localhost:8080")
                .build();
    }
}