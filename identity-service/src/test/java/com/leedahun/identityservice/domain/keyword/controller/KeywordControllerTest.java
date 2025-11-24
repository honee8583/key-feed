package com.leedahun.identityservice.domain.keyword.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.when;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.common.message.SuccessMessage;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.util.test.WithAnonymousUser;
import com.leedahun.identityservice.domain.keyword.dto.KeywordCreateRequestDto;
import com.leedahun.identityservice.domain.keyword.dto.KeywordResponseDto;
import com.leedahun.identityservice.domain.keyword.service.KeywordService;
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
@WebMvcTest(controllers = KeywordController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class KeywordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeywordService keywordService;

    @Test
    @DisplayName("[GET /api/keywords] 내 키워드 목록 조회 성공 시 200 OK와 키워드 목록을 반환한다")
    void getMyKeywords_success() throws Exception {
        // Given
        List<KeywordResponseDto> keywordList = List.of(
                new KeywordResponseDto(1L, "Java", true),
                new KeywordResponseDto(2L, "Spring", false)
        );

        when(keywordService.getKeywords(any())).thenReturn(keywordList);

        // When & Then
        mockMvc.perform(get("/api/keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is(SuccessMessage.READ_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data", hasSize(2)));

        // 서비스가 정확히 1번 호출되었는지 검증
        then(keywordService).should(times(1)).getKeywords(any());
    }

    @Test
    @DisplayName("[POST /api/keywords] 키워드 추가 성공 시 201 Created와 생성된 키워드를 반환한다")
    void addKeyword_success() throws Exception {
        // given
        KeywordCreateRequestDto requestDto = new KeywordCreateRequestDto("New Keyword");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        KeywordResponseDto responseDto = new KeywordResponseDto(3L, "New Keyword", false);

        when(keywordService.addKeyword(any(), any())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/keywords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WRITE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.keywordId").value(3))
                .andExpect(jsonPath("$.data.name").value("New Keyword"))
                .andExpect(jsonPath("$.data.isNotificationEnabled").value(false));

        // Verify
        then(keywordService).should(times(1)).addKeyword(any(), any());
    }

    @Test
    @DisplayName("[PATCH /api/keywords/{id}/toggle] 키워드 알림 토글 성공 시 200 OK와 수정된 키워드를 반환한다")
    void toggleKeywordNotification_success() throws Exception {
        // given
        Long keywordId = 10L;

        KeywordResponseDto responseDto = new KeywordResponseDto(keywordId, "Toggled", true);

        given(keywordService.toggleKeywordNotification(any(), any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/keywords/{keywordId}/toggle", keywordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.keywordId").value(10))
                .andExpect(jsonPath("$.data.isNotificationEnabled").value(true));

        // Verify
        then(keywordService).should(times(1)).toggleKeywordNotification(any(), any());
    }

    @Test
    @DisplayName("[DELETE /api/keywords/{id}] 키워드 삭제 성공 시 200 OK와 null 데이터를 반환한다")
    void deleteKeyword_success() throws Exception {
        // given
        Long keywordId = 11L;

        doNothing().when(keywordService).deleteKeyword(any(), any());

        // when & then
        mockMvc.perform(delete("/api/keywords/{keywordId}", keywordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.DELETE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // Verify
        then(keywordService).should(times(1)).deleteKeyword(any(), any());
    }

    @Test
    @DisplayName("[DELETE /api/keywords/{id}] 존재하지 않는 키워드 삭제 시 409 CONFLICT를 반환한다")
    void deleteKeyword_fail_notFound() throws Exception {
        // given
        Long badKeywordId = 99L;

        willThrow(new EntityNotFoundException("Keyword", badKeywordId))
                .given(keywordService).deleteKeyword(any(), any());

        // when & then
        mockMvc.perform(delete("/api/keywords/{keywordId}", badKeywordId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(nullValue()));

        // Verify
        then(keywordService).should(times(1)).deleteKeyword(any(), any());
    }
}