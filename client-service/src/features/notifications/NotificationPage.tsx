import { useCallback, useEffect, useRef, useState } from 'react'
import './NotificationPage.css'
import { notificationApi, type NotificationDto } from '../../services/notificationApi'

const LAST_EVENT_STORAGE_KEY = 'notification:lastEventId'

type NotificationItem = {
  id: string
  title: string
  description: string
  time: string
  tag?: string
  icon: string
  linkUrl?: string
  isLive?: boolean
}

export function NotificationPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [nextCursorId, setNextCursorId] = useState<string | null>(null)
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
    <div className="notifications-page">
      <div className="notifications-page__content">
        <header className="notifications-header">
          <div className="notifications-header__top">
            <div className="notifications-title">
              <h1>
                알림
              </h1>
            </div>
            <button type="button" className="notifications-icon-button" aria-label="알림 설정 및 옵션">
              <span aria-hidden>⋯</span>
            </button>
          </div>
        </header>

        <section className="notifications-list" aria-label="알림 목록">
          {isLoading && !notifications.length ? (
            <div className="notifications-status" role="status">
              알림을 불러오는 중입니다...
            </div>
          ) : null}

          {error && !notifications.length ? (
            <div className="notifications-status is-error" role="alert">
              {error}
            </div>
          ) : null}

          {!isLoading && !error && !notifications.length ? (
            <div className="notifications-status">표시할 알림이 없어요.</div>
          ) : null}

          {notifications.map((notification) => (
            <NotificationCard key={notification.id} item={notification} />
          ))}

          <div ref={loadMoreRef} className="notifications-load-more-trigger" aria-hidden />

          {isFetchingNext ? (
            <div className="notifications-status is-inline" role="status">
              이전 알림을 불러오는 중입니다...
            </div>
          ) : null}
        </section>
      </div>
    </div>
  )
}

function NotificationCard({ item }: { item: NotificationItem }) {
  const { title, description, time, tag, icon, linkUrl, isLive } = item
  return (
    <article className="notification-card">
      <div className="notification-icon" aria-hidden>
        <span>{icon}</span>
      </div>
      <div className="notification-content">
        <div className="notification-title-row">
          <p className="notification-title">{title}</p>
        </div>
        <p className="notification-description">{description}</p>
        <div className="notification-meta">
          <span className="notification-time" aria-label={`${time}에 받은 알림`}>
            <span className={`notification-time__dot${isLive ? ' is-live' : ''}`} aria-hidden />
            {time}
          </span>
          {tag ? <span className="notification-tag">{tag}</span> : null}
          {linkUrl ? (
            <a className="notification-link" href={linkUrl} target="_blank" rel="noreferrer">
              원문 보기
            </a>
          ) : null}
        </div>
      </div>
      <button type="button" className="notification-menu" aria-label="알림 옵션">
        <span aria-hidden>⋯</span>
      </button>
    </article>
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
