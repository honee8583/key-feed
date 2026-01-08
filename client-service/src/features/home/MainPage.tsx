import { useCallback, useEffect, useRef, useState } from 'react'
import { useAuth } from '../auth'
import { HighlightCard, type HighlightCardProps } from './components/HighlightCard'
import { feedApi, type FeedContent } from '../../services/feedApi'
import { ApiError } from '../../services/apiClient'
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
      if (fetchError instanceof ApiError && fetchError.status === 503) {
        setError('서버 점검 중이거나 접속이 지연되고 있어요. 잠시 후 다시 시도해주세요.')
      } else {
        const message =
          fetchError instanceof Error
            ? fetchError.message
            : '콘텐츠를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.'
        setError(message)
      }
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
    <div className="min-h-screen py-8 pb-[140px] bg-[radial-gradient(circle_at_15%_15%,rgba(255,255,255,0.08),transparent_55%),#050505] font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif] text-slate-50">
      <div className="w-full max-w-[440px] mx-auto flex flex-col gap-6">
        <header className="bg-gradient-to-br from-[#020202] to-[#161616] text-white rounded-[32px] p-8 px-7 flex justify-between items-start relative overflow-hidden border border-white/8 max-[480px]:p-7 max-[480px]:px-6">
          <div className="absolute -left-10 -top-[60px] w-[180px] h-[180px] rounded-full bg-white/5 blur-[30px]" />
          <div className="relative z-10">
            <p className="m-0 mb-2 text-sm opacity-70">콘텐츠 피드</p>
            <h1 className="m-0 text-[32px] tracking-[-0.02em]">Discover</h1>
            <p className="mt-3 mb-0 text-slate-100/65">맞춤 콘텐츠를 탐색하세요</p>
          </div>
        </header>

        <section
          className="flex gap-3 overflow-x-auto py-1 pr-1 scroll-snap-x-proximity [&::-webkit-scrollbar]:h-1 [&::-webkit-scrollbar-thumb]:bg-white/25 [&::-webkit-scrollbar-thumb]:rounded-full"
          aria-label="활성 키워드"
        >
          {keywordCategories.map((label, index) => (
            <button
              key={label}
              type="button"
              className={`flex-none py-2.5 px-5 rounded-full border scroll-snap-start transition-colors ${
                index === 0
                  ? 'bg-slate-50 border-slate-50 text-[#050505]'
                  : 'border-white/20 bg-transparent text-slate-50 font-semibold hover:bg-white/5'
              }`}
            >
              {label}
            </button>
          ))}
        </section>

        <nav className="relative grid grid-cols-[repeat(auto-fit,minmax(0,1fr))] border-b border-white/8" aria-label="콘텐츠 분류">
          {contentTabs.map((tab, index) => (
            <button
              key={tab}
              type="button"
              className={`border-none bg-transparent pb-3 font-semibold relative text-center transition-colors ${
                index === 0
                  ? 'text-slate-50 after:content-[""] after:absolute after:left-0 after:right-0 after:bottom-0 after:h-[3px] after:rounded-full after:bg-slate-50'
                  : 'text-white/45 hover:text-white/65'
              }`}
            >
              {tab}
            </button>
          ))}
        </nav>

        <section className="flex flex-col gap-8" aria-label="하이라이트 콘텐츠">
          {articles.map(({ id, ...card }) => (
            <HighlightCard key={id} {...card} {...highlightCardActionIcons} />
          ))}

          {isInitialLoading ? (
            <div className="mt-3 p-4 rounded-[20px] border border-white/8 bg-white/3 text-center text-slate-50/80 text-sm leading-relaxed" role="status">
              맞춤 콘텐츠를 불러오는 중이에요...
            </div>
          ) : null}

          {error ? (
            <div className="mt-3 p-4 rounded-[20px] border border-[rgba(255,100,110,0.4)] bg-white/3 text-center text-[#ff8da1] text-sm leading-relaxed" role="alert">
              <p className="m-0 mb-2">{error}</p>
              <button
                type="button"
                className="mt-1 py-2 px-[18px] rounded-full border border-slate-50/60 bg-transparent text-slate-50 font-semibold cursor-pointer hover:bg-white/5"
                onClick={handleRetry}
              >
                다시 시도
              </button>
            </div>
          ) : null}

          {showEmptyState ? (
            <div className="mt-3 p-4 rounded-[20px] border border-white/8 bg-white/3 text-center text-slate-50/80 text-sm leading-relaxed">
              표시할 콘텐츠가 없어요.
            </div>
          ) : null}

          {hasArticles ? <div ref={loadMoreTriggerRef} className="w-full h-px" aria-hidden /> : null}

          {isFetchingMore ? (
            <div className="mt-3 p-4 rounded-[20px] border border-white/8 bg-white/3 text-center text-slate-50/80 text-sm leading-relaxed" role="status">
              추가 콘텐츠를 불러오는 중이에요...
            </div>
          ) : null}

          {!hasNext && hasArticles ? (
            <div className="mt-3 p-4 rounded-[20px] border border-white/8 bg-white/3 text-center text-slate-50/80 text-sm leading-relaxed">
              피드를 모두 확인했어요.
            </div>
          ) : null}
        </section>

        <footer className="text-sm text-slate-50/60 leading-relaxed">
          <p>
            {welcomeName}님을 위한 개인화 피드입니다. <span className="text-slate-50 font-semibold">콘텐츠 피드</span>
            에서 바로 업데이트를 확인하세요.
          </p>
        </footer>
      </div>

      <button
        type="button"
        className="fixed right-[max(16px,calc((100vw-440px)/2))] bottom-[calc(24px+120px)] w-14 h-14 border-none rounded-full bg-gradient-to-br from-[#155dfc] to-[#4f39f6] shadow-[0_20px_25px_rgba(0,0,0,0.15),0_8px_10px_rgba(0,0,0,0.12)] inline-flex items-center justify-center cursor-pointer z-30 min-[720px]:bottom-[calc(40px+120px)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-slate-50 focus-visible:outline-offset-[3px]"
        aria-label="콘텐츠 소스 추가"
        aria-haspopup="dialog"
        aria-expanded={isAddSourceOpen}
        onClick={handleOpenAddSource}
      >
        <img src={addSourceIcon} alt="" className="w-6 h-6" aria-hidden />
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
