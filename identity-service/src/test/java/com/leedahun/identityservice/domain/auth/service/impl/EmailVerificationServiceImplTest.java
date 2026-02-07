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
import com.leedahun.identityservice.common.mail.EmailClient;
import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.entity.EmailPurpose;
import com.leedahun.identityservice.domain.auth.entity.EmailVerification;
import com.leedahun.identityservice.domain.auth.entity.EmailVerifyStatus;
import com.leedahun.identityservice.domain.auth.exception.EmailVerificationAlreadyDoneException;
import com.leedahun.identityservice.domain.auth.exception.EmailVerificationAttemptLimitExceededException;
import com.leedahun.identityservice.domain.auth.exception.EmailVerificationExpiredException;
import com.leedahun.identityservice.domain.auth.exception.EmailVerificationLockedException;
import com.leedahun.identityservice.domain.auth.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private EmailClient emailClient;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private static final String EMAIL = "user@test.com";
    private static final String CODE = "123456";
    private static final String SUBJECT = "[Key Feed] 테스트 이메일";
    private static final String HTML = "<html>EMAIL</html>";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "maxAttempts", 5);
        ReflectionTestUtils.setField(emailVerificationService, "expireMinutes", 10);
        ReflectionTestUtils.setField(emailVerificationService, "lockMinutes", 3);
    }

    @Nested
    @DisplayName("이메일 인증 발송 테스트")
    class SendVerificationEmailTests {

        @Test
        @DisplayName("이전 인증 기록이 없으면 새 인증을 저장하고 메일을 전송한다")
        void sendVerificationEmail_firstTime_success() {
            // given
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.empty());
            given(templateEngine.process(anyString(), any(Context.class))).willReturn(HTML);

            // when
            emailVerificationService.sendVerificationEmail(EMAIL, EmailPurpose.SIGNUP, SUBJECT);

            // then
            then(emailVerificationRepository).should().save(any(EmailVerification.class));
            then(emailClient).should().sendOneEmail(eq(EMAIL), eq(SUBJECT), eq(HTML));
        }

        @Test
        @DisplayName("이미 인증된 경우 예외를 발생한다")
        void sendVerificationEmail_alreadyVerified_throws() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.VERIFIED)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(EMAIL, EmailPurpose.SIGNUP, SUBJECT))
                    .isInstanceOf(EmailVerificationAlreadyDoneException.class);

            then(emailClient).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("잠금 상태이고 잠금 기간이 만료되지 않은 경우 예외가 발생한다")
        void sendVerificationEmail_lockedAndStillLocked_throws() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().plusMinutes(5))
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(EMAIL, EmailPurpose.SIGNUP, SUBJECT))
                    .isInstanceOf(EmailVerificationLockedException.class);

            then(emailClient).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("잠금 기간이 만료된 경우 초기화 후 재발송한다")
        void sendVerificationEmail_lockedButExpired_thenResetAndSend() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().minusMinutes(1))
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));
            given(templateEngine.process(anyString(), any(Context.class))).willReturn(HTML);

            // when
            emailVerificationService.sendVerificationEmail(EMAIL, EmailPurpose.SIGNUP, SUBJECT);

            // then
            verify(emailVerificationRepository, times(1)).save(any(EmailVerification.class));
            then(emailClient).should().sendOneEmail(eq(EMAIL), eq(SUBJECT), eq(HTML));
        }

        @Test
        @DisplayName("대기 상태이고 아직 유효한 경우 코드 갱신 후 재발송한다")
        void sendVerificationEmail_pendingAndValid_thenUpdateAndSend() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));
            given(templateEngine.process(anyString(), any(Context.class))).willReturn(HTML);

            // when
            emailVerificationService.sendVerificationEmail(EMAIL, EmailPurpose.SIGNUP, SUBJECT);

            // then
            then(emailVerificationRepository).should().save(any(EmailVerification.class));
            then(emailClient).should().sendOneEmail(eq(EMAIL), eq(SUBJECT), eq(HTML));
        }
    }

    @Nested
    @DisplayName("인증 코드 검증 테스트")
    class VerifyCodeTests {

        @Test
        @DisplayName("이미 인증 완료 상태인 경우 바로 응답한다")
        void verifyCode_alreadyVerified_returns() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.VERIFIED)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when
            EmailVerificationConfirmResponseDto result = emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.SIGNUP);

            // then
            assertThat(result.getStatus()).isEqualTo(EmailVerifyStatus.VERIFIED);
            then(emailVerificationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잠금 상태 유지 시간이 남았을 경우 예외가 발생한다")
        void verifyCode_lockedStill_throws() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().plusMinutes(3))
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.SIGNUP))
                    .isInstanceOf(EmailVerificationLockedException.class);

            then(emailVerificationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("인증 가능 시간이 만료되었을 경우 만료 상태로 저장 후 예외를 발생한다")
        void verifyCode_expired_throwsAndSaveExpired() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.PENDING)
                    .expiresAt(LocalDateTime.now().minusSeconds(1))
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.SIGNUP))
                    .isInstanceOf(EmailVerificationExpiredException.class);

            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            then(emailVerificationRepository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(EmailVerifyStatus.EXPIRED);
        }

        @Test
        @DisplayName("코드가 일치할 경우 인증 완료 상태로 저장한다")
        void verifyCode_codeMatches_success() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .code(CODE)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when
            EmailVerificationConfirmResponseDto result = emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.SIGNUP);

            // then
            assertThat(result.getStatus()).isEqualTo(EmailVerifyStatus.VERIFIED);
            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            then(emailVerificationRepository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(EmailVerifyStatus.VERIFIED);
        }

        @Test
        @DisplayName("코드가 불일치하고 시도 횟수가 남았을 경우 시도 횟수를 증가시킨다")
        void verifyCode_codeNotMatch_underMax_increasesCount() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .code(CODE)
                    .attemptCount(1)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when
            EmailVerificationConfirmResponseDto result = emailVerificationService.verifyCode(EMAIL, "wrong_code", EmailPurpose.SIGNUP);

            // then
            assertThat(result.getStatus()).isEqualTo(EmailVerifyStatus.PENDING);
            then(emailVerificationRepository).should().save(any(EmailVerification.class));
        }

        @Test
        @DisplayName("코드가 불일치하고 시도 횟수가 한계에 도달한 경우 잠금 상태로 저장한다")
        void verifyCode_codeNotMatch_reachMax_thenLockedAndThrows() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.SIGNUP)
                    .status(EmailVerifyStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .code(CODE)
                    .attemptCount(4)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.of(emailVerification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "wrong_code", EmailPurpose.SIGNUP))
                    .isInstanceOf(EmailVerificationAttemptLimitExceededException.class);

            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            then(emailVerificationRepository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(EmailVerifyStatus.LOCKED);
            assertThat(captor.getValue().getLockedUntil()).isNotNull();
        }

        @Test
        @DisplayName("기록이 없을 경우 예외가 발생한다")
        void verifyCode_noRecord_throws() {
            // given
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.SIGNUP))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, CODE, EmailPurpose.SIGNUP))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("인증 완료 여부 확인 테스트")
    class IsVerifiedTests {

        @Test
        @DisplayName("인증 완료 상태이면 true를 반환한다")
        void isVerified_verified_returnsTrue() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.RESET)
                    .status(EmailVerifyStatus.VERIFIED)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.RESET))
                    .willReturn(Optional.of(emailVerification));

            // when
            boolean result = emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("인증 미완료 상태이면 false를 반환한다")
        void isVerified_notVerified_returnsFalse() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.RESET)
                    .status(EmailVerifyStatus.PENDING)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.RESET))
                    .willReturn(Optional.of(emailVerification));

            // when
            boolean result = emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("기록이 없으면 false를 반환한다")
        void isVerified_noRecord_returnsFalse() {
            // given
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.RESET))
                    .willReturn(Optional.empty());

            // when
            boolean result = emailVerificationService.isVerified(EMAIL, EmailPurpose.RESET);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("인증 레코드 삭제 테스트")
    class DeleteVerificationTests {

        @Test
        @DisplayName("인증 레코드가 있으면 삭제한다")
        void deleteVerification_exists_deletes() {
            // given
            EmailVerification emailVerification = EmailVerification.builder()
                    .email(EMAIL)
                    .purpose(EmailPurpose.RESET)
                    .build();
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.RESET))
                    .willReturn(Optional.of(emailVerification));

            // when
            emailVerificationService.deleteVerification(EMAIL, EmailPurpose.RESET);

            // then
            then(emailVerificationRepository).should().delete(emailVerification);
        }

        @Test
        @DisplayName("인증 레코드가 없으면 아무것도 하지 않는다")
        void deleteVerification_notExists_doesNothing() {
            // given
            given(emailVerificationRepository.findTopByEmailAndPurposeOrderByIdDesc(EMAIL, EmailPurpose.RESET))
                    .willReturn(Optional.empty());

            // when
            emailVerificationService.deleteVerification(EMAIL, EmailPurpose.RESET);

            // then
            then(emailVerificationRepository).should(never()).delete(any());
        }
    }
}
