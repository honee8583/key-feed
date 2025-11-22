package com.leedahun.feedservice.domain.feed.controller;

import static com.leedahun.feedservice.common.message.SuccessMessage.READ_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leedahun.feedservice.auth.WithAnonymousUser;
import com.leedahun.feedservice.common.response.CommonPageResponse;
import com.leedahun.feedservice.domain.feed.dto.ContentFeedResponseDto;
import com.leedahun.feedservice.domain.feed.service.FeedService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithAnonymousUser
@WebMvcTest(FeedController.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @Test
    @DisplayName("나의 피드 조회 성공: 키워드와 파라미터를 기반으로 피드 목록을 반환한다")
    void getMyFeeds_success() throws Exception {
        // given
        Long userId = 1L;
        Long lastId = 100L;
        int size = 20;

        // 사용자의 활성 키워드 조회
        List<String> mockKeywords = List.of("IT", "Development");
        when(feedService.fetchActiveKeywordNames(any())).thenReturn(mockKeywords);

        // 피드 조회 결과
        CommonPageResponse<ContentFeedResponseDto> mockResponse = CommonPageResponse.<ContentFeedResponseDto>builder()
                .content(Collections.emptyList())
                .hasNext(true)
                .nextCursorId(10L)
                .build();
        when(feedService.getPersonalizedFeed(any(), anyLong(), anyInt())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/feed")
                        .param("lastId", String.valueOf(lastId))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").exists());

        verify(feedService).fetchActiveKeywordNames(userId);
        verify(feedService).getPersonalizedFeed(mockKeywords, lastId, size);
    }

    @Test
    @DisplayName("required=false인 파라미터는 null 또는 기본값으로 처리된다")
    void getMyFeeds_without_params() throws Exception {
        // given
        Long userId = 2L;
        List<String> mockKeywords = List.of("Sports");

        when(feedService.fetchActiveKeywordNames(userId)).thenReturn(mockKeywords);

        CommonPageResponse<ContentFeedResponseDto> mockResponse = CommonPageResponse.<ContentFeedResponseDto>builder()
                .content(Collections.emptyList())
                .hasNext(true)
                .nextCursorId(10L)
                .build();
        when(feedService.getPersonalizedFeed(any(), isNull(), anyInt())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/feed")
                        .param("size", "10")) // lastId는 보내지 않음 -> null
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").exists());
    }
}