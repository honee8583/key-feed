package com.leedahun.identityservice.domain.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.bookmark.service.BookmarkService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookmarkInternalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class BookmarkInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("[POST /internal/bookmarks/user/{userId}/check] 북마크 여부 Map 조회 성공 시 200 OK와 Map을 반환한다")
    void getBookmarkMap_success() throws Exception {
        // given
        Long userId = 1L;
        List<String> contentIds = List.of("100", "200");
        Map<String, Long> responseMap = Map.of("100", 10L, "200", 20L);

        when(bookmarkService.getBookmarkMap(eq(userId), any())).thenReturn(responseMap);

        // when & then
        mockMvc.perform(post("/internal/bookmarks/user/{userId}/check", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.100").value(10L))
                .andExpect(jsonPath("$.200").value(20L));

        verify(bookmarkService).getBookmarkMap(eq(userId), any());
    }
}