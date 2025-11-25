import type { KeyboardEvent, MouseEvent } from 'react'

export type HighlightCardProps = {
  image: string
  isNew?: boolean
  linkUrl?: string
  source: string
  timeAgo: string
  title: string
  summary: string
  tag: string
  typeIcon: string
  typeLabel: string
  bookmarkIcon?: string
  shareIcon?: string
  onBookmarkClick?: () => void
  onShareClick?: () => void
}

export function HighlightCard({
  image,
  isNew,
  linkUrl,
  source,
  timeAgo,
  title,
  summary,
  tag,
  typeIcon,
  typeLabel,
  bookmarkIcon,
  shareIcon,
  onBookmarkClick,
  onShareClick,
}: HighlightCardProps) {
  const showActions = bookmarkIcon || shareIcon
  const isInteractive = Boolean(linkUrl)

  const handleCardClick = () => {
    if (!linkUrl) return
    window.open(linkUrl, '_blank', 'noopener,noreferrer')
  }

  const handleCardKeyDown = (event: KeyboardEvent<HTMLElement>) => {
    if (!linkUrl) return
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      handleCardClick()
    }
  }

  const handleActionClick = (callback?: () => void) => (event: MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation()
    callback?.()
  }

  return (
    <article
      className={`highlight-card${isInteractive ? ' is-interactive' : ''}`}
      role={isInteractive ? 'link' : undefined}
      tabIndex={isInteractive ? 0 : undefined}
      onClick={handleCardClick}
      onKeyDown={handleCardKeyDown}
    >
      <div className="highlight-card__media">
        <img src={image} alt="" loading="lazy" />
        {isNew ? <span className="badge badge--alert">NEW</span> : null}
        <span className="badge badge--type">
          <span aria-hidden>{typeIcon}</span> {typeLabel}
        </span>
      </div>
      <div className="highlight-card__meta">
        <span>{source}</span>
        <span aria-hidden>•</span>
        <span>{timeAgo}</span>
      </div>
      <div className="highlight-card__body">
        <h2>{title}</h2>
        <p>{summary}</p>
      </div>
      <div className="highlight-card__footer">
        <span className="card-tag">{tag}</span>
        {showActions ? (
          <div className="card-actions">
            {bookmarkIcon ? (
              <button type="button" aria-label="저장" onClick={handleActionClick(onBookmarkClick)}>
                <img src={bookmarkIcon} alt="" aria-hidden />
              </button>
            ) : null}
            {shareIcon ? (
              <button type="button" aria-label="공유" onClick={handleActionClick(onShareClick)}>
                <img src={shareIcon} alt="" aria-hidden />
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </article>
  )
}
