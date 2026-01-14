package com.leedahun.feedservice.domain.feed.service.impl;

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
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    @InjectMocks
    private FeedServiceImpl feedService;

    @Mock
    private UserInternalApiClient userInternalApiClient;

    @Mock
    private ContentDocumentRepository contentDocumentRepository;

    @Nested
    @DisplayName("유저 구독 소스 ID 목록 조회 (FetchUserSourceIds)")
    class FetchUserSourceIdsTest {

        @Test
        @DisplayName("성공: FeignClient로부터 소스 목록을 받아와 ID 리스트로 반환한다")
        void success() {
            // given
            Long userId = 1L;
            SourceResponseDto mockDto = mock(SourceResponseDto.class);
            when(mockDto.getSourceId()).thenReturn(10L);

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(mockDto));

            // when
            List<Long> result = feedService.fetchUserSourceIds(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(10L);
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
            Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
            FeignException feignException = new FeignException.NotFound("Not Found", request, null, null);

            when(userInternalApiClient.getUserSources(userId)).thenThrow(feignException);

            // when
            // then
            assertThatThrownBy(() -> feedService.fetchUserSourceIds(userId))
                    .isInstanceOf(InternalApiRequestException.class);
        }

        @Test
        @DisplayName("실패: 기타 예외 발생 시 InternalServerProcessingException을 던진다")
        void fail_general_exception() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getUserSources(userId)).thenThrow(new RuntimeException("DB Error"));

            // when
            // then
            assertThatThrownBy(() -> feedService.fetchUserSourceIds(userId))
                    .isInstanceOf(InternalServerProcessingException.class);
        }
    }

    @Nested
    @DisplayName("개인화 피드 조회 (GetPersonalizedFeeds)")
    class GetPersonalizedFeedsTest {

        @Test
        @DisplayName("성공: sourceIds가 비어있으면 저장소 조회 없이 빈 페이지를 반환한다")
        void success_empty_sourceIds() {
            // given
            Long userId = 1L;
            List<Long> sourceIds = Collections.emptyList();

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceIds, null, 10);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.isHasNext()).isFalse();
            verifyNoInteractions(contentDocumentRepository);
        }

        @Test
        @DisplayName("성공: 첫 페이지 조회 (lastPublishedAt null) - 다음 페이지가 있는 경우")
        void success_first_page_has_next() {
            // given
            Long userId = 1L;
            List<Long> sourceIds = List.of(100L, 200L);
            int size = 2;

            // doc1, doc2: 결과 리스트에 포함되어 DTO 변환 시 getter가 호출되므로 스터빙 필요 (Helper 사용)
            ContentDocument doc1 = createMockContentDocument("doc1", LocalDateTime.now());
            ContentDocument doc2 = createMockContentDocument("doc2", LocalDateTime.now().minusHours(1));

            // doc3: 단순히 리스트 크기(hasNext) 확인용으로만 쓰이고 잘려나감.
            // 내부 메서드가 호출되지 않으므로 스터빙을 하지 않은 'Raw Mock' 사용 (UnnecessaryStubbing 방지)
            ContentDocument doc3 = mock(ContentDocument.class);

            // size(2) + 1 = 3개를 반환하도록 설정
            when(contentDocumentRepository.searchBySourceIdsFirstPage(eq(sourceIds), any(Pageable.class)))
                    .thenReturn(List.of(doc1, doc2, doc3));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceIds, null, size);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.isHasNext()).isTrue();
            assertThat(response.getNextCursorId()).isNotNull();
            verify(contentDocumentRepository).searchBySourceIdsFirstPage(eq(sourceIds), any(Pageable.class));
        }

        @Test
        @DisplayName("성공: 커서 기반 조회 (lastPublishedAt not null) - 다음 페이지가 없는 경우")
        void success_next_page_no_next() {
            // given
            Long userId = 1L;
            List<Long> sourceIds = List.of(100L);
            long lastPublishedAt = System.currentTimeMillis();
            int size = 10;

            ContentDocument doc1 = createMockContentDocument("doc1", LocalDateTime.now());

            when(contentDocumentRepository.searchBySourceIdsAndCursor(eq(sourceIds), anyString(), any(Pageable.class)))
                    .thenReturn(List.of(doc1));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceIds, lastPublishedAt, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isHasNext()).isFalse();
            verify(contentDocumentRepository).searchBySourceIdsAndCursor(eq(sourceIds), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공: 유저 ID가 있고 피드가 존재하면 북마크 정보를 매핑한다")
        void success_map_bookmark_info() {
            // given
            Long userId = 1L;
            List<Long> sourceIds = List.of(100L);
            int size = 10;

            ContentDocument doc = createMockContentDocument("content-1", LocalDateTime.now());
            when(contentDocumentRepository.searchBySourceIdsFirstPage(eq(sourceIds), any(Pageable.class)))
                    .thenReturn(List.of(doc));

            Map<String, Long> bookmarkMap = Map.of("content-1", 999L);
            when(userInternalApiClient.getBookmarkedContentIds(eq(userId), anyList()))
                    .thenReturn(bookmarkMap);

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceIds, null, size);

            // then
            assertThat(response.getContent().get(0).getBookmarkId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("예외 처리: 북마크 조회 중 에러가 발생해도 피드 목록은 정상 반환된다 (로그 출력 후 진행)")
        void exception_ignore_bookmark_api_fail() {
            // given
            Long userId = 1L;
            List<Long> sourceIds = List.of(100L);
            int size = 10;

            ContentDocument doc = createMockContentDocument("doc1", LocalDateTime.now());
            when(contentDocumentRepository.searchBySourceIdsFirstPage(eq(sourceIds), any(Pageable.class)))
                    .thenReturn(List.of(doc));

            // 북마크 API 호출 시 예외 발생 설정
            when(userInternalApiClient.getBookmarkedContentIds(eq(userId), anyList()))
                    .thenThrow(new RuntimeException("Internal API Connection Fail"));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceIds, null, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getContentId()).isEqualTo("doc1");
            assertThat(response.getContent().get(0).getBookmarkId()).isNull(); // 예외 발생 시 null 처리 확인
            verify(userInternalApiClient).getBookmarkedContentIds(eq(userId), anyList());
        }
    }

    @Nested
    @DisplayName("ID 기반 피드 조회 (GetContentsByIds)")
    class GetContentsByIdsTest {

        @Test
        @DisplayName("성공: ID 목록으로 문서를 조회하여 반환한다")
        void success() {
            // given
            List<String> contentIds = List.of("c1", "c2");
            ContentDocument doc1 = createMockContentDocument("c1", LocalDateTime.now());
            ContentDocument doc2 = createMockContentDocument("c2", LocalDateTime.now());

            when(contentDocumentRepository.findAllById(contentIds)).thenReturn(List.of(doc1, doc2));

            // when
            List<ContentFeedResponseDto> result = feedService.getContentsByIds(contentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(ContentFeedResponseDto::getContentId)).contains("c1", "c2");
        }
    }

    // --- Helper Method ---
    private ContentDocument createMockContentDocument(String id, LocalDateTime publishedAt) {
        ContentDocument doc = mock(ContentDocument.class);
        // 실제로 호출될 객체들만 이 메서드를 통해 생성하므로 strict stubbing 준수 가능
        when(doc.getId()).thenReturn(id);
        when(doc.getPublishedAt()).thenReturn(publishedAt);
        return doc;
    }
}