package com.leedahun.feedservice.domain.feed.service.impl;

import static com.leedahun.feedservice.common.message.ErrorMessage.IDENTITY_SERVICE_REQUEST_FAIL;
import static com.leedahun.feedservice.common.message.ErrorMessage.USER_SOURCE_REQUEST_FAIL;
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
import com.leedahun.feedservice.domain.client.dto.SourceResponseDto;
import com.leedahun.feedservice.domain.feed.document.ContentDocument;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.repository.ContentDocumentRepository;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import java.time.Instant;
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
    @DisplayName("유저 구독 소스 ID 목록 조회 (FetchUserSourceIds)")
    class FetchUserSourceIdsTest {

        @Test
        @DisplayName("성공: FeignClient로부터 소스 목록을 받아와 ID 리스트로 반환한다")
        void success() {
            // given
            Long userId = 1L;
            SourceResponseDto source1 = SourceResponseDto.builder().build();
            setSourceId(source1, 10L);
            SourceResponseDto source2 = SourceResponseDto.builder().build();
            setSourceId(source2, 20L);

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(source1, source2));

            // when
            List<Long> result = feedService.fetchUserSourceIds(userId);

            // then
            assertThat(result).hasSize(2).containsExactly(10L, 20L);
        }

        @Test
        @DisplayName("성공: 구독한 소스가 없으면 빈 리스트를 반환한다")
        void success_empty() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getUserSources(userId)).thenReturn(Collections.emptyList());

            // when
            List<Long> result = feedService.fetchUserSourceIds(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패: FeignException 발생 시 InternalApiRequestException을 던진다")
        void fail_feign_exception() {
            // given
            Long userId = 1L;
            Request request = Request.create(HttpMethod.GET, "url", Map.of(), null, null, null);
            FeignException feignException = new FeignException.NotFound("Not Found", request, null, null);

            when(userInternalApiClient.getUserSources(userId)).thenThrow(feignException);

            // when & then
            assertThatThrownBy(() -> feedService.fetchUserSourceIds(userId))
                    .isInstanceOf(InternalApiRequestException.class)
                    .hasMessageContaining(IDENTITY_SERVICE_REQUEST_FAIL.getMessage());
        }

        @Test
        @DisplayName("실패: 기타 예외 발생 시 InternalServerProcessingException을 던진다")
        void fail_general_exception() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getUserSources(userId)).thenThrow(new RuntimeException("Server Error"));

            // when & then
            assertThatThrownBy(() -> feedService.fetchUserSourceIds(userId))
                    .isInstanceOf(InternalServerProcessingException.class)
                    .hasMessageContaining(USER_SOURCE_REQUEST_FAIL.getMessage());
        }

        private void setSourceId(SourceResponseDto dto, Long id) {
            try {
                java.lang.reflect.Field field = SourceResponseDto.class.getDeclaredField("sourceId");
                field.setAccessible(true);
                field.set(dto, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("개인화 피드 조회 (GetPersonalizedFeeds)")
    class GetPersonalizedFeedsTest {

        @Test
        @DisplayName("구독한 소스가 없을 경우 빈 피드를 반환한다")
        void getPersonalizedFeeds_EmptySourceIds() {
            // given
            List<Long> sourceIds = Collections.emptyList();

            // when
            CommonPageResponse<ContentFeedResponseDto> response =
                    feedService.getPersonalizedFeeds(sourceIds, null, 10);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.isHasNext()).isFalse();

            verify(contentDocumentRepository, times(0)).searchBySourceIdsFirstPage(any(), any());
            verify(contentDocumentRepository, times(0)).searchBySourceIdsAndCursor(any(), any(), any());
        }

        @Test
        @DisplayName("첫 페이지 조회 (lastId is null) - 다음 페이지가 있는 경우")
        void getPersonalizedFeeds_FirstPage_HasNext() {
            // given
            List<Long> sourceIds = List.of(1L, 2L);
            int size = 2;

            List<ContentDocument> documents = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            documents.add(createDocument(10L, "Title 1", now));
            documents.add(createDocument(11L, "Title 2", now.minusHours(1)));
            documents.add(createDocument(12L, "Title 3", now.minusHours(2)));

            when(contentDocumentRepository.searchBySourceIdsFirstPage(eq(sourceIds), any(Pageable.class)))
                    .thenReturn(documents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response =
                    feedService.getPersonalizedFeeds(sourceIds, null, size);

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
        @DisplayName("두 번째 페이지 조회 (lastId 존재) - 다음 페이지가 없는 경우")
        void getPersonalizedFeeds_NextPage_NoNext() {
            // given
            List<Long> sourceIds = List.of(1L);
            int size = 10;

            LocalDateTime cursorTime = LocalDateTime.of(2025, 12, 6, 12, 0, 0);
            long lastId = cursorTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            String expectedDateString = ES_DATE_FORMATTER.format(Instant.ofEpochMilli(lastId));

            List<ContentDocument> documents = List.of(
                    createDocument(100L, "Old Article", cursorTime.minusDays(1))
            );

            when(contentDocumentRepository.searchBySourceIdsAndCursor(eq(sourceIds), eq(expectedDateString), any(Pageable.class)))
                    .thenReturn(documents);

            // when
            CommonPageResponse<ContentFeedResponseDto> response =
                    feedService.getPersonalizedFeeds(sourceIds, lastId, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isHasNext()).isFalse();
            assertThat(response.getNextCursorId()).isNull();

            verify(contentDocumentRepository).searchBySourceIdsAndCursor(eq(sourceIds), eq(expectedDateString), any(Pageable.class));
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

    @Nested
    @DisplayName("ID 기반 피드 조회 (GetContentsByIds)")
    class GetContentsByIdsTest {

        @Test
        @DisplayName("성공: ID 목록으로 문서를 조회하여 DTO로 반환한다")
        void getContentsByIds_success() {
            // given
            List<String> ids = List.of("1", "2");
            LocalDateTime now = LocalDateTime.now();
            List<ContentDocument> docs = List.of(
                    ContentDocument.builder().contentId(1L).title("T1").publishedAt(now).build(),
                    ContentDocument.builder().contentId(2L).title("T2").publishedAt(now).build()
            );

            when(contentDocumentRepository.findAllById(ids)).thenReturn(docs);

            // when
            List<ContentFeedResponseDto> result = feedService.getContentsByIds(ids);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("T1");
        }
    }
}