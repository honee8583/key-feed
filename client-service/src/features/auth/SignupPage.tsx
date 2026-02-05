import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import { LoginInput } from './components/LoginInput'
import { CheckIcon, MailIcon, UserIcon } from './components/AuthIcons'
import logo from '../../assets/logo.png'

type AgreementKey = 'terms' | 'privacy' | 'marketing'

const AGREEMENT_ITEMS: { key: AgreementKey; label: string; required: boolean }[] = [
  { key: 'terms', label: '(필수) 이용약관 동의', required: true },
  { key: 'privacy', label: '(필수) 개인정보 처리방침 동의', required: true },
  { key: 'marketing', label: '(선택) 마케팅 정보 수신 동의', required: false },
]

export function SignupPage() {
  const navigate = useNavigate()
  const redirectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [agreements, setAgreements] = useState({
    all: false,
    terms: false,
    privacy: false,
    marketing: false,
  })
  const [verificationCode, setVerificationCode] = useState('')
  const [isVerificationSent, setIsVerificationSent] = useState(false)
  const [isEmailVerified, setIsEmailVerified] = useState(false)
  const [isVerifyingCode, setIsVerifyingCode] = useState(false)

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSendingCode, setIsSendingCode] = useState(false)
  const [feedback, setFeedback] = useState<'idle' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) {
        clearTimeout(redirectTimerRef.current)
      }
    }
  }, [])

  const isEmailValid = useMemo(() => /\S+@\S+\.\S+/.test(email), [email])
  const passwordsMatch = password.trim().length >= 8 && password === confirmPassword
  const requiredAccepted = agreements.terms && agreements.privacy

  const isFormValid = useMemo(
    () =>
      name.trim().length > 0 &&
      isEmailValid &&
      isEmailVerified && // 이메일 인증 필수
      password.trim().length >= 8 &&
      passwordsMatch &&
      requiredAccepted,
    [name, isEmailValid, isEmailVerified, password, passwordsMatch, requiredAccepted],
  )

  const handleSendVerification = async () => {
    if (!isEmailValid) {
      setFeedback('error')
      setMessage('유효한 이메일 주소를 입력해주세요.')
      return
    }

    setIsSendingCode(true)
    setFeedback('idle')
    setMessage('')
    try {
      await authApi.sendVerificationCode(email)
      setIsVerificationSent(true) // 인증번호 전송 상태 활성화
      setFeedback('success')
      setMessage('인증번호를 전송했어요. 메일함을 확인해주세요.')
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage('인증번호 전송에 실패했어요. 잠시 후 다시 시도해주세요.')
    } finally {
      setIsSendingCode(false)
    }
  }

  const handleVerifyCode = async () => {
    if (verificationCode.length !== 6) return

    setIsVerifyingCode(true)
    setFeedback('idle')
    setMessage('')
    try {
      await authApi.confirmVerificationCode(email, verificationCode)
      setIsEmailVerified(true) // 이메일 인증 완료
      setFeedback('success')
      setMessage('이메일 인증이 완료되었어요.')
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage('인증번호가 올바르지 않거나 만료되었어요.')
    } finally {
      setIsVerifyingCode(false)
    }
  }

  const handleAgreementChange = (key: AgreementKey, checked: boolean) => {
    setAgreements((prev) => {
      const next = { ...prev, [key]: checked }
      return { ...next, all: next.terms && next.privacy && next.marketing }
    })
  }

  const handleAllAgreements = (checked: boolean) => {
    setAgreements({
      all: checked,
      terms: checked,
      privacy: checked,
      marketing: checked,
    })
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!isFormValid) return

    setIsSubmitting(true)
    setFeedback('idle')
    setMessage('')

    try {
      await authApi.signup({
        name: name.trim(),
        email: email.trim(),
        password,
        marketingOptIn: agreements.marketing,
      })
      setFeedback('success')
      setMessage('가입이 완료되었어요! 로그인 화면으로 이동합니다.')
      redirectTimerRef.current = setTimeout(() => {
        navigate('/login')
      }, 1500)
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage('가입에 실패했어요. 입력 정보를 다시 확인해주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-black flex justify-center items-center relative overflow-hidden font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif] py-10">
      {/* Background Decor */}
      <div className="absolute top-[-100px] left-[-100px] w-[600px] h-[600px] bg-[rgba(173,70,255,0.1)] rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-100px] right-[-100px] w-[600px] h-[600px] bg-[rgba(43,127,255,0.1)] rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-[393px] px-6 relative z-10 flex flex-col gap-8">
        {/* Header */}
        <div className="flex flex-col items-center">
             <div className="w-[80px] h-[80px] rounded-[24px] flex items-center justify-center mb-6 overflow-hidden shadow-[0px_10px_15px_0px_rgba(0,0,0,0.5)]">
               <img src={logo} alt="KeyFeed Logo" className="w-full h-full object-cover" />
             </div>
          <h1 className="text-[28px] font-bold text-white tracking-[-0.02em] mb-2">Create Account</h1>
          <p className="text-[15px] text-[#94A3B8] tracking-[-0.01em]">정보의 홍수 속에서 진짜 필요한 것만</p>
        </div>

        {/* Form */}
        <form className="flex flex-col gap-6" onSubmit={handleSubmit}>
          <div className="flex flex-col gap-4">
            {/* Name */}
            <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">이름</span>
              <LoginInput
                icon={<UserIcon />}
                type="text"
                placeholder="이름을 입력하세요"
                value={name}
                onChange={(event) => setName(event.target.value)}
                disabled={isSubmitting}
                required
              />
            </label>

            {/* Email */}
            <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">이메일</span>
              <div className="flex gap-2">
                <div className="flex-1">
                  <LoginInput
                    icon={<MailIcon />}
                    type="email"
                    placeholder="이메일"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    disabled={isSubmitting || isEmailVerified}
                    required
                  />
                </div>
                <button
                  type="button"
                  onClick={handleSendVerification}
                  disabled={isSendingCode || !isEmailValid || isEmailVerified}
                  className={`h-[52px] px-4 rounded-[16px] border border-white/10 text-[13px] font-medium whitespace-nowrap transition-colors cursor-pointer ${
                    isEmailVerified 
                      ? 'bg-green-500/10 text-green-500 border-green-500/20' 
                      : 'bg-[#1E293B] text-[#94A3B8] hover:text-white hover:bg-[#2A3649] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-[#1E293B] disabled:hover:text-[#94A3B8]'
                  }`}
                >
                  {isEmailVerified ? '인증됨' : (isSendingCode ? '전송중' : '인증요청')}
                </button>
              </div>
            </label>

            {/* Verification Code */}
            {isVerificationSent && !isEmailVerified && (
              <div className="flex gap-2 animate-fade-in-up">
                <div className="flex-1">
                  <LoginInput
                    type="text"
                    placeholder="인증번호 6자리"
                    value={verificationCode}
                    onChange={(event) => setVerificationCode(event.target.value.replace(/[^0-9]/g, '').slice(0, 6))}
                    className="text-center tracking-widest pl-4"
                    required
                  />
                </div>
                <button
                  type="button"
                  onClick={handleVerifyCode}
                  disabled={verificationCode.length !== 6 || isVerifyingCode}
                  className="h-[52px] px-4 rounded-[16px] bg-[#1E293B] border border-white/10 text-[13px] font-medium text-[#94A3B8] whitespace-nowrap hover:bg-[#2A3649] hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
                >
                  {isVerifyingCode ? '확인중' : '확인'}
                </button>
              </div>
            )}

            {/* Password */}
            <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">비밀번호</span>
              <LoginInput
                type="password"
                placeholder="비밀번호 (8자 이상)"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                 disabled={isSubmitting}
                minLength={8}
                required
              />
              <LoginInput
                type="password"
                placeholder="비밀번호 확인"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                 disabled={isSubmitting}
                minLength={8}
                required
              />
            </label>
          </div>

          {/* Agreements */}
          <div className="bg-[#1E293B] border border-white/5 rounded-[20px] p-5 flex flex-col gap-4">
            <label className="flex items-center gap-3 cursor-pointer group select-none">
              <div className={`w-5 h-5 rounded-md border flex items-center justify-center transition-all ${agreements.all ? 'bg-[#2B7FFF] border-[#2B7FFF]' : 'border-[#475569] group-hover:border-[#64748B]'}`}>
                {agreements.all && <CheckIcon />}
              </div>
              <input
                type="checkbox"
                checked={agreements.all}
                onChange={(event) => handleAllAgreements(event.target.checked)}
                className="hidden"
              />
              <span className="text-[14px] font-semibold text-white">전체 동의</span>
            </label>
            
            <div className="flex flex-col gap-3 pl-1">
              {AGREEMENT_ITEMS.map(({ key, label }) => (
                <div key={key} className="flex items-center justify-between text-[13px]">
                  <label className="flex items-center gap-3 cursor-pointer group flex-1 select-none">
                    <div className={`w-4 h-4 rounded border flex items-center justify-center transition-all flex-shrink-0 ${agreements[key] ? 'bg-[#2B7FFF] border-[#2B7FFF]' : 'border-[#334155] group-hover:border-[#475569]'}`}>
                      {agreements[key] && <CheckIcon size={10} />}
                    </div>
                    <input
                      type="checkbox"
                      checked={agreements[key]}
                      onChange={(event) => handleAgreementChange(key, event.target.checked)}
                      className="hidden"
                    />
                    <span className="text-[#94A3B8] group-hover:text-[#CBD5E1] transition-colors">{label}</span>
                  </label>
                  <button type="button" className="text-[#64748B] hover:text-[#94A3B8] text-xs underline underline-offset-2 bg-transparent border-none cursor-pointer p-0">보기</button>
                </div>
              ))}
            </div>
          </div>

          {feedback !== 'idle' && (
            <div className={`p-4 rounded-[16px] text-[13px] font-medium text-center ${
              feedback === 'error' 
                ? 'bg-red-500/10 text-red-400 border border-red-500/20' 
                : 'bg-green-500/10 text-green-400 border border-green-500/20'
            }`}>
              {message}
            </div>
          )}

          <button
            className="w-full h-[56px] rounded-[16px] text-white text-[16px] font-bold tracking-tight transition-transform active:scale-[0.98] cursor-pointer border-none shadow-[0_4px_14px_0_rgba(43,127,255,0.4)] disabled:opacity-50 disabled:cursor-not-allowed"
            type="submit"
            disabled={!isFormValid || isSubmitting}
            style={{ background: 'linear-gradient(90deg, #2B7FFF 0%, #9810FA 100%)' }}
          >
            {isSubmitting ? '가입하는 중...' : '회원가입'}
          </button>
        </form>

        <div className="flex justify-center gap-2 text-[14px] text-[#64748B]">
          <span>이미 계정이 있으신가요?</span>
          <Link 
            to="/login" 
            className="font-semibold bg-clip-text text-transparent bg-gradient-to-r from-[#2B7FFF] to-[#9810FA] hover:opacity-80 transition-opacity"
          >
            로그인
          </Link>
        </div>
      </div>
    </div>
  )
}
