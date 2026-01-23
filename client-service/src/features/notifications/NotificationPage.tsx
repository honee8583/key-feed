import { useEffect, useRef } from 'react'
import notificationIcon from '../../assets/navigation/notification_btn.png'
import { NotificationCard, NotificationStatus } from './components'
import { useNotifications } from '../../hooks/useNotifications'

export function NotificationPage() {
  const {
    notifications,
    isLoading,
    error,
    hasNext,
    isFetchingNext,
    fetchNextPage,
  } = useNotifications()
  
  const loadMoreRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (!hasNext) {
      return
    }
    const target = loadMoreRef.current
    if (!target) {
      return
    }
    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry && entry.isIntersecting) {
          void fetchNextPage()
        }
      },
      { root: null, rootMargin: '0px 0px 200px 0px', threshold: 0 }
    )
    observer.observe(target)
    return () => observer.disconnect()
  }, [fetchNextPage, hasNext])

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_15%_20%,rgba(255,255,255,0.08),transparent_45%),#050505] py-7 pb-40 flex justify-center text-slate-50 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[420px] px-4 flex flex-col gap-[18px]">
        <header className="bg-gradient-to-br from-[rgba(15,15,20,0.95)] to-[rgba(10,10,16,0.85)] border border-white/8 shadow-[0_18px_30px_rgba(2,6,23,0.4)] rounded-[28px] p-6 flex flex-col gap-4">
          <div className="flex items-center justify-between gap-3">
            <div className="inline-flex items-center gap-2.5">
              <img src={notificationIcon} alt="" className="w-5 h-5 flex-shrink-0 mt-1" aria-hidden="true" />
              <h1 className="m-0 text-[26px] tracking-[-0.02em] inline-flex items-center gap-2 text-slate-50">
                알림
              </h1>
            </div>
            <button
              type="button"
              className="w-10 h-10 rounded-2xl border border-white/18 bg-white/8 shadow-[0_10px_24px_rgba(2,6,23,0.5)] inline-flex items-center justify-center cursor-pointer text-slate-50/70 text-lg hover:bg-white/12"
              aria-label="알림 설정 및 옵션"
            >
              <span aria-hidden>⋯</span>
            </button>
          </div>
        </header>

        <section className="flex flex-col gap-3.5" aria-label="알림 목록">
          {isLoading && !notifications.length ? (
            <NotificationStatus role="status">알림을 불러오는 중입니다...</NotificationStatus>
          ) : null}

          {error && !notifications.length ? (
            <NotificationStatus variant="error" role="alert">
              {error}
            </NotificationStatus>
          ) : null}

          {!isLoading && !error && !notifications.length ? (
            <NotificationStatus>표시할 알림이 없어요.</NotificationStatus>
          ) : null}

          {notifications.map((notification) => (
            <NotificationCard key={notification.id} item={notification} />
          ))}

          <div ref={loadMoreRef} className="w-full h-px" aria-hidden />

          {isFetchingNext ? (
            <NotificationStatus variant="inline" role="status">
              이전 알림을 불러오는 중입니다...
            </NotificationStatus>
          ) : null}
        </section>
      </div>
    </div>
  )
}


