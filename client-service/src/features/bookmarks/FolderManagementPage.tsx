import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import './FolderManagementPage.css'
import { bookmarkApi, type BookmarkFolderDto, type BookmarkItemDto } from '../../services/bookmarkApi'
import { FolderManagementModal } from './FolderManagementModal'

export function FolderManagementPage() {
  const navigate = useNavigate()
  const [folders, setFolders] = useState<BookmarkFolderDto[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)

  // Bookmark viewing state
  const [selectedFolder, setSelectedFolder] = useState<BookmarkFolderDto | null>(null)
  const [bookmarks, setBookmarks] = useState<BookmarkItemDto[]>([])
  const [isLoadingBookmarks, setIsLoadingBookmarks] = useState(false)
  const [bookmarkError, setBookmarkError] = useState<string | null>(null)
  const [hasMore, setHasMore] = useState(false)
  const nextCursorIdRef = useRef<number | null>(null)
  const isLoadingMore = useRef(false)

  const fetchFolders = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await bookmarkApi.listFolders()
      setFolders(response)
    } catch (err) {
      const message = err instanceof Error ? err.message : '폴더 목록을 불러오지 못했습니다.'
      setError(message)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void fetchFolders()
  }, [fetchFolders])

  const handleCreateFolder = async (name: string, icon: string, color: string) => {
    await bookmarkApi.createFolder({ name, icon, color })
    await fetchFolders()
  }

  const handleDeleteFolder = async (folderId: number) => {
    if (!confirm('이 폴더를 삭제하시겠습니까?')) {
      return
    }
    try {
      await bookmarkApi.deleteFolder(folderId)
      await fetchFolders()
    } catch (err) {
      const message = err instanceof Error ? err.message : '폴더 삭제에 실패했습니다.'
      alert(message)
    }
  }

  const fetchBookmarks = useCallback(
    async (folderId: number, reset = false) => {
      if (isLoadingMore.current) return

      isLoadingMore.current = true
      setIsLoadingBookmarks(true)
      setBookmarkError(null)

      try {
        const lastId = reset ? undefined : nextCursorIdRef.current ?? undefined
        const response = await bookmarkApi.listBookmarks({
          folderId,
          lastId,
        })

        setBookmarks((prev) => (reset ? response.content : [...prev, ...response.content]))
        nextCursorIdRef.current = response.nextCursorId
        setHasMore(response.hasNext)
      } catch (err) {
        const message =
          err instanceof Error ? err.message : '북마크를 불러오지 못했습니다.'
        setBookmarkError(message)
      } finally {
        setIsLoadingBookmarks(false)
        isLoadingMore.current = false
      }
    },
    []
  )

  const handleFolderSelect = (folder: BookmarkFolderDto) => {
    setSelectedFolder(folder)
    setBookmarks([])
    nextCursorIdRef.current = null
    void fetchBookmarks(folder.folderId, true)
  }

  const handleBackToFolders = () => {
    setSelectedFolder(null)
    setBookmarks([])
    setBookmarkError(null)
  }

  const handleBack = () => {
    navigate('/bookmarks')
  }

  const loadMore = useCallback(() => {
    if (hasMore && !isLoadingBookmarks && selectedFolder) {
      void fetchBookmarks(selectedFolder.folderId, false)
    }
  }, [hasMore, isLoadingBookmarks, selectedFolder, fetchBookmarks])

  return (
    <>
      <div className="folder-page">
        <header className="folder-page__header">
          <button type="button" className="folder-page__back" onClick={selectedFolder ? handleBackToFolders : handleBack} aria-label="뒤로가기">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M12.5 15L7.5 10L12.5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
          <div className="folder-page__header-text">
            <h1>{selectedFolder ? selectedFolder.name : '폴더 관리'}</h1>
            <p>{selectedFolder ? `${bookmarks.length}개의 북마크` : `${folders.length}개 폴더`}</p>
          </div>
          {!selectedFolder && (
            <button
              type="button"
              className="folder-page__add-button"
              onClick={() => setIsCreateModalOpen(true)}
            >
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 3.33333V12.6667M3.33333 8H12.6667" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
              </svg>
              추가
            </button>
          )}
        </header>

          <div className="folder-page__content">
            {!selectedFolder ? (
              <>
                {error ? (
                  <div className="folder-page__error">{error}</div>
                ) : isLoading ? (
                  <div className="folder-page__loading">폴더를 불러오는 중...</div>
                ) : (
                  <div className="folder-list">
                    {folders.map((folder) => (
                      <FolderItem
                        key={folder.folderId}
                        folder={folder}
                        onDelete={handleDeleteFolder}
                        onSelect={handleFolderSelect}
                      />
                    ))}
                    {folders.length === 0 && (
                      <div className="folder-list__empty">등록된 폴더가 없습니다.</div>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div className="folder-bookmarks">
                {bookmarkError ? (
                  <div className="folder-page__error">{bookmarkError}</div>
                ) : null}

                {isLoadingBookmarks && !bookmarks.length ? (
                  <div className="folder-page__loading">북마크를 불러오는 중...</div>
                ) : null}

                <div className="bookmark-list">
                  {bookmarks.map((bookmark) => (
                    <BookmarkCard key={bookmark.bookmarkId} item={bookmark} />
                  ))}
                </div>

                {!isLoadingBookmarks && !bookmarks.length && !bookmarkError ? (
                  <div className="folder-list__empty">이 폴더에 북마크가 없습니다.</div>
                ) : null}

                {hasMore && !isLoadingBookmarks ? (
                  <div className="bookmark-load-more">
                    <button type="button" onClick={loadMore}>
                      더 보기
                    </button>
                  </div>
                ) : null}

                {isLoadingBookmarks && bookmarks.length > 0 ? (
                  <div className="folder-page__loading">추가 북마크를 불러오는 중...</div>
                ) : null}
              </div>
            )}
          </div>
      </div>

      <FolderManagementModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreateFolder={handleCreateFolder}
      />
    </>
  )
}

type FolderItemProps = {
  folder: BookmarkFolderDto
  onDelete: (folderId: number) => void
  onSelect: (folder: BookmarkFolderDto) => void
}

function FolderItem({ folder, onDelete, onSelect }: FolderItemProps) {
  const [showMenu, setShowMenu] = useState(false)
  const isDefaultFolder = folder.folderId <= 2

  const getIconBackground = (color?: string) => {
    if (!color) return '#dbeafe'
    // 색상을 밝게 변환 (임시로 기본값 사용)
    const colorMap: Record<string, string> = {
      '#2b7fff': '#dbeafe',
      '#ad46ff': '#f3e8ff',
      '#00c950': '#dcfce7',
      '#fb2c36': '#fee2e2',
      '#ff6900': '#fed7aa',
      '#f6339a': '#fce7f3',
      '#f0b100': '#fef9c2',
      '#6a7282': '#f3f4f6',
    }
    return colorMap[color] || '#dbeafe'
  }

  return (
    <div className="folder-item">
      <div
        className="folder-item__clickable"
        onClick={() => onSelect(folder)}
      >
        <div className="folder-item__icon" style={{ backgroundColor: getIconBackground(folder.color) }}>
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <path
              d="M3.5 8.16667C3.5 6.08001 5.19333 4.38667 7.28 4.38667H9.92493C10.5139 4.38667 11.0797 4.62116 11.4934 5.03487L13.727 7.26844C14.1407 7.68215 14.7065 7.91663 15.2954 7.91663H22.1667C24.2533 7.91663 25.9467 9.60997 25.9467 11.6966V19.8333C25.9467 21.92 24.2533 23.6133 22.1667 23.6133H7.28C5.19333 23.6133 3.5 21.92 3.5 19.8333V8.16667Z"
              stroke={folder.color || '#2b7fff'}
              strokeWidth="2"
            />
          </svg>
        </div>
        <div className="folder-item__content">
          <div className="folder-item__name-row">
            <h3>{folder.name}</h3>
            {isDefaultFolder && <span className="folder-item__badge">기본</span>}
          </div>
          <p className="folder-item__count">0개의 콘텐츠</p>
        </div>
      </div>
      <div className="folder-item__actions">
        <button
          type="button"
          className="folder-item__menu-button"
          onClick={(e) => {
            e.stopPropagation()
            setShowMenu(!showMenu)
          }}
          aria-label="메뉴"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="5" r="1.5" fill="currentColor" />
            <circle cx="10" cy="10" r="1.5" fill="currentColor" />
            <circle cx="10" cy="15" r="1.5" fill="currentColor" />
          </svg>
        </button>
        {showMenu && (
          <div className="folder-item__menu">
            <button type="button" onClick={(e) => { e.stopPropagation(); /* TODO: 수정 기능 */ setShowMenu(false) }}>
              수정
            </button>
            {!isDefaultFolder && (
              <button type="button" onClick={(e) => { e.stopPropagation(); onDelete(folder.folderId); setShowMenu(false) }}>
                삭제
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function BookmarkCard({ item }: { item: BookmarkItemDto }) {
  const { content, folderName, createdAt } = item

  if (!content) {
    return null
  }

  const { title, summary, sourceName, originalUrl, thumbnailUrl, publishedAt } = content

  const formattedDate = new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(createdAt))

  const formattedPublishedDate = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(publishedAt))

  const DEFAULT_THUMBNAIL =
    'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=400&h=250&fit=crop'

  return (
    <article className="bookmark-card">
      <div className="bookmark-card__meta">
        <span className="bookmark-card__category">{folderName}</span>
        <span className="bookmark-card__date">{formattedDate}</span>
      </div>

      {thumbnailUrl ? (
        <div className="bookmark-card__thumbnail">
          <img src={thumbnailUrl} alt="" loading="lazy" />
        </div>
      ) : (
        <div className="bookmark-card__thumbnail">
          <img src={DEFAULT_THUMBNAIL} alt="" loading="lazy" />
        </div>
      )}

      <h2>{title}</h2>
      <p className="bookmark-card__summary">{summary}</p>

      <div className="bookmark-card__footer">
        <div>
          <p className="bookmark-card__source">{sourceName}</p>
          <p className="bookmark-card__reading">{formattedPublishedDate}</p>
        </div>
        <div className="bookmark-card__cta">
          <button type="button" aria-label="노트 추가">
            노트
          </button>
          <a href={originalUrl} target="_blank" rel="noreferrer">
            원문 보기
          </a>
        </div>
      </div>
    </article>
  )
}
