package com.leedahun.identityservice.domain.auth.service;

import com.leedahun.identityservice.domain.auth.dto.LoginRequestDto;
import com.leedahun.identityservice.domain.auth.dto.LoginResult;
import com.leedahun.identityservice.domain.auth.dto.TokenResult;

public interface LoginService {

    LoginResult login(LoginRequestDto loginRequestDto);

    TokenResult reissueTokens(String refreshToken);

}
