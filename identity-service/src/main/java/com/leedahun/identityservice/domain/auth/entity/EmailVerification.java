package com.leedahun.identityservice.domain.auth.entity;

import com.leedahun.identityservice.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmailVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private EmailPurpose purpose;

    @Column(length = 6)
    private String code;

    private LocalDateTime expiresAt;

    @Builder.Default
    private Integer attemptCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EmailVerifyStatus status =  EmailVerifyStatus.PENDING;

    private LocalDateTime lockedUntil;

    public void updateStatus(EmailVerifyStatus status) {
        this.status = status;
    }

    public void updateLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void increaseAttemptCount() {
        this.attemptCount++;
    }

    public void resetAttemptCount() {
        this.attemptCount = 0;
    }

    public void clearLockedUntil() {
        this.lockedUntil = null;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void resetExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
