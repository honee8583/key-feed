import { type FormEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import './AddSourceSheet.css'
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
    <div className="add-source-portal" role="dialog" aria-modal aria-label="콘텐츠 소스 추가">
      <div className="add-source-overlay" onClick={dismissSheet} />
      <section className="add-source-sheet">
        <header className="add-source-sheet__header">
          <div>
            <p className="sheet-eyebrow">KeyFeed 설정</p>
            <h2>소스 추가하기</h2>
          </div>
          <button type="button" className="sheet-close" aria-label="소스 추가 닫기" onClick={dismissSheet}>
            <span aria-hidden>×</span>
          </button>
        </header>

        <form className="add-source-form" onSubmit={handleSubmit}>
          <fieldset className="source-type-field">
            <legend>소스 타입</legend>
            <div className="source-type-grid">
              {SOURCE_TYPE_OPTIONS.map((option) => {
                const isSelected = option.id === sourceType
                return (
                  <button
                    key={option.id}
                    type="button"
                    className={isSelected ? 'is-selected' : undefined}
                    onClick={() => setSourceType(option.id)}
                  >
                    <img src={option.icon} alt="" aria-hidden />
                    <span>{option.label}</span>
                  </button>
                )
              })}
            </div>
          </fieldset>

          <label className="form-control">
            <span className="form-label">소스 이름</span>
            <input
              ref={nameInputRef}
              type="text"
              placeholder="예: Tech Blog Korea"
              value={sourceName}
              onChange={(event) => setSourceName(event.target.value)}
            />
          </label>

          <label className="form-control">
            <span className="form-label">소스 URL</span>
            <div className="input-with-icon">
              <span aria-hidden>🔗</span>
              <input
                type="url"
                placeholder="https://example.com/feed"
                value={sourceUrl}
                onChange={(event) => setSourceUrl(event.target.value)}
              />
            </div>
            <p className="form-helper">RSS 피드, 채널 URL, 또는 블로그 주소를 입력하세요</p>
          </label>

          <div className="popular-sources">
            <p className="form-label">인기 소스</p>
            <ul>
              {POPULAR_SOURCES.map((source) => (
                <li key={source.name}>
                  <span>{source.name}</span>
                  <button type="button" onClick={() => handlePopularSelect(source.name, source.url, source.typeId)}>
                    추가
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {submitError ? (
            <p className="sheet-error" role="alert">
              {submitError}
            </p>
          ) : null}

          <div className="sheet-actions">
            <button type="submit" className="primary" disabled={isSubmitDisabled || isSubmitting}>
              {isSubmitting ? '추가 중...' : '소스 추가하기'}
            </button>
            <button type="button" onClick={dismissSheet}>
              취소
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
