package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.PasswordChangeRequestDto;

public interface UserService {

    void changePassword(Long userId, PasswordChangeRequestDto requestDto);

}
