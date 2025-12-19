import type { ReactNode } from 'react'

type NotificationStatusProps = {
  children: ReactNode
  variant?: 'default' | 'error' | 'inline'
  role?: 'status' | 'alert'
}

export function NotificationStatus({ children, variant = 'default', role }: NotificationStatusProps) {
  const className = ['notifications-status']
  if (variant === 'error') {
    className.push('is-error')
  }
  if (variant === 'inline') {
    className.push('is-inline')
  }

  return (
    <div className={className.join(' ')} role={role}>
      {children}
    </div>
  )
}
