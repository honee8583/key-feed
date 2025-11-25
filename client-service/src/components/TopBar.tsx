import { useAuth } from '../features/auth'
import './TopBar.css'

export function TopBar() {
  const { user } = useAuth()
  const initials = user?.name?.[0]?.toUpperCase() ?? 'K'

  return (
    <header className="app-topbar">
      <div className="app-topbar__brand">
        <span aria-hidden>KeyFeed</span>
      </div>
      <div className="app-topbar__profile">
        <div className="app-topbar__avatar" aria-hidden>
          {initials}
        </div>
        <div className="app-topbar__text">
          <strong>{user?.name ?? 'KeyFeed 멤버'}</strong>
          <small>{user?.email ?? '멤버 전용 피드'}</small>
        </div>
      </div>
    </header>
  )
}
