import { useLocation, useNavigate } from 'react-router-dom'
import feedIcon from '../assets/navigation/home_btn.png'
import searchIcon from '../assets/navigation/search_btn.png'
import bookmarkIcon from '../assets/navigation/bookmark_btn.png'
import profileIcon from '../assets/navigation/profile_btn.png'
import notificationIcon from '../assets/navigation/notification_btn.png'

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
    path: '/explore',
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
    <nav
      className="fixed left-1/2 bottom-6 -translate-x-1/2 w-[calc(100%-32px)] max-w-[440px] grid grid-cols-5 gap-1.5 p-3 px-[18px] rounded-[28px] border border-white/25 bg-gradient-to-br from-white/18 to-white/8 shadow-[0_30px_55px_rgba(15,23,42,0.25),0_18px_30px_rgba(2,6,23,0.22)] backdrop-blur-[22px] z-40 min-[480px]:bottom-8"
      aria-label="하단 내비게이션"
    >
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
            className={`relative flex flex-col items-center gap-2 py-2 pb-1.5 text-[11px] font-semibold tracking-[-0.02em] rounded-[18px] transition-all duration-250 ease-in-out ${
              isActive
                ? 'text-white bg-gradient-to-br from-[rgba(90,201,255,0.32)] to-[rgba(128,90,255,0.28)] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.12),0_10px_22px_rgba(32,39,80,0.45)]'
                : 'text-slate-100/65 hover:text-slate-100/80'
            } active:translate-y-px`}
            onClick={handleClick}
            aria-current={isActive ? 'page' : undefined}
          >
            <span
              className={`w-11 h-11 rounded-2xl inline-flex items-center justify-center transition-all duration-250 ${
                isActive
                  ? 'bg-[rgba(5,9,24,0.25)] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.35),0_10px_20px_rgba(8,10,24,0.45)]'
                  : 'bg-white/8'
              }`}
              aria-hidden
            >
              <img
                className={`w-[22px] h-[22px] object-contain transition-all duration-250 ${
                  isActive ? 'grayscale-0' : 'grayscale-[0.2]'
                }`}
                src={icon}
                alt=""
                loading="lazy"
              />
            </span>
            <span className="leading-none">{label}</span>
            {badge ? (
              <span className="absolute top-1 right-4 min-w-[18px] h-[18px] px-[5px] rounded-full bg-gradient-to-br from-[#ff5f6d] to-[#ffc371] border border-white/80 text-slate-900 text-[10px] font-bold inline-flex items-center justify-center shadow-[0_4px_8px_rgba(2,6,23,0.25)]">
                {badge}
              </span>
            ) : null}
          </button>
        )
      })}
    </nav>
  )
}
