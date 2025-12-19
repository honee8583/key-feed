import { useEffect, useMemo, useState } from 'react'
import './BookmarksPage.css'
import { bookmarkApi, type BookmarkFolderDto } from '../../services/bookmarkApi'

type BookmarkItem = {
  id: string
  title: string
  summary: string
  tags: string[]
  category: string
  savedAt: string
  readingTime: string
  source: string
  highlight?: string
  link?: string
  folderId?: number
}

const categoryTabs = ['전체', '블로그', '뉴스', '영상', '커뮤니티', '리포트']
const quickFilters = ['최신순', '읽지 않음', '노트 있음', '원문 링크']

const mockBookmarks: BookmarkItem[] = [
  {
    id: 'bk-2024-01',
    title: 'AI가 불러온 디자인 워크플로우 혁신',
    summary: 'AI 보조 도구 시대에 디자이너가 어떤 새로운 습관을 가져야 하는지를 실제 사례와 함께 정리했습니다.',
    tags: ['디자인', 'AI', '워크플로우'],
    category: '블로그',
    savedAt: '2024-04-09T06:30:00.000Z',
    readingTime: '8분 소요',
    source: 'Better Design',
    highlight: '“AI를 툴이 아닌 파트너로 바라볼 때 협업의 즐거움이 커집니다.”',
    link: 'https://example.com/design-ai',
    folderId: 1,
  },
  {
    id: 'bk-2024-02',
    title: 'Next.js 15 미리보기 총정리',
    summary: '서버 액션과 새로운 라우팅 규칙까지, Next.js 15에서 기대할 만한 기능을 한눈에 볼 수 있게 정리했습니다.',
    tags: ['Next.js', 'React', '릴리즈노트'],
    category: '뉴스',
    savedAt: '2024-04-08T15:10:00.000Z',
    readingTime: '5분 소요',
    source: 'KeyFeed Digest',
    link: 'https://example.com/next15',
    folderId: 2,
  },
  {
    id: 'bk-2024-03',
    title: '웹 접근성 체크리스트 2024',
    summary: 'WCAG 2.2 기준으로 업데이트된 체크리스트와 실무 적용 팁을 모았어요.',
    tags: ['접근성', '가이드', '프론트엔드'],
    category: '리포트',
    savedAt: '2024-04-07T04:20:00.000Z',
    readingTime: '13분 소요',
    source: 'Inclusive Web Korea',
    highlight: '팀별로 30분씩 투자하면 전반적인 접근성 점수를 빠르게 끌어올릴 수 있습니다.',
    folderId: 4,
  },
  {
    id: 'bk-2024-04',
    title: 'Developer 커뮤니티가 사랑한 아티클 모음',
    summary: 'Reddit과 Hacker News에서 한 주간 화제가 된 기술 글을 모았습니다.',
    tags: ['커뮤니티', '트렌드'],
    category: '커뮤니티',
    savedAt: '2024-04-05T22:42:00.000Z',
    readingTime: '10분 소요',
    source: 'Community Radar',
    folderId: 2,
  },
]

