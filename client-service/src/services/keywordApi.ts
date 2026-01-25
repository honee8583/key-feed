import { apiClient } from './apiClient'

export type Keyword = {
  keywordId: number
  name: string
  isNotificationEnabled: boolean
}

type KeywordResponse = {
  status: number
  message: string
  data: Keyword
}

type KeywordListResponse = {
  status: number
  message: string
  data: Keyword[]
}

type CommonResponse = {
  status: number
  message: string
  data: null
}

export const keywordApi = {
  async getKeywords(): Promise<Keyword[]> {
    const response = await apiClient.request<KeywordListResponse>('/keywords')
    return response.data
  },

  async addKeyword(name: string): Promise<Keyword> {
    const response = await apiClient.request<KeywordResponse>('/keywords', {
      method: 'POST',
      body: { name },
    })
    return response.data
  },

  async deleteKeyword(keywordId: number): Promise<void> {
    await apiClient.request<CommonResponse>(`/keywords/${keywordId}`, {
      method: 'DELETE',
    })
  },
}
