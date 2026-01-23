package com.leedahun.identityservice.domain.source.service.impl;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.common.message.ErrorMessage;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.source.dto.SourceRequestDto;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.entity.Source;
import com.leedahun.identityservice.domain.source.entity.UserSource;
import com.leedahun.identityservice.domain.source.exception.SourceValidationException;
import com.leedahun.identityservice.domain.source.repository.SourceRepository;
import com.leedahun.identityservice.domain.source.repository.UserSourceRepository;
import com.leedahun.identityservice.domain.source.validator.RobotsTxtValidator;
import com.leedahun.identityservice.domain.source.validator.RssFeedValidator;
import com.leedahun.identityservice.domain.source.validator.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.leedahun.identityservice.common.message.ErrorMessage.RSS_PARSING_FAILED;
import static com.leedahun.identityservice.domain.source.validator.UrlValidator.ValidationResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceServiceImplTest {

    @InjectMocks
    private SourceServiceImpl sourceService;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private UserSourceRepository userSourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrlValidator urlValidator;

    @Mock
    private RobotsTxtValidator robotsTxtValidator;

    @Mock
    private RssFeedValidator rssFeedValidator;

    private final Long USER_ID = 1L;
    private final String INPUT_URL = "https://d2.naver.com";
    private final String RSS_URL = "https://d2.naver.com/d2.atom";
    private final String SOURCE_NAME = "네이버 D2";

    @Test
    @DisplayName("내 소스 목록 조회 성공")
    void getSourcesByUser_Success() {
        // given
        UserSource userSource = UserSource.builder()
                .id(10L)
                .user(User.builder().id(USER_ID).build())
                .source(Source.builder().url(RSS_URL).build())
                .userDefinedName(SOURCE_NAME)
                .receiveFeed(true)
                .build();

        when(userSourceRepository.findByUserId(USER_ID)).thenReturn(List.of(userSource));

        // when
        List<SourceResponseDto> result = sourceService.getSourcesByUser(USER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserDefinedName()).isEqualTo(SOURCE_NAME);
        assertThat(result.get(0).getReceiveFeed()).isTrue();
    }

    @Test
    @DisplayName("새 소스 등록 성공 - 모든 검증 통과")
    void addSource_Success() {
        // Jsoup Mocking Context
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder().url(INPUT_URL).name(SOURCE_NAME).build();
            User user = User.builder().id(USER_ID).build();

            // 1. User 조회
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // 2. URL Validator 통과
            when(urlValidator.validate(INPUT_URL)).thenReturn(ValidationResult.valid());

            // 3. Jsoup Discovery (HTML에서 RSS 링크 발견 시뮬레이션)
            mockJsoupConnection(jsoupMock, INPUT_URL, RSS_URL);

            // 4. Robots.txt 통과
            when(robotsTxtValidator.isAllowedToCrawl(RSS_URL)).thenReturn(true);

            // 5. RSS Parsing 통과
            when(rssFeedValidator.canParseFeed(RSS_URL)).thenReturn(true);

            // 6. Repository 저장 동작
            when(sourceRepository.findByUrl(RSS_URL)).thenReturn(Optional.empty()); // 새 소스
            when(sourceRepository.save(any(Source.class))).thenReturn(Source.builder().id(100L).url(RSS_URL).build());
            when(userSourceRepository.existsByUserIdAndSourceId(USER_ID, 100L)).thenReturn(false);
            when(userSourceRepository.save(any(UserSource.class))).thenAnswer(i -> i.getArgument(0));

            // when
            SourceResponseDto response = sourceService.addSource(USER_ID, request);

            // then
            assertThat(response.getUrl()).isEqualTo(RSS_URL);

            // 검증 순서 확인
            verify(urlValidator).validate(INPUT_URL);
            verify(robotsTxtValidator).isAllowedToCrawl(RSS_URL);
            verify(rssFeedValidator).canParseFeed(RSS_URL);
        }
    }

    @Test
    @DisplayName("새 소스 등록 실패 - 기본 URL 검증 실패")
    void addSource_Fail_UrlValidation() {
        // given
        SourceRequestDto request = SourceRequestDto.builder().url("invalid-url").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

        // Validator가 Invalid 리턴
        when(urlValidator.validate(anyString()))
                .thenReturn(ValidationResult.invalid("잘못된 URL"));

        // when & then
        assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                .isInstanceOf(SourceValidationException.class)
                .hasMessage("잘못된 URL");

        // 이후 로직 실행 안됨 검증
        verify(robotsTxtValidator, never()).isAllowedToCrawl(anyString());
    }

    @Test
    @DisplayName("새 소스 등록 실패 - Robots.txt 차단")
    void addSource_Fail_RobotsTxtDisallowed() {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder().url(INPUT_URL).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

            when(urlValidator.validate(INPUT_URL)).thenReturn(ValidationResult.valid());
            mockJsoupConnection(jsoupMock, INPUT_URL, RSS_URL); // RSS 주소 발견

            // Robots.txt 차단 설정
            when(robotsTxtValidator.isAllowedToCrawl(RSS_URL)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                    .isInstanceOf(SourceValidationException.class)
                    .hasMessage(ErrorMessage.ROBOTS_TXT_DISALLOWED.getMessage());

            // RSS 파싱 검사는 실행되지 않아야 함
            verify(rssFeedValidator, never()).canParseFeed(anyString());
        }
    }

    @Test
    @DisplayName("새 소스 등록 실패 - RSS 파싱 실패")
    void addSource_Fail_RssParsing() {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder().url(INPUT_URL).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

            when(urlValidator.validate(INPUT_URL)).thenReturn(ValidationResult.valid());
            mockJsoupConnection(jsoupMock, INPUT_URL, RSS_URL);
            when(robotsTxtValidator.isAllowedToCrawl(RSS_URL)).thenReturn(true);

            // RSS 파싱 실패 설정
            when(rssFeedValidator.canParseFeed(RSS_URL)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                    .isInstanceOf(SourceValidationException.class)
                    .hasMessage(RSS_PARSING_FAILED.getMessage());

            // 저장은 실행되지 않아야 함
            verify(sourceRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("새 소스 등록 실패 - 이미 등록된 소스 (EntityAlreadyExistsException)")
    void addSource_Fail_DuplicateSource() {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder().url(INPUT_URL).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

            // 모든 검증 통과
            when(urlValidator.validate(INPUT_URL)).thenReturn(ValidationResult.valid());
            mockJsoupConnection(jsoupMock, INPUT_URL, RSS_URL);
            when(robotsTxtValidator.isAllowedToCrawl(RSS_URL)).thenReturn(true);
            when(rssFeedValidator.canParseFeed(RSS_URL)).thenReturn(true);

            // 이미 DB에 소스가 있고, 유저 매핑도 있음
            Source existingSource = Source.builder().id(99L).url(RSS_URL).build();
            when(sourceRepository.findByUrl(RSS_URL)).thenReturn(Optional.of(existingSource));
            when(userSourceRepository.existsByUserIdAndSourceId(USER_ID, 99L)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }
    }

    @Test
    @DisplayName("소스 구독 취소 성공")
    void removeUserSource_Success() {
        Long userSourceId = 10L;
        UserSource userSource = UserSource.builder().id(userSourceId).build();
        when(userSourceRepository.findByIdAndUserId(userSourceId, USER_ID)).thenReturn(Optional.of(userSource));

        sourceService.removeUserSource(USER_ID, userSourceId);

        verify(userSourceRepository).delete(userSource);
    }

    @Test
    @DisplayName("소스 검색 성공 - 키워드가 있을 때 검색 리포지토리를 호출한다")
    void searchMySources_Success_WithKeyword() {
        // given
        String keyword = "naver";
        UserSource userSource = UserSource.builder()
                .id(20L)
                .user(User.builder().id(USER_ID).build())
                .source(Source.builder().url(RSS_URL).build())
                .userDefinedName(SOURCE_NAME)
                .receiveFeed(true)
                .build();

        when(userSourceRepository.searchByUserIdAndKeyword(USER_ID, keyword))
                .thenReturn(List.of(userSource));

        // when
        List<SourceResponseDto> result = sourceService.searchMySources(USER_ID, keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserDefinedName()).isEqualTo(SOURCE_NAME);
        assertThat(result.get(0).getReceiveFeed()).isTrue();
        verify(userSourceRepository).searchByUserIdAndKeyword(USER_ID, keyword);
    }

    @Test
    @DisplayName("소스 검색 성공 - 키워드가 없으면 전체 목록 조회 메서드를 호출한다")
    void searchMySources_Success_NoKeyword() {
        // given
        String emptyKeyword = "   ";
        when(userSourceRepository.findByUserId(USER_ID)).thenReturn(List.of());

        // when
        sourceService.searchMySources(USER_ID, emptyKeyword);

        // then
        verify(userSourceRepository).findByUserId(USER_ID);
        verify(userSourceRepository, never()).searchByUserIdAndKeyword(anyLong(), anyString());
    }

    @Test
    @DisplayName("피드 수신 여부 토글 성공")
    void toggleReceiveFeed_Success() {
        // given
        Long userSourceId = 10L;
        UserSource userSource = UserSource.builder()
                .id(userSourceId)
                .user(User.builder().id(USER_ID).build())
                .source(Source.builder().id(100L).url(RSS_URL).build())
                .userDefinedName(SOURCE_NAME)
                .receiveFeed(true)
                .build();

        when(userSourceRepository.findByIdAndUserId(userSourceId, USER_ID))
                .thenReturn(Optional.of(userSource));

        // when
        SourceResponseDto result = sourceService.toggleReceiveFeed(USER_ID, userSourceId);

        // then
        assertThat(result.getReceiveFeed()).isFalse();
        assertThat(userSource.getReceiveFeed()).isFalse();
    }

    @Test
    @DisplayName("피드 수신 여부 토글 실패 - 소스를 찾을 수 없음")
    void toggleReceiveFeed_Fail_NotFound() {
        // given
        Long userSourceId = 999L;
        when(userSourceRepository.findByIdAndUserId(userSourceId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sourceService.toggleReceiveFeed(USER_ID, userSourceId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private void mockJsoupConnection(MockedStatic<Jsoup> jsoupMock, String inputUrl, String detectedRssUrl) {
        try {
            Connection connection = mock(Connection.class);
            when(connection.timeout(anyInt())).thenReturn(connection);
            when(connection.userAgent(anyString())).thenReturn(connection);

            // HTML 문자열 생성
            String html = "<html><head><link rel='alternate' type='application/rss+xml' href='" + detectedRssUrl + "'></head><body></body></html>";

            jsoupMock.when(() -> Jsoup.parse(anyString())).thenCallRealMethod();

            Document document = Jsoup.parse(html);
            document.setBaseUri(inputUrl);

            when(connection.get()).thenReturn(document);

            // Jsoup.connect()는 가짜 Connection을 반환하도록 설정
            jsoupMock.when(() -> Jsoup.connect(inputUrl)).thenReturn(connection);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}