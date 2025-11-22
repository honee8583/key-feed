package com.leedahun.identityservice.domain.keyword.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.util.test.WithAnonymousUser;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithAnonymousUser
@WebMvcTest(controllers = KeywordInternalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class KeywordInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KeywordService keywordService;

    @Test
    @DisplayName("userId로 키워드 목록을 조회하면 200 OK와 JSON 리스트를 반환한다")
    void getActiveKeywords_Success() throws Exception {
        // given
        Long userId = 100L;

        KeywordResponseDto keyword1 = KeywordResponseDto.builder()
                .keywordId(1L)
                .name("Java")
                .isNotificationEnabled(true)
                .build();

        KeywordResponseDto keyword2 = KeywordResponseDto.builder()
                .keywordId(2L)
                .name("Spring Boot")
                .isNotificationEnabled(false)
                .build();

        List<KeywordResponseDto> responseList = List.of(keyword1, keyword2);

        // Mocking: 서비스가 호출되면 미리 준비한 리스트를 반환하도록 설정
        when(keywordService.getKeywords(userId)).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/internal/users/{userId}/keywords", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"));

        // Verify: 서비스 메서드가 실제로 호출되었는지 검증
        verify(keywordService).getKeywords(userId);
    }

    @Test
    @DisplayName("키워드가 없는 경우 빈 리스트를 반환한다")
    void getActiveKeywords_Empty() throws Exception {
        // given
        Long userId = 200L;
        when(keywordService.getKeywords(userId)).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/internal/users/{userId}/keywords", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0))
                .andExpect(content().json("[]"));
    }
}