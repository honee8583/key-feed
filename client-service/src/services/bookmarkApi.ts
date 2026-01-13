import { apiClient } from './apiClient'

type BookmarkFolderResponse = {
  status: number
  message: string
  data: BookmarkFolderDto[]
}

export type BookmarkFolderDto = {
  folderId: number
  name: string
  icon?: string
  color?: string
}

export type ContentDto = {
  contentId: string
  title: string
  summary: string
  sourceName: string
  originalUrl: string
  thumbnailUrl?: string
  publishedAt: string
}

export type BookmarkItemDto = {
  bookmarkId: number
  folderId?: number
  folderName?: string
  contentId: string
  content?: ContentDto
  createdAt: string
}

type BookmarkListResponse = {
  status: number
  message: string
  data: {
    content: BookmarkItemDto[]
    nextCursorId: number | null
    hasNext: boolean
  }
}

type CreateFolderRequest = {
  name: string
  icon?: string
  color?: string
}

type CreateFolderResponse = {
  status: number
  message: string
  data: BookmarkFolderDto
}

export const bookmarkApi = {
  async listFolders(): Promise<BookmarkFolderDto[]> {
    const response = await apiClient.request<BookmarkFolderResponse>('/bookmarks/folders')
    return response.data ?? []
  },

  async createFolder(request: CreateFolderRequest): Promise<BookmarkFolderDto> {
    const response = await apiClient.request<CreateFolderResponse>('/bookmarks/folders', {
      method: 'POST',
      body: request,
    })
    return response.data
  },

  async createBookmark(contentId: string): Promise<number> {
    const response = await apiClient.request<{ status: number; message: string; data: number }>(
      '/bookmarks',
      {
        method: 'POST',
        body: { contentId },
      },
    )
    return response.data
  },

  async deleteBookmark(bookmarkId: number): Promise<void> {
    await apiClient.request(`/bookmarks/${bookmarkId}`, {
      method: 'DELETE',
    })
  },

  async moveBookmark(bookmarkId: number, folderId: number): Promise<void> {
    await apiClient.request(`/bookmarks/${bookmarkId}/folder`, {
      method: 'PATCH',
      body: { folderId },
    })
  },

  async deleteFolder(folderId: number): Promise<void> {
    await apiClient.request(`/bookmarks/folders/${folderId}`, {
      method: 'DELETE',
    })
  },

  async listBookmarks(params: { folderId?: number | null; lastId?: number }): Promise<{
    content: BookmarkItemDto[]
    nextCursorId: number | null
    hasNext: boolean
  }> {
    const queryParams = new URLSearchParams()
    if (params.folderId != null) {
      queryParams.append('folderId', params.folderId.toString())
    }
    if (params.lastId != null) {
      queryParams.append('lastId', params.lastId.toString())
    }

    const url = `/bookmarks${queryParams.toString() ? `?${queryParams.toString()}` : ''}`
    const response = await apiClient.request<BookmarkListResponse>(url)
    return response.data
  },
}
