package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.entity.EmailPurpose;

public interface EmailVerificationService {

    /**
     * 이메일 인증 코드 발송
     * @param email 수신자 이메일
     * @param purpose 인증 목적 (SIGNUP, RESET, CHANGE)
     * @param subject 이메일 제목
     */
    void sendVerificationEmail(String email, EmailPurpose purpose, String subject);

    /**
     * 이메일 인증 코드 검증
     * @param email 이메일
     * @param code 인증 코드
     * @param purpose 인증 목적
     * @return 인증 결과
     */
    EmailVerificationConfirmResponseDto verifyCode(String email, String code, EmailPurpose purpose);

    /**
     * 이메일 인증 완료 여부 확인
     * @param email 이메일
     * @param purpose 인증 목적
     * @return 인증 완료 여부
     */
    boolean isVerified(String email, EmailPurpose purpose);

    /**
     * 인증 완료된 레코드 삭제 (비밀번호 재설정 완료 후 정리)
     * @param email 이메일
     * @param purpose 인증 목적
     */
    void deleteVerification(String email, EmailPurpose purpose);
}
