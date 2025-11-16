package com.leedahun.identityservice.domain.auth.dto;

import com.leedahun.identityservice.domain.auth.entity.EmailVerification;
import com.leedahun.identityservice.domain.auth.entity.EmailVerifyStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationConfirmResponseDto {
    private EmailVerifyStatus status;  // 인증상태
    private int attempts;              // 시도횟수
    private LocalDateTime retryAt;     // 잠금 기간
    private LocalDateTime expiresAt;   // 만료 기간

    public static EmailVerificationConfirmResponseDto from(EmailVerification emailVerification) {
        return EmailVerificationConfirmResponseDto.builder()
                .status(emailVerification.getStatus())
                .attempts(emailVerification.getAttemptCount())
                .retryAt(emailVerification.getLockedUntil())
                .expiresAt(emailVerification.getExpiresAt())
                .build();
    }
}
