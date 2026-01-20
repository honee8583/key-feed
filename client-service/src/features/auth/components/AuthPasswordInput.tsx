import { useState, type InputHTMLAttributes } from 'react'
import { EyeIcon, EyeOffIcon, LockIcon } from './AuthIcons'

interface AuthPasswordInputProps extends InputHTMLAttributes<HTMLInputElement> {
  placeholder?: string
}

export function AuthPasswordInput({ placeholder = '비밀번호를 입력하세요', className = '', ...props }: AuthPasswordInputProps) {
  const [showPassword, setShowPassword] = useState(false)

  return (
    <div className="relative group">
      <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 transition-colors group-focus-within:text-slate-300">
        <LockIcon />
      </div>
      <input
        type={showPassword ? 'text' : 'password'}
        placeholder={placeholder}
        className={`w-full h-[52px] bg-[#111] border border-white/10 rounded-[16px] pl-11 pr-12 text-white placeholder:text-slate-600 focus:outline-none focus:border-white/30 focus:bg-[#161616] transition-all text-[15px] disabled:opacity-50 ${className}`}
        {...props}
      />
      <button
        type="button"
        className="absolute right-4 top-1/2 -translate-y-1/2 border-none bg-transparent p-1 flex items-center justify-center cursor-pointer text-slate-500 hover:text-slate-300 transition-colors"
        onClick={() => setShowPassword((prev) => !prev)}
        aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
      >
        {showPassword ? <EyeOffIcon /> : <EyeIcon />}
      </button>
    </div>
  )
}
