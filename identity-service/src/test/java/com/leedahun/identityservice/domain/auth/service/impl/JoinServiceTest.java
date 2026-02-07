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

import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmRequestDto;
import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.dto.JoinRequestDto;
import com.leedahun.identityservice.domain.auth.entity.EmailPurpose;
import com.leedahun.identityservice.domain.auth.entity.EmailVerifyStatus;
import com.leedahun.identityservice.domain.auth.entity.User;
import com.leedahun.identityservice.domain.auth.exception.UserAlreadyExistsException;
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
class JoinServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private JoinServiceImpl joinService;

    private static final String EMAIL = "user@test.com";
    private static final String NAME = "tester";
    private static final String RAW_PW = "plainPW!";
    private static final String ENC_PW = "$2a$10$encoded";
    private static final String EMAIL_CODE = "123456";

    @Nested
    @DisplayName("회원가입 테스트")
    class JoinTests {

        @Test
        @DisplayName("신규 이메일이면 사용자를 저장하고 비밀번호를 암호화한다")
        void join_success() {
            // given
            JoinRequestDto joinRequest = JoinRequestDto.builder()
                    .email(EMAIL)
                    .name(NAME)
                    .password(RAW_PW)
                    .build();
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
            given(passwordEncoder.encode(RAW_PW)).willReturn(ENC_PW);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // when
            joinService.join(joinRequest);

            // then
            verify(userRepository, times(1)).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getUsername()).isEqualTo(NAME);
            assertThat(saved.getPassword()).isEqualTo(ENC_PW);
        }

        @Test
        @DisplayName("이메일 중복이면 UserAlreadyExistsException을 발생시킨다")
        void join_duplicateEmail_throws() {
            // given
            JoinRequestDto joinRequest = JoinRequestDto.builder()
                    .email(EMAIL)
                    .name(NAME)
                    .password(RAW_PW)
                    .build();
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(new User()));

            // when & then
            assertThatThrownBy(() -> joinService.join(joinRequest))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("회원가입 이메일 전송 요청 테스트")
    class SendJoinEmailTests {

        @Test
        @DisplayName("회원가입 이메일 요청 시 EmailVerificationService를 호출한다")
        void sendJoinEmail_delegatesToEmailVerificationService() {
            // when
            joinService.sendJoinEmail(EMAIL);

            // then
            then(emailVerificationService).should().sendVerificationEmail(
                    eq(EMAIL),
                    eq(EmailPurpose.SIGNUP),
                    anyString()
            );
        }
    }

    @Nested
    @DisplayName("회원가입 이메일 코드 검증 테스트")
    class VerifyEmailCodeTests {

        @Test
        @DisplayName("코드 검증 시 EmailVerificationService를 호출한다")
        void verifyEmailCode_delegatesToEmailVerificationService() {
            // given
            EmailVerificationConfirmRequestDto requestDto = EmailVerificationConfirmRequestDto.builder()
                    .email(EMAIL)
                    .code(EMAIL_CODE)
                    .build();
            EmailVerificationConfirmResponseDto expectedResponse = EmailVerificationConfirmResponseDto.builder()
                    .status(EmailVerifyStatus.VERIFIED)
                    .build();
            given(emailVerificationService.verifyCode(EMAIL, EMAIL_CODE, EmailPurpose.SIGNUP))
                    .willReturn(expectedResponse);

            // when
            EmailVerificationConfirmResponseDto result = joinService.verifyEmailCode(requestDto);

            // then
            assertThat(result.getStatus()).isEqualTo(EmailVerifyStatus.VERIFIED);
            then(emailVerificationService).should().verifyCode(EMAIL, EMAIL_CODE, EmailPurpose.SIGNUP);
        }
    }
}
