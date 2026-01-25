package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.PasswordChangeRequestDto;
import com.leedahun.identityservice.domain.auth.dto.WithdrawRequestDto;

public interface UserService {

    void changePassword(Long userId, PasswordChangeRequestDto requestDto);

    void withdraw(Long userId, WithdrawRequestDto requestDto);

}
