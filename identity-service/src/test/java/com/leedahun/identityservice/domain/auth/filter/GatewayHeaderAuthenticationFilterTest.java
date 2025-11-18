package com.leedahun.identityservice.domain.auth.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayHeaderAuthenticationFilterTest {

    private GatewayHeaderAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Roles";

    @BeforeEach
    void setUp() {
        filter = new GatewayHeaderAuthenticationFilter();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        // 컨텍스트 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 X-User-Id와 X-User-Roles 헤더가 존재할 경우 인증 성공")
    void doFilterInternal_withValidHeaders_shouldSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader(USER_ID_HEADER, "123");
        request.addHeader(ROLE_HEADER, "ROLE_USER");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 null이 아닌지 확인
        assertThat(authentication).isNotNull();

        // 인증 객체가 올바른 타입인지 확인
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);

        // Principal (사용자 ID)이 올바르게 설정되었는지 확인
        assertThat(authentication.getPrincipal()).isEqualTo(123L);

        // Credentials는 null이어야 함
        assertThat(authentication.getCredentials()).isNull();

        // 권한이 올바르게 설정되었는지 확인
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities())
                .map(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }

    @Test
    @DisplayName("X-User-Id 헤더가 없을 경우 인증 객체를 생성하지 않음")
    void doFilterInternal_missingUserIdHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader(ROLE_HEADER, "ROLE_USER"); // Role 헤더만 존재

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 생성되지 않아야 함
        assertThat(authentication).isNull();
    }

    @Test
    @DisplayName("X-User-Roles 헤더가 없을 경우 인증 객체를 생성하지 않음")
    void doFilterInternal_missingRoleHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader(USER_ID_HEADER, "123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 생성되지 않아야 함
        assertThat(authentication).isNull();
    }

    @Test
    @DisplayName("X-User-Id 헤더가 비어있을 경우(blank) 인증 객체를 생성하지 않음")
    void doFilterInternal_blankUserIdHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader(USER_ID_HEADER, " ");
        request.addHeader(ROLE_HEADER, "ROLE_USER");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 생성되지 않아야 함
        assertThat(authentication).isNull();
    }

    @Test
    @DisplayName("X-User-Id 헤더가 숫자가 아닐 경우(NumberFormatException) 인증 객체를 생성하지 않음")
    void doFilterInternal_invalidUserIdHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader(USER_ID_HEADER, "not-a-number");
        request.addHeader(ROLE_HEADER, "ROLE_USER");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // NumberFormatException이 발생하고 catch 블록으로 이동하므로 인증 객체가 생성되지 않아야 함
        assertThat(authentication).isNull();
    }
}