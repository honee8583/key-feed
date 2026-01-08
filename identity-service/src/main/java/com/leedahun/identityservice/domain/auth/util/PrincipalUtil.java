package com.leedahun.identityservice.domain.auth.util;

import com.leedahun.identityservice.domain.auth.dto.LoginUser;

public class PrincipalUtil {

    public static Long getUserId(LoginUser loginUser) {
        if (loginUser == null) {
            return null;
        }

        return loginUser.getId();
    }

}
