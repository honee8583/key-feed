package com.leedahun.identityservice.domain.auth.dto;

import com.leedahun.identityservice.domain.auth.entity.Role;
import com.leedahun.identityservice.domain.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private String accessToken;

    public static LoginResponseDto from(User user, String accessToken) {
        return LoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .role(user.getRole())
                .accessToken(accessToken)
                .build();
    }
}
