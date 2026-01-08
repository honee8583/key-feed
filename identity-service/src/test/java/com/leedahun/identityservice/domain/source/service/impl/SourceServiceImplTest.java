package com.leedahun.identityservice.domain.source.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.source.dto.SourceRequestDto;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.entity.Source;
import com.leedahun.identityservice.domain.source.entity.UserSource;
import com.leedahun.identityservice.domain.source.exception.InvalidRssUrlException;
import com.leedahun.identityservice.domain.source.repository.SourceRepository;
import com.leedahun.identityservice.domain.source.repository.UserSourceRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    private final Long USER_ID = 1L;
    private final String INPUT_URL = "https://d2.naver.com";
    private final String RSS_URL = "https://d2.naver.com/d2.atom";
    private final String SOURCE_NAME = "네이버 D2";

    @Test
    @DisplayName("내 소스 목록 조회 성공")
    void getSourcesByUser_Success() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .build();

        Source source = Source.builder()
                .id(100L)
                .url(RSS_URL)
                .build();

        UserSource userSource = UserSource.builder()
                .id(10L)
                .user(user)
                .source(source)
                .userDefinedName(SOURCE_NAME)
                .build();

        when(userSourceRepository.findByUserId(USER_ID)).thenReturn(List.of(userSource));

        // when
        List<SourceResponseDto> result = sourceService.getSourcesByUser(USER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserDefinedName()).isEqualTo(SOURCE_NAME);
        assertThat(result.get(0).getUrl()).isEqualTo(RSS_URL);
    }

    @Test
    @DisplayName("새 소스 등록 성공 - Jsoup으로 RSS 발견")
    void addSource_NewSource_Success() {
        String html = "<html><head>" +
                "<link rel='alternate' type='application/rss+xml' href='" + RSS_URL + "'>" +
                "</head><body></body></html>";
        Document realDocument = Jsoup.parse(html);
        realDocument.setBaseUri(INPUT_URL);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder()
                    .url(INPUT_URL)
                    .name(SOURCE_NAME)
                    .build();

            User user = User.builder()
                    .id(USER_ID)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            Connection connection = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(INPUT_URL)).thenReturn(connection);
            when(connection.timeout(anyInt())).thenReturn(connection);
            when(connection.get()).thenReturn(realDocument);

            when(sourceRepository.findByUrl(RSS_URL)).thenReturn(Optional.empty());
            when(sourceRepository.save(any(Source.class))).thenReturn(Source.builder().id(100L).url(RSS_URL).build());

            // 이전에 저장한 적 없음
            when(userSourceRepository.existsByUserIdAndSourceId(USER_ID, 100L)).thenReturn(false);

            when(userSourceRepository.save(any(UserSource.class))).thenAnswer(invocation -> {
                UserSource us = invocation.getArgument(0);  // 호출할때 넘겨준 첫번째 인자
                return UserSource.builder()
                        .id(200L)
                        .user(us.getUser())
                        .source(us.getSource())
                        .userDefinedName(us.getUserDefinedName())
                        .build();
            });

            // when
            SourceResponseDto response = sourceService.addSource(USER_ID, request);

            // then
            assertThat(response.getUrl()).isEqualTo(RSS_URL); // 발견된 RSS 주소
            assertThat(response.getUserDefinedName()).isEqualTo(SOURCE_NAME);

            // Verify
            verify(sourceRepository, times(1)).save(any(Source.class));
            verify(userSourceRepository, times(1)).save(any(UserSource.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("새 소스 등록 실패 - Jsoup 접속 오류 (InvalidRssUrlException)")
    void addSource_JsoupConnectionFail() throws IOException {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder()
                    .url(INPUT_URL)
                    .name(SOURCE_NAME)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

            Connection connection = mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(INPUT_URL)).thenReturn(connection);
            when(connection.timeout(anyInt())).thenReturn(connection);

            // 예외 발생
            when(connection.get()).thenThrow(new IOException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                    .isInstanceOf(InvalidRssUrlException.class);
        }
    }

    @Test
    @DisplayName("새 소스 등록 - Jsoup 파싱 실패 시 원본 URL 사용")
    void addSource_JsoupParseFail_UseOriginalUrl() {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder()
                    .url(RSS_URL)
                    .name(SOURCE_NAME)
                    .build();

            User user = User.builder()
                    .id(USER_ID)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // Jsoup이 알 수 없는 에러를 발생시키는 경우
            jsoupMock.when(() -> Jsoup.connect(RSS_URL)).thenThrow(new RuntimeException("Unexpected parse error"));

            // Source는 원본 URL(RSS_URL)로 검색됨
            when(sourceRepository.findByUrl(RSS_URL)).thenReturn(Optional.empty());
            when(sourceRepository.save(any(Source.class))).thenAnswer(i -> i.getArgument(0));
            when(userSourceRepository.save(any(UserSource.class))).thenAnswer(i -> i.getArgument(0));

            // when
            SourceResponseDto response = sourceService.addSource(USER_ID, request);

            // then
            // 예외가 발생하지 않고 원본 URL 그대로 사용되는지 확인
            assertThat(response.getUrl()).isEqualTo(RSS_URL);
        }
    }

    @Test
    @DisplayName("소스 등록 실패 - 이미 등록된 소스")
    void addSource_DuplicateSource() {
        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            // given
            SourceRequestDto request = SourceRequestDto.builder()
                    .url(INPUT_URL)
                    .name(SOURCE_NAME)
                    .build();

            Source existingSource = Source.builder()
                    .id(99L)
                    .url(INPUT_URL)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

            // 알 수 없는 에러 발생
            jsoupMock.when(() -> Jsoup.connect(INPUT_URL)).thenThrow(new RuntimeException("Error"));

            // 이미 존재하는 소스 리턴
            when(sourceRepository.findByUrl(INPUT_URL)).thenReturn(Optional.of(existingSource));

            // 이미 동일한 소스를 저장한 적이 있는 경우
            when(userSourceRepository.existsByUserIdAndSourceId(USER_ID, 99L)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sourceService.addSource(USER_ID, request))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }
    }

    @Test
    @DisplayName("소스 구독 취소 성공")
    void removeUserSource_Success() {
        // given
        Long userSourceId = 10L;
        UserSource userSource = UserSource.builder()
                .id(userSourceId)
                .build();
        when(userSourceRepository.findByIdAndUserId(userSourceId, USER_ID)).thenReturn(Optional.of(userSource));

        // when
        sourceService.removeUserSource(USER_ID, userSourceId);

        // then
        verify(userSourceRepository, times(1)).delete(userSource);
    }

    @Test
    @DisplayName("소스 구독 취소 실패 - 존재하지 않는 구독")
    void removeUserSource_NotFound() {
        // given
        Long userSourceId = 10L;
        when(userSourceRepository.findByIdAndUserId(userSourceId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sourceService.removeUserSource(USER_ID, userSourceId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}