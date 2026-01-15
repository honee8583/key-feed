package com.leedahun.identityservice.domain.keyword.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.util.test.WithAnonymousUser;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private ObjectMapper objectMapper;

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

        when(keywordService.getKeywords(userId)).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/internal/users/{userId}/keywords", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"));

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

    @Test
    @DisplayName("[POST /internal/keywords/match-users] 키워드 목록으로 매칭되는 유저 ID 조회 성공 시 200 OK와 ID 리스트를 반환한다")
    void findUserIdsByKeywords_Success() throws Exception {
        // given
        Set<String> keywords = Set.of("Java", "Spring", "Kafka");
        List<Long> matchedUserIds = List.of(10L, 20L, 30L);
        Long sourceId = 1L;

        when(keywordService.findUserIdsByKeywordsAndSource(anySet(), anyLong())).thenReturn(matchedUserIds);

        // when & then
        mockMvc.perform(post("/internal/keywords/match-users")
                        .param("sourceId", String.valueOf(sourceId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(keywords)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0]").value(10L))
                .andExpect(jsonPath("$[1]").value(20L))
                .andExpect(jsonPath("$[2]").value(30L));

        verify(keywordService).findUserIdsByKeywordsAndSource(anySet(), anyLong());
    }

    @Test
    @DisplayName("[POST /internal/keywords/match-users] 매칭되는 유저가 없는 경우 200 OK와 빈 리스트를 반환한다")
    void findUserIdsByKeywords_NoMatch() throws Exception {
        // given
        Set<String> keywords = Set.of("NonExistentKeyword");
        Long sourceId = 1L;

        when(keywordService.findUserIdsByKeywordsAndSource(anySet(), anyLong())).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(post("/internal/keywords/match-users")
                        .param("sourceId", String.valueOf(sourceId)) // 필수 파라미터 sourceId 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(keywords)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0))
                .andExpect(content().json("[]"));

        verify(keywordService).findUserIdsByKeywordsAndSource(anySet(), anyLong());
    }
}