import { useCallback, useEffect, useRef, useState } from 'react'
import { notificationApi, type NotificationDto } from '../../services/notificationApi'
import { NotificationCard, NotificationStatus } from './components'
import type { NotificationItem } from './types'

const LAST_EVENT_STORAGE_KEY = 'notification:lastEventId'

export function NotificationPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [, setNextCursorId] = useState<string | null>(null)
  const [hasNext, setHasNext] = useState(false)
  const [isFetchingNext, setIsFetchingNext] = useState(false)
  const [hasLoadedHistory, setHasLoadedHistory] = useState(false)
  const loadMoreRef = useRef<HTMLDivElement | null>(null)
  const isFetchingNextRef = useRef(false)
  const nextCursorRef = useRef<string | null>(null)
  const hasNextRef = useRef(false)
  const hasFetchedInitialRef = useRef(false)
  const hasSubscribedRef = useRef(false)
  const hasReceivedLiveNotificationRef = useRef(false)

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
        setNextCursorId(response.nextCursorId)
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
      } finally {
        setIsLoading(false)
        setHasLoadedHistory(true)
      }
    }

    void fetchHistory()
  }, [])

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
      setNextCursorId(response.nextCursorId)
      setHasNext(response.hasNext)
      nextCursorRef.current = response.nextCursorId
      hasNextRef.current = response.hasNext
    } catch (nextError) {
      console.error('이전 알림을 불러오지 못했습니다.', nextError)
    } finally {
      setIsFetchingNext(false)
      isFetchingNextRef.current = false
    }
  }, [])

  useEffect(() => {
    if (!hasLoadedHistory || hasSubscribedRef.current) {
      return
    }
    hasSubscribedRef.current = true
    const eventSource = notificationApi.subscribe({
      onMessage: (data, event) => {
        const eventType = event.type || 'message'
        // Spring Boot SSE emits events named "notification"; allow default as a fallback.
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

  useEffect(() => {
    if (!hasNext) {
      return
    }
    const target = loadMoreRef.current
    if (!target) {
      return
    }
    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry && entry.isIntersecting) {
          void fetchNextPage()
        }
      },
      { root: null, rootMargin: '0px 0px 200px 0px', threshold: 0 }
    )
    observer.observe(target)
    return () => observer.disconnect()
  }, [fetchNextPage, hasNext])

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_15%_20%,rgba(255,255,255,0.08),transparent_45%),#050505] py-7 pb-40 flex justify-center text-slate-50 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[420px] flex flex-col gap-[18px]">
        <header className="bg-gradient-to-br from-[rgba(15,15,20,0.95)] to-[rgba(10,10,16,0.85)] border border-white/8 shadow-[0_18px_30px_rgba(2,6,23,0.4)] rounded-[28px] p-6 flex flex-col gap-4">
          <div className="flex items-center justify-between gap-3">
            <div className="inline-flex items-center gap-2.5">
              <h1 className="m-0 text-[26px] tracking-[-0.02em] inline-flex items-center gap-2 text-slate-50">
                알림
              </h1>
            </div>
            <button
              type="button"
              className="w-10 h-10 rounded-2xl border border-white/18 bg-white/8 shadow-[0_10px_24px_rgba(2,6,23,0.5)] inline-flex items-center justify-center cursor-pointer text-slate-50/70 text-lg hover:bg-white/12"
              aria-label="알림 설정 및 옵션"
            >
              <span aria-hidden>⋯</span>
            </button>
          </div>
        </header>

        <section className="flex flex-col gap-3.5" aria-label="알림 목록">
          {isLoading && !notifications.length ? (
            <NotificationStatus role="status">알림을 불러오는 중입니다...</NotificationStatus>
          ) : null}

          {error && !notifications.length ? (
            <NotificationStatus variant="error" role="alert">
              {error}
            </NotificationStatus>
          ) : null}

          {!isLoading && !error && !notifications.length ? (
            <NotificationStatus>표시할 알림이 없어요.</NotificationStatus>
          ) : null}

          {notifications.map((notification) => (
            <NotificationCard key={notification.id} item={notification} />
          ))}

          <div ref={loadMoreRef} className="w-full h-px" aria-hidden />

          {isFetchingNext ? (
            <NotificationStatus variant="inline" role="status">
              이전 알림을 불러오는 중입니다...
            </NotificationStatus>
          ) : null}
        </section>
      </div>
    </div>
  )
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

function formatRelativePublishedAt(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '방금 전'
  }

  const diff = Date.now() - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < 0) {
    return formatAbsoluteDate(date)
  }

  if (diff < minute) {
    return '방금 전'
  }

  if (diff < hour) {
    const minutes = Math.floor(diff / minute)
    return `${minutes}분 전`
  }

  if (diff < day) {
    const hours = Math.floor(diff / hour)
    return `${hours}시간 전`
  }

  if (diff < day * 7) {
    const days = Math.floor(diff / day)
    return `${days}일 전`
  }

  return formatAbsoluteDate(date)
}

function formatAbsoluteDate(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}.${month}.${day}`
}
