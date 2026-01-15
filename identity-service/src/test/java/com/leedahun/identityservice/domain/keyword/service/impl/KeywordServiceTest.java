package com.leedahun.identityservice.domain.keyword.service.impl;

import com.leedahun.identityservice.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.entity.Keyword;
import com.leedahun.identityservice.domain.keyword.exception.KeywordLimitExceededException;
import com.leedahun.identityservice.domain.keyword.repository.KeywordRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KeywordServiceImpl keywordService;

    private User testUser;

    private final int KEYWORD_MAX_COUNT = 5;
    private final Long USER_ID = 1L;
    private final Long KEYWORD_ID = 10L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keywordService, "keywordMaxCount", KEYWORD_MAX_COUNT);

        testUser = User.builder()
                .email("test@test.com")
                .build();
    }

    @Nested
    @DisplayName("키워드 목록 조회")
    class GetKeywords {

        @Test
        @DisplayName("성공: 유저의 키워드 목록을 DTO로 변환하여 반환")
        void getKeywords_success() {
            // Given
            Keyword k1 = Keyword.builder().id(1L).name("Java").user(testUser).isNotificationEnabled(true).build();
            Keyword k2 = Keyword.builder().id(2L).name("Spring").user(testUser).isNotificationEnabled(false).build();
            List<Keyword> keywords = List.of(k1, k2);

            when(keywordRepository.findByUserId(USER_ID)).thenReturn(keywords);

            // When
            List<KeywordResponseDto> result = keywordService.getKeywords(USER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Java");
            assertThat(result.get(0).getIsNotificationEnabled()).isTrue();
            assertThat(result.get(1).getName()).isEqualTo("Spring");
            assertThat(result.get(1).getIsNotificationEnabled()).isFalse();
        }

        @Test
        @DisplayName("성공: 키워드가 없는 유저는 빈 목록 반환")
        void getKeywords_empty() {
            // Given
            when(keywordRepository.findByUserId(USER_ID)).thenReturn(List.of());

            // When
            List<KeywordResponseDto> result = keywordService.getKeywords(USER_ID);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("키워드 추가")
    class AddKeyword {

        @BeforeEach
        void setUp() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        }

        @Test
        @DisplayName("성공: 새 키워드를 성공적으로 추가")
        void addKeyword_success() {
            // Given
            String newKeywordName = "NewKeyword";

            when(keywordRepository.existsByNameAndUser(newKeywordName, testUser)).thenReturn(false);

            when(keywordRepository.countByUserId(USER_ID)).thenReturn((long) KEYWORD_MAX_COUNT - 1);

            when(keywordRepository.save(any(Keyword.class))).thenAnswer(invocation -> {
                Keyword savedKeyword = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedKeyword, "id", 99L);
                return savedKeyword;
            });

            // when
            KeywordResponseDto result = keywordService.addKeyword(USER_ID, newKeywordName);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(newKeywordName);
            assertThat(result.getKeywordId()).isEqualTo(99L);

            ArgumentCaptor<Keyword> keywordCaptor = ArgumentCaptor.forClass(Keyword.class);
            verify(keywordRepository).save(keywordCaptor.capture());
            assertThat(keywordCaptor.getValue().getUser()).isEqualTo(testUser);
            assertThat(keywordCaptor.getValue().getName()).isEqualTo(newKeywordName);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 유저 ID")
        void addKeyword_fail_userNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> keywordService.addKeyword(USER_ID, "AnyKeyword"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("실패: 이미 존재하는 키워드")
        void addKeyword_fail_alreadyExists() {
            // Given
            String existingKeywordName = "ExistingKeyword";

            when(keywordRepository.existsByNameAndUser(existingKeywordName, testUser)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> keywordService.addKeyword(USER_ID, existingKeywordName))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining("Keyword")
                    .hasMessageContaining(existingKeywordName);

            // save가 호출되지 않았는지 확인
            verify(keywordRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 키워드 최대 개수 초과")
        void addKeyword_fail_limitExceeded() {
            // Given
            String newKeywordName = "NewKeyword";

            // 중복없음
            when(keywordRepository.existsByNameAndUser(newKeywordName, testUser)).thenReturn(false);

            // 보유한 키워드 개수
            when(keywordRepository.countByUserId(USER_ID)).thenReturn((long) KEYWORD_MAX_COUNT);

            // When & Assert
            assertThatThrownBy(() -> keywordService.addKeyword(USER_ID, newKeywordName))
                    .isInstanceOf(KeywordLimitExceededException.class);

            // save가 호출되지 않았는지 확인
            verify(keywordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("키워드의 알림 설정 토글")
    class ToggleKeyword {

        @Test
        @DisplayName("성공: 알림 활성화(false -> true)")
        void toggleKeyword_success_offToOn() {
            // Given
            Keyword existingKeyword = Keyword.builder()
                    .id(KEYWORD_ID)
                    .name("Test")
                    .user(testUser)
                    .isNotificationEnabled(false)
                    .build();

            when(keywordRepository.findByIdAndUserId(KEYWORD_ID, USER_ID)).thenReturn(Optional.of(existingKeyword));

            when(keywordRepository.save(any(Keyword.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            KeywordResponseDto result = keywordService.toggleKeywordNotification(USER_ID, KEYWORD_ID);

            // Then
            assertThat(result.getIsNotificationEnabled()).isTrue();
            assertThat(existingKeyword.isNotificationEnabled()).isTrue();
            verify(keywordRepository).save(existingKeyword);
        }

        @Test
        @DisplayName("성공: 알림 비활성화(true -> false)")
        void toggleKeyword_success_onToOff() {
            // Given
            Keyword existingKeyword = Keyword.builder()
                    .id(KEYWORD_ID)
                    .name("Test")
                    .user(testUser)
                    .isNotificationEnabled(true)
                    .build();

            when(keywordRepository.findByIdAndUserId(KEYWORD_ID, USER_ID)).thenReturn(Optional.of(existingKeyword));
            when(keywordRepository.save(any(Keyword.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            KeywordResponseDto result = keywordService.toggleKeywordNotification(USER_ID, KEYWORD_ID);

            // Then
            assertThat(result.getIsNotificationEnabled()).isFalse();
            assertThat(existingKeyword.isNotificationEnabled()).isFalse();
            verify(keywordRepository).save(existingKeyword);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 키워드")
        void toggleKeyword_fail_notFound() {
            // Given
            when(keywordRepository.findByIdAndUserId(KEYWORD_ID, USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> keywordService.toggleKeywordNotification(USER_ID, KEYWORD_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Keyword");

            verify(keywordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteKeyword (키워드 삭제)")
    class DeleteKeyword {

        @Test
        @DisplayName("성공: 키워드 삭제")
        void deleteKeyword_success() {
            // Given
            Keyword existingKeyword = Keyword.builder()
                    .id(KEYWORD_ID)
                    .user(testUser)
                    .name("ToDelete")
                    .build();

            when(keywordRepository.findByIdAndUserId(KEYWORD_ID, USER_ID)).thenReturn(Optional.of(existingKeyword));

            doNothing().when(keywordRepository).delete(existingKeyword);

            // When
            keywordService.deleteKeyword(USER_ID, KEYWORD_ID);

            // Then
            // delete 메서드가 올바른 객체를 인자로 받아 호출되었는지 확인
            verify(keywordRepository, times(1)).delete(existingKeyword);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 키워드")
        void deleteKeyword_fail_notFound() {
            // Given
            when(keywordRepository.findByIdAndUserId(KEYWORD_ID, USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> keywordService.deleteKeyword(USER_ID, KEYWORD_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Keyword");

            // delete가 호출되지 않았는지 확인
            verify(keywordRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("키워드와 소스 ID로 유저 ID 목록 조회")
    class FindUserIdsByKeywordsAndSource {

        @Test
        @DisplayName("성공: 키워드 목록과 Source ID에 매칭되는 유저 ID 리스트 반환")
        void findUserIdsByKeywordsAndSource_success() {
            // Given
            Set<String> keywords = Set.of("Java", "Spring", "Kafka");
            Long sourceId = 100L;
            List<Long> expectedUserIds = List.of(10L, 20L, 30L);

            when(keywordRepository.findUserIdsByNamesAndSourceId(keywords, sourceId))
                    .thenReturn(expectedUserIds);

            // When
            List<Long> result = keywordService.findUserIdsByKeywordsAndSource(keywords, sourceId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(10L, 20L, 30L);

            // 리포지토리 호출 검증
            verify(keywordRepository, times(1)).findUserIdsByNamesAndSourceId(keywords, sourceId);
        }

        @Test
        @DisplayName("성공: 매칭되는 유저가 없으면 빈 리스트 반환")
        void findUserIdsByKeywordsAndSource_noMatch() {
            // Given
            Set<String> keywords = Set.of("NonExisting");
            Long sourceId = 100L;
            when(keywordRepository.findUserIdsByNamesAndSourceId(keywords, sourceId))
                    .thenReturn(List.of());

            // When
            List<Long> result = keywordService.findUserIdsByKeywordsAndSource(keywords, sourceId);

            // Then
            assertThat(result).isEmpty();
            verify(keywordRepository, times(1)).findUserIdsByNamesAndSourceId(keywords, sourceId);
        }
    }

}