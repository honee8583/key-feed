package com.leedahun.feedservice.domain.feed.service.impl;

import com.leedahun.feedservice.common.error.exception.InternalApiRequestException;
import com.leedahun.feedservice.common.error.exception.InternalServerProcessingException;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.infra.client.UserInternalApiClient;
import com.leedahun.feedservice.infra.client.dto.SourceResponseDto;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.entity.Content;
import com.leedahun.feedservice.domain.feed.repository.ContentRepository;
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
    private ContentRepository contentRepository;

    @Nested
    @DisplayName("유저 구독 소스 매핑 조회 (FetchUserSourceMapping)")
    class FetchUserSourceMappingTest {

        @Test
        @DisplayName("성공: FeignClient로부터 소스 목록을 받아와 sourceId-userDefinedName 매핑을 반환한다")
        void success() {
            // given
            Long userId = 1L;
            SourceResponseDto mockDto = SourceResponseDto.builder()
                    .sourceId(10L)
                    .userDefinedName("내 기술 블로그")
                    .build();

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(mockDto));

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(10L)).isEqualTo("내 기술 블로그");
        }

        @Test
        @DisplayName("성공: 구독한 소스가 없으면 빈 맵을 반환한다")
        void success_empty() {
            // given
            Long userId = 1L;
            when(userInternalApiClient.getUserSources(userId)).thenReturn(Collections.emptyList());

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

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
            assertThatThrownBy(() -> feedService.fetchUserSourceMapping(userId))
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
            assertThatThrownBy(() -> feedService.fetchUserSourceMapping(userId))
                    .isInstanceOf(InternalServerProcessingException.class);
        }

        @Test
        @DisplayName("성공: userDefinedName이 null인 소스는 매핑에서 제외된다")
        void success_null_userDefinedName_filtered() {
            // given
            Long userId = 1L;
            SourceResponseDto sourceWithNull = SourceResponseDto.builder()
                    .sourceId(10L)
                    .userDefinedName(null)
                    .build();

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(sourceWithNull));

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: userDefinedName이 빈 문자열인 소스는 매핑에서 제외된다")
        void success_empty_userDefinedName_filtered() {
            // given
            Long userId = 1L;
            SourceResponseDto sourceWithEmpty = SourceResponseDto.builder()
                    .sourceId(10L)
                    .userDefinedName("")
                    .build();

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(sourceWithEmpty));

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: userDefinedName이 공백만 있는 소스는 매핑에서 제외된다")
        void success_whitespace_userDefinedName_filtered() {
            // given
            Long userId = 1L;
            SourceResponseDto sourceWithWhitespace = SourceResponseDto.builder()
                    .sourceId(10L)
                    .userDefinedName("   ")
                    .build();

            when(userInternalApiClient.getUserSources(userId)).thenReturn(List.of(sourceWithWhitespace));

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 유효한 userDefinedName만 매핑에 포함된다")
        void success_mixed_userDefinedName_only_valid_included() {
            // given
            Long userId = 1L;
            SourceResponseDto validSource = SourceResponseDto.builder()
                    .sourceId(10L)
                    .userDefinedName("내 기술 블로그")
                    .build();
            SourceResponseDto nullSource = SourceResponseDto.builder()
                    .sourceId(20L)
                    .userDefinedName(null)
                    .build();
            SourceResponseDto emptySource = SourceResponseDto.builder()
                    .sourceId(30L)
                    .userDefinedName("")
                    .build();
            SourceResponseDto anotherValidSource = SourceResponseDto.builder()
                    .sourceId(40L)
                    .userDefinedName("개발 뉴스")
                    .build();

            when(userInternalApiClient.getUserSources(userId))
                    .thenReturn(List.of(validSource, nullSource, emptySource, anotherValidSource));

            // when
            Map<Long, String> result = feedService.fetchUserSourceMapping(userId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(10L)).isEqualTo("내 기술 블로그");
            assertThat(result.get(40L)).isEqualTo("개발 뉴스");
            assertThat(result).doesNotContainKey(20L);
            assertThat(result).doesNotContainKey(30L);
        }
    }

    @Nested
    @DisplayName("개인화 피드 조회 (GetPersonalizedFeeds)")
    class GetPersonalizedFeedsTest {

        @Test
        @DisplayName("성공: sourceMapping이 비어있으면 저장소 조회 없이 빈 페이지를 반환한다")
        void success_empty_sourceMapping() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Collections.emptyMap();

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, null, 10);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.isHasNext()).isFalse();
            verifyNoInteractions(contentRepository);
        }

        @Test
        @DisplayName("성공: 첫 페이지 조회 (lastId=null) - 다음 페이지가 있는 경우")
        void success_first_page_has_next() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Map.of(100L, "소스1", 200L, "소스2");
            int size = 2;

            Content content1 = createContent(1L, 100L, LocalDateTime.now());
            Content content2 = createContent(2L, 200L, LocalDateTime.now().minusHours(1));
            Content content3 = createContent(3L, 100L, LocalDateTime.now().minusHours(2));

            // size(2) + 1 = 3개를 반환하도록 설정
            when(contentRepository.findBySourceIdIn(anyList(), any(Pageable.class)))
                    .thenReturn(List.of(content1, content2, content3));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, null, size);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.isHasNext()).isTrue();
            assertThat(response.getNextCursorId()).isNotNull();
            verify(contentRepository).findBySourceIdIn(anyList(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공: 커서 기반 조회 (lastId != null) - 다음 페이지가 없는 경우")
        void success_next_page_no_next() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Map.of(100L, "내 기술 블로그");
            long lastId = 100L;
            int size = 10;

            Content content1 = createContent(1L, 100L, LocalDateTime.now());

            when(contentRepository.findBySourceIdInAndIdBefore(anyList(), eq(lastId), any(Pageable.class)))
                    .thenReturn(List.of(content1));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, lastId, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isHasNext()).isFalse();
            verify(contentRepository).findBySourceIdInAndIdBefore(anyList(), eq(lastId), any(Pageable.class));
        }

        @Test
        @DisplayName("성공: 유저 ID가 있고 피드가 존재하면 북마크 정보를 매핑한다")
        void success_map_bookmark_info() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Map.of(100L, "내 기술 블로그");
            int size = 10;

            Content content = createContent(1L, 100L, LocalDateTime.now());
            when(contentRepository.findBySourceIdIn(anyList(), any(Pageable.class)))
                    .thenReturn(List.of(content));

            Map<String, Long> bookmarkMap = Map.of("1", 999L);
            when(userInternalApiClient.getBookmarkedContentIds(eq(userId), anyList()))
                    .thenReturn(bookmarkMap);

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, null, size);

            // then
            assertThat(response.getContent().get(0).getBookmarkId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("예외 처리: 북마크 조회 중 에러가 발생해도 피드 목록은 정상 반환된다 (로그 출력 후 진행)")
        void exception_ignore_bookmark_api_fail() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Map.of(100L, "내 기술 블로그");
            int size = 10;

            Content content = createContent(1L, 100L, LocalDateTime.now());
            when(contentRepository.findBySourceIdIn(anyList(), any(Pageable.class)))
                    .thenReturn(List.of(content));

            when(userInternalApiClient.getBookmarkedContentIds(eq(userId), anyList()))
                    .thenThrow(new RuntimeException("Internal API Connection Fail"));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, null, size);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getContentId()).isEqualTo("1");
            assertThat(response.getContent().get(0).getBookmarkId()).isNull();
            verify(userInternalApiClient).getBookmarkedContentIds(eq(userId), anyList());
        }

        @Test
        @DisplayName("성공: userDefinedName이 피드 sourceName에 정상 매핑된다")
        void success_userDefinedName_mapped_to_sourceName() {
            // given
            Long userId = 1L;
            Map<Long, String> sourceMapping = Map.of(100L, "내 기술 블로그", 200L, "개발 뉴스");
            int size = 10;

            Content content1 = createContent(1L, 100L, LocalDateTime.now());
            Content content2 = createContent(2L, 200L, LocalDateTime.now().minusHours(1));

            when(contentRepository.findBySourceIdIn(anyList(), any(Pageable.class)))
                    .thenReturn(List.of(content1, content2));

            // when
            CommonPageResponse<ContentFeedResponseDto> response = feedService.getPersonalizedFeeds(userId, sourceMapping, null, size);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getSourceName()).isEqualTo("내 기술 블로그");
            assertThat(response.getContent().get(1).getSourceName()).isEqualTo("개발 뉴스");
        }
    }

    @Nested
    @DisplayName("ID 기반 피드 조회 (GetContentsByIds)")
    class GetContentsByIdsTest {

        @Test
        @DisplayName("성공: ID 목록으로 콘텐츠를 조회하여 반환한다")
        void success() {
            // given
            List<String> contentIds = List.of("1", "2");
            Content content1 = createContent(1L, 100L, LocalDateTime.now());
            Content content2 = createContent(2L, 200L, LocalDateTime.now());

            when(contentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(content1, content2));

            // when
            List<ContentFeedResponseDto> result = feedService.getContentsByIds(contentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(ContentFeedResponseDto::getContentId)).contains("1", "2");
        }
    }

    // --- Helper Method ---
    private Content createContent(Long id, Long sourceId, LocalDateTime publishedAt) {
        return Content.builder()
                .id(id)
                .sourceId(sourceId)
                .sourceName("sourceName-" + sourceId)
                .title("title-" + id)
                .summary("summary-" + id)
                .publishedAt(publishedAt)
                .build();
    }
}
