package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmRequestDto;
import com.leedahun.identityservice.domain.auth.dto.EmailVerificationConfirmResponseDto;
import com.leedahun.identityservice.domain.auth.dto.JoinRequestDto;

public interface JoinService {

    void join(JoinRequestDto joinRequestDto);

    void sendJoinEmail(String email);

    EmailVerificationConfirmResponseDto verifyEmailCode(EmailVerificationConfirmRequestDto emailVerificationConfirmRequestDto);

}
