import type { InputHTMLAttributes, ReactNode } from 'react'

interface AuthInputProps extends InputHTMLAttributes<HTMLInputElement> {
  icon?: ReactNode
}

export function AuthInput({ icon, className = '', ...props }: AuthInputProps) {
  return (
    <div className="relative group">
      {icon && (
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 transition-colors group-focus-within:text-slate-300">
          {icon}
        </div>
      )}
      <input
        className={`w-full h-[52px] bg-[#111] border border-white/10 rounded-[16px] pr-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-white/30 focus:bg-[#161616] transition-all text-[15px] disabled:opacity-50 ${
          icon ? 'pl-11' : 'pl-4'
        } ${className}`}
        {...props}
      />
    </div>
  )
}
