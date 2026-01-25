import { useCallback, useEffect, useRef, useState } from 'react'
import { useAuth } from '../auth'
import { TagIcon, PlusIcon } from '../../components/common/Icons'
import { KeywordManagementModal } from './components/KeywordManagementModal'
import { HighlightCard, type HighlightCardProps } from './components/HighlightCard'
import type { CreatedSource } from '../../services/sourceApi'
import { AddSourceSheet } from './components/AddSourceSheet'
import bookmarkIcon from '../../assets/home/bookmark_btn.png'
import addSourceIcon from '../../assets/home/source_add_btn.png'
import { useFeed } from '../../hooks/useFeed'

const figureAssets = {
  bookmark: bookmarkIcon,
}

type AddSourceResult = { name: string; url: string; type: string; created: CreatedSource }

const highlightCardActionIcons: Pick<HighlightCardProps, 'bookmarkIcon'> = {
  bookmarkIcon: figureAssets.bookmark,
}

export function MainPage() {
  const { user } = useAuth()
  const welcomeName = user?.name ?? 'KeyFeed 멤버'
  const {
    articles,
    isInitialLoading,
    isFetchingMore,
    error,
    hasNext,
    loadNextPage,
    handleRetry,
    handleBookmarkClick,
    refresh,
  } = useFeed()
  
  const loadMoreTriggerRef = useRef<HTMLDivElement | null>(null)
  const [isAddSourceOpen, setIsAddSourceOpen] = useState(false)
  const [isKeywordModalOpen, setIsKeywordModalOpen] = useState(false)

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

  const hasArticles = articles.length > 0
  const showEmptyState = !isInitialLoading && !error && !hasArticles

  const handleOpenAddSource = () => setIsAddSourceOpen(true)
  const handleCloseAddSource = () => setIsAddSourceOpen(false)

  const handleAddSourceSubmit = useCallback(
    (result: AddSourceResult) => {
      void result
      void refresh()
    },
    [refresh],
  )

  return (
    <div className="min-h-screen py-8 pb-[140px] bg-[radial-gradient(circle_at_15%_15%,rgba(255,255,255,0.08),transparent_55%),#050505] font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif] text-slate-50">
      <div className="w-full max-w-[440px] mx-auto flex flex-col gap-6 px-5">
        <header className="bg-gradient-to-br from-[#020202] to-[#161616] text-white rounded-[32px] p-8 px-7 flex justify-between items-start relative overflow-hidden border border-white/8 max-[480px]:p-7 max-[480px]:px-6">
          <div className="absolute -left-10 -top-[60px] w-[180px] h-[180px] rounded-full bg-white/5 blur-[30px]" />
          <div className="relative z-10">
            <p className="m-0 mb-2 text-sm opacity-70">콘텐츠 피드</p>
            <h1 className="m-0 text-[32px] tracking-[-0.02em]">Discover</h1>
            <p className="mt-3 mb-0 text-slate-100/65">맞춤 콘텐츠를 탐색하세요</p>
          </div>
        </header>

        <button 
          onClick={() => setIsKeywordModalOpen(true)}
          className="w-full bg-[#161b26] border border-white/5 rounded-[24px] p-5 flex items-center justify-between cursor-pointer hover:bg-[#1c2230] transition-colors group text-left"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-[18px] bg-[#3B82F6]/10 flex items-center justify-center text-[#3B82F6] group-hover:scale-110 transition-transform">
              <TagIcon className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-[17px] font-bold text-white mb-0.5">키워드 관리</h3>
              <p className="text-[14px] text-slate-400">관심 키워드를 추가하고 관리하세요</p>
            </div>
          </div>
          <PlusIcon className="w-6 h-6 text-slate-500 group-hover:text-white transition-colors" />
        </button>



        <section className="flex flex-col gap-8" aria-label="하이라이트 콘텐츠">
          {articles.map(({ id, isBookmarked, ...card }) => (
            <HighlightCard
              key={id}
              {...card}
              {...highlightCardActionIcons}
              isBookmarked={isBookmarked}
              onBookmarkClick={() => handleBookmarkClick(id, Boolean(isBookmarked), card.bookmarkId)}
            />
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
      <KeywordManagementModal isOpen={isKeywordModalOpen} onClose={() => setIsKeywordModalOpen(false)} />
    </div>
  )
}
