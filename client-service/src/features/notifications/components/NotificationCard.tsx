import type { NotificationItem } from '../types'

type NotificationCardProps = {
  item: NotificationItem
}

export function NotificationCard({ item }: NotificationCardProps) {
  const { title, description, time, tag, icon, linkUrl, isLive } = item
  return (
    <article className="notification-card">
      <div className="notification-icon" aria-hidden>
        <span>{icon}</span>
      </div>
      <div className="notification-content">
        <div className="notification-title-row">
          <p className="notification-title">{title}</p>
        </div>
        <p className="notification-description">{description}</p>
        <div className="notification-meta">
          <span className="notification-time" aria-label={`${time}에 받은 알림`}>
            <span className={`notification-time__dot${isLive ? ' is-live' : ''}`} aria-hidden />
            {time}
          </span>
          {tag ? <span className="notification-tag">{tag}</span> : null}
          {linkUrl ? (
            <a className="notification-link" href={linkUrl} target="_blank" rel="noreferrer">
              원문 보기
            </a>
          ) : null}
        </div>
      </div>
      <button type="button" className="notification-menu" aria-label="알림 옵션">
        <span aria-hidden>⋯</span>
      </button>
    </article>
  )
}
