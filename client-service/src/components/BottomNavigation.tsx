import { useLocation, useNavigate } from 'react-router-dom'
import feedIcon from '../assets/navigation/home_btn.png'
import searchIcon from '../assets/navigation/search_btn.png'
import bookmarkIcon from '../assets/navigation/bookmark_btn.png'
import profileIcon from '../assets/navigation/profile_btn.png'
import notificationIcon from '../assets/navigation/notification_btn.png'
import './BottomNavigation.css'

type NavItem = {
  label: string
  path?: string
  icon: string
  badge?: number
}

const NAV_ITEMS: NavItem[] = [
  {
    label: '피드',
    path: '/home',
    icon: feedIcon,
  },
  {
    label: '탐색',
    icon: searchIcon,
  },
  {
    label: '북마크',
    path: '/bookmarks',
    icon: bookmarkIcon,
  },
  {
    label: '알림',
    path: '/notifications',
    icon: notificationIcon,
    badge: 3,
  },
  {
    label: '프로필',
    path: '/profile',
    icon: profileIcon,
  },
]

export function BottomNavigation() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <nav className="bottom-navigation" aria-label="하단 내비게이션">
      {NAV_ITEMS.map(({ label, path, badge, icon }) => {
        const isActive = Boolean(path && location.pathname.startsWith(path))
        const handleClick = () => {
          if (path) {
            navigate(path)
          }
        }

        return (
          <button
            key={label}
            type="button"
            className={isActive ? 'is-active' : undefined}
            onClick={handleClick}
            aria-current={isActive ? 'page' : undefined}
          >
            <span className="nav-icon-ring" aria-hidden>
              <img className="nav-icon" src={icon} alt="" loading="lazy" />
            </span>
            <span className="nav-label">{label}</span>
            {badge ? <span className="nav-badge">{badge}</span> : null}
          </button>
        )
      })}
    </nav>
  )
}
