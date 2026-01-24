import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { userApi } from '../../services/userApi'

type PasswordInputProps = {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder: string
  error?: string
  showPassword?: boolean
  onToggleShowPassword?: () => void
}

function PasswordInput({
  label,
  value,
  onChange,
  placeholder,
  error,
  showPassword,
  onToggleShowPassword,
}: PasswordInputProps) {
  const isPasswordHidden = !showPassword

  return (
    <div className="space-y-2">
      <label className="text-[15px] font-medium">{label}</label>
      <div className="relative">
        <div className={`absolute left-4 top-1/2 -translate-y-1/2 ${error ? 'text-rose-500' : 'text-[#99A1AF]'}`}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
        </div>
        <input 
          type={isPasswordHidden ? "password" : "text"}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`w-full bg-[#1C1C1E] border rounded-xl h-[52px] pl-12 pr-12 text-white placeholder-[#585858] focus:outline-none transition-colors ${
            error ? 'border-rose-500 focus:border-rose-500' : 'border-[#2C2C2E] focus:border-[#5a4cf5]'
          }`}
        />
        {onToggleShowPassword && (
          <button 
            type="button"
            onClick={onToggleShowPassword}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-[#99A1AF]"
          >
            {showPassword ? (
               <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            ) : (
               <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            )}
          </button>
        )}
      </div>
      {error && (
        <p className="text-xs text-rose-500 pl-1">{error}</p>
      )}
    </div>
  )
}

export function SecuritySettingsPage() {
  const navigate = useNavigate()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  
  const [showCurrentPassword, setShowCurrentPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  
  // Validation errors
  const [errors, setErrors] = useState<{
    currentPassword?: string
    newPassword?: string
    confirmPassword?: string
  }>({})
  
  // General error for toast/alert - currently using alert() directly, so just logging for now or removing if unused.
  // const [globalError, setGlobalError] = useState<string | null>(null)

  const handleSave = async () => {
    // Reset errors
    setErrors({})
    // setGlobalError(null)

    if (!currentPassword || !newPassword || !confirmPassword) {
      alert('모든 항목을 입력해주세요.')
      return
    }

    try {
      const response = await userApi.changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      })
      
      if (response && response.status === 200) {
        alert(response.message || '비밀번호가 성공적으로 변경되었습니다.')
        navigate(-1)
      }
    } catch (error: unknown) {
      const apiError = error as { status: number; message: string; data: unknown }
      if (apiError && apiError.status) {
        // Validation Error
        if (apiError.status === 400) {
          if (apiError.data && typeof apiError.data === 'object' && apiError.data !== null) {
            // Field specific validation errors
             setErrors(apiError.data as { currentPassword?: string; newPassword?: string; confirmPassword?: string })
          } else {
             // General 400 error (e.g. mismatch confirm or same password) with message
             alert(apiError.message || '입력값이 올바르지 않습니다.')
          }
        } 
        // Unauthorized (Wrong current password)
        else if (apiError.status === 401) {
           alert(apiError.message || '비밀번호가 일치하지 않습니다.')
        }
        else {
           alert(apiError.message || '요청 처리에 실패했습니다.')
        }
      } else {
         console.error('Password change error:', error)
         alert('알 수 없는 오류가 발생했습니다.')
      }
    }
  }

  return (
    <div className="min-h-screen bg-black text-white px-4 pt-4 pb-[100px] flex flex-col">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate(-1)} className="p-1 -ml-1">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
        <div className="flex flex-col">
          <h1 className="text-xl font-bold leading-tight">보안 설정</h1>
          <p className="text-sm text-[#99A1AF]">비밀번호를 변경하세요</p>
        </div>
      </div>

      {/* Safe Password Tip */}
      <div className="bg-[#1C1C1E] border border-[#2C2C2E] rounded-xl p-4 mb-6 flex gap-3 items-start">
        <div className="w-10 h-10 rounded-full bg-[#0A84FF]/20 flex items-center justify-center shrink-0">
           <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0A84FF" strokeWidth="2">
             <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
           </svg>
        </div>
        <div>
          <h3 className="font-bold text-[15px] mb-1">안전한 비밀번호 만들기</h3>
          <p className="text-sm text-[#99A1AF] leading-snug">
            최소 8자 이상, 영문/숫자/특수문자를 조합하여 안전한 비밀번호를 만들어주세요.
          </p>
        </div>
      </div>

      {/* Form Fields */}
      <div className="space-y-6">
        
        {/* Current Password */}
        <PasswordInput
          label="현재 비밀번호"
          value={currentPassword}
          onChange={setCurrentPassword}
          placeholder="현재 비밀번호를 입력하세요"
          error={errors.currentPassword}
          showPassword={showCurrentPassword}
          onToggleShowPassword={() => setShowCurrentPassword((prev) => !prev)}
        />

        {/* New Password */}
        <PasswordInput
          label="새 비밀번호"
          value={newPassword}
          onChange={setNewPassword}
          placeholder="새 비밀번호를 입력하세요"
          error={errors.newPassword}
          showPassword={showNewPassword}
          onToggleShowPassword={() => setShowNewPassword((prev) => !prev)}
        />

        {/* Confirm Password */}
        <PasswordInput
          label="새 비밀번호 확인"
          value={confirmPassword}
          onChange={setConfirmPassword}
          placeholder="새 비밀번호를 다시 입력하세요"
          error={errors.confirmPassword}
          showPassword={showConfirmPassword}
          onToggleShowPassword={() => setShowConfirmPassword((prev) => !prev)}
        />

      </div>

      {/* Security Tips */}
      <div className="bg-[#1C1C1E] rounded-xl p-5 mt-8 border border-[#2C2C2E]">
        <div className="flex items-center gap-2 mb-3">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0A84FF" strokeWidth="2">
             <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          <span className="font-bold text-[15px]">보안 팁</span>
        </div>
        <ul className="space-y-2 text-sm text-[#99A1AF]">
          <li className="flex items-start gap-2">
            <span className="w-1 h-1 rounded-full bg-[#0A84FF] mt-2 shrink-0"></span>
            다른 사이트와 동일한 비밀번호를 사용하지 마세요
          </li>
          <li className="flex items-start gap-2">
            <span className="w-1 h-1 rounded-full bg-[#0A84FF] mt-2 shrink-0"></span>
            개인정보(이름, 생일 등)를 포함하지 마세요
          </li>
          <li className="flex items-start gap-2">
             <span className="w-1 h-1 rounded-full bg-[#0A84FF] mt-2 shrink-0"></span>
             정기적으로 비밀번호를 변경하세요
          </li>
        </ul>
      </div>

      {/* Buttons */}
      <div className="mt-auto pt-6 space-y-3">
        <button 
          onClick={handleSave}
          className="w-full h-[52px] bg-[#5a4cf5] hover:bg-[#4a3ce5] text-white font-bold rounded-xl transition-colors"
        >
          비밀번호 변경
        </button>
        <button 
          onClick={() => navigate(-1)}
          className="w-full h-[52px] bg-[#2C2C2E] hover:bg-[#3C3C3E] text-[#99A1AF] font-medium rounded-xl transition-colors"
        >
          취소
        </button>
      </div>
    </div>
  )
}

