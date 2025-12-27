import { useEffect, useMemo, useState, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import './BookmarksPage.css'
import { bookmarkApi, type BookmarkFolderDto, type BookmarkItemDto } from '../../services/bookmarkApi'

const quickFilters = ['최신순', '읽지 않음', '노트 있음', '원문 링크']

export function BookmarksPage() {
  const navigate = useNavigate()
  const [searchTerm, setSearchTerm] = useState('')
  const [activeQuickFilter, setActiveQuickFilter] = useState(quickFilters[0])
  const [folders, setFolders] = useState<BookmarkFolderDto[]>([])
  const [activeFolderId, setActiveFolderId] = useState<number | null>(null)
  const [isLoadingFolders, setIsLoadingFolders] = useState(false)
  const [folderError, setFolderError] = useState<string | null>(null)

  // Bookmarks state
  const [bookmarks, setBookmarks] = useState<BookmarkItemDto[]>([])
  const [isLoadingBookmarks, setIsLoadingBookmarks] = useState(false)
  const [bookmarkError, setBookmarkError] = useState<string | null>(null)
  const [hasMore, setHasMore] = useState(false)
  const nextCursorIdRef = useRef<number | null>(null)

  useEffect(() => {
    const fetchFolders = async () => {
      setIsLoadingFolders(true)
      setFolderError(null)
      try {
        const response = await bookmarkApi.listFolders()
        setFolders(response)
      } catch (error) {
        const message =
          error instanceof Error ? error.message : '폴더 목록을 불러오지 못했습니다.'
        setFolderError(message)
      } finally {
        setIsLoadingFolders(false)
      }
    }

    void fetchFolders()
  }, [])

  const fetchBookmarks = useCallback(async (folderId: number | null, reset = false) => {
    setIsLoadingBookmarks(true)
    setBookmarkError(null)

    try {
      const lastId = reset ? undefined : nextCursorIdRef.current ?? undefined
      const response = await bookmarkApi.listBookmarks({
        folderId: folderId,
        lastId,
      })

      setBookmarks((prev) => (reset ? response.content : [...prev, ...response.content]))
      nextCursorIdRef.current = response.nextCursorId
      setHasMore(response.hasNext)
    } catch (error) {
      const message =
        error instanceof Error ? error.message : '북마크를 불러오지 못했습니다.'
      setBookmarkError(message)
    } finally {
      setIsLoadingBookmarks(false)
    }
  }, [])

  // Fetch bookmarks when folder changes
  useEffect(() => {
    nextCursorIdRef.current = null
    void fetchBookmarks(activeFolderId, true)
  }, [activeFolderId, fetchBookmarks])

  const filteredBookmarks = useMemo(() => {
    if (!searchTerm) return bookmarks

    return bookmarks.filter((bookmark) => {
      const title = bookmark.content?.title || ''
      const summary = bookmark.content?.summary || ''
      return (
        title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        summary.toLowerCase().includes(searchTerm.toLowerCase())
      )
    })
  }, [searchTerm, bookmarks])

  const handleLoadMore = () => {
    if (hasMore && !isLoadingBookmarks) {
      void fetchBookmarks(activeFolderId, false)
    }
  }

  return (
    <div className="bookmarks-page">
      <header className="bookmarks-header">
        <div className="bookmarks-header__top">
          <h1>저장된 콘텐츠</h1>
        </div>

        <div className="bookmarks-folders-scroll">
          <button
            type="button"
            className={`folder-chip folder-chip--all${activeFolderId === null ? ' is-active' : ''}`}
            onClick={() => setActiveFolderId(null)}
          >
            <FolderIcon />
            <span>전체</span>
            <span className="folder-chip__badge">5</span>
          </button>
          {!isLoadingFolders && folders.length ? (
            folders.map((folder) => {
              const isActive = folder.folderId === activeFolderId
              return (
                <button
                  key={folder.folderId}
                  type="button"
                  className={`folder-chip${isActive ? ' is-active' : ''}`}
                  style={{
                    '--folder-color': folder.color || '#ad46ff',
                  } as React.CSSProperties}
                  onClick={() => setActiveFolderId(folder.folderId)}
                >
                  <FolderIcon />
                  <span>{folder.name}</span>
                  <span className="folder-chip__badge">0</span>
                </button>
              )
            })
          ) : null}
          <button
            type="button"
            className="folder-chip folder-chip--manage"
            onClick={() => navigate('/bookmarks/folders')}
          >
            <PlusIcon />
            <span>관리</span>
          </button>
        </div>
      </header>

      <div className="bookmark-search">
        <div className="bookmark-search__field">
          <input
            type="search"
            placeholder="저장한 콘텐츠 검색"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />
        </div>
        <div className="bookmark-search__filters" aria-label="빠른 정렬">
          {quickFilters.map((filter) => (
            <button
              key={filter}
              type="button"
              className={filter === activeQuickFilter ? 'is-active' : undefined}
              onClick={() => setActiveQuickFilter(filter)}
            >
              {filter}
            </button>
          ))}
        </div>
      </div>

      <section className="bookmark-list" aria-label="저장된 콘텐츠 목록">
        {bookmarkError ? (
          <div className="bookmark-status bookmark-status--error">{bookmarkError}</div>
        ) : null}

        {isLoadingBookmarks && !bookmarks.length ? (
          <div className="bookmark-status">북마크를 불러오는 중...</div>
        ) : null}

        {filteredBookmarks.map((bookmark) => (
          <BookmarkCard key={bookmark.bookmarkId} item={bookmark} />
        ))}

        {!isLoadingBookmarks && !filteredBookmarks.length && !bookmarkError ? (
          <div className="bookmark-status">저장된 북마크가 없어요.</div>
        ) : null}

        {hasMore && !isLoadingBookmarks ? (
          <button className="load-more-button" onClick={handleLoadMore}>
            더 보기
          </button>
        ) : null}

        {isLoadingBookmarks && bookmarks.length > 0 ? (
          <div className="bookmark-status">추가 북마크를 불러오는 중...</div>
        ) : null}
      </section>
    </div>
  )
}

function BookmarkCard({ item }: { item: BookmarkItemDto }) {
  const { content, folderName, createdAt } = item

  if (!content) {
    return null
  }

  const { title, summary, sourceName, originalUrl, publishedAt } = content

  const formattedCreatedDate = new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(createdAt))

  const formattedPublishedDate = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(publishedAt))

  return (
    <article className="bookmark-card">
      <div className="bookmark-card__meta">
        <span className="bookmark-card__category">{folderName || '전체'}</span>
        <span className="bookmark-card__date">{formattedCreatedDate}</span>
      </div>
      <h2>{title}</h2>
      <p className="bookmark-card__summary">{summary}</p>

      <div className="bookmark-card__footer">
        <div>
          <p className="bookmark-card__source">{sourceName}</p>
          <p className="bookmark-card__reading">{formattedPublishedDate}</p>
        </div>
        <div className="bookmark-card__cta">
          {originalUrl ? (
            <a href={originalUrl} target="_blank" rel="noreferrer">
              원문 보기
            </a>
          ) : null}
        </div>
      </div>
    </article>
  )
}

function FolderIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden>
      <path
        d="M2 4.5C2 3.67 2.67 3 3.5 3H5.38C5.72 3 6.04 3.14 6.27 3.38L7.73 4.88C7.96 5.12 8.28 5.26 8.62 5.26H13C13.83 5.26 14.5 5.93 14.5 6.76V11.5C14.5 12.33 13.83 13 13 13H3.5C2.67 13 2 12.33 2 11.5V4.5Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function PlusIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden>
      <path
        d="M8 3V13M3 8H13"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  )
}
