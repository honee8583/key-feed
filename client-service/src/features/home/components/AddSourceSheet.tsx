import { type FormEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { sourceApi, type CreatedSource } from '../../../services/sourceApi'



type SourceType = 'blog' | 'news' | 'video' | 'community'

type AddSourceSheetProps = {
  isOpen: boolean
  onClose: () => void
  onSubmit?: (payload: { name: string; url: string; type: SourceType; created: CreatedSource }) => void
  initialName?: string
  initialUrl?: string
}

export function AddSourceSheet({ isOpen, onClose, onSubmit, initialName = '', initialUrl = '' }: AddSourceSheetProps) {
  const [sourceType, setSourceType] = useState<SourceType>('blog')
  const [sourceName, setSourceName] = useState(initialName)
  const [sourceUrl, setSourceUrl] = useState(initialUrl)
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
    
    // Reset or Initialize state when opened
    setSourceName(initialName)
    setSourceUrl(initialUrl)
    setSourceType('blog')
    setSubmitError(null)
    
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
  }, [isOpen, dismissSheet, initialName, initialUrl])

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
      className="fixed inset-0 z-[60] flex items-end justify-center"
      role="dialog"
      aria-modal
      aria-label="콘텐츠 소스 추가"
    >
      <div 
        className="absolute inset-0 bg-black/50 backdrop-blur-[2px] z-[1] transition-opacity duration-200"
        onClick={dismissSheet} 
      />
      
      <div 
        className="relative z-[2] w-full max-w-[478px] bg-[#1e2939] rounded-t-[24px] overflow-hidden flex flex-col max-h-[85vh] animate-[sheet-slide-up_240ms_ease-out]"
      >
        <div className="flex justify-between items-center p-5 pb-2 shrink-0">
          <h2 className="text-[18px] font-bold text-slate-50 m-0">소스 추가하기</h2>
          <button
            type="button"
            className="w-8 h-8 flex items-center justify-center rounded-full bg-slate-800 text-slate-400 hover:text-white transition-colors"
            onClick={dismissSheet}
            aria-label="닫기"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>

        <div className="p-5 pb-0 shrink-0">
           <p className="text-[14px] text-slate-400">새로운 콘텐츠 소스를 추가하여 피드를 구성하세요.</p>
        </div>

        <div className="flex-1 overflow-y-auto p-5 py-6 flex flex-col gap-6">
          {/* Form Fields */}
          <div className="flex flex-col gap-4">
             <div className="flex flex-col gap-2">
                <label className="text-[13px] font-semibold text-slate-300 ml-1">소스 이름</label>
                <input
                  ref={nameInputRef}
                  type="text"
                  placeholder="예: Tech Blog Korea"
                  value={sourceName}
                  onChange={(event) => setSourceName(event.target.value)}
                  className="w-full h-12 rounded-xl border border-white/10 bg-[rgba(2,6,23,0.6)] px-4 text-[15px] text-white placeholder:text-slate-500/70 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 transition-all"
                />
             </div>

             <div className="flex flex-col gap-2">
                <label className="text-[13px] font-semibold text-slate-300 ml-1">소스 URL</label>
                <div className="flex items-center gap-3 w-full h-12 rounded-xl border border-white/10 bg-[rgba(2,6,23,0.6)] px-4 focus-within:border-blue-500/50 focus-within:ring-1 focus-within:ring-blue-500/50 transition-all">
                   <span className="text-lg opacity-50" aria-hidden>🔗</span>
                   <input
                     type="url"
                     placeholder="https://example.com/feed"
                     value={sourceUrl}
                     onChange={(event) => setSourceUrl(event.target.value)}
                     className="flex-1 bg-transparent border-none p-0 text-[15px] text-white placeholder:text-slate-500/70 focus:outline-none"
                   />
                </div>
                <p className="text-[12px] text-slate-500 ml-1">RSS 피드, 채널 URL, 또는 블로그 주소를 입력하세요</p>
             </div>
          </div>
        </div>

        {submitError ? (
          <p className="px-5 py-2 text-[13px] text-red-400 text-center animate-pulse" role="alert">
            {submitError}
          </p>
        ) : null}

        {/* Footer Actions */}
        <div className="p-4 border-t border-white/5 bg-[#1e2939] flex flex-col gap-3 pb-8">
            <button
               type="button"
               className="w-full h-[52px] rounded-[14px] bg-[#3b82f6] text-white text-[16px] font-bold hover:bg-[#2563eb] active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100"
               onClick={handleSubmit}
               disabled={isSubmitDisabled || isSubmitting}
             >
               {isSubmitting ? '추가 중...' : '소스 추가하기'}
             </button>
             <button
               type="button"
               className="w-full h-[52px] rounded-[14px] border border-white/10 bg-white/5 text-[#d1d5dc] text-[16px] font-medium hover:bg-white/10 active:scale-[0.98] transition-all"
               onClick={dismissSheet}
             >
               취소
             </button>
        </div>
      </div>
    </div>
  )
}
