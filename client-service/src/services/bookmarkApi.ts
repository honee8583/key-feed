import { apiClient } from './apiClient'

type BookmarkFolderResponse = {
  status: number
  message: string
  data: BookmarkFolderDto[]
}

export type BookmarkFolderDto = {
  folderId: number
  name: string
}

export const bookmarkApi = {
  async listFolders(): Promise<BookmarkFolderDto[]> {
    const response = await apiClient.request<BookmarkFolderResponse>('/bookmarks/folders')
    return response.data ?? []
  },
}
