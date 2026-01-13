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

class RssFeedValidatorTest {

    private WireMockServer wireMockServer;
    private RssFeedValidator rssFeedValidator;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        rssFeedValidator = new RssFeedValidator();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private String getMockUrl(String path) {
        return "http://localhost:" + wireMockServer.port() + path;
    }

    @Test
    @DisplayName("성공: 정상적인 RSS 2.0 XML을 파싱하면 true 반환")
    void canParseFeed_Success_Rss20() {
        // given
        String validRssXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <title>테스트 블로그</title>
                        <link>https://example.com</link>
                        <description>테스트입니다</description>
                        <item>
                            <title>첫 번째 글</title>
                            <link>https://example.com/1</link>
                        </item>
                    </channel>
                </rss>
                """;

        stubFor(get(urlEqualTo("/rss"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(validRssXml)));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/rss"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공: 제어 문자(0x08 등)가 포함된 XML도 정제 후 파싱 성공")
    void canParseFeed_Success_WithControlCharacters() {
        // given
        // \u0008 (Backspace) 같은 제어 문자는 XML 표준 위반이라 파싱 에러가 나야 정상이지만,
        // 코드 내 replaceAll 로직이 이를 제거해주는지 테스트
        String dirtyXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <title>제어문자\u0008포함된\u000B피드</title> 
                        <item><title>Test</title></item>
                    </channel>
                </rss>
                """;

        stubFor(get(urlEqualTo("/dirty-rss"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(dirtyXml)));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/dirty-rss"));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패: RSS 형식이 아닌 일반 HTML 응답 시 false 반환")
    void canParseFeed_Fail_HtmlResponse() {
        // given
        String htmlBody = "<html><body><h1>이것은 블로그 메인입니다.</h1></body></html>";

        stubFor(get(urlEqualTo("/blog"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(htmlBody)));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/blog"));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패: XML이지만 RSS/Atom 구조가 없는 빈 껍데기일 경우 false 반환")
    void canParseFeed_Fail_InvalidStructure() {
        // given
        // 유효한 XML이지만 <rss>나 <feed> 태그가 없음
        String invalidXml = "<note><to>User</to><from>Me</from></note>";

        stubFor(get(urlEqualTo("/xml-api"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(invalidXml)));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/xml-api"));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패: 404 Not Found 응답 시 false 반환")
    void canParseFeed_Fail_404() {
        // given
        stubFor(get(urlEqualTo("/not-found"))
                .willReturn(aResponse().withStatus(404)));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/not-found"));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패: 타임아웃(10초) 발생 시 false 반환")
    void canParseFeed_Fail_Timeout() {
        // given
        stubFor(get(urlEqualTo("/timeout"))
                .willReturn(aResponse()
                        .withFixedDelay(11000) // 11초 지연 (TIMEOUT 10초)
                        .withStatus(200)
                        .withBody("<rss>...</rss>")));

        // when
        boolean result = rssFeedValidator.canParseFeed(getMockUrl("/timeout"));

        // then
        assertThat(result).isFalse();
    }
}