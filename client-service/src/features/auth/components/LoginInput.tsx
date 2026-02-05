import { useState, type InputHTMLAttributes, type ReactNode } from 'react'
import { EyeIcon, EyeOffIcon } from './AuthIcons'

interface LoginInputProps extends InputHTMLAttributes<HTMLInputElement> {
  icon?: ReactNode
}

export function LoginInput({ icon, className = '', type = 'text', ...props }: LoginInputProps) {
  const [showPassword, setShowPassword] = useState(false)
  const isPassword = type === 'password'
  const inputType = isPassword && showPassword ? 'text' : type

  return (
    <div className="bg-[#1E293B] rounded-[16px] px-4 h-[52px] flex items-center border border-transparent focus-within:border-[#2B7FFF] transition-colors relative">
      {icon && (
        <div className="text-[#64748B] mr-3 shrink-0 flex items-center justify-center">
          {icon}
        </div>
      )}
      <input
        type={inputType}
        className={`bg-transparent border-none text-white text-[15px] w-full h-full focus:outline-none placeholder-[#64748B] disabled:opacity-50 ${className}`}
        {...props}
      />
      {isPassword && (
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="ml-2 p-1 text-[#64748B] hover:text-white transition-colors cursor-pointer flex items-center justify-center"
          aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
        >
          {showPassword ? <EyeOffIcon /> : <EyeIcon />}
        </button>
      )}
    </div>
  )
}
