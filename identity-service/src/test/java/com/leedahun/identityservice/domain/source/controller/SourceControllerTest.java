package com.leedahun.identityservice.domain.source.controller;

import static com.leedahun.identityservice.common.message.SuccessMessage.DELETE_SUCCESS;
import static com.leedahun.identityservice.common.message.SuccessMessage.READ_SUCCESS;
import static com.leedahun.identityservice.common.message.SuccessMessage.UPDATE_SUCCESS;
import static com.leedahun.identityservice.common.message.SuccessMessage.WRITE_SUCCESS;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.common.error.exception.EntityNotFoundException;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.util.test.WithAnonymousUser;
import com.leedahun.identityservice.domain.source.dto.SourceRequestDto;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.service.SourceService;
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
@WebMvcTest(controllers = SourceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class SourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SourceService sourceService;

    private final Long USER_ID = 1L;

    @Test
    @DisplayName("[GET /api/sources/my] 내 소스 목록 조회 성공 시 200 OK와 소스 목록을 반환한다")
    void getMySources_success() throws Exception {
        // given
        List<SourceResponseDto> sourceList = List.of(
                SourceResponseDto.builder()
                        .sourceId(10L)
                        .userSourceId(5L)
                        .userDefinedName("네이버 D2")
                        .url("https://d2.naver.com/d2.atom")
                        .receiveFeed(true)
                        .build(),
                SourceResponseDto.builder()
                        .sourceId(11L)
                        .userSourceId(6L)
                        .userDefinedName("우아한형제들")
                        .url("https://techblog.woowahan.com/feed")
                        .receiveFeed(true)
                        .build()
        );

        when(sourceService.getSourcesByUser(any())).thenReturn(sourceList);

        // when & then
        mockMvc.perform(get("/api/sources/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].userDefinedName").value("네이버 D2"))
                .andExpect(jsonPath("$.data[1].userDefinedName").value("우아한형제들"));

        // verify
        verify(sourceService, times(1)).getSourcesByUser(any());
    }

    @Test
    @DisplayName("[GET /api/sources/my/search] 소스 검색 성공 시 200 OK와 검색 결과를 반환한다")
    void searchMySources_Success() throws Exception {
        // given
        String keyword = "tech";
        List<SourceResponseDto> searchResult = List.of(
                SourceResponseDto.builder()
                        .sourceId(11L)
                        .userSourceId(6L)
                        .userDefinedName("우아한형제들")
                        .url("https://techblog.woowahan.com/feed")
                        .receiveFeed(true)
                        .build()
        );

        when(sourceService.searchMySources(any(), eq(keyword))).thenReturn(searchResult);

        // when & then
        mockMvc.perform(get("/api/sources/my/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userDefinedName").value("우아한형제들"));

        // verify
        verify(sourceService, times(1)).searchMySources(any(), eq(keyword));
    }

    @Test
    @DisplayName("[POST /api/sources] 새 소스 등록 성공 시 200 OK와 생성된 소스 정보를 반환한다")
    void addSource_success() throws Exception {
        // given
        SourceRequestDto requestDto = SourceRequestDto.builder()
                .name("우아한형제들")
                .url("https://techblog.woowahan.com")
                .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        SourceResponseDto responseDto = SourceResponseDto.builder()
                .sourceId(11L)
                .userSourceId(6L)
                .userDefinedName("우아한형제들")
                .url("https://techblog.woowahan.com/feed")
                .receiveFeed(true)
                .build();

        when(sourceService.addSource(any(), any(SourceRequestDto.class))).thenReturn(responseDto);

        //when & then
        mockMvc.perform(post("/api/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(WRITE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.userDefinedName").value("우아한형제들"))
                .andExpect(jsonPath("$.data.url").value("https://techblog.woowahan.com/feed"));

        // verify
        verify(sourceService, times(1)).addSource(any(), any(SourceRequestDto.class));
    }

    @Test
    @DisplayName("[POST /api/sources] 유효하지 않은 요청(이름 누락) 시 400 Bad Request를 반환한다")
    void addSource_fail_validation() throws Exception {
        // given
        SourceRequestDto invalidRequest = SourceRequestDto.builder()
                .name("") // 유효성 검사 실패
                .url("https://techblog.woowahan.com")
                .build();
        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        // when & then
        mockMvc.perform(post("/api/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[DELETE /api/sources/my/{userSourceId}] 소스 철회 성공 시 200 OK와 null 데이터를 반환한다")
    void removeSource_success() throws Exception {
        // given
        Long userSourceId = 5L;
        doNothing().when(sourceService).removeUserSource(any(), any());

        // when & then
        mockMvc.perform(delete("/api/sources/my/{userSourceId}", userSourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(DELETE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // verify
        verify(sourceService, times(1)).removeUserSource(any(), any());
    }

    @Test
    @DisplayName("[DELETE /api/sources/my/{userSourceId}] 존재하지 않는 소스 철회 시 404 Not Found를 반환한다")
    void removeSource_fail_notFound() throws Exception {
        // given
        Long badUserSourceId = 99L;
        doThrow(new EntityNotFoundException("UserSource", badUserSourceId))
                .when(sourceService).removeUserSource(any(), any());

        // when & then
        mockMvc.perform(delete("/api/sources/my/{userSourceId}", badUserSourceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(nullValue()));

        // verify
        verify(sourceService, times(1)).removeUserSource(any(), any());
    }

    @Test
    @DisplayName("[PATCH /api/sources/my/{userSourceId}/receive-feed] 피드 수신 여부 토글 성공 시 200 OK와 변경된 소스 정보를 반환한다")
    void toggleReceiveFeed_success() throws Exception {
        // given
        Long userSourceId = 5L;

        SourceResponseDto responseDto = SourceResponseDto.builder()
                .sourceId(10L)
                .userSourceId(userSourceId)
                .userDefinedName("네이버 D2")
                .url("https://d2.naver.com/d2.atom")
                .receiveFeed(false)
                .build();

        when(sourceService.toggleReceiveFeed(any(), eq(userSourceId))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/sources/my/{userSourceId}/receive-feed", userSourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(UPDATE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.receiveFeed").value(false));

        // verify
        verify(sourceService, times(1)).toggleReceiveFeed(any(), eq(userSourceId));
    }

    @Test
    @DisplayName("[PATCH /api/sources/my/{userSourceId}/receive-feed] 존재하지 않는 소스 토글 시 404 Not Found를 반환한다")
    void toggleReceiveFeed_fail_notFound() throws Exception {
        // given
        Long badUserSourceId = 99L;

        when(sourceService.toggleReceiveFeed(any(), eq(badUserSourceId)))
                .thenThrow(new EntityNotFoundException("UserSource", badUserSourceId));

        // when & then
        mockMvc.perform(patch("/api/sources/my/{userSourceId}/receive-feed", badUserSourceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(nullValue()));

        // verify
        verify(sourceService, times(1)).toggleReceiveFeed(any(), eq(badUserSourceId));
    }
}