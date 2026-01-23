package com.leedahun.identityservice.domain.source.controller;

import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.source.dto.SourceResponseDto;
import com.leedahun.identityservice.domain.source.service.SourceService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SourceInternalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class SourceInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SourceService sourceService;

    @Test
    @DisplayName("[GET /internal/sources/user/{userId}] 유저의 피드 수신 활성화된 소스 목록 조회 성공 시 200 OK와 리스트를 반환한다")
    void getUserSources_success() throws Exception {
        // given
        Long userId = 1L;
        SourceResponseDto source1 = SourceResponseDto.builder().receiveFeed(true).build();
        SourceResponseDto source2 = SourceResponseDto.builder().receiveFeed(true).build();
        List<SourceResponseDto> responseList = List.of(source1, source2);

        when(sourceService.getActiveSourcesByUser(userId)).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/internal/sources/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        verify(sourceService).getActiveSourcesByUser(userId);
    }
}