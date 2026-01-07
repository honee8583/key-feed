import { type FormEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { sourceApi, type CreatedSource } from '../../../services/sourceApi'

const SOURCE_TYPE_OPTIONS = [
  {
    id: 'blog',
    label: '블로그',
    icon: 'http://localhost:3845/assets/9dff08aab9abaa6a04c4d1dd71ca4149994a9f17.svg',
  },
  {
    id: 'news',
    label: '뉴스',
    icon: 'http://localhost:3845/assets/4c4dbc3c438d8bb5d1ee1251d638b23560b083c7.svg',
  },
  {
    id: 'video',
    label: '유튜브',
    icon: 'http://localhost:3845/assets/ea0201bc68deb42715a27ba06681095c5f489e39.svg',
  },
  {
    id: 'community',
    label: '커뮤니티',
    icon: 'http://localhost:3845/assets/004e5c039213e718cb0046d6da0e7a169a20d5f1.svg',
  },
] as const

const POPULAR_SOURCES = [
  { name: 'Hacker News', url: 'https://news.ycombinator.com/rss', typeId: 'news' as const },
  { name: 'Dev.to', url: 'https://dev.to/feed', typeId: 'blog' as const },
  { name: 'Medium', url: 'https://medium.com/feed', typeId: 'blog' as const },
]

type SourceType = (typeof SOURCE_TYPE_OPTIONS)[number]['id']

type AddSourceSheetProps = {
  isOpen: boolean
  onClose: () => void
  onSubmit?: (payload: { name: string; url: string; type: SourceType; created: CreatedSource }) => void
}

export function AddSourceSheet({ isOpen, onClose, onSubmit }: AddSourceSheetProps) {
  const [sourceType, setSourceType] = useState<SourceType>('blog')
  const [sourceName, setSourceName] = useState('')
  const [sourceUrl, setSourceUrl] = useState('')
  const nameInputRef = useRef<HTMLInputElement | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const dismissSheet = useCallback(() => {
    setSourceName('')
    setSourceUrl('')
    setSourceType('blog')
    setSubmitError(null)
    onClose()
  }, [onClose])

  useEffect(() => {
    if (!isOpen) {
      return
    }
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    nameInputRef.current?.focus()

    const handleKeydown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        dismissSheet()
      }
    }

    window.addEventListener('keydown', handleKeydown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeydown)
    }
  }, [isOpen, dismissSheet])

  const handlePopularSelect = (name: string, url: string, typeId: SourceType) => {
    setSubmitError(null)
    setSourceName(name)
    setSourceUrl(url)
    setSourceType(typeId)
    nameInputRef.current?.focus()
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const trimmedName = sourceName.trim()
    const trimmedUrl = sourceUrl.trim()
    if (!trimmedName || !trimmedUrl) {
      setSubmitError('소스 이름과 URL을 모두 입력해주세요.')
      return
    }

    try {
      setSubmitError(null)
      setIsSubmitting(true)
      const createdSource = await sourceApi.create({ name: trimmedName, url: trimmedUrl })
      onSubmit?.({ name: trimmedName, url: trimmedUrl, type: sourceType, created: createdSource })
      dismissSheet()
    } catch (error) {
      const message = error instanceof Error ? error.message : '소스 추가에 실패했습니다. 다시 시도해주세요.'
      setSubmitError(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const isSubmitDisabled = useMemo(() => {
    return !sourceName.trim() || !sourceUrl.trim()
  }, [sourceName, sourceUrl])

  if (!isOpen) {
    return null
  }

  return (
    <div
      className="fixed inset-0 z-[60] flex items-end justify-center animate-[sheet-slide-up_240ms_ease-out]"
      role="dialog"
      aria-modal
      aria-label="콘텐츠 소스 추가"
    >
      <div className="absolute inset-0 bg-black/50 backdrop-blur-[2px] z-[1]" onClick={dismissSheet} />
      <section className="relative z-[2] w-full max-w-[478px] max-h-[calc(100%-80px)] rounded-t-[24px] bg-white shadow-[0_-24px_48px_rgba(15,23,42,0.18)] p-6 overflow-y-auto max-[480px]:rounded-t-2xl max-[480px]:p-5">
        <header className="flex justify-between items-start gap-3 pb-3 border-b border-slate-200/80">
          <div>
            <p className="m-0 text-[13px] text-[#6a7282]">KeyFeed 설정</p>
            <h2 className="mt-1 mb-0 text-xl text-[#101828]">소스 추가하기</h2>
          </div>
          <button
            type="button"
            className="border-none bg-slate-900/4 w-9 h-9 rounded-full text-xl cursor-pointer text-slate-900 hover:bg-slate-900/8 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#155dfc] focus-visible:outline-offset-2"
            aria-label="소스 추가 닫기"
            onClick={dismissSheet}
          >
            <span aria-hidden>×</span>
          </button>
        </header>

        <form className="flex flex-col gap-6 pt-6" onSubmit={handleSubmit}>
          <fieldset className="border-none p-0 m-0 flex flex-col gap-3">
            <legend className="text-sm font-semibold text-[#101828]">소스 타입</legend>
            <div className="grid grid-cols-2 gap-3">
              {SOURCE_TYPE_OPTIONS.map((option) => {
                const isSelected = option.id === sourceType
                return (
                  <button
                    key={option.id}
                    type="button"
                    className={`flex items-center gap-3 rounded-xl border-2 p-2.5 px-3.5 bg-white font-semibold cursor-pointer transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#155dfc] focus-visible:outline-offset-2 ${
                      isSelected
                        ? 'border-[#155dfc] bg-[#eef4ff] text-[#1c398e]'
                        : 'border-[#e5e7eb] text-[#364153] hover:border-[#d1d5db]'
                    }`}
                    onClick={() => setSourceType(option.id)}
                  >
                    <img src={option.icon} alt="" className="w-5 h-5" aria-hidden />
                    <span>{option.label}</span>
                  </button>
                )
              })}
            </div>
          </fieldset>

          <label className="flex flex-col gap-2">
            <span className="text-sm font-semibold text-[#101828]">소스 이름</span>
            <input
              ref={nameInputRef}
              type="text"
              placeholder="예: Tech Blog Korea"
              value={sourceName}
              onChange={(event) => setSourceName(event.target.value)}
              className="rounded-[10px] border border-slate-900/8 px-3 py-2.5 text-sm bg-[rgba(229,229,229,0.3)] focus:outline-none focus:ring-2 focus:ring-[#155dfc]"
            />
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-sm font-semibold text-[#101828]">소스 URL</span>
            <div className="flex items-center gap-2 rounded-[10px] border border-slate-900/8 px-3 bg-[rgba(229,229,229,0.3)]">
              <span className="text-base" aria-hidden>🔗</span>
              <input
                type="url"
                placeholder="https://example.com/feed"
                value={sourceUrl}
                onChange={(event) => setSourceUrl(event.target.value)}
                className="flex-1 border-none px-0 py-2.5 bg-transparent focus:outline-none focus:ring-0"
              />
            </div>
            <p className="m-0 text-xs text-[#6a7282]">RSS 피드, 채널 URL, 또는 블로그 주소를 입력하세요</p>
          </label>

          <div>
            <p className="text-sm font-semibold text-[#101828] mb-2 mt-0">인기 소스</p>
            <ul className="list-none m-2 mt-0 p-0 flex flex-col gap-2">
              {POPULAR_SOURCES.map((source) => (
                <li key={source.name} className="flex justify-between items-center border border-[#e4e7ec] rounded-[10px] px-3 py-2.5">
                  <span className="font-medium text-[#101828]">{source.name}</span>
                  <button
                    type="button"
                    className="border-none bg-none text-[#6a7282] font-semibold cursor-pointer hover:text-[#475467] focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#155dfc] focus-visible:outline-offset-2"
                    onClick={() => handlePopularSelect(source.name, source.url, source.typeId)}
                  >
                    추가
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {submitError ? (
            <p className="m-0 text-[13px] text-[#fb2c36]" role="alert">
              {submitError}
            </p>
          ) : null}

          <div className="flex flex-col gap-3 mt-3">
            <button
              type="submit"
              className="rounded-[10px] border-none px-3 py-3 text-[15px] font-semibold cursor-pointer bg-gradient-to-r from-[#155dfc] to-[#4f39f6] text-slate-50 transition-opacity duration-160 disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#155dfc] focus-visible:outline-offset-2"
              disabled={isSubmitDisabled || isSubmitting}
            >
              {isSubmitting ? '추가 중...' : '소스 추가하기'}
            </button>
            <button
              type="button"
              className="rounded-[10px] border-none px-3 py-3 text-[15px] font-semibold cursor-pointer bg-transparent border border-slate-900/10 text-[#101828] hover:bg-slate-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#155dfc] focus-visible:outline-offset-2"
              onClick={dismissSheet}
            >
              취소
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
