import { useState, useEffect, useCallback } from 'react'
import './FolderManagementModal.css'

type IconType = 'folder' | 'star' | 'clock' | 'heart' | 'bookmark' | 'tag' | 'archive' | 'folder-open'
type ColorType = '#2b7fff' | '#ad46ff' | '#00c950' | '#fb2c36' | '#ff6900' | '#f6339a' | '#f0b100' | '#6a7282'

type FolderManagementModalProps = {
  isOpen: boolean
  onClose: () => void
  onCreateFolder: (name: string, icon: IconType, color: ColorType) => Promise<void>
}

const ICONS: IconType[] = ['folder', 'star', 'clock', 'heart', 'bookmark', 'tag', 'archive', 'folder-open']
const COLORS: ColorType[] = ['#2b7fff', '#ad46ff', '#00c950', '#fb2c36', '#ff6900', '#f6339a', '#f0b100', '#6a7282']

const ICON_LABELS: Record<IconType, string> = {
  folder: '폴더',
  star: '별',
  clock: '시계',
  heart: '하트',
  bookmark: '북마크',
  tag: '태그',
  archive: '보관함',
  'folder-open': '열린 폴더',
}

export function FolderManagementModal({ isOpen, onClose, onCreateFolder }: FolderManagementModalProps) {
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
    <div className="folder-modal-overlay" onClick={handleClose}>
      <div className="folder-modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal aria-label="새 폴더 만들기">
        <button
          type="button"
          className="folder-modal__close"
          onClick={handleClose}
          aria-label="닫기"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M12 4L4 12M4 4L12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>

        <div className="folder-modal__header">
          <h2>새 폴더 만들기</h2>
          <p>콘텐츠를 정리할 새 폴더를 만드세요.</p>
        </div>

        <div className="folder-modal__content">
          <div className="folder-modal__field">
            <label htmlFor="folder-name">폴더 이름</label>
            <input
              id="folder-name"
              type="text"
              placeholder="예: 개발, 디자인, 마케팅..."
              value={folderName}
              onChange={(e) => setFolderName(e.target.value)}
              maxLength={50}
            />
          </div>

          <div className="folder-modal__field">
            <label>아이콘</label>
            <div className="folder-modal__icon-grid">
              {ICONS.map((icon) => (
                <button
                  key={icon}
                  type="button"
                  className={`folder-modal__icon-option${selectedIcon === icon ? ' is-selected' : ''}`}
                  onClick={() => setSelectedIcon(icon)}
                  aria-label={ICON_LABELS[icon]}
                >
                  <IconComponent icon={icon} />
                </button>
              ))}
            </div>
          </div>

          <div className="folder-modal__field">
            <label>색상</label>
            <div className="folder-modal__color-grid">
              {COLORS.map((color) => (
                <button
                  key={color}
                  type="button"
                  className={`folder-modal__color-option${selectedColor === color ? ' is-selected' : ''}`}
                  onClick={() => setSelectedColor(color)}
                  aria-label={color}
                >
                  <div className="folder-modal__color-swatch" style={{ backgroundColor: color }} />
                </button>
              ))}
            </div>
          </div>

          <div className="folder-modal__field">
            <label>미리보기</label>
            <div className="folder-modal__preview">
              <div className="folder-modal__preview-icon" style={{ backgroundColor: `${selectedColor}1a` }}>
                <div style={{ color: selectedColor }}>
                  <IconComponent icon={selectedIcon} />
                </div>
              </div>
              <div className="folder-modal__preview-text">
                <p className="folder-modal__preview-name">{folderName || '폴더 이름'}</p>
                <p className="folder-modal__preview-count">0개의 콘텐츠</p>
              </div>
            </div>
          </div>
        </div>

        {submitError ? (
          <p className="folder-modal__error" role="alert">
            {submitError}
          </p>
        ) : null}

        <div className="folder-modal__footer">
          <button
            type="button"
            className="folder-modal__button folder-modal__button--primary"
            onClick={handleSubmit}
            disabled={!isFormValid || isSubmitting}
          >
            {isSubmitting ? '만드는 중...' : '만들기'}
          </button>
          <button
            type="button"
            className="folder-modal__button folder-modal__button--secondary"
            onClick={handleClose}
          >
            취소
          </button>
        </div>
      </div>
    </div>
  )
}

function IconComponent({ icon }: { icon: IconType }) {
  switch (icon) {
    case 'folder':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M2.5 5.83333C2.5 4.91286 3.24619 4.16667 4.16667 4.16667H7.08925C7.53857 4.16667 7.96634 4.34226 8.28141 4.65734L9.82259 6.19851C10.1377 6.51359 10.5654 6.68917 11.0148 6.68917H15.8333C16.7538 6.68917 17.5 7.43537 17.5 8.35583V14.1667C17.5 15.0871 16.7538 15.8333 15.8333 15.8333H4.16667C3.24619 15.8333 2.5 15.0871 2.5 14.1667V5.83333Z"
            stroke="currentColor"
            strokeWidth="1.5"
          />
        </svg>
      )
    case 'star':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M10 2.5L12.245 7.755L18 8.5L14 12.755L15 18.5L10 15.755L5 18.5L6 12.755L2 8.5L7.755 7.755L10 2.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
        </svg>
      )
    case 'clock':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <circle cx="10" cy="10" r="7.5" stroke="currentColor" strokeWidth="1.5" />
          <path d="M10 6V10L13 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      )
    case 'heart':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M10 17.5C10 17.5 2.5 12.5 2.5 7.5C2.5 4.5 4.5 2.5 7 2.5C8.5 2.5 10 3.5 10 3.5C10 3.5 11.5 2.5 13 2.5C15.5 2.5 17.5 4.5 17.5 7.5C17.5 12.5 10 17.5 10 17.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
        </svg>
      )
    case 'bookmark':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M5 3.75C5 3.05964 5.55964 2.5 6.25 2.5H13.75C14.4404 2.5 15 3.05964 15 3.75V17.5L10 14.1667L5 17.5V3.75Z"
            stroke="currentColor"
            strokeWidth="1.5"
          />
        </svg>
      )
    case 'tag':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M2.5 7.5L7.5 2.5L17.5 12.5L12.5 17.5L2.5 7.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
          <circle cx="8.75" cy="8.75" r="1.25" fill="currentColor" />
        </svg>
      )
    case 'archive':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M3.75 5.83333H16.25M3.75 5.83333V15.8333C3.75 16.2936 4.1231 16.6667 4.58333 16.6667H15.4167C15.8769 16.6667 16.25 16.2936 16.25 15.8333V5.83333M3.75 5.83333L5 3.33333H15L16.25 5.83333M8.33333 9.16667H11.6667"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </svg>
      )
    case 'folder-open':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path
            d="M2.5 5.83333C2.5 4.91286 3.24619 4.16667 4.16667 4.16667H7.08925C7.53857 4.16667 7.96634 4.34226 8.28141 4.65734L9.82259 6.19851C10.1377 6.51359 10.5654 6.68917 11.0148 6.68917H15.8333C16.7538 6.68917 17.5 7.43537 17.5 8.35583V9.16667M2.5 14.1667L4.16667 9.16667H18.3333L16.6667 14.1667H2.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </svg>
      )
    default:
      return null
  }
}
