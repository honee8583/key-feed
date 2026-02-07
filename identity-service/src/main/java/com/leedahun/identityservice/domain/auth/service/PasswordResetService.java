package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetConfirmRequestDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetRequestDto;
import com.leedahun.identityservice.domain.auth.dto.PasswordResetVerifyRequestDto;

public interface PasswordResetService {

    /**
     * 비밀번호 재설정을 위한 인증 이메일 발송
     * @param requestDto 이메일 주소
     */
    void requestPasswordReset(PasswordResetRequestDto requestDto);

    /**
     * 인증 코드 검증
     * @param requestDto 이메일, 인증 코드
     * @return 인증 결과
     */
    EmailVerificationConfirmResponseDto verifyCode(PasswordResetVerifyRequestDto requestDto);

    /**
     * 비밀번호 재설정 완료
     * @param requestDto 이메일, 새 비밀번호, 비밀번호 확인
     */
    void resetPassword(PasswordResetConfirmRequestDto requestDto);
}
