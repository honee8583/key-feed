import { useEffect, useState } from 'react'
import { ToggleSwitch } from '../../components/ToggleSwitch'
import { sourceApi, type CreatedSource } from '../../services/sourceApi'
import './SourceManagementPage.css'

const filterChips = ['전체', 'RSS', '뉴스레터', 'YouTube', '블로그']

type ManagedSource = CreatedSource & {
  status: 'active' | 'paused'
  type: string
  frequency: string
  tags: string[]
}

export function SourceManagementPage() {
  const [items, setItems] = useState<ManagedSource[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    const fetchSources = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const response = await sourceApi.listMy()
        if (cancelled) return
        setItems(response.map(mapSourceToCard))
      } catch (fetchError) {
        if (cancelled) return
        setError(fetchError instanceof Error ? fetchError.message : '소스를 불러오지 못했습니다.')
      } finally {
        if (cancelled) return
        setIsLoading(false)
      }
    }

    void fetchSources()

    return () => {
      cancelled = true
    }
  }, [])

  const handleToggle = (userSourceId: number) => {
    setItems((prev) =>
      prev.map((item) =>
        item.userSourceId === userSourceId
          ? {
              ...item,
              status: item.status === 'active' ? 'paused' : 'active',
            }
          : item,
      ),
    )
  }

  return (
    <div className="source-page">
      <div className="source-page__glow source-page__glow--blue" aria-hidden />
      <div className="source-page__glow source-page__glow--purple" aria-hidden />
      <div className="source-page__content">
        <header className="source-hero">
          <div>
            <p className="source-hero__eyebrow">소스 관리</p>
            <h1>내 콘텐츠 허브</h1>
            <p className="source-hero__desc">구독 중인 채널을 정리하고 새 소스를 연결하세요.</p>
          </div>
          <button type="button" className="source-hero__cta">
            + 새 소스 추가
          </button>
        </header>

        <div className="source-search">
          <input type="search" placeholder="소스 이름 또는 URL 검색" aria-label="소스 검색" />
          <button type="button">필터</button>
        </div>

        <div className="source-filters" aria-label="소스 유형 필터">
          {filterChips.map((chip, index) => (
            <button key={chip} type="button" className={index === 0 ? 'is-selected' : undefined}>
              {chip}
            </button>
          ))}
        </div>

        <section className="source-cards" aria-label="연결된 소스 목록">
          {isLoading ? <p className="source-status">연결된 소스를 불러오는 중...</p> : null}
          {error ? <p className="source-status is-error">{error}</p> : null}
          {!isLoading && !error && items.length === 0 ? (
            <p className="source-status">아직 연결된 소스가 없어요. 새 소스를 추가해보세요.</p>
          ) : null}
          {items.map((source) => (
            <article key={source.userSourceId} className="source-card">
              <header className="source-card__header">
                <div>
                  <p className="source-card__name">{source.userDefinedName}</p>
                  <p className="source-card__meta">
                    {source.type} · {source.frequency}
                  </p>
                </div>
                <ToggleSwitch
                  active={source.status === 'active'}
                  ariaLabel={source.status === 'active' ? '활성화됨' : '비활성화됨'}
                  onToggle={() => handleToggle(source.userSourceId)}
                />
              </header>
              <p className="source-card__url">{source.url}</p>
              <div className="source-card__tags">
                {source.tags.map((tag) => (
                  <span key={tag}>{tag}</span>
                ))}
              </div>
              <div className="source-card__actions">
                <button type="button">편집</button>
                <button type="button">동기화</button>
                <button type="button">삭제</button>
              </div>
            </article>
          ))}
        </section>
      </div>
    </div>
  )
}

function mapSourceToCard(source: CreatedSource): ManagedSource {
  const hostname = safeHostname(source.url)
  const type = deriveSourceType(hostname)
  const frequency = type === 'YouTube' ? '실시간' : '매일 업데이트'
  return {
    ...source,
    status: 'active',
    type,
    frequency,
    tags: [hostname],
  }
}

function safeHostname(url: string) {
  try {
    return new URL(url).hostname.replace('www.', '')
  } catch (error) {
    return url
  }
}

function deriveSourceType(host: string) {
  if (host.includes('youtube') || host.includes('youtu.be')) return 'YouTube'
  if (host.includes('newsletter')) return '뉴스레터'
  if (host.includes('community')) return '커뮤니티'
  return 'RSS'
}
