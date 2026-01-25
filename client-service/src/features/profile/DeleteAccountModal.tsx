import { useState } from 'react'
import { userApi } from '../../services/userApi'

type DeleteAccountModalProps = {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}

export function DeleteAccountModal({ isOpen, onClose, onSuccess }: DeleteAccountModalProps) {
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!isOpen) return null

  const handleDelete = async () => {
    if (!password) {
      setError('비밀번호를 입력해주세요.')
      return
    }

    if (!window.confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      await userApi.deleteAccount(password)
      onSuccess()
    } catch (err: unknown) {
      let errorMessage = '계정 삭제에 실패했습니다.'
      
      if (err && typeof err === 'object' && 'status' in err) {
        const apiError = err as { status: number; message?: string; msg?: string; data?: { password?: string } }
        const message = apiError.message || apiError.msg

        switch (apiError.status) {
          case 400:
            errorMessage = apiError.data?.password || message || '잘못된 요청입니다.'
            break
          case 401:
            errorMessage = message || '비밀번호가 일치하지 않습니다.'
            break
          default:
            if (message) errorMessage = message
            break
        }
      }
      setError(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
      <div className="w-full max-w-[346px] bg-[#101828] border border-[#1e2939] rounded-[24px] p-6 relative overflow-hidden flex flex-col items-center">
        
        {/* Icon */}
        <div className="w-14 h-14 rounded-2xl bg-[#FB2C36]/10 flex items-center justify-center mb-6">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11.6667 3.5H16.3333M3.5 7H24.5M21.5833 7V22.1667C21.5833 22.7855 21.3375 23.379 20.8999 23.8166C20.4623 24.2542 19.8688 24.5 19.25 24.5H8.75C8.13116 24.5 7.53767 24.2542 7.10008 23.8166C6.6625 23.379 6.41667 22.7855 6.41667 22.1667V7H21.5833Z" stroke="#FB2C36" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M11.6667 11.6667V18.6667M16.3333 11.6667V18.6667" stroke="#FB2C36" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>

        {/* Text */}
        <h3 className="text-white text-[16px] font-medium leading-[24px] tracking-[-0.3px] mb-2 text-center">
          계정을 삭제하시겠습니까?
        </h3>
        <p className="text-[#99a1af] text-[14px] leading-[20px] tracking-[-0.15px] text-center mb-6">
          모든 데이터가 영구적으로 삭제되며<br/>복구할 수 없습니다.
        </p>

        {/* Password Input */}
        <div className="w-full mb-4">
          <label className="block text-[#d1d5dc] text-[13px] font-medium mb-1.5 ml-1">
            비밀번호 확인
          </label>
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => {
                setPassword(e.target.value)
                setError(null)
              }}
              placeholder="비밀번호를 입력하세요"
              disabled={isSubmitting}
              className={`w-full h-[48px] bg-[#1e2939] border ${error ? 'border-[#FB2C36]' : 'border-[#344054]'} rounded-[12px] pl-4 pr-10 text-white text-[15px] placeholder-[#667085] focus:outline-none focus:border-[#FB2C36] transition-colors`}
            />
             <button 
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#98A2B3] hover:text-white transition-colors"
            >
              {showPassword ? (
                 <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              ) : (
                 <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              )}
            </button>
          </div>
           {error && (
            <p className="text-[#FB2C36] text-[12px] mt-1.5 ml-1 animate-pulse">
              {error}
            </p>
          )}
        </div>

        {/* Buttons */}
        <div className="w-full flex flex-col gap-3">
          <button
            onClick={handleDelete}
            disabled={isSubmitting}
            className="w-full h-[48px] bg-[#FB2C36] hover:bg-[#D92029] disabled:opacity-50 disabled:cursor-not-allowed rounded-[16px] text-white text-[16px] font-medium tracking-[-0.3px] transition-colors flex items-center justify-center"
          >
             {isSubmitting ? (
               <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
             ) : '삭제하기'}
          </button>
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="w-full h-[48px] bg-[#1e2939] hover:bg-[#2C3849] rounded-[16px] text-[#d1d5dc] text-[16px] font-medium tracking-[-0.3px] transition-colors"
          >
            취소
          </button>
        </div>

      </div>
    </div>
  )
}
