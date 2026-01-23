import { apiClient } from './apiClient'

export type CreateSourceRequest = {
  name: string
  url: string
}

export type CreatedSource = {
  sourceId: number
  userSourceId: number
  userDefinedName: string
  url: string
  lastCrawledAt?: string
  receiveFeed: boolean
}

type CreateSourceResponse = {
  status: number
  message: string
  data: CreatedSource
}

type SourceListResponse = {
  status: number
  message: string
  data: CreatedSource[]
}

export const sourceApi = {
  async create(payload: CreateSourceRequest) {
    const response = await apiClient.request<CreateSourceResponse>('/sources', {
      method: 'POST',
      body: payload,
    })

    return response.data
  },
  async listMy() {
    const response = await apiClient.request<SourceListResponse>('/sources/my', {
      method: 'GET',
    })

    return response.data
  },
  async searchMy(keyword: string) {
    const response = await apiClient.request<SourceListResponse>(
      `/sources/my/search?keyword=${encodeURIComponent(keyword)}`,
      {
        method: 'GET',
      }
    )

    return response.data
  },
  async delete(userSourceId: number) {
    await apiClient.request(`/sources/my/${userSourceId}`, {
      method: 'DELETE',
    })
  },
  async toggleReceiveFeed(userSourceId: number) {
    const response = await apiClient.request<CreateSourceResponse>(
      `/sources/my/${userSourceId}/receive-feed`,
      {
        method: 'PATCH',
      }
    )
    return response.data
  },
}
