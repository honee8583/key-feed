package com.leedahun.feedservice.domain.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.feedservice.domain.feed.dto.KeywordResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.openfeign.client.config.identity-service.url=http://localhost:${wiremock.server.port}"
        }
)
@AutoConfigureWireMock(port = 0)
class UserInternalApiClientTest {

    @Autowired
    private UserInternalApiClient userInternalApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @EnableFeignClients(clients = UserInternalApiClient.class)
    @EnableAutoConfiguration
    static class TestConfig {

    }

    // WireMock이 가짜 서버 역할을 하여 네트워크 요청을 보내고 응답을 받는 전체 흐름을 테스트
    @Test
    @DisplayName("userId로 활성 키워드 목록을 조회한다")
    void getActiveKeywords_Success() throws JsonProcessingException {
        // given
        Long userId = 100L;

        KeywordResponseDto dto1 = KeywordResponseDto.builder()
                .keywordId(1L)
                .name("Spring Boot")
                .isNotificationEnabled(false)
                .build();

        KeywordResponseDto dto2 = KeywordResponseDto.builder()
                .keywordId(2L)
                .name("MSA")
                .isNotificationEnabled(false)
                .build();

        List<KeywordResponseDto> mockResponse = List.of(dto1, dto2);
        String responseBody = objectMapper.writeValueAsString(mockResponse);

        // WireMock Stub 설정
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/keywords"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)));

        // when
        List<KeywordResponseDto> result = userInternalApiClient.getActiveKeywords(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Spring Boot");
        assertThat(result.get(1).getName()).isEqualTo("MSA");
    }

}