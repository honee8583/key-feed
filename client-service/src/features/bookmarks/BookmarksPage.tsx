import { useEffect, useMemo, useState, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { bookmarkApi, type BookmarkFolderDto, type BookmarkItemDto } from '../../services/bookmarkApi'
import { FolderSelectSheet } from './FolderSelectSheet'
import { BookmarkFolderIcon } from './BookmarkFolderIcon'

const quickFilters = ['최신순', '읽지 않음', '노트 있음', '원문 링크']

export function BookmarksPage() {
  const navigate = useNavigate()
  const [searchTerm, setSearchTerm] = useState('')
  const [activeQuickFilter, setActiveQuickFilter] = useState(quickFilters[0])
  const [folders, setFolders] = useState<BookmarkFolderDto[]>([])
  const [activeFolderId, setActiveFolderId] = useState<number | null>(null)
  const [isLoadingFolders, setIsLoadingFolders] = useState(false)
  const [, setFolderError] = useState<string | null>(null)

  // Bookmarks state
  const [bookmarks, setBookmarks] = useState<BookmarkItemDto[]>([])
  const [isLoadingBookmarks, setIsLoadingBookmarks] = useState(false)
  const [bookmarkError, setBookmarkError] = useState<string | null>(null)
  const [hasMore, setHasMore] = useState(false)
  const nextCursorIdRef = useRef<number | null>(null)

  const [isFolderSheetOpen, setIsFolderSheetOpen] = useState(false)
  const [movingBookmarkId, setMovingBookmarkId] = useState<number | null>(null)

  const fetchFolders = useCallback(async () => {
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
  }, [])

  useEffect(() => {
    void fetchFolders()
  }, [fetchFolders])

  const handleOpenFolderSheet = (bookmarkId: number) => {
    setMovingBookmarkId(bookmarkId)
    setIsFolderSheetOpen(true)
  }

  const handleFolderSelect = async (folderId: number) => {
    if (movingBookmarkId === null) return

    try {
      await bookmarkApi.moveBookmark(movingBookmarkId, folderId)
      
      // Update UI optimistically or refetch
      // If we are in "All", update folder name/id
      // If we are in a specific folder, remove it?
      // For simplicity, let's update local state to reflect change (or remove if moved out)
      
      setBookmarks((prev) => 
        prev.map(b => {
          if (b.bookmarkId !== movingBookmarkId) return b
          
          // Find folder name
          const targetFolder = folders.find(f => f.folderId === folderId)
          const folderName = folderId === 0 ? '미분류' : (targetFolder?.name || '')
          
          return { ...b, folderId, folderName }
        })
      )

      // If viewing a specific folder and moved out, we might want to filter it out
      if (activeFolderId !== null && activeFolderId !== folderId) {
        setBookmarks(prev => prev.filter(b => b.bookmarkId !== movingBookmarkId))
      }
      
      // Also update folders list to reflect count changes if we were tracking counts (we are not yet)
      
    } catch (error) {
      console.error('Failed to move bookmark', error)
      const message = error instanceof Error ? error.message : '폴더 이동에 실패했습니다.'
      setBookmarkError(message)
    } finally {
      setIsFolderSheetOpen(false)
      setMovingBookmarkId(null)
    }
  }



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
    <div className="min-h-screen bg-black text-slate-50 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[440px] mx-auto min-h-screen flex flex-col bg-black border-x border-[#1e2939]/30">
        <header className="bg-black/80 backdrop-blur-md border-b border-[#1e2939] px-5 pt-3 sticky top-0 z-10">
          <div className="flex justify-between items-center h-9 mb-3">
            <h1 className="m-0 text-xl font-bold text-white tracking-[-0.45px]">저장된 콘텐츠</h1>
          </div>

          <div className="flex gap-2 overflow-x-auto pb-2 mb-2 scrollbar-none -mx-5 px-5">
            <button
              type="button"
              className={`${activeFolderId === null ? 'bg-white text-black border-white' : 'bg-[#1e2939] text-[#d1d5dc] border-[#364153]'} inline-flex items-center gap-2 h-[38px] px-[13px] rounded-[10px] border whitespace-nowrap flex-shrink-0 transition-opacity hover:opacity-85`}
              onClick={() => setActiveFolderId(null)}
            >
              <FolderIcon />
              <span>전체</span>
            </button>
            <button
              type="button"
              className={`${activeFolderId === 0 ? 'bg-white text-black border-white' : 'bg-[#1e2939] text-[#d1d5dc] border-[#364153]'} inline-flex items-center gap-2 h-[38px] px-[13px] rounded-[10px] border whitespace-nowrap flex-shrink-0 transition-opacity hover:opacity-85`}
              onClick={() => setActiveFolderId(0)}
            >
              <FolderIcon />
              <span>미분류</span>
            </button>
            {!isLoadingFolders && folders.length ? (
              folders.map((folder) => {
                const isActive = folder.folderId === activeFolderId
                return (
                  <button
                    key={folder.folderId}
                    type="button"
                    className={`${isActive ? 'opacity-100' : 'opacity-95 hover:opacity-85'} inline-flex items-center gap-2 h-[38px] px-[13px] rounded-[10px] border whitespace-nowrap flex-shrink-0 transition-opacity text-[14px]`}
                    style={{
                      // 색상 커스터마이징이 있으면 border/background 색으로 반영
                      borderColor: (folder.color || '#ad46ff') + '33',
                      background: (folder.color || '#ad46ff') + '1A',
                      color: (folder.color || '#ad46ff'),
                    } as React.CSSProperties}
                    onClick={() => setActiveFolderId(folder.folderId)}
                  >
                    <BookmarkFolderIcon icon={folder.icon} width={16} height={16} />
                    <span>{folder.name}</span>
                  </button>
                )
              })
            ) : null}
            <button
              type="button"
              className="inline-flex items-center gap-2 h-[38px] px-[13px] rounded-[10px] border border-[#364153] bg-[#1e2939] text-[#d1d5dc] whitespace-nowrap flex-shrink-0 transition-opacity hover:opacity-85"
              onClick={() => navigate('/bookmarks/folders')}
            >
              <PlusIcon />
              <span>관리</span>
            </button>
          </div>
        </header>

        <div className="flex flex-col gap-3 bg-[rgba(15,15,20,0.8)] border border-white/10 rounded-[24px] p-[18px] mx-5 mt-4 mb-2 text-slate-50">
          <div>
            <input
              className="w-full rounded-[14px] border border-white/10 bg-[rgba(2,6,23,0.6)] px-3.5 py-3 text-[14px] placeholder:text-slate-300/70"
              type="search"
              placeholder="저장한 콘텐츠 검색"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </div>
          <div className="flex flex-wrap gap-2" aria-label="빠른 정렬">
            {quickFilters.map((filter) => {
              const isActive = filter === activeQuickFilter
              return (
                <button
                  key={filter}
                  type="button"
                  className={`${isActive ? 'bg-[rgba(59,130,246,0.2)] text-[#bfdbfe] border-[rgba(59,130,246,0.5)]' : 'bg-transparent text-slate-300/95 border-white/10'} rounded-full border px-[14px] py-[6px] text-[13px] cursor-pointer`}
                  onClick={() => setActiveQuickFilter(filter)}
                >
                  {filter}
                </button>
              )
            })}
          </div>
        </div>

        <section className="flex flex-col gap-[18px] px-5 pb-[140px]" aria-label="저장된 콘텐츠 목록">
          {bookmarkError ? (
            <div className="p-[22px] rounded-[20px] border border-[rgba(239,68,68,0.5)] bg-[rgba(239,68,68,0.05)] text-[#fca5a5] text-center text-[14px]">
              {bookmarkError}
            </div>
          ) : null}

          {isLoadingBookmarks && !bookmarks.length ? (
            <div className="p-[22px] rounded-[20px] border border-white/20 bg-[rgba(255,255,255,0.03)] text-slate-50/80 text-center text-[14px]">
              북마크를 불러오는 중...
            </div>
          ) : null}

          {filteredBookmarks.map((bookmark) => (
            <BookmarkCard 
              key={bookmark.bookmarkId} 
              item={bookmark} 
              onMoveClick={() => handleOpenFolderSheet(bookmark.bookmarkId)}
            />
          ))}

          {!isLoadingBookmarks && !filteredBookmarks.length && !bookmarkError ? (
            <div className="p-[22px] rounded-[20px] border border-white/20 bg-[rgba(255,255,255,0.03)] text-slate-50/80 text-center text-[14px]">
              저장된 북마크가 없어요.
            </div>
          ) : null}

          {hasMore && !isLoadingBookmarks ? (
            <button className="w-full p-4 rounded-[16px] border border-white/20 bg-[rgba(15,15,20,0.8)] text-slate-50 text-[14px] font-medium cursor-pointer transition hover:bg-white/10 hover:border-white/30" onClick={handleLoadMore}>
              더 보기
            </button>
          ) : null}

          {isLoadingBookmarks && bookmarks.length > 0 ? (
            <div className="p-[22px] rounded-[20px] border border-white/20 bg-[rgba(255,255,255,0.03)] text-slate-50/80 text-center text-[14px]">
              추가 북마크를 불러오는 중...
            </div>
          ) : null}
        </section>

        <FolderSelectSheet
          isOpen={isFolderSheetOpen}
          onClose={() => setIsFolderSheetOpen(false)}
          onSelectFolder={handleFolderSelect}
          folders={folders}
          currentFolderId={bookmarks.find(b => b.bookmarkId === movingBookmarkId)?.folderId}
          onFolderCreated={fetchFolders}
        />
      </div>
    </div>
  )
}

