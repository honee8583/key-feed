import { useCallback, useEffect, useRef, useState } from 'react'
import './MainPage.css'
import { useAuth } from '../auth'
import { HighlightCard, type HighlightCardProps } from './components/HighlightCard'
import { feedApi, type FeedContent } from '../../services/feedApi'
import type { CreatedSource } from '../../services/sourceApi'
import { AddSourceSheet } from './components/AddSourceSheet'
import bookmarkIcon from '../../assets/home/bookmark_btn.png'
import addSourceIcon from '../../assets/home/source_add_btn.png'

const keywordCategories = ['전체', 'Next.js', 'AI', '클린코드', 'TypeScript', 'Supabase', 'React']
const contentTabs = ['전체', '블로그', '뉴스', '영상', '커뮤니티']

const figureAssets = {
  bookmark: bookmarkIcon,
}

type HighlightArticle = HighlightCardProps & { id: string }
type AddSourceResult = { name: string; url: string; type: string; created: CreatedSource }

const highlightCardActionIcons: Pick<HighlightCardProps, 'bookmarkIcon'> = {
  bookmarkIcon: figureAssets.bookmark,
}

const FEED_PAGE_SIZE = 10
const DEFAULT_THUMBNAIL = 'https://placehold.co/600x400?text=KeyFeed'
const ARTICLE_TYPE_ICON = '📰'
const ARTICLE_TYPE_LABEL = '콘텐츠'
const NEW_CONTENT_WINDOW_MS = 1000 * 60 * 60 * 24

