import { useCallback, useEffect, useRef, useState } from 'react'
import { notificationApi, type NotificationDto } from '../services/notificationApi'
import { formatRelativePublishedAt } from '../utils/dateUtils'
import type { NotificationItem } from '../features/notifications/types'

const LAST_EVENT_STORAGE_KEY = 'notification:lastEventId'

export function useNotifications() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [hasNext, setHasNext] = useState(false)
  const [isFetchingNext, setIsFetchingNext] = useState(false)
  const [hasLoadedHistory, setHasLoadedHistory] = useState(false)
  
  const isFetchingNextRef = useRef(false)
  const nextCursorRef = useRef<string | null>(null)
  const hasNextRef = useRef(false)
  const hasFetchedInitialRef = useRef(false)
  const hasSubscribedRef = useRef(false)
  const hasReceivedLiveNotificationRef = useRef(false)

  // Fetch initial history
  useEffect(() => {
    if (hasFetchedInitialRef.current) {
      return
    }
    hasFetchedInitialRef.current = true
    const fetchHistory = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const response = await notificationApi.list({ size: 20 })
        console.info('[notification] history nextCursorId', response.nextCursorId)
        setNotifications(response.items.map((dto) => mapToNotificationItem(dto, { isLive: false })))
        setHasNext(response.hasNext)
        nextCursorRef.current = response.nextCursorId
        hasNextRef.current = response.hasNext
        if (!hasReceivedLiveNotificationRef.current && response.items.length > 0) {
          const latestHistoryId = response.items[0]?.id ?? response.items[0]?.contentId
          if (latestHistoryId !== undefined && latestHistoryId !== null) {
            localStorage.setItem(LAST_EVENT_STORAGE_KEY, `${latestHistoryId}`)
          }
        }
      } catch (historyError) {
        const message =
          historyError instanceof Error
            ? historyError.message
            : '알림을 불러오는 중 문제가 발생했습니다.'
        setError(message)
        if (historyError instanceof Error && historyError.message.includes('인증')) {
          console.info('[notification] 인증이 만료되어 로그인 페이지로 이동합니다.')
        }
      } finally {
        setIsLoading(false)
        setHasLoadedHistory(true)
      }
    }

    void fetchHistory()
  }, [])

  // Fetch next page
  const fetchNextPage = useCallback(async () => {
    if (isFetchingNextRef.current || !hasNextRef.current || !nextCursorRef.current) {
      return
    }
    const cursor = nextCursorRef.current
    isFetchingNextRef.current = true
    setIsFetchingNext(true)
    try {
      const response = await notificationApi.list({ lastId: cursor, size: 20 })
      console.info('[notification] next page nextCursorId', response.nextCursorId)
      setNotifications((prev) => [
        ...prev,
        ...response.items.map((dto) => mapToNotificationItem(dto, { isLive: false })),
      ])
      setHasNext(response.hasNext)
      nextCursorRef.current = response.nextCursorId
      hasNextRef.current = response.hasNext
    } catch (nextError) {
      console.error('이전 알림을 불러오지 못했습니다.', nextError)
      if (nextError instanceof Error && nextError.message.includes('인증')) {
        console.info('[notification] 인증이 만료되어 로그인 페이지로 이동합니다.')
      }
    } finally {
      setIsFetchingNext(false)
      isFetchingNextRef.current = false
    }
  }, [])

  // SSE Subscription
  useEffect(() => {
    if (!hasLoadedHistory || hasSubscribedRef.current) {
      return
    }
    hasSubscribedRef.current = true
    const eventSource = notificationApi.subscribe({
      onMessage: (data, event) => {
        const eventType = event.type || 'message'
        if (eventType !== 'notification' && eventType !== 'message') {
          return
        }
        const next = mapToNotificationItem(data, { isLive: true })
        setNotifications((prev) => [next, ...prev])
        hasReceivedLiveNotificationRef.current = true
        if (event.lastEventId) {
          localStorage.setItem(LAST_EVENT_STORAGE_KEY, event.lastEventId)
        }
      },
      onError: () => setError('실시간 알림 연결이 원활하지 않습니다.'),
    })

    return () => {
      hasSubscribedRef.current = false
      eventSource.close()
    }
  }, [hasLoadedHistory])

  return {
    notifications,
    isLoading,
    error,
    hasNext,
    isFetchingNext,
    fetchNextPage,
  }
}

function mapToNotificationItem(dto: NotificationDto, options?: { isLive?: boolean }): NotificationItem {
  const createdAt = dto.createdAt ?? new Date().toISOString()
  const fallbackId = dto.id ?? dto.contentId ?? Date.now()
  return {
    id: fallbackId.toString(),
    title: dto.title ?? dto.type ?? '새 알림',
    description: dto.content ?? dto.message ?? dto.originalUrl ?? '새로운 알림이 도착했습니다.',
    time: formatRelativePublishedAt(createdAt),
    tag: dto.keyword ? `#${dto.keyword}` : undefined,
    icon: resolveIcon(dto.type),
    linkUrl: dto.originalUrl,
    isLive: options?.isLive ?? false,
  }
}

function resolveIcon(type?: string) {
  if (!type) return '📰'
  const normalized = type.toLowerCase()
  if (normalized.includes('system')) return '⚙️'
  if (normalized.includes('keyword') || normalized.includes('match')) return '🔔'
  return '📰'
}
