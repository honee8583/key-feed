import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { bookmarkApi, type BookmarkFolderDto, type BookmarkItemDto } from '../../services/bookmarkApi'
import { FolderManagementModal } from './FolderManagementModal'
import { BookmarkFolderIcon } from './BookmarkFolderIcon'

export function FolderManagementPage() {
  const navigate = useNavigate()
  const [folders, setFolders] = useState<BookmarkFolderDto[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingFolder, setEditingFolder] = useState<BookmarkFolderDto | null>(null)

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

  const handleCreateOrUpdateFolder = async (name: string, icon: string, color: string) => {
    if (editingFolder) {
      await bookmarkApi.updateFolder(editingFolder.folderId, { name, icon, color })
    } else {
      await bookmarkApi.createFolder({ name, icon, color })
    }
    await fetchFolders()
    setEditingFolder(null)
  }

  const openCreateModal = () => {
    setEditingFolder(null)
    setIsModalOpen(true)
  }

  const openEditModal = (folder: BookmarkFolderDto) => {
    setEditingFolder(folder)
    setIsModalOpen(true)
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
      <div className="min-h-screen bg-black text-slate-50 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif] pb-[100px]">
        <div className="w-full max-w-[440px] mx-auto min-h-screen flex flex-col bg-black border-x border-[#1e2939]/30">
          <header className="sticky top-0 z-10 bg-black/80 backdrop-blur-md border-b border-white/10 px-4 py-3 flex items-center gap-3">
          <button
            type="button"
            className="w-10 h-10 flex items-center justify-center -ml-2 text-slate-300 hover:text-white transition-colors"
            onClick={selectedFolder ? handleBackToFolders : handleBack}
            aria-label="뒤로가기"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M15 19L8 12L15 5"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="text-[17px] font-bold leading-tight truncate">
              {selectedFolder ? selectedFolder.name : '폴더 관리'}
            </h1>
            {!selectedFolder && (
              <p className="text-[13px] text-slate-500 mt-0.5">
                {`${folders.length}개 폴더`}
              </p>
            )}
          </div>
          {!selectedFolder && (
            <button
              type="button"
              className="px-3 py-1.5 rounded-lg bg-white text-black text-[13px] font-semibold flex items-center gap-1.5 hover:bg-slate-200 transition-colors"
              onClick={openCreateModal}
            >
              <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
                <path
                  d="M8 3.33333V12.6667M3.33333 8H12.6667"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                />
              </svg>
              추가
            </button>
          )}
        </header>

        <div className="p-4">
          {!selectedFolder ? (
            <>
              {error ? (
                <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm text-center">
                  {error}
                </div>
              ) : isLoading ? (
                <div className="p-10 text-center text-slate-500 text-sm">폴더를 불러오는 중...</div>
              ) : (
                <div className="flex flex-col gap-3">
                  {folders.map((folder) => (
                    <FolderItem
                      key={folder.folderId}
                      folder={folder}
                      onDelete={handleDeleteFolder}
                      onSelect={handleFolderSelect}
                      onEdit={openEditModal}
                    />
                  ))}
                  {folders.length === 0 && (
                    <div className="py-20 text-center text-slate-500 text-sm">
                      등록된 폴더가 없습니다.
                    </div>
                  )}
                </div>
              )}
            </>
          ) : (
            <div className="flex flex-col gap-4">
              {bookmarkError ? (
                <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm text-center">
                  {bookmarkError}
                </div>
              ) : null}

              {isLoadingBookmarks && !bookmarks.length ? (
                <div className="p-10 text-center text-slate-500 text-sm">북마크를 불러오는 중...</div>
              ) : null}

              <div className="flex flex-col gap-4">
                {bookmarks.map((bookmark) => (
                  <BookmarkCard key={bookmark.bookmarkId} item={bookmark} />
                ))}
              </div>

              {!isLoadingBookmarks && !bookmarks.length && !bookmarkError ? (
                <div className="py-20 text-center text-slate-500 text-sm">
                  이 폴더에 북마크가 없습니다.
                </div>
              ) : null}

              {hasMore && !isLoadingBookmarks ? (
                <div className="mt-4 text-center">
                  <button
                    type="button"
                    className="px-6 py-2.5 rounded-full border border-white/10 bg-white/5 text-slate-300 text-sm font-medium hover:bg-white/10 transition-colors"
                    onClick={loadMore}
                  >
                    더 보기
                  </button>
                </div>
              ) : null}

              {isLoadingBookmarks && bookmarks.length > 0 ? (
                <div className="p-4 text-center text-slate-500 text-sm">
                  추가 북마크를 불러오는 중...
                </div>
              ) : null}
            </div>
          )}
        </div>
        </div>
      </div>

      <FolderManagementModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateOrUpdateFolder}
        initialData={editingFolder ? {
          name: editingFolder.name,
          icon: editingFolder.icon || 'folder',
          color: editingFolder.color || '#2b7fff'
        } : undefined}
      />
    </>
  )
}

