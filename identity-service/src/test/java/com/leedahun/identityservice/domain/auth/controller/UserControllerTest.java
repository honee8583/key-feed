package com.leedahun.identityservice.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.common.message.SuccessMessage;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.dto.PasswordChangeRequestDto;
import com.leedahun.identityservice.domain.auth.dto.WithdrawRequestDto;
import com.leedahun.identityservice.domain.auth.exception.InvalidPasswordException;
import com.leedahun.identityservice.domain.auth.exception.PasswordMismatchException;
import com.leedahun.identityservice.domain.auth.exception.SamePasswordException;
import com.leedahun.identityservice.domain.auth.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    UserService userService;

    private static final Long USER_ID = 1L;

    private void setAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 비밀번호 변경 성공 시 200 OK와 성공 메시지를 반환한다")
    void changePassword_success() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("currentPW!")
                .newPassword("newPassword!")
                .confirmPassword("newPassword!")
                .build();

        willDoNothing().given(userService).changePassword(eq(USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value(SuccessMessage.PASSWORD_CHANGE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(userService).should(times(1)).changePassword(eq(USER_ID), any(PasswordChangeRequestDto.class));
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 현재 비밀번호가 틀리면 InvalidPasswordException이 발생한다")
    void changePassword_wrongCurrentPassword_throws() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword!")
                .confirmPassword("newPassword!")
                .build();

        willThrow(new InvalidPasswordException()).given(userService).changePassword(eq(USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 새 비밀번호 확인이 일치하지 않으면 PasswordMismatchException이 발생한다")
    void changePassword_confirmPasswordMismatch_throws() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("currentPW!")
                .newPassword("newPassword!")
                .confirmPassword("differentPassword")
                .build();

        willThrow(new PasswordMismatchException()).given(userService).changePassword(eq(USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 현재 비밀번호와 동일하면 SamePasswordException이 발생한다")
    void changePassword_samePassword_throws() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("currentPW!")
                .newPassword("currentPW!")
                .confirmPassword("currentPW!")
                .build();

        willThrow(new SamePasswordException()).given(userService).changePassword(eq(USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 현재 비밀번호가 비어있으면 400 BAD REQUEST를 반환한다")
    void changePassword_blankCurrentPassword_returns400() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("")
                .newPassword("newPassword!")
                .confirmPassword("newPassword!")
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[PATCH /api/users/password] 새 비밀번호가 8자 미만이면 400 BAD REQUEST를 반환한다")
    void changePassword_shortNewPassword_returns400() throws Exception {
        // given
        setAuthentication(USER_ID);

        PasswordChangeRequestDto requestDto = PasswordChangeRequestDto.builder()
                .currentPassword("currentPW!")
                .newPassword("short")
                .confirmPassword("short")
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[DELETE /api/users] 회원탈퇴 성공 시 200 OK와 성공 메시지를 반환한다")
    void withdraw_success() throws Exception {
        // given
        setAuthentication(USER_ID);

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password("currentPW!")
                .build();

        willDoNothing().given(userService).withdraw(eq(USER_ID), any(WithdrawRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WITHDRAW_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(userService).should(times(1)).withdraw(eq(USER_ID), any(WithdrawRequestDto.class));
    }

    @Test
    @DisplayName("[DELETE /api/users] 비밀번호가 틀리면 InvalidPasswordException이 발생한다")
    void withdraw_wrongPassword_throws() throws Exception {
        // given
        setAuthentication(USER_ID);

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password("wrongPassword")
                .build();

        willThrow(new InvalidPasswordException()).given(userService).withdraw(eq(USER_ID), any(WithdrawRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[DELETE /api/users] 비밀번호가 비어있으면 400 BAD REQUEST를 반환한다")
    void withdraw_blankPassword_returns400() throws Exception {
        // given
        setAuthentication(USER_ID);

        WithdrawRequestDto requestDto = WithdrawRequestDto.builder()
                .password("")
                .build();

        // when & then
        mockMvc.perform(delete("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

}
