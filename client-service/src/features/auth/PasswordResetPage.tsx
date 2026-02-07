import { useState, useMemo, type FormEvent, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { ArrowLeftIcon, MailIcon } from './components/AuthIcons'
import { LoginInput } from './components/LoginInput'
import { VerificationInput } from './components/VerificationInput'
import { authApi } from '../../services/authApi'

export function PasswordResetPage() {
  const [step, setStep] = useState<1 | 2 | 3>(1)
  const [email, setEmail] = useState('')
  const [verificationCode, setVerificationCode] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState<'idle' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')
  
  // Timer state
  const [timeLeft, setTimeLeft] = useState(180) // 3 minutes
  const [isTimerActive, setIsTimerActive] = useState(false)
  const navigate = useNavigate()

  const isEmailValid = useMemo(() => /\S+@\S+\.\S+/.test(email), [email])
  const isPasswordValid = useMemo(() => password.length >= 8, [password])
  const isPasswordMatch = useMemo(() => password === confirmPassword, [password, confirmPassword])

  // Timer logic
  useEffect(() => {
    let interval: ReturnType<typeof setInterval>
    if (isTimerActive && timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((prev) => prev - 1)
      }, 1000)
    } else if (timeLeft === 0) {
      setIsTimerActive(false)
    }
    return () => clearInterval(interval)
  }, [isTimerActive, timeLeft])

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m}:${s.toString().padStart(2, '0')}`
  }

  const handleSendCode = async (e?: FormEvent) => {
    e?.preventDefault()
    if (!isEmailValid) return

    setIsLoading(true)
    setFeedback('idle')
    setMessage('')

    try {
      await authApi.sendVerificationCode(email)
      
      if (step === 2) {
        toast.success('인증번호가 재발송되었습니다.')
      }

      setStep(2)
      setTimeLeft(180)
      setIsTimerActive(true)
      setFeedback('success')
      setMessage('')
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage(error instanceof Error ? error.message : '인증번호 전송에 실패했습니다. 다시 시도해주세요.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleVerifyCode = async (e: FormEvent) => {
    e.preventDefault()
    if (verificationCode.length !== 6) return

    setIsLoading(true)
    setFeedback('idle')
    setMessage('')

    try {
      const response = await authApi.confirmVerificationCode(email, verificationCode)
      
      if (response.data.status === 'VERIFIED') {
        setStep(3)
        setFeedback('idle')
        setMessage('')
        setIsTimerActive(false)
      } else {
        setFeedback('error')
        setMessage(response.message || '인증번호가 일치하지 않습니다.')
      }
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage(error instanceof Error ? error.message : '인증번호 확인에 실패했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResetPassword = async (e: FormEvent) => {
    e.preventDefault()
    if (!isPasswordValid || !isPasswordMatch) return

    setIsLoading(true)
    setFeedback('idle')
    setMessage('')

    try {
      await authApi.resetPassword({ 
        email, 
        newPassword: password, 
        confirmPassword 
      })

      toast.success('비밀번호가 성공적으로 변경되었습니다.')
      navigate('/login')
    } catch (error) {
      console.error(error)
      setFeedback('error')
      
      let errorMsg = '비밀번호 변경에 실패했습니다. 다시 시도해주세요.'
      const err = error as { response?: { status: number; data?: { message?: string } } }

      if (err?.response?.status === 400) {
        const message = err.response.data?.message || ''
        if (message.includes('동일한 비밀번호')) {
          errorMsg = '현재 비밀번호와 다른 새 비밀번호를 입력해주세요.'
        } else if (message.includes('일치하지 않습니다')) {
          errorMsg = '비밀번호가 일치하지 않습니다.'
        } else if (message) {
          errorMsg = message
        }
      } else if (error instanceof Error) {
        errorMsg = error.message
      }

      setMessage(errorMsg)
    } finally {
      setIsLoading(false)
    }
  }

  const handleBack = () => {
    if (step === 2) {
      setStep(1)
      setFeedback('idle')
      setMessage('')
      setVerificationCode('')
      setIsTimerActive(false)
    } else if (step === 3) {
       setStep(2)
       setFeedback('idle')
       setMessage('')
    }
  }

  return (
    <div className="min-h-screen bg-black flex justify-center items-center relative overflow-hidden font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif] py-10">
      {/* Background Decor */}
      <div className="absolute top-[-100px] left-[-100px] w-[600px] h-[600px] bg-[rgba(173,70,255,0.1)] rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-100px] right-[-100px] w-[600px] h-[600px] bg-[rgba(43,127,255,0.1)] rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-[393px] px-6 relative z-10 flex flex-col gap-10">
        
        {/* Header */}
        <div className="flex flex-col gap-6">
          {step === 1 ? (
            <Link 
              to="/login" 
              className="self-start inline-flex items-center gap-1.5 text-[#94A3B8] hover:text-white transition-colors text-[16px] font-normal no-underline"
            >
              <ArrowLeftIcon />
              <span>돌아가기</span>
            </Link>
          ) : (
            <button 
              onClick={handleBack}
              className="self-start inline-flex items-center gap-1.5 text-[#94A3B8] hover:text-white transition-colors text-[16px] font-normal cursor-pointer bg-transparent border-none p-0"
            >
              <ArrowLeftIcon />
              <span>돌아가기</span>
            </button>
          )}

          <div className="flex flex-col gap-2">
             {/* Step Indicator */}
             <div className="flex items-center gap-2 mb-2">
                <div className={`w-2 h-2 rounded-full transition-all duration-300 ${step === 1 ? 'w-8 bg-[#2B7FFF]' : 'bg-[#2B7FFF]'}`}></div>
                <div className={`w-2 h-2 rounded-full transition-all duration-300 ${step === 2 ? 'w-8 bg-[#2B7FFF]' : (step > 2 ? 'bg-[#2B7FFF]' : 'bg-[#334155]')}`}></div>
                <div className={`w-2 h-2 rounded-full transition-all duration-300 ${step === 3 ? 'w-8 bg-[#2B7FFF]' : 'bg-[#334155]'}`}></div>
             </div>
            
            {step === 1 && (
              <>
                <h1 className="text-[28px] font-bold text-white tracking-[-0.02em] leading-tight">
                  비밀번호를<br/>잊으셨나요?
                </h1>
                <p className="text-[15px] text-[#94A3B8] tracking-[-0.01em]">
                  가입하신 이메일을 입력하시면<br/>
                  비밀번호 재설정 인증번호를 보내드려요.
                </p>
              </>
            )}

            {step === 2 && (
              <>
                <h1 className="text-[28px] font-bold text-white tracking-[-0.02em] leading-tight">
                  인증번호를<br/>발송했어요
                </h1>
                <p className="text-[15px] text-[#94A3B8] tracking-[-0.01em]">
                  <span className="text-white font-medium">{email}</span>으로<br/>
                  발송된 인증번호 6자리를 입력해주세요.
                   {isTimerActive && (
                    <span className="text-[#2B7FFF] font-medium ml-2">
                       {formatTime(timeLeft)}
                    </span>
                  )}
                </p>
              </>
            )}

            {step === 3 && (
               <>
                <h1 className="text-[28px] font-bold text-white tracking-[-0.02em] leading-tight">
                  비밀번호를<br/>재설정해주세요
                </h1>
                <p className="text-[15px] text-[#94A3B8] tracking-[-0.01em]">
                  새로운 비밀번호를 입력해주세요.
                </p>
              </>
            )}
          </div>
        </div>

        {/* Form Steps */}
        {step === 1 && (
          <form className="flex flex-col gap-8" onSubmit={handleSendCode}>
            <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">이메일</span>
              <LoginInput
                icon={<MailIcon />}
                type="email"
                placeholder="example@email.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                disabled={isLoading}
                required
              />
            </label>

            {feedback !== 'idle' && (
              <p className={`text-center text-[14px] ${feedback === 'error' ? 'text-red-400' : 'text-emerald-400'}`}>
                {message}
              </p>
            )}

            <button
              className="w-full h-[56px] rounded-[16px] text-white text-[16px] font-bold tracking-tight transition-transform active:scale-[0.98] cursor-pointer border-none shadow-[0_4px_14px_0_rgba(43,127,255,0.4)] disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              disabled={!isEmailValid || isLoading}
              style={{ background: 'linear-gradient(90deg, #2B7FFF 0%, #9810FA 100%)' }}
            >
              {isLoading ? '전송 중...' : '인증번호 받기'}
            </button>
          </form>
        )}

        {step === 2 && (
          <form className="flex flex-col gap-8" onSubmit={handleVerifyCode}>
             <label className="flex flex-col gap-3">
              <span className="text-[14px] font-semibold text-white ml-1">인증번호</span>
              <VerificationInput
                value={verificationCode}
                onChange={(val) => setVerificationCode(val)}
                disabled={isLoading}
              />
            </label>

            <button
              type="button"
              onClick={() => handleSendCode()}
              disabled={isLoading || isTimerActive}
              className="text-[#94A3B8] text-[14px] underline underline-offset-4 hover:text-white transition-colors bg-transparent border-none cursor-pointer self-center"
            >
              인증번호 재발송
            </button>

            {feedback !== 'idle' && (
              <p className={`text-center text-[14px] ${feedback === 'error' ? 'text-red-400' : 'text-emerald-400'}`}>
                {message}
              </p>
            )}

            <button
              className="w-full h-[56px] rounded-[16px] text-white text-[16px] font-bold tracking-tight transition-transform active:scale-[0.98] cursor-pointer border-none shadow-[0_4px_14px_0_rgba(43,127,255,0.4)] disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              disabled={verificationCode.length !== 6 || isLoading}
              style={{ background: 'linear-gradient(90deg, #2B7FFF 0%, #9810FA 100%)' }}
            >
              {isLoading ? '확인 중...' : '인증하기'}
            </button>
          </form>
        )}

        {step === 3 && (
          <form className="flex flex-col gap-8" onSubmit={handleResetPassword}>
             <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">새 비밀번호</span>
              <LoginInput
                type="password"
                placeholder="영문, 숫자, 특수문자 포함 8자 이상"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={isLoading}
                required
              />
            </label>

             <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">비밀번호 확인</span>
              <LoginInput
                type="password"
                placeholder="비밀번호를 다시 입력해주세요"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                disabled={isLoading}
                required
              />
               {!isPasswordMatch && confirmPassword.length > 0 && (
                <span className="text-red-400 text-[12px] ml-1">비밀번호가 일치하지 않습니다.</span>
              )}
            </label>

            {feedback !== 'idle' && (
              <p className={`text-center text-[14px] ${feedback === 'error' ? 'text-red-400' : 'text-emerald-400'}`}>
                {message}
              </p>
            )}

            <button
              className="w-full h-[56px] rounded-[16px] text-white text-[16px] font-bold tracking-tight transition-transform active:scale-[0.98] cursor-pointer border-none shadow-[0_4px_14px_0_rgba(43,127,255,0.4)] disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              disabled={!isPasswordValid || !isPasswordMatch || isLoading}
              style={{ background: 'linear-gradient(90deg, #2B7FFF 0%, #9810FA 100%)' }}
            >
              {isLoading ? '변경 중...' : '완료'}
            </button>
          </form>
        )}

      </div>
    </div>
  )
}
