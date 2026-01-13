package com.leedahun.identityservice.domain.source.validator;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class RobotsTxtValidatorTest {

    private WireMockServer wireMockServer;
    private RobotsTxtValidator robotsTxtValidator;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        robotsTxtValidator = new RobotsTxtValidator();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private String getMockUrl(String path) {
        return "http://localhost:" + wireMockServer.port() + path;
    }

    @Test
    @DisplayName("기본 허용: robots.txt 파일이 없으면(404) 크롤링 허용")
    void isAllowed_WhenRobotsTxtNotFound_ReturnsTrue() {
        // given
        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse().withStatus(404)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/any/path"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("차단: 모든 봇(*)에 대해 특정 경로 금지 시 차단 확인")
    void isAllowed_WhenDisallowedForAll_ReturnsFalse() {
        // given
        String robotsTxtBody = "User-agent: *\nDisallow: /admin";

        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(robotsTxtBody)));

        // when
        // /admin/login 경로는 /admin 으로 시작하므로 차단되어야 함
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/admin/login"));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("허용: 금지된 경로가 아니면 크롤링 허용")
    void isAllowed_WhenPathNotDisallowed_ReturnsTrue() {
        // given
        String robotsTxtBody = "User-agent: *\nDisallow: /admin";

        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(robotsTxtBody)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/public/news"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("봇 이름 매칭: KeyFeedBot에 대한 규칙이 우선 적용되어 차단")
    void isAllowed_WhenDisallowedForKeyFeedBot_ReturnsFalse() {
        // given
        // *는 허용하지만, KeyFeedBot은 /private를 금지하는 경우
        String robotsTxtBody =
                "User-agent: *\n" +
                        "Disallow: \n" + // 전체 허용
                        "\n" +
                        "User-agent: KeyFeedBot\n" +
                        "Disallow: /private";

        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(robotsTxtBody)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/private/secret"));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("봇 이름 매칭: 다른 봇(Googlebot)의 금지 규칙은 무시하고 허용")
    void isAllowed_WhenDisallowedForOtherBot_ReturnsTrue() {
        // given
        String robotsTxtBody =
                "User-agent: Googlebot\n" +
                        "Disallow: /news\n" +
                        "\n" +
                        "User-agent: *\n" +
                        "Disallow: /admin";

        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(robotsTxtBody)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/news/today"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예외 발생: 서버 연결 실패(Timeout) 시 기본적으로 허용 (Fail-Open)")
    void isAllowed_WhenConnectionFails_ReturnsTrue() {
        // given
        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withFixedDelay(6000) // 5초 타임아웃보다 길게 지연
                        .withStatus(200)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/any/path"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("주석 처리: # 주석 뒤에 있는 내용은 무시")
    void isAllowed_IgnoreComments() {
        // given
        String robotsTxtBody =
                "User-agent: * # 모든 봇 적용\n" +
                        "Disallow: /admin # 관리자 페이지 금지";

        stubFor(get(urlEqualTo("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(robotsTxtBody)));

        // when
        boolean result = robotsTxtValidator.isAllowedToCrawl(getMockUrl("/admin/dashboard"));

        // then
        assertThat(result).isFalse();
    }
}