export function MainPage() {
  const { user } = useAuth()
  const welcomeName = user?.name ?? 'KeyFeed 멤버'
  const [articles, setArticles] = useState<HighlightArticle[]>([])
  const [nextCursorId, setNextCursorId] = useState<number | null>(null)
  const [hasNext, setHasNext] = useState(true)
  const [isInitialLoading, setIsInitialLoading] = useState(false)
  const [isFetchingMore, setIsFetchingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const loadMoreTriggerRef = useRef<HTMLDivElement | null>(null)
  const [isAddSourceOpen, setIsAddSourceOpen] = useState(false)

  const loadFeed = useCallback(async (cursor?: number) => {
    setError(null)
    const isPaginationRequest = typeof cursor === 'number'

    if (isPaginationRequest) {
      setIsFetchingMore(true)
    } else {
      setIsInitialLoading(true)
    }

    try {
      const response = await feedApi.list({
        size: FEED_PAGE_SIZE,
        ...(isPaginationRequest ? { lastId: cursor } : undefined),
      })

      setArticles((prev) => {
        const mapped = response.content.map(convertToHighlightArticle)
        return isPaginationRequest ? [...prev, ...mapped] : mapped
      })

      setNextCursorId(response.nextCursorId ?? null)
      setHasNext(Boolean(response.hasNext))
    } catch (fetchError) {
      const message =
        fetchError instanceof Error
          ? fetchError.message
          : '콘텐츠를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.'
      setError(message)
    } finally {
      if (isPaginationRequest) {
        setIsFetchingMore(false)
      } else {
        setIsInitialLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    void loadFeed()
  }, [loadFeed])

  const loadNextPage = useCallback(() => {
    if (error || !hasNext || isFetchingMore || isInitialLoading) {
      return
    }

    if (nextCursorId === null) {
      return
    }

    void loadFeed(nextCursorId)
  }, [error, hasNext, isFetchingMore, isInitialLoading, nextCursorId, loadFeed])

  useEffect(() => {
    if (!articles.length) {
      return undefined
    }

    const target = loadMoreTriggerRef.current
    if (!target) {
      return undefined
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            loadNextPage()
          }
        })
      },
      { rootMargin: '200px 0px' },
    )

    observer.observe(target)

    return () => observer.disconnect()
  }, [articles.length, loadNextPage])

  const handleRetry = useCallback(() => {
    if (!articles.length) {
      void loadFeed()
      return
    }

    if (nextCursorId !== null) {
      void loadFeed(nextCursorId)
      return
    }

    void loadFeed()
  }, [articles.length, loadFeed, nextCursorId])

  const hasArticles = articles.length > 0
  const showEmptyState = !isInitialLoading && !error && !hasArticles

  const handleOpenAddSource = () => setIsAddSourceOpen(true)
  const handleCloseAddSource = () => setIsAddSourceOpen(false)

  const handleAddSourceSubmit = useCallback(
    (result: AddSourceResult) => {
      void result
      void loadFeed()
    },
    [loadFeed],
  )

  return (
    <div className="main-page">
      <div className="discover-page">
        <header className="discover-hero">
          <div className="discover-hero__copy">
            <p className="discover-hero__eyebrow">콘텐츠 피드</p>
            <h1>Discover</h1>
            <p>맞춤 콘텐츠를 탐색하세요</p>
          </div>
        </header>

        <section className="keyword-pills" aria-label="활성 키워드">
          {keywordCategories.map((label, index) => (
            <button key={label} type="button" className={index === 0 ? 'is-selected' : undefined}>
              {label}
            </button>
          ))}
        </section>

        <nav className="feed-tabs" aria-label="콘텐츠 분류">
          {contentTabs.map((tab, index) => (
            <button key={tab} type="button" className={index === 0 ? 'is-active' : undefined}>
              {tab}
            </button>
          ))}
        </nav>

        <section className="highlight-feed" aria-label="하이라이트 콘텐츠">
          {articles.map(({ id, ...card }) => (
            <HighlightCard key={id} {...card} {...highlightCardActionIcons} />
          ))}

          {isInitialLoading ? (
            <div className="feed-status" role="status">
              맞춤 콘텐츠를 불러오는 중이에요...
            </div>
          ) : null}

          {error ? (
            <div className="feed-status is-error" role="alert">
              <p>{error}</p>
              <button type="button" onClick={handleRetry}>
                다시 시도
              </button>
            </div>
          ) : null}

          {showEmptyState ? <div className="feed-status">표시할 콘텐츠가 없어요.</div> : null}

          {hasArticles ? <div ref={loadMoreTriggerRef} className="feed-observer" aria-hidden /> : null}

          {isFetchingMore ? (
            <div className="feed-status" role="status">
              추가 콘텐츠를 불러오는 중이에요...
            </div>
          ) : null}

          {!hasNext && hasArticles ? <div className="feed-status">피드를 모두 확인했어요.</div> : null}
        </section>

        <footer className="discover-footer">
          <p>
            {welcomeName}님을 위한 개인화 피드입니다. <span>콘텐츠 피드</span>에서 바로 업데이트를 확인하세요.
          </p>
        </footer>
      </div>

      <button
        type="button"
        className="add-source-fab"
        aria-label="콘텐츠 소스 추가"
        aria-haspopup="dialog"
        aria-expanded={isAddSourceOpen}
        onClick={handleOpenAddSource}
      >
        <img src={addSourceIcon} alt="" aria-hidden />
      </button>

      <AddSourceSheet isOpen={isAddSourceOpen} onClose={handleCloseAddSource} onSubmit={handleAddSourceSubmit} />
    </div>
  )
}

function convertToHighlightArticle(item: FeedContent): HighlightArticle {
  return {
    id: item.contentId.toString(),
    title: item.title,
    summary: item.summary,
    linkUrl: item.originalUrl,
    source: item.sourceName,
    timeAgo: formatRelativePublishedAt(item.publishedAt),
    tag: buildTagFromSource(item.sourceName),
    typeIcon: ARTICLE_TYPE_ICON,
    typeLabel: ARTICLE_TYPE_LABEL,
    image: item.thumbnailUrl ?? DEFAULT_THUMBNAIL,
    isNew: isRecentPublication(item.publishedAt),
  }
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

function buildTagFromSource(source: string) {
  const trimmed = source?.trim()
  if (!trimmed) {
    return '#KeyFeed'
  }
  return `#${trimmed.replace(/\s+/g, '')}`
}

function isRecentPublication(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return false
  }

  const diff = Date.now() - date.getTime()
  return diff >= 0 && diff <= NEW_CONTENT_WINDOW_MS
}
