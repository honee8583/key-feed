package com.leedahun.identityservice.domain.bookmark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.identityservice.common.message.SuccessMessage;
import com.leedahun.identityservice.common.response.CursorPage;
import com.leedahun.identityservice.domain.auth.config.SecurityConfig;
import com.leedahun.identityservice.domain.auth.util.test.WithAnonymousUser;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkFolderResponseDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkRequestDto;
import com.leedahun.identityservice.domain.bookmark.dto.BookmarkResponseDto;
import com.leedahun.identityservice.domain.bookmark.service.BookmarkService;
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
@WebMvcTest(controllers = BookmarkController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class}
        ))
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("[POST /api/bookmarks/folders] 북마크 폴더 생성 성공 시 201 Created와 ID를 반환한다")
    void createFolder_success() throws Exception {
        // given
        BookmarkFolderRequestDto request = BookmarkFolderRequestDto.builder()
                .name("bookmark")
                .build();
        Long createdFolderId = 1L;

        when(bookmarkService.createFolder(any(), any(BookmarkFolderRequestDto.class)))
                .thenReturn(createdFolderId);

        // when & then
        mockMvc.perform(post("/api/bookmarks/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WRITE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value(createdFolderId));

        verify(bookmarkService).createFolder(any(), any(BookmarkFolderRequestDto.class));
    }

    @Test
    @DisplayName("[DELETE /api/bookmarks/{id}/folder] 북마크를 폴더에서 제거 성공 시 200 OK를 반환한다")
    void removeBookmarkFromFolder_success() throws Exception {
        // given
        Long bookmarkId = 10L;

        // when & then
        mockMvc.perform(delete("/api/bookmarks/{bookmarkId}/folder", bookmarkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_SUCCESS.getMessage()));

        verify(bookmarkService).removeBookmarkFromFolder(any(), eq(bookmarkId));
    }

    @Test
    @DisplayName("[PATCH /api/bookmarks/{id}/folder] 북마크 폴더 이동 성공 시 200 OK를 반환한다")
    void moveBookmark_success() throws Exception {
        // given
        Long bookmarkId = 10L;
        Long targetFolderId = 50L;
        String requestBody = "{\"folderId\": " + targetFolderId + "}";

        // when & then
        mockMvc.perform(patch("/api/bookmarks/{bookmarkId}/folder", bookmarkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_SUCCESS.getMessage()));

        verify(bookmarkService).moveBookmark(any(), eq(bookmarkId), eq(targetFolderId));
    }

    @Test
    @DisplayName("[GET /api/bookmarks/folders] 북마크 폴더 목록 조회 성공 시 200 OK와 리스트를 반환한다")
    void getFolders_success() throws Exception {
        // given
        BookmarkFolderResponseDto folder1 = BookmarkFolderResponseDto.builder().folderId(1L).name("Folder1").build();
        BookmarkFolderResponseDto folder2 = BookmarkFolderResponseDto.builder().folderId(2L).name("Folder2").build();
        List<BookmarkFolderResponseDto> responseList = List.of(folder1, folder2);

        when(bookmarkService.getFolders(any())).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/bookmarks/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Folder1"));

        verify(bookmarkService).getFolders(any());
    }

    @Test
    @DisplayName("[POST /api/bookmarks] 북마크 등록 성공 시 201 Created와 ID를 반환한다")
    void addBookmark_success() throws Exception {
        // given
        BookmarkRequestDto request = new BookmarkRequestDto("100", null);
        Long createdBookmarkId = 55L;

        when(bookmarkService.addBookmark(any(), any(BookmarkRequestDto.class)))
                .thenReturn(createdBookmarkId);

        // when & then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WRITE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value(createdBookmarkId));

        verify(bookmarkService).addBookmark(any(), any(BookmarkRequestDto.class));
    }

    @Test
    @DisplayName("[GET /api/bookmarks] 북마크 목록 조회 성공 시 200 OK와 커서 페이지 결과를 반환한다")
    void getBookmarks_success() throws Exception {
        // given
        Long folderId = 1L;
        Long lastId = 100L;
        int size = 10;

        BookmarkResponseDto bookmark = BookmarkResponseDto.builder()
                .bookmarkId(10L)
                .build();
        CursorPage<BookmarkResponseDto> cursorPage = CursorPage.<BookmarkResponseDto>builder()
                .content(List.of(bookmark))
                .hasNext(false)
                .nextCursorId(null)
                .build();

        when(bookmarkService.getBookmarks(any(), eq(lastId), eq(folderId), eq(size)))
                .thenReturn(cursorPage);

        // when & then
        mockMvc.perform(get("/api/bookmarks")
                        .param("folderId", String.valueOf(folderId))
                        .param("lastId", String.valueOf(lastId))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content[0].bookmarkId").value(10L))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(bookmarkService).getBookmarks(any(), eq(lastId), eq(folderId), eq(size));
    }

    @Test
    @DisplayName("[GET /api/bookmarks] 파라미터가 없으면 기본값으로 조회한다")
    void getBookmarks_defaultParams() throws Exception {
        // given
        CursorPage<BookmarkResponseDto> emptyPage = new CursorPage<>(Collections.emptyList(), null, false);

        when(bookmarkService.getBookmarks(any(), eq(null), eq(null), anyInt()))
                .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(bookmarkService).getBookmarks(any(), eq(null), eq(null), eq(20));
    }

    @Test
    @DisplayName("[DELETE /api/bookmarks/{id}] 북마크 해제 성공 시 200 OK를 반환한다")
    void deleteBookmark_success() throws Exception {
        // given
        Long bookmarkId = 99L;

        // when & then
        mockMvc.perform(delete("/api/bookmarks/{bookmarkId}", bookmarkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessage.DELETE_SUCCESS.getMessage()));

        verify(bookmarkService).deleteBookmark(any(), eq(bookmarkId));
    }
}