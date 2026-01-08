package com.leedahun.feedservice.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.feedservice.auth.WithAnonymousUser;
import com.leedahun.feedservice.common.message.SuccessMessage;
import com.leedahun.feedservice.common.response.CommonPageResponse;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithAnonymousUser
@WebMvcTest(controllers = FeedController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedService feedService;

    @Test
    @DisplayName("[GET /api/feed] 파라미터가 포함된 피드 목록 조회 성공 시 200 OK를 반환한다")
    void getMyFeeds_success() throws Exception {
        // given
        Long lastId = 100L;
        int size = 20;
        List<Long> sourceIds = List.of(1L, 2L);

        ContentFeedResponseDto feedItem = new ContentFeedResponseDto();

        CommonPageResponse<ContentFeedResponseDto> response = new CommonPageResponse<>(
                List.of(feedItem),
                90L,
                true
        );

        when(feedService.fetchUserSourceIds(any())).thenReturn(sourceIds);
        when(feedService.getPersonalizedFeeds(eq(sourceIds), eq(lastId), eq(size)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feed")
                        .param("lastId", String.valueOf(lastId))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(true));

        verify(feedService).fetchUserSourceIds(any());
        verify(feedService).getPersonalizedFeeds(eq(sourceIds), eq(lastId), eq(size));
    }

    @Test
    @DisplayName("[GET /api/feed] 파라미터가 없으면 기본값으로 피드 목록을 조회한다")
    void getMyFeeds_defaultParams() throws Exception {
        // given
        List<Long> sourceIds = List.of(1L, 2L);

        CommonPageResponse<ContentFeedResponseDto> emptyResponse = new CommonPageResponse<>(
                Collections.emptyList(),
                null,
                false
        );

        when(feedService.fetchUserSourceIds(any())).thenReturn(sourceIds);
        when(feedService.getPersonalizedFeeds(eq(sourceIds), eq(null), eq(10)))
                .thenReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(feedService).fetchUserSourceIds(any());
        verify(feedService).getPersonalizedFeeds(eq(sourceIds), eq(null), eq(10));
    }
}