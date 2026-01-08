import { useAuth } from '../features/auth'

export function TopBar() {
  const { user } = useAuth()
  const initials = user?.name?.[0]?.toUpperCase() ?? 'K'

  return (
    <header className="fixed top-0 left-0 right-0 h-16 px-8 flex items-center justify-between backdrop-blur-[18px] bg-white/80 border-b border-slate-200/80 z-30 max-sm:px-4">
      <div className="app-topbar__brand">
        <span className="font-bold text-lg text-slate-900" aria-hidden>
          KeyFeed
        </span>
      </div>
      <div className="flex items-center gap-3">
        <div
          className="w-10 h-10 rounded-[14px] bg-gradient-to-br from-[#155dfc] to-[#4f39f6] text-white font-bold flex items-center justify-center"
          aria-hidden
        >
          {initials}
        </div>
        <div className="flex flex-col leading-tight max-sm:[&_small]:hidden">
          <strong className="text-sm text-slate-900">{user?.name ?? 'KeyFeed 멤버'}</strong>
          <small className="text-xs text-slate-500">{user?.email ?? '멤버 전용 피드'}</small>
        </div>
      </div>
    </header>
  )
}
