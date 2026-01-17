import { useState, useEffect, useCallback } from 'react'

import {
  BookmarkFolderIcon,
  COLORS,
  ICONS,
  ICON_LABELS,
  type ColorType,
  type IconType,
} from './BookmarkFolderIcon'

type FolderManagementModalProps = {
  isOpen: boolean
  onClose: () => void
  onCreateFolder: (name: string, icon: IconType, color: ColorType) => Promise<void>
  zIndex?: number
}

export function FolderManagementModal({ isOpen, onClose, onCreateFolder, zIndex = 50 }: FolderManagementModalProps) {
  const [folderName, setFolderName] = useState('')
  const [selectedIcon, setSelectedIcon] = useState<IconType>('folder')
  const [selectedColor, setSelectedColor] = useState<ColorType>('#2b7fff')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const handleClose = useCallback(() => {
    setFolderName('')
    setSelectedIcon('folder')
    setSelectedColor('#2b7fff')
    setSubmitError(null)
    onClose()
  }, [onClose])

  useEffect(() => {
    if (!isOpen) {
      return
    }
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleKeydown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        handleClose()
      }
    }

    window.addEventListener('keydown', handleKeydown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeydown)
    }
  }, [isOpen, handleClose])

  if (!isOpen) return null

  const handleSubmit = async () => {
    if (!folderName.trim() || isSubmitting) return

    setIsSubmitting(true)
    setSubmitError(null)
    try {
      await onCreateFolder(folderName.trim(), selectedIcon, selectedColor)
      handleClose()
    } catch (error) {
      const message =
        error instanceof Error ? error.message : '폴더 생성에 실패했습니다. 다시 시도해주세요.'
      setSubmitError(message)
      console.error('Failed to create folder:', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  const isFormValid = folderName.trim().length > 0

  return (
    <div 
      className="fixed inset-0 flex items-end justify-center" 
      style={{ zIndex }}
      onClick={handleClose}
      role="dialog"
      aria-modal
    >
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity duration-200"
        onClick={handleClose} 
      />
      
      <div 
        className="relative z-10 w-full max-w-[480px] bg-[#1e2939] rounded-t-[24px] overflow-hidden flex flex-col max-h-[90vh] shadow-2xl animate-[sheet-slide-up_240ms_ease-out]" 
        onClick={(e) => e.stopPropagation()} 
        aria-label="새 폴더 만들기"
      >
        <div className="flex justify-between items-center p-5 pb-2 shrink-0">
          <h2 className="text-[18px] font-bold text-slate-50 m-0">새 폴더 만들기</h2>
          <button
            type="button"
            className="w-8 h-8 flex items-center justify-center rounded-full bg-slate-800 text-slate-400 hover:text-white transition-colors"
            onClick={handleClose}
            aria-label="닫기"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>

        <div className="p-5 pb-0 shrink-0">
           <p className="text-[14px] text-slate-400">콘텐츠를 정리할 새 폴더를 만드세요.</p>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-6 flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <label htmlFor="folder-name" className="text-[13px] font-semibold text-slate-300 ml-1">폴더 이름</label>
            <input
              id="folder-name"
              type="text"
              className="w-full h-12 rounded-xl border border-white/10 bg-[rgba(2,6,23,0.6)] px-4 text-[15px] text-white placeholder:text-slate-500/70 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 transition-all"
              placeholder="예: 개발, 디자인, 마케팅..."
              value={folderName}
              onChange={(e) => setFolderName(e.target.value)}
              maxLength={50}
            />
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-[13px] font-semibold text-slate-300 ml-1">아이콘</label>
            <div className="grid grid-cols-8 gap-2">
              {ICONS.map((icon) => (
                <button
                  key={icon}
                  type="button"
                  className={`aspect-square rounded-xl flex items-center justify-center transition-all ${
                    selectedIcon === icon 
                      ? 'bg-blue-500 text-white shadow-lg shadow-blue-500/20 scale-105' 
                      : 'bg-white/5 text-slate-400 hover:bg-white/10 hover:text-slate-200'
                  }`}
                  onClick={() => setSelectedIcon(icon)}
                  aria-label={ICON_LABELS[icon]}
                >
                  <BookmarkFolderIcon icon={icon} width={20} height={20} />
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-[13px] font-semibold text-slate-300 ml-1">색상</label>
            <div className="grid grid-cols-8 gap-2">
              {COLORS.map((color) => (
                <button
                  key={color}
                  type="button"
                  className={`aspect-square rounded-xl flex items-center justify-center transition-all ${
                    selectedColor === color 
                      ? 'bg-white/10 ring-2 ring-blue-500 ring-offset-2 ring-offset-[#1e2939] scale-105' 
                      : 'hover:bg-white/5'
                  }`}
                  onClick={() => setSelectedColor(color)}
                  aria-label={color}
                >
                  <div 
                    className="w-5 h-5 rounded-full shadow-sm" 
                    style={{ backgroundColor: color }} 
                  />
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-[13px] font-semibold text-slate-300 ml-1">미리보기</label>
            <div className="w-full bg-[#111] border border-white/5 rounded-2xl p-4 flex items-center gap-4">
              <div 
                className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 transition-colors" 
                style={{ backgroundColor: `${selectedColor}1a` }}
              >
                <div style={{ color: selectedColor }}>
                  <BookmarkFolderIcon icon={selectedIcon} width={24} height={24} />
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[16px] font-bold text-slate-50 truncate transition-all">
                  {folderName || '폴더 이름'}
                </p>
                <p className="text-[13px] text-slate-500 mt-0.5">0개의 콘텐츠</p>
              </div>
            </div>
          </div>
        </div>

        {submitError ? (
          <p className="px-6 py-2 text-[13px] text-red-400 text-center animate-pulse" role="alert">
            {submitError}
          </p>
        ) : null}

        <div className="p-4 border-t border-white/5 bg-[#1e2939] flex flex-col gap-3 pb-8">
          <button
            type="button"
            className="w-full h-[52px] rounded-[14px] bg-[#3b82f6] text-white text-[16px] font-bold hover:bg-[#2563eb] active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100"
            onClick={handleSubmit}
            disabled={!isFormValid || isSubmitting}
          >
            {isSubmitting ? '만드는 중...' : '만들기'}
          </button>
          <button
            type="button"
            className="w-full h-[52px] rounded-[14px] border border-white/10 bg-white/5 text-[#d1d5dc] text-[16px] font-medium hover:bg-white/10 active:scale-[0.98] transition-all"
            onClick={handleClose}
          >
            취소
          </button>
        </div>
      </div>
    </div>
  )
}
