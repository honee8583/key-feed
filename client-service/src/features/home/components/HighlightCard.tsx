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
  onBookmarkClick?: () => void
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
  onBookmarkClick,
}: HighlightCardProps) {
  const showActions = Boolean(bookmarkIcon)
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
      className={`bg-[#0f0f0f] rounded-[28px] p-5 border border-white/8 flex flex-col gap-4 shadow-none max-[480px]:p-4 ${
        isInteractive ? 'cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-white/80 focus-visible:outline-offset-1' : ''
      }`}
      role={isInteractive ? 'link' : undefined}
      tabIndex={isInteractive ? 0 : undefined}
      onClick={handleCardClick}
      onKeyDown={handleCardKeyDown}
    >
      <div className="relative rounded-3xl overflow-hidden aspect-[338/212]">
        <img src={image} alt="" className="w-full h-full object-cover block" loading="lazy" />
        {isNew ? (
          <span className="absolute top-4 left-4 inline-flex items-center gap-1 py-1.5 px-3.5 rounded-full text-[13px] font-semibold text-white bg-gradient-to-br from-[#fb2c36] to-[#e60076]">
            NEW
          </span>
        ) : null}
        <span className="absolute top-4 right-4 inline-flex items-center gap-1 py-1.5 px-3.5 rounded-full text-[13px] font-semibold text-white bg-white/15">
          <span aria-hidden>{typeIcon}</span> {typeLabel}
        </span>
      </div>
      <div className="flex gap-2 text-white/55 text-[13px]">
        <span>{source}</span>
        <span aria-hidden>•</span>
        <span>{timeAgo}</span>
      </div>
      <div className="flex flex-col">
        <h2 className="m-0 mb-2 text-[22px]">{title}</h2>
        <p className="m-0 text-white/70 leading-relaxed">{summary}</p>
      </div>
      <div className="flex justify-between items-center">
        <span className="py-2 px-3.5 rounded-full bg-white/10 text-slate-50 font-semibold">{tag}</span>
        {showActions ? (
          <div className="flex gap-2.5">
            {bookmarkIcon ? (
              <button
                type="button"
                className="w-11 h-11 rounded-2xl border border-white/10 bg-white/5 inline-flex items-center justify-center hover:bg-white/10 transition-colors"
                aria-label="저장"
                onClick={handleActionClick(onBookmarkClick)}
              >
                <img className="w-[18px] h-[18px]" src={bookmarkIcon} alt="" aria-hidden />
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </article>
  )
}