function BookmarkCard({ item, onMoveClick }: { item: BookmarkItemDto; onMoveClick: () => void }) {
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

  return (
    <article className="bg-[#111] border border-white/5 rounded-[20px] overflow-hidden">
      <div className="p-5 flex flex-col gap-3">
        <div className="flex justify-between items-center text-[12px]">
          <button 
            type="button"
            className="font-semibold text-[#a5b4fc] hover:text-[#c7d2fe] transition-colors flex items-center gap-1"
            onClick={onMoveClick}
          >
            <span>{folderName || '미분류'}</span>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M9.5 4.5L6 8L2.5 4.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
          <span className="text-slate-500">{formattedDate} saved</span>
        </div>

        <div className="flex gap-4">
          <div className="flex-1 min-w-0 flex flex-col gap-2">
            <h2 className="text-[16px] font-bold text-slate-50 leading-snug line-clamp-2">
              {title}
            </h2>
            <p className="text-[13px] text-slate-400 leading-relaxed line-clamp-2">{summary}</p>
          </div>
          {thumbnailUrl && (
            <div className="w-20 h-20 rounded-xl bg-slate-800 overflow-hidden shrink-0">
               <img src={thumbnailUrl} alt="" className="w-full h-full object-cover" />
            </div>
          )}
        </div>

        <div className="flex items-center justify-between pt-2 mt-1 border-t border-white/5">
          <div className="flex flex-col">
             <span className="text-[12px] font-semibold text-slate-300">{sourceName}</span>
             <span className="text-[11px] text-slate-500">{formattedPublishedDate}</span>
          </div>
          <div className="flex gap-2">
            <button
               type="button"
               className="w-8 h-8 flex items-center justify-center rounded-lg border border-white/10 bg-white/5 text-slate-300 hover:bg-white/10 transition-colors"
               onClick={onMoveClick}
               aria-label="폴더 이동"
            >
              <FolderIcon />
            </button>
            {originalUrl && (
              <a
                href={originalUrl}
                target="_blank"
                rel="noreferrer"
                className="w-8 h-8 flex items-center justify-center rounded-lg border border-white/10 bg-white/5 text-slate-300 no-underline hover:bg-white/10 transition-colors"
                aria-label="원문 보기"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="2" y1="12" x2="22" y2="12"></line>
                  <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
                </svg>
              </a>
            )}
          </div>
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
