import { useLocation, useNavigate } from 'react-router-dom'
import { FeedIcon, SearchIcon, BookmarkIcon, NotificationIcon, ProfileIcon } from './NavigationIcons'

type NavItem = {
  label: string
  path?: string
  icon: React.ComponentType<{ className?: string }>
  badge?: number
}

const NAV_ITEMS: NavItem[] = [
  {
    label: '피드',
    path: '/home',
    icon: FeedIcon,
  },
  {
    label: '탐색',
    path: '/explore',
    icon: SearchIcon,
  },
  {
    label: '북마크',
    path: '/bookmarks',
    icon: BookmarkIcon,
  },
  {
    label: '알림',
    path: '/notifications',
    icon: NotificationIcon,
  },
  {
    label: '프로필',
    path: '/profile',
    icon: ProfileIcon,
  },
]

export function BottomNavigation() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <nav
      className="fixed left-1/2 bottom-6 -translate-x-1/2 w-[calc(100%-32px)] max-w-[440px] flex items-center justify-between px-2 py-3 rounded-[32px] bg-[#0F172A]/90 backdrop-blur-xl border border-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.4)] z-50 min-[480px]:bottom-8"
      aria-label="하단 내비게이션"
    >
      {NAV_ITEMS.map(({ label, path, badge, icon: Icon }) => {
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
            className="flex-1 relative flex flex-col items-center gap-[4px] py-1 cursor-pointer group"
            onClick={handleClick}
            aria-current={isActive ? 'page' : undefined}
          >
            {/* Active Background Glow */}
            {isActive && (
              <div 
                className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[56px] h-[56px] rounded-[20px] opacity-90 transition-all duration-300 pointer-events-none"
                style={{
                  background: 'linear-gradient(135deg, rgba(43, 127, 255, 0.2) 0%, rgba(81, 162, 255, 0.15) 50%, rgba(173, 70, 255, 0.2) 100%)',
                  boxShadow: '0 0 12px rgba(66, 133, 244, 0.15)'
                }}
              />
            )}

            {/* Icon Component */}
            <div className={`relative w-[24px] h-[24px] z-10 transition-transform duration-200 ${isActive ? 'scale-105' : 'group-hover:scale-105'}`}>
              <Icon 
                className={`w-full h-full transition-colors duration-300 ${isActive ? 'text-[#5eaaff]' : 'text-[#99A1AF]'}`} 
              />
              {badge ? (
                <span className="absolute -top-1 -right-1 min-w-[14px] h-[14px] px-[3px] rounded-full bg-[#ef4444] text-white text-[9px] font-bold flex items-center justify-center border border-[#0F172A]">
                  {badge}
                </span>
              ) : null}
            </div>

            {/* Label */}
            <span 
              className={`text-[11px] font-medium leading-none z-10 transition-colors duration-300 ${
                isActive ? 'text-[#5eaaff] font-bold' : 'text-[#99A1AF]'
              }`}
            >
              {label}
            </span>
          </button>
        )
      })}
    </nav>
  )
}