export function BookmarksPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const [activeCategory, setActiveCategory] = useState(categoryTabs[0])
  const [activeQuickFilter, setActiveQuickFilter] = useState(quickFilters[0])
  const [folders, setFolders] = useState<BookmarkFolderDto[]>([])
  const [activeFolderId, setActiveFolderId] = useState<number | null>(null)
  const [isLoadingFolders, setIsLoadingFolders] = useState(false)
  const [folderError, setFolderError] = useState<string | null>(null)

  useEffect(() => {
    const fetchFolders = async () => {
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
    }

    void fetchFolders()
  }, [])

  const filteredBookmarks = useMemo(() => {
    return mockBookmarks.filter((bookmark) => {
      const matchesCategory = activeCategory === '전체' || bookmark.category === activeCategory
      const matchesSearch =
        !searchTerm ||
        bookmark.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        bookmark.tags.some((tag) => tag.toLowerCase().includes(searchTerm.toLowerCase()))
      const matchesFolder =
        !activeFolderId || bookmark.folderId === activeFolderId || bookmark.folderId === undefined
      return matchesCategory && matchesSearch && matchesFolder
    })
  }, [activeCategory, searchTerm, activeFolderId])

  const bookmarkCount = mockBookmarks.length
  const newItemsCount = mockBookmarks.filter((item) => item.highlight).length

  return (
    <div className="bookmarks-page">
      <header className="bookmarks-hero">
        <div className="bookmarks-hero__content">
          <p className="bookmarks-hero__eyebrow">My Library</p>
          <h1>북마크</h1>
          <p className="bookmarks-hero__subtitle">인사이트를 저장하고 다시 찾아보세요</p>
        </div>
        <div className="bookmarks-hero__action">
          <button type="button">폴더 관리</button>
        </div>
      </header>

      <section className="bookmark-stats" aria-label="북마크 요약">
        <article>
          <p className="bookmark-stats__label">전체 북마크</p>
          <p className="bookmark-stats__value">{bookmarkCount}</p>
          <p className="bookmark-stats__hint">최근 7일 기준</p>
        </article>
        <article>
          <p className="bookmark-stats__label">새로 저장됨</p>
          <p className="bookmark-stats__value accent">{newItemsCount}</p>
          <p className="bookmark-stats__hint">하이라이트 포함</p>
        </article>
        <article>
          <p className="bookmark-stats__label">읽지 않은 항목</p>
          <p className="bookmark-stats__value">3</p>
          <p className="bookmark-stats__hint">태그별 정리 필요</p>
        </article>
      </section>

      <section className="bookmark-folders" aria-label="북마크 폴더 목록">
        <div className="bookmark-folders__header">
          <h2>폴더</h2>
          {folderError ? <span className="bookmark-folders__error">{folderError}</span> : null}
        </div>
        {isLoadingFolders ? (
          <div className="bookmark-folders__status">폴더를 불러오는 중...</div>
        ) : null}
        {!isLoadingFolders && folders.length ? (
          <div className="bookmark-folders__list">
            <button
              type="button"
              className={`bookmark-folder-chip${activeFolderId === null ? ' is-active' : ''}`}
              onClick={() => setActiveFolderId(null)}
            >
              <span>전체</span>
            </button>
            {folders.map((folder) => {
              const isActive = folder.folderId === activeFolderId
              return (
                <button
                  key={folder.folderId}
                  type="button"
                  className={`bookmark-folder-chip${isActive ? ' is-active' : ''}`}
                  onClick={() => setActiveFolderId(folder.folderId)}
                >
                  <span>{folder.name}</span>
                </button>
              )
            })}
          </div>
        ) : null}
        {!isLoadingFolders && !folders.length && !folderError ? (
          <div className="bookmark-folders__status">등록된 폴더가 없어요.</div>
        ) : null}
      </section>

      <div className="bookmark-search">
        <div className="bookmark-search__field">
          <input
            type="search"
            placeholder="저장한 콘텐츠 검색"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />
        </div>
        <div className="bookmark-search__filters" aria-label="빠른 정렬">
          {quickFilters.map((filter) => (
            <button
              key={filter}
              type="button"
              className={filter === activeQuickFilter ? 'is-active' : undefined}
              onClick={() => setActiveQuickFilter(filter)}
            >
              {filter}
            </button>
          ))}
        </div>
      </div>

      <nav className="bookmark-tabs" aria-label="저장된 콘텐츠 카테고리">
        {categoryTabs.map((category) => (
          <button
            key={category}
            type="button"
            className={category === activeCategory ? 'is-active' : undefined}
            onClick={() => setActiveCategory(category)}
          >
            {category}
          </button>
        ))}
      </nav>

      <section className="bookmark-list" aria-label="저장된 콘텐츠 목록">
        {filteredBookmarks.map((bookmark) => (
          <BookmarkCard key={bookmark.id} item={bookmark} />
        ))}
        {!filteredBookmarks.length ? (
          <div className="bookmark-status">조건에 맞는 북마크가 없어요.</div>
        ) : null}
      </section>
    </div>
  )
}

function BookmarkCard({ item }: { item: BookmarkItem }) {
  const { title, summary, tags, savedAt, readingTime, source, category, highlight, link } = item
  const formattedDate = new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(savedAt))

  return (
    <article className="bookmark-card">
      <div className="bookmark-card__meta">
        <span className="bookmark-card__category">{category}</span>
        <span className="bookmark-card__date">{formattedDate}</span>
      </div>
      <h2>{title}</h2>
      <p className="bookmark-card__summary">{summary}</p>
      {highlight ? <blockquote>{highlight}</blockquote> : null}

      <div className="bookmark-card__tags">
        {tags.map((tag) => (
          <span key={tag}>#{tag}</span>
        ))}
      </div>

      <div className="bookmark-card__footer">
        <div>
          <p className="bookmark-card__source">{source}</p>
          <p className="bookmark-card__reading">{readingTime}</p>
        </div>
        <div className="bookmark-card__cta">
          <button type="button" aria-label="노트 추가">
            노트
          </button>
          {link ? (
            <a href={link} target="_blank" rel="noreferrer">
              원문 보기
            </a>
          ) : null}
        </div>
      </div>
    </article>
  )
}
