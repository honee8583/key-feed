import { apiClient } from './apiClient'

export type FeedContent = {
  contentId: number
  title: string
  summary: string
  sourceName: string
  originalUrl: string
  thumbnailUrl: string | null
  publishedAt: string
  bookmarked: boolean
}

export type FeedListParams = {
  size?: number
  lastId?: number
}

export type FeedResponse = {
  content: FeedContent[]
  nextCursorId: number | null
  hasNext: boolean
}

type FeedResponseEnvelope = {
  status: number
  message: string
  data: FeedResponse
}

export const feedApi = {
  async list(params?: FeedListParams): Promise<FeedResponse> {
    const searchParams = new URLSearchParams()

    if (typeof params?.size === 'number') {
      searchParams.set('size', String(params.size))
    }

    if (typeof params?.lastId === 'number') {
      searchParams.set('lastId', String(params.lastId))
    }

    const query = searchParams.toString()
    const path = query ? `/feed?${query}` : '/feed'

    const response = await apiClient.request<FeedResponseEnvelope>(path)
    return response.data
  },
}
