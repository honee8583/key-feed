package com.leedahun.feedservice.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.feedservice.config.SecurityConfig;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.service.FeedService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedInternalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class FeedInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedService feedService;

    @Test
    @DisplayName("[POST /internal/feeds/contents] ID 목록으로 피드 조회 성공 시 200 OK와 리스트를 반환한다")
    void getContentsByIds_success() throws Exception {
        // given
        List<String> contentIds = List.of("1", "2");

        ContentFeedResponseDto dto1 = new ContentFeedResponseDto();
        ContentFeedResponseDto dto2 = new ContentFeedResponseDto();
        // DTO 필드 설정이 필요하다면 여기서 setter나 reflection 사용

        List<ContentFeedResponseDto> responseList = List.of(dto1, dto2);

        when(feedService.getContentsByIds(any())).thenReturn(responseList);

        // when & then
        mockMvc.perform(post("/internal/feeds/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        verify(feedService).getContentsByIds(any());
    }
}