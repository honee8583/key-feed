import { useLocation, useNavigate } from 'react-router-dom'
import './BottomNavigation.css'

const NAV_ITEMS = [
  {
    label: '피드',
    path: '/home',
    icon: 'http://localhost:3845/assets/868bbbcae1aa6c1c740f92b2ff6fa54f75af995e.svg',
  },
  {
    label: '탐색',
    icon: 'http://localhost:3845/assets/a935ae8594333cb5e22064fcf0ed7e7909b94395.svg',
  },
  {
    label: '저장됨',
    icon: 'http://localhost:3845/assets/3008441642aaa3a201a49a748114abcfe802ca2c.svg',
  },
  {
    label: '알림',
    icon: 'http://localhost:3845/assets/4526c92732d3e606769f4cbb7cb27589bc3e4d72.svg',
    badge: 3,
  },
  {
    label: '프로필',
    path: '/profile',
    icon: 'http://localhost:3845/assets/1ff36808c7b560838b8db4a90b16bb121b10fcd2.svg',
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
          <button key={label} type="button" className={isActive ? 'is-active' : undefined} onClick={handleClick}>
            <img className="nav-icon" src={icon} alt="" aria-hidden loading="lazy" />
            <span>{label}</span>
            {badge ? <span className="nav-badge">{badge}</span> : null}
          </button>
        )
      })}
    </nav>
  )
}