type FolderItemProps = {
  folder: BookmarkFolderDto
  onDelete: (folderId: number) => void
  onSelect: (folder: BookmarkFolderDto) => void
  onEdit: (folder: BookmarkFolderDto) => void
}

function FolderItem({ folder, onDelete, onSelect, onEdit }: FolderItemProps) {
  const [showMenu, setShowMenu] = useState(false)
  // const isDefaultFolder = folder.folderId <= 2

  // 색상을 받아서 Tailwind 클래스나 스타일에 적용하기 위한 유틸
  const getIconStyle = (color?: string) => {
    // 기본값
    const baseColor = color || '#2b7fff'
    return {
      color: baseColor,
      backgroundColor: `${baseColor}15`, // 15 = ~8% opacity
    }
  }

  const iconStyle = getIconStyle(folder.color)

  return (
    <div className="relative group bg-[#111] border border-white/5 rounded-2xl transition-colors hover:border-white/10">
      <div
        className="flex items-center gap-4 p-4 cursor-pointer"
        onClick={() => onSelect(folder)}
      >
        <div
          className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0"
          style={iconStyle}
        >
          <BookmarkFolderIcon icon={folder.icon} width={24} height={24} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-0.5">
            <h3 className="text-[17px] font-semibold text-slate-50 truncate">{folder.name}</h3>

          </div>
          <p className="text-[13px] text-slate-500">
             {/* API에서 개수를 받을 수 있다면 표시, 현재는 임시 */}
             폴더 열기
          </p>
        </div>
      </div>

      <div className="absolute top-4 right-4">
        <button
          type="button"
          className="w-8 h-8 flex items-center justify-center rounded-full text-slate-400 hover:bg-white/10 hover:text-white transition-colors"
          onClick={(e) => {
            e.stopPropagation()
            setShowMenu(!showMenu)
          }}
          aria-label="메뉴"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" className="rotate-90">
            <circle cx="5" cy="10" r="1.5" fill="currentColor" />
            <circle cx="10" cy="10" r="1.5" fill="currentColor" />
            <circle cx="15" cy="10" r="1.5" fill="currentColor" />
          </svg>
        </button>

        {showMenu && (
          <>
            <div
              className="fixed inset-0 z-20 cursor-default"
              onClick={(e) => {
                e.stopPropagation()
                setShowMenu(false)
              }}
            />
            <div className="absolute right-0 top-full mt-1 w-32 bg-[#1e2939] border border-white/10 rounded-xl shadow-xl z-30 overflow-hidden py-1">
              <button
                type="button"
                className="w-full text-left px-4 py-2.5 text-[14px] text-slate-200 hover:bg-white/5 transition-colors"
                onClick={(e) => {
                  e.stopPropagation()
                  onEdit(folder)
                  setShowMenu(false)
                }}
              >
                수정
              </button>
              <button
                type="button"
                className="w-full text-left px-4 py-2.5 text-[14px] text-red-400 hover:bg-red-500/10 transition-colors"
                onClick={(e) => {
                  e.stopPropagation()
                  onDelete(folder.folderId)
                  setShowMenu(false)
                }}
              >
                삭제
              </button>
            </div>
          </>
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



  return (
    <article className="bg-[#111] border border-white/5 rounded-[20px] overflow-hidden">
      <div className="p-5 flex flex-col gap-3">
        <div className="flex justify-between items-center text-[12px]">
          <span className="font-semibold text-[#a5b4fc]">{folderName || '전체'}</span>
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
            {originalUrl && (
              <a
                href={originalUrl}
                target="_blank"
                rel="noreferrer"
                className="px-3 py-1.5 rounded-lg border border-white/10 bg-white/5 text-[12px] text-slate-300 no-underline hover:bg-white/10 transition-colors"
              >
                원문 보기
              </a>
            )}
          </div>
        </div>
      </div>
    </article>
  )
}
