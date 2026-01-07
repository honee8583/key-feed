import type { ReactNode } from 'react'

type NotificationStatusProps = {
  children: ReactNode
  variant?: 'default' | 'error' | 'inline'
  role?: 'status' | 'alert'
}

export function NotificationStatus({ children, variant = 'default', role }: NotificationStatusProps) {
  const baseClasses =
    variant === 'inline'
      ? 'mt-3 bg-transparent border-none shadow-none py-2 pb-5 text-slate-400/90'
      : variant === 'error'
        ? 'p-[18px] text-center rounded-2xl bg-[rgba(127,29,29,0.4)] border border-red-400/40 text-[#fecaca] text-sm shadow-[inset_0_0_0_1px_rgba(255,255,255,0.02),0_12px_30px_rgba(2,6,23,0.35)]'
        : 'p-[18px] text-center rounded-2xl bg-[rgba(15,15,20,0.75)] border border-white/6 text-slate-50/70 text-sm shadow-[inset_0_0_0_1px_rgba(255,255,255,0.02),0_12px_30px_rgba(2,6,23,0.35)]'

  return (
    <div className={baseClasses} role={role}>
      {children}
    </div>
  )
}
