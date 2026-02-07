package com.leedahun.identityservice.domain.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetConfirmRequestDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetRequestDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetVerifyRequestDto;
import com.leedahun.identityservice.domain.auth.entity.EmailPurpose;
import com.leedahun.identityservice.domain.auth.entity.EmailVerifyStatus;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.exception.EmailVerificationRequiredException;
import com.leedahun.identityservice.domain.auth.exception.PasswordMismatchException;
import com.leedahun.identityservice.domain.auth.exception.SamePasswordException;
import com.leedahun.identityservice.domain.auth.repository.UserRepository;
import com.leedahun.identityservice.domain.auth.service.EmailVerificationService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private static final String EMAIL = "user@test.com";
    private static final String CODE = "123456";
    private static final String NEW_PASSWORD = "newPassword123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    @Nested
    @DisplayName("비밀번호 재설정 요청 테스트")
    class RequestPasswordResetTests {

        @Test
        @DisplayName("등록된 사용자의 이메일로 요청하면 인증 이메일이 발송된다")
        void requestPasswordReset_success() {
            // given
            PasswordResetRequestDto requestDto = PasswordResetRequestDto.builder()
                    .email(EMAIL)
                    .build();
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(new User()));

            // when
            passwordResetService.requestPasswordReset(requestDto);

            // then
            then(emailVerificationService).should().sendVerificationEmail(
                    eq(EMAIL),
                    eq(EmailPurpose.RESET),
                    anyString()
            );
        }

        @Test
        @DisplayName("등록되지 않은 이메일로 요청하면 EntityNotFoundException이 발생한다")
        void requestPasswordReset_userNotFound_throws() {
            // given
            PasswordResetRequestDto requestDto = PasswordResetRequestDto.builder()
                    .email(EMAIL)
                    .build();
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> passwordResetService.requestPasswordReset(requestDto))
                    .isInstanceOf(EntityNotFoundException.class);

            then(emailVerificationService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("인증 코드 검증 테스트")
    class VerifyCodeTests {

        @Test
        @DisplayName("올바른 인증 코드로 검증하면 성공한다")
        void verifyCode_success() {
            // given
            PasswordResetVerifyRequestDto requestDto = PasswordResetVerifyRequestDto.builder()
                    .email(EMAIL)
                    .code(CODE)
                    .build();
            EmailVerificationConfirmResponseDto expectedResponse = EmailVerificationConfirmResponseDto.builder()
                    .status(EmailVerifyStatus.VERIFIED)
                    .build();
            given(emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.RESET))
                    .willReturn(expectedResponse);

            // when
            EmailVerificationConfirmResponseDto result = passwordResetService.verifyCode(requestDto);

            // then
            assertThat(result.getStatus()).isEqualTo(EmailVerifyStatus.VERIFIED);
            then(emailVerificationService).should().verifyCode(EMAIL, CODE, EmailPurpose.RESET);
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 확정 테스트")
    class ResetPasswordTests {

        @Test
        @DisplayName("인증 완료 후 비밀번호 재설정에 성공한다")
        void resetPassword_success() {
            // given
            String oldEncodedPassword = "$2a$10$oldEncoded";
            PasswordResetConfirmRequestDto requestDto = PasswordResetConfirmRequestDto.builder()
                    .email(EMAIL)
                    .newPassword(NEW_PASSWORD)
                    .confirmPassword(NEW_PASSWORD)
                    .build();
            User user = User.builder()
                    .email(EMAIL)
                    .password(oldEncodedPassword)
                    .build();

            given(emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET)).willReturn(true);
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(NEW_PASSWORD, oldEncodedPassword)).willReturn(false);
            given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            // when
            passwordResetService.resetPassword(requestDto);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
            then(emailVerificationService).should().deleteVerification(EMAIL, EmailPurpose.RESET);
        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 PasswordMismatchException이 발생한다")
        void resetPassword_passwordMismatch_throws() {
            // given
            PasswordResetConfirmRequestDto requestDto = PasswordResetConfirmRequestDto.builder()
                    .email(EMAIL)
                    .newPassword(NEW_PASSWORD)
                    .confirmPassword("differentPassword")
                    .build();

            // when & then
            assertThatThrownBy(() -> passwordResetService.resetPassword(requestDto))
                    .isInstanceOf(PasswordMismatchException.class);

            then(userRepository).should(never()).save(any());
            then(emailVerificationService).should(never()).deleteVerification(anyString(), any());
        }

        @Test
        @DisplayName("이메일 인증이 완료되지 않았으면 EmailVerificationRequiredException이 발생한다")
        void resetPassword_notVerified_throws() {
            // given
            PasswordResetConfirmRequestDto requestDto = PasswordResetConfirmRequestDto.builder()
                    .email(EMAIL)
                    .newPassword(NEW_PASSWORD)
                    .confirmPassword(NEW_PASSWORD)
                    .build();
            given(emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> passwordResetService.resetPassword(requestDto))
                    .isInstanceOf(EmailVerificationRequiredException.class);

            then(userRepository).should(never()).save(any());
            then(emailVerificationService).should(never()).deleteVerification(anyString(), any());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면 EntityNotFoundException이 발생한다")
        void resetPassword_userNotFound_throws() {
            // given
            PasswordResetConfirmRequestDto requestDto = PasswordResetConfirmRequestDto.builder()
                    .email(EMAIL)
                    .newPassword(NEW_PASSWORD)
                    .confirmPassword(NEW_PASSWORD)
                    .build();
            given(emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET)).willReturn(true);
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> passwordResetService.resetPassword(requestDto))
                    .isInstanceOf(EntityNotFoundException.class);

            then(emailVerificationService).should(never()).deleteVerification(anyString(), any());
        }

        @Test
        @DisplayName("새 비밀번호가 기존 비밀번호와 동일하면 SamePasswordException이 발생한다")
        void resetPassword_samePassword_throws() {
            // given
            String oldEncodedPassword = "$2a$10$oldEncoded";
            PasswordResetConfirmRequestDto requestDto = PasswordResetConfirmRequestDto.builder()
                    .email(EMAIL)
                    .newPassword(NEW_PASSWORD)
                    .confirmPassword(NEW_PASSWORD)
                    .build();
            User user = User.builder()
                    .email(EMAIL)
                    .password(oldEncodedPassword)
                    .build();

            given(emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET)).willReturn(true);
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(NEW_PASSWORD, oldEncodedPassword)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> passwordResetService.resetPassword(requestDto))
                    .isInstanceOf(SamePasswordException.class);

            then(userRepository).should(never()).save(any());
            then(emailVerificationService).should(never()).deleteVerification(anyString(), any());
        }
    }
}
