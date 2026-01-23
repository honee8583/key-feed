import { useCallback, useEffect, useState } from 'react'
import { feedApi, type FeedContent } from '../services/feedApi'
import { bookmarkApi } from '../services/bookmarkApi'
import { ApiError } from '../services/apiClient'
import { FEED_PAGE_SIZE, NEW_CONTENT_WINDOW_MS } from '../constants/config'
import { formatRelativePublishedAt } from '../utils/dateUtils'
import type { HighlightCardProps } from '../features/home/components/HighlightCard'

const DEFAULT_THUMBNAIL = 'https://placehold.co/600x400?text=KeyFeed'
const ARTICLE_TYPE_ICON = '📰'
const ARTICLE_TYPE_LABEL = '콘텐츠'

export type HighlightArticle = HighlightCardProps & { id: string; bookmarkId: number | null }

export function useFeed() {
  const [articles, setArticles] = useState<HighlightArticle[]>([])
  const [nextCursorId, setNextCursorId] = useState<number | null>(null)
  const [hasNext, setHasNext] = useState(true)
  const [isInitialLoading, setIsInitialLoading] = useState(false)
  const [isFetchingMore, setIsFetchingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

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

  const handleBookmarkClick = useCallback(
    async (articleId: string, currentBookmarked: boolean, bookmarkId: number | null) => {
      // Optimistic update
      setArticles((prev) =>
        prev.map((article) => {
          if (article.id !== articleId) return article
          const nextBookmarkId = currentBookmarked ? null : -1
          return {
            ...article,
            bookmarkId: nextBookmarkId,
            isBookmarked: !currentBookmarked,
          }
        }),
      )

      try {
        if (!currentBookmarked) {
          // Add bookmark
          const newBookmarkId = await bookmarkApi.createBookmark(articleId)
          // Update with actual ID from server
          setArticles((prev) =>
            prev.map((article) =>
              article.id === articleId ? { ...article, bookmarkId: newBookmarkId } : article,
            ),
          )
        } else {
          // Remove bookmark
          if (bookmarkId) {
            await bookmarkApi.deleteBookmark(bookmarkId)
          }
        }
      } catch (error) {
        // Revert on error
        setArticles((prev) =>
          prev.map((article) =>
            article.id === articleId
              ? {
                  ...article,
                  bookmarkId: bookmarkId, // Restore original ID
                  isBookmarked: currentBookmarked,
                }
              : article,
          ),
        )
      }
    },
    [],
  )

  const refresh = useCallback(() => {
    return loadFeed()
  }, [loadFeed])

  return {
    articles,
    isInitialLoading,
    isFetchingMore,
    error,
    hasNext,
    loadNextPage,
    handleRetry,
    handleBookmarkClick,
    refresh,
  }
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
    isBookmarked: Boolean(item.bookmarkId),
    bookmarkId: item.bookmarkId,
  }
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
