import { EventSourcePolyfill } from 'event-source-polyfill'
import { apiClient, parseJsonWithLargeIntSupport } from './apiClient'
import { getAccessToken } from './authStorage'

export type NotificationResponseDto = {
  title: string
  message: string
}

export type NotificationDto = NotificationResponseDto & {
  id?: number | string | null
  userId?: number
  contentId?: number | string | null
  content?: string
  keyword?: string
  originalUrl?: string
  createdAt?: string
  read?: boolean
  type?: string
}

type NotificationEventPayload = {
  id: number | string
  title: string
  message: string
} & Partial<Omit<NotificationDto, 'id' | 'title' | 'message'>>

type NotificationListEnvelope = {
  status: number
  message: string
  data: {
    content: NotificationDto[]
    nextCursorId?: number | string | null
    hasNext?: boolean
  }
}

type NotificationListParams = {
  lastId?: number | string | null
  size?: number
}

export type NotificationListResult = {
  items: NotificationDto[]
  nextCursorId: string | null
  hasNext: boolean
}

const baseUrl =
  (import.meta.env.VITE_API_BASE && import.meta.env.VITE_API_BASE.trim()) || 'http://localhost:8000/api'

export const notificationApi = {
  async list(params: NotificationListParams = {}): Promise<NotificationListResult> {
    const query = new URLSearchParams()
    const size = params.size ?? 20
    if (size) {
      query.set('size', size.toString())
    }
    if (params.lastId !== undefined && params.lastId !== null && `${params.lastId}`.trim() !== '') {
      query.set('lastId', `${params.lastId}`)
    }
    const search = query.toString()
    const path = `/notifications${search ? `?${search}` : ''}`
    const response = await apiClient.request<NotificationListEnvelope>(path)
    const { content, nextCursorId, hasNext } = response.data
    const resolvedNextCursor = nextCursorId != null ? `${nextCursorId}` : null
    return {
      items: content ?? [],
      nextCursorId: resolvedNextCursor,
      hasNext: typeof hasNext === 'boolean' ? hasNext : Boolean(resolvedNextCursor),
    }
  },

  subscribe({
    onMessage,
    onError,
  }: {
    onMessage: (data: NotificationDto, event: MessageEvent<string>) => void
    onError?: (event: Event) => void
  }): EventSource {
    const storedLastEventId = localStorage.getItem('notification:lastEventId') ?? undefined
    const token = getAccessToken()

    const headers: Record<string, string> = {}
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
    if (storedLastEventId) {
      headers['Last-Event-ID'] = storedLastEventId
    }

    const eventSource = new EventSourcePolyfill(`${baseUrl}/notifications/subscribe`, {
      withCredentials: true,
      headers,
    })

    const handleMessage = (event: MessageEvent<string>) => {
      try {
        const trimmedPayload = event.data.trim()
        if (trimmedPayload === 'connected' || trimmedPayload === '"connected"') {
          // Spring SSE sends a dummy handshake payload; ignore it.
          return
        }
        const parsed = parseJsonWithLargeIntSupport(event.data) as NotificationEventPayload
        const resolvedEventId = resolveEventId(event)
        const parsedId = parsed.id != null ? `${parsed.id}` : null
        const enriched: NotificationDto = {
          ...parsed,
          id: parsed.id ?? parsed.contentId ?? resolvedEventId ?? undefined,
        }
        onMessage(enriched, event)
        const lastEventId = resolvedEventId ?? parsedId
        if (lastEventId) {
          headers['Last-Event-ID'] = lastEventId
          localStorage.setItem('notification:lastEventId', lastEventId)
        }
      } catch (error) {
        console.error('알림 데이터를 파싱하지 못했습니다.', error)
      }
    }

    // Debug log for each subscribe attempt
    console.info('[notification] subscribe opened', {
      hasToken: Boolean(token),
      hasLastEventId: Boolean(storedLastEventId),
      url: `${baseUrl}/notifications/subscribe`,
    })

    eventSource.addEventListener('notification', handleMessage)
    eventSource.onmessage = handleMessage

    if (onError) {
      eventSource.onerror = onError
    }

    return eventSource as unknown as EventSource
  },
}

function resolveEventId(event: MessageEvent<string>) {
  const typedEvent = event as MessageEvent<string> & { id?: string }
  const normalizedLastEventId = typeof typedEvent.lastEventId === 'string' ? typedEvent.lastEventId.trim() : ''
  if (normalizedLastEventId) {
    return normalizedLastEventId
  }
  if (typedEvent.id) {
    const normalizedId = `${typedEvent.id}`.trim()
    if (normalizedId) {
      return normalizedId
    }
  }
  return null
}

export function getNotificationEventId(event: MessageEvent<string>) {
  return resolveEventId(event)
}
