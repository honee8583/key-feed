package com.leedahun.identityservice.domain.source.validator;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.leedahun.identityservice.domain.source.validator.UrlValidator.ValidationLevel;
import com.leedahun.identityservice.domain.source.validator.UrlValidator.ValidationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class UrlValidatorTest {

    private WireMockServer wireMockServer;
    private UrlValidator urlValidator;

    @BeforeEach
    void setUp() {
        // 랜덤 포트로 가짜 서버 시작
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        urlValidator = new UrlValidator();
    }

    @AfterEach
    void tearDown() {
        // 테스트 종료 후 가짜 서버 중지
        wireMockServer.stop();
    }

    // 가짜 서버의 URL 주소 생성 헬퍼
    private String getMockUrl(String path) {
        return "http://localhost:" + wireMockServer.port() + path;
    }

    @Test
    @DisplayName("성공: 유효한 URL이고 XML 컨텐츠 타입이면 VALID 반환")
    void validate_Success_Xml() {
        // given
        String path = "/rss";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=utf-8")));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("성공: HTML 컨텐츠 타입도 VALID 반환 (RSS 자동 탐색 대상이므로)")
    void validate_Success_Html() {
        // given
        String path = "/blog";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("경고: 연결은 성공했으나 지원하지 않는 Content-Type일 경우 WARNING 반환")
    void validate_Warning_InvalidContentType() {
        // given
        String path = "/image.png";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "image/png")));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.WARNING);
    }

    @Test
    @DisplayName("실패: 404 Not Found 응답 시 INVALID 반환")
    void validate_Fail_404() {
        // given
        String path = "/not-found";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(404)));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("실패: 403 Forbidden 응답 시 INVALID 반환")
    void validate_Fail_403() {
        // given
        String path = "/forbidden";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(403)));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("실패: 500 Server Error 응답 시 INVALID 반환")
    void validate_Fail_500() {
        // given
        String path = "/error";
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(500)));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("실패: 연결 시간 초과 (Timeout) 발생 시 INVALID 반환")
    void validate_Fail_Timeout() {
        // given
        String path = "/timeout";
        // 10초 타임아웃 설정이므로, 11초 지연을 줌
        stubFor(head(urlEqualTo(path))
                .willReturn(aResponse()
                        .withFixedDelay(11000)
                        .withStatus(200)));

        // when
        ValidationResult result = urlValidator.validate(getMockUrl(path));

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("실패: 지원하지 않는 프로토콜 (ftp) 사용")
    void validate_Fail_InvalidProtocol() {
        // given
        String ftpUrl = "ftp://example.com/resource";

        // when
        ValidationResult result = urlValidator.validate(ftpUrl);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("실패: 잘못된 URL 형식 (MalformedURLException)")
    void validate_Fail_MalformedUrl() {
        // given
        String invalidUrl = "this-is-not-a-url";

        // when
        ValidationResult result = urlValidator.validate(invalidUrl);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }
}