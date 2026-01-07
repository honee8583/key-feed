import type { NotificationItem } from '../types'

type NotificationCardProps = {
  item: NotificationItem
}

export function NotificationCard({ item }: NotificationCardProps) {
  const { title, description, time, tag, icon, linkUrl, isLive } = item
  return (
    <article className="grid grid-cols-[auto_1fr_auto] gap-3 p-5 rounded-[24px] border border-white/8 bg-gradient-to-br from-[rgba(12,12,18,0.92)] to-[rgba(9,9,14,0.85)] shadow-[0_18px_32px_rgba(2,6,23,0.55)] relative">
      <div
        className="w-[52px] h-[52px] rounded-[18px] bg-white/6 flex items-center justify-center text-2xl shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08),0_12px_24px_rgba(2,6,23,0.7)]"
        aria-hidden
      >
        <span>{icon}</span>
      </div>
      <div className="flex flex-col gap-1.5 min-w-0">
        <div className="flex items-center gap-2.5">
          <p className="m-0 text-base font-semibold tracking-[-0.02em] text-slate-50 flex-1 min-w-0">{title}</p>
        </div>
        <p className="m-0 text-sm text-slate-50/70 leading-snug">{description}</p>
        <div className="flex items-center gap-2 flex-wrap">
          <span className="inline-flex items-center gap-1.5 text-xs text-slate-400/90" aria-label={`${time}에 받은 알림`}>
            <span
              className={`inline-block w-1 h-1 rounded-full ${
                isLive
                  ? 'bg-gradient-to-br from-[#f87171] to-[#f43f5e] shadow-[0_0_6px_rgba(248,113,113,0.8)]'
                  : 'bg-slate-200/50'
              }`}
              aria-hidden
            />
            {time}
          </span>
          {tag ? (
            <span className="inline-flex items-center py-[3px] px-[11px] rounded-[10px] bg-[rgba(37,99,235,0.15)] text-[#a5b4fc] text-xs font-semibold border border-indigo-400/40 shadow-[0_8px_16px_rgba(2,6,23,0.4)]">
              {tag}
            </span>
          ) : null}
          {linkUrl ? (
            <a
              className="text-[#a5b4fc] font-semibold no-underline hover:underline focus-visible:underline"
              href={linkUrl}
              target="_blank"
              rel="noreferrer"
            >
              원문 보기
            </a>
          ) : null}
        </div>
      </div>
      <button
        type="button"
        className="w-8 h-8 rounded-[10px] border border-white/10 bg-slate-50/8 shadow-[0_10px_24px_rgba(2,6,23,0.6)] inline-flex items-center justify-center text-slate-50/70 cursor-pointer text-base hover:bg-slate-50/12"
        aria-label="알림 옵션"
      >
        <span aria-hidden>⋯</span>
      </button>
    </article>
  )
}
