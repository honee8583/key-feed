package com.leedahun.feedservice.domain.feed.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.client.UserInternalApiClient;
import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import com.leedahun.feedservice.domain.feed.repository.ContentDocumentRepository;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @InjectMocks
    private FeedServiceImpl feedService;

    @Mock
    private ContentDocumentRepository contentDocumentRepository;

    @Mock
    private UserInternalApiClient userInternalApiClient;

    private static final DateTimeFormatter ES_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .withZone(ZoneOffset.UTC);

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
        @DisplayName("키워드가 없을 경우 빈 피드를 반환한다")
        void getPersonalizedFeed_EmptyKeywords() {
            // given
            List<String> keywords = Collections.emptyList();

            // when
            CommonPageResponse<ContentFeedResponseDto> response =
                    feedService.getPersonalizedFeed(keywords, null, 10);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.isHasNext()).isFalse();

            verify(contentDocumentRepository, times(0)).searchByKeywordsFirstPage(any(), any());
            verify(contentDocumentRepository, times(0)).searchByKeywordsAndCursor(any(), any(), any());
        }

        @Test
        @DisplayName("첫 페이지 조회(lastId가 null) - 다음 페이지가 있는 경우")
        void getPersonalizedFeed_FirstPage_HasNext() {
            // given
            List<String> keywords = List.of("Kafka", "Spring");
            String searchPattern = "Kafka Spring";
            int size = 2;

            List<ContentDocument> documents = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            documents.add(createDocument(1L, "Title 1", now));
            documents.add(createDocument(2L, "Title 2", now.minusHours(1)));
            documents.add(createDocument(3L, "Title 3", now.minusHours(2)));

            when(contentDocumentRepository.searchByKeywordsFirstPage(eq(searchPattern), any(Pageable.class))).thenReturn(documents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeed(keywords, null, size);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getTitle()).isEqualTo("Title 1");
            assertThat(response.getContent().get(1).getTitle()).isEqualTo("Title 2");

            assertThat(response.isHasNext()).isTrue();

            long expectedCursor = documents.get(1).getPublishedAt()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli();
            assertThat(response.getNextCursorId()).isEqualTo(expectedCursor);
        }

        @Test
        @DisplayName("두 번째 페이지 조회(lastId 존재) - 다음 페이지가 없는 경우")
        void getPersonalizedFeed_NextPage_NoNext() {
            // given
            List<String> keywords = List.of("Kafka");
            String searchPattern = "Kafka";
            int size = 10;

            LocalDateTime cursorTime = LocalDateTime.of(2025, 12, 6, 12, 0, 0);
            long lastId = cursorTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            String expectedDateString = ES_DATE_FORMATTER.format(cursorTime.atZone(ZoneOffset.UTC));

            List<ContentDocument> documents = List.of(
                    createDocument(100L, "Old Article", cursorTime.minusDays(1))
            );

            when(contentDocumentRepository.searchByKeywordsAndCursor(eq(searchPattern), eq(expectedDateString), any(Pageable.class)))
                    .thenReturn(documents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response =
                    feedService.getPersonalizedFeed(keywords, lastId, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isHasNext()).isFalse();
            assertThat(response.getNextCursorId()).isNull(); // 다음 페이지가 없으므로 null

            verify(contentDocumentRepository, times(1))
                    .searchByKeywordsAndCursor(eq(searchPattern), eq(expectedDateString), any(Pageable.class));
        }

        private ContentDocument createDocument(Long id, String title, LocalDateTime publishedAt) {
            return ContentDocument.builder()
                    .contentId(id)
                    .title(title)
                    .summary("Summary...")
                    .publishedAt(publishedAt)
                    .sourceName("Test Source")
                    .originalUrl("http://test.com/" + id)
                    .build();
        }
    }
}