package com.leedahun.identityservice.domain.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.dto.PasswordChangeRequestDto;
import com.leedahun.identityservice.domain.auth.dto.WithdrawRequestDto;
import com.leedahun.identityservice.domain.auth.entity.Role;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.exception.InvalidPasswordException;
import com.leedahun.identityservice.domain.auth.exception.PasswordMismatchException;
import com.leedahun.identityservice.domain.auth.exception.SamePasswordException;
import com.leedahun.identityservice.domain.auth.exception.UserAlreadyWithdrawnException;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkFolderRepository;
import com.leedahun.identityservice.domain.bookmark.repository.BookmarkRepository;
import com.leedahun.identityservice.domain.keyword.repository.KeywordRepository;
import com.leedahun.identityservice.domain.source.repository.UserSourceRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    KeywordRepository keywordRepository;

    @Mock
    UserSourceRepository userSourceRepository;

    @Mock
    BookmarkFolderRepository bookmarkFolderRepository;

    @Mock
    BookmarkRepository bookmarkRepository;

    @InjectMocks
    UserServiceImpl userService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@test.com";
    private static final String CURRENT_PASSWORD = "currentPW!";
    private static final String NEW_PASSWORD = "newPassword!";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String NEW_ENCODED_PASSWORD = "$2a$10$newencoded";

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword(CURRENT_PASSWORD)
                .newPassword(NEW_PASSWORD)
                .confirmPassword(NEW_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(passwordEncoder.matches(NEW_PASSWORD, ENCODED_PASSWORD)).willReturn(false);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(NEW_ENCODED_PASSWORD);

        // when
        userService.changePassword(USER_ID, requestDto);

        // then
        verify(userRepository).findById(USER_ID);
        verify(passwordEncoder).matches(CURRENT_PASSWORD, ENCODED_PASSWORD);
        verify(passwordEncoder).matches(NEW_PASSWORD, ENCODED_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        assertThat(user.getPassword()).isEqualTo(NEW_ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자 없음")
    void changePassword_userNotFound_throws() {
        // given
        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword(CURRENT_PASSWORD)
                .newPassword(NEW_PASSWORD)
                .confirmPassword(NEW_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, requestDto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_wrongCurrentPassword_throws() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("wrongPassword")
                .newPassword(NEW_PASSWORD)
                .confirmPassword(NEW_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).willReturn(false);

        // when / then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 확인 불일치")
    void changePassword_confirmPasswordMismatch_throws() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword(CURRENT_PASSWORD)
                .newPassword(NEW_PASSWORD)
                .confirmPassword("differentPassword")
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, requestDto))
                .isInstanceOf(PasswordMismatchException.class);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호와 동일")
    void changePassword_samePassword_throws() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword(CURRENT_PASSWORD)
                .newPassword(CURRENT_PASSWORD)
                .confirmPassword(CURRENT_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.changePassword(USER_ID, requestDto))
                .isInstanceOf(SamePasswordException.class);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void withdraw_success() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password(CURRENT_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

        // when
        userService.withdraw(USER_ID, requestDto);

        // then
        verify(userRepository).findById(USER_ID);
        verify(passwordEncoder).matches(CURRENT_PASSWORD, ENCODED_PASSWORD);
        verify(bookmarkRepository).deleteAllByUserId(USER_ID);
        verify(bookmarkFolderRepository).deleteAllByUserId(USER_ID);
        verify(userSourceRepository).deleteAllByUserId(USER_ID);
        verify(keywordRepository).deleteAllByUserId(USER_ID);
        assertThat(user.isWithdraw()).isTrue();
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 사용자 없음")
    void withdraw_userNotFound_throws() {
        // given
        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password(CURRENT_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.withdraw(USER_ID, requestDto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(bookmarkRepository, never()).deleteAllByUserId(any());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 이미 탈퇴한 사용자")
    void withdraw_alreadyWithdrawn_throws() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .isWithdraw(true)
                .build();

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password(CURRENT_PASSWORD)
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

        // when / then
        assertThatThrownBy(() -> userService.withdraw(USER_ID, requestDto))
                .isInstanceOf(UserAlreadyWithdrawnException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(bookmarkRepository, never()).deleteAllByUserId(any());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 비밀번호 불일치")
    void withdraw_wrongPassword_throws() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password("wrongPassword")
                .build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).willReturn(false);

        // when / then
        assertThatThrownBy(() -> userService.withdraw(USER_ID, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(bookmarkRepository, never()).deleteAllByUserId(any());
    }

}
