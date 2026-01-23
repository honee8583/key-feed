import { useEffect, useMemo, useRef, useState, type CSSProperties, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import type { SocialProvider } from '../../services/authApi'
import { AuthInput } from './components/AuthInput'
import { AuthPasswordInput } from './components/AuthPasswordInput'
import { ArrowLeftIcon, CheckIcon, GoogleIcon, MailIcon, UserIcon } from './components/AuthIcons'
import logo from '../../assets/logo.png'

type AgreementKey = 'terms' | 'privacy' | 'marketing'

type SocialButtonConfig = {
  id: SocialProvider
  label: string
  background: string
  border: string
  textColor: string
  iconBackground: string
  iconColor: string
  icon: ReactNode
}

const SOCIAL_BUTTONS: SocialButtonConfig[] = [
  {
    id: 'kakao',
    label: '카카오로 계속하기',
    background: '#fee500',
    border: 'rgba(253,199,0,0.3)',
    textColor: '#101828',
    iconBackground: '#3c1e1e',
    iconColor: '#fee500',
    icon: 'K',
  },
  {
    id: 'naver',
    label: '네이버로 계속하기',
    background: '#03c75a',
    border: 'rgba(0,201,80,0.3)',
    textColor: '#ffffff',
    iconBackground: '#ffffff',
    iconColor: '#03c75a',
    icon: 'N',
  },
  {
    id: 'google',
    label: '구글로 계속하기',
    background: '#ffffff',
    border: 'rgba(255,255,255,0.2)',
    textColor: '#101828',
    iconBackground: '#ffffff',
    iconColor: '#101828',
    icon: <GoogleIcon />,
  },
]

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

  const handleSocialLogin = async (provider: SocialProvider) => {
    setIsSubmitting(true)
    setFeedback('idle')
    setMessage('')

    try {
      await authApi.loginWithProvider(provider)
      setFeedback('success')
      setMessage(`${provider.toUpperCase()} 인증 페이지로 이동합니다.`)
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage('간편 가입이 실패했어요. 다시 시도해주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen w-full bg-black flex flex-col items-center justify-center p-5 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[400px] flex flex-col gap-8">
        {/* Header */}
        <header className="flex flex-col items-center gap-6">
          <Link 
            to="/login" 
            className="self-start inline-flex items-center gap-1.5 text-slate-400 hover:text-white transition-colors text-[15px] font-medium no-underline"
          >
            <ArrowLeftIcon />
            <span>돌아가기</span>
          </Link>
          
          <div className="flex flex-col items-center gap-3 text-center">
            <div className="w-[72px] h-[72px] rounded-[24px] bg-[#111] border border-white/10 flex items-center justify-center mb-1 overflow-hidden shadow-2xl shadow-black/50">
               <img src={logo} alt="KeyFeed Logo" className="w-full h-full object-cover opacity-90" />
            </div>
            <div className="flex flex-col gap-1">
              <h1 className="m-0 text-2xl font-bold text-white tracking-tight">회원가입</h1>
              <p className="m-0 text-[15px] text-slate-400 font-normal">정보의 홍수 속에서 진짜 필요한 것만</p>
            </div>
          </div>
        </header>

        {/* Main Card */}
        <main className="flex flex-col gap-6">
          {/* Social Login */}
          <div className="flex flex-col gap-3">
            {SOCIAL_BUTTONS.map(
              ({ id, label, background, border, textColor, iconBackground, iconColor, icon }) => (
                <button
                  key={id}
                  type="button"
                  className="relative h-[52px] flex items-center justify-center gap-3 rounded-[16px] text-[15px] font-medium transition-all duration-200 hover:opacity-90 active:scale-[0.98] cursor-pointer"
                  style={{ 
                    background, 
                    color: textColor,
                    border: '1px solid ' + border 
                  } as CSSProperties}
                  onClick={() => handleSocialLogin(id)}
                  disabled={isSubmitting}
                >
                  <span
                    className="absolute left-4 inline-flex items-center justify-center w-6 h-6 rounded-full text-[10px]"
                    style={{ background: iconBackground, color: iconColor } as CSSProperties}
                    aria-hidden
                  >
                    {icon}
                  </span>
                  <span>{label}</span>
                </button>
              ),
            )}
          </div>

          <div className="relative flex items-center gap-4 py-2">
            <div className="flex-1 h-[1px] bg-white/10"></div>
            <span className="text-[13px] text-slate-500 font-medium">또는 이메일로 가입</span>
            <div className="flex-1 h-[1px] bg-white/10"></div>
          </div>

          {/* Email Form */}
          <form className="flex flex-col gap-5" onSubmit={handleSubmit}>
            <div className="flex flex-col gap-4">
              <AuthInput
                icon={<UserIcon />}
                type="text"
                placeholder="이름"
                value={name}
                onChange={(event) => setName(event.target.value)}
                disabled={isSubmitting}
                required
              />

              <div className="flex gap-2">
                <div className="flex-1">
                  <AuthInput
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
                  className={`h-[52px] px-4 rounded-[16px] border border-white/10 text-[13px] font-medium whitespace-nowrap transition-colors cursor-pointer ${isEmailVerified ? 'bg-green-500/10 text-green-500 border-green-500/20' : 'bg-[#111] text-slate-300 hover:bg-[#161616] hover:text-white'}`}
                >
                  {isEmailVerified ? '인증됨' : (isSendingCode ? '전송중' : '인증요청')}
                </button>
              </div>

              {/* Verification Code Input */}
              {isVerificationSent && !isEmailVerified && (
                <div className="flex gap-2 animate-fade-in-up">
                  <div className="flex-1">
                    <AuthInput
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
                    className="h-[52px] px-4 rounded-[16px] bg-[#111] border border-white/10 text-[13px] font-medium text-slate-300 whitespace-nowrap hover:bg-[#161616] hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
                  >
                    {isVerifyingCode ? '확인중' : '확인'}
                  </button>
                </div>
              )}

              <AuthPasswordInput
                placeholder="비밀번호 (8자 이상)"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={isSubmitting}
                minLength={8}
                required
              />

              <AuthPasswordInput
                placeholder="비밀번호 확인"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                disabled={isSubmitting}
                minLength={8}
                required
              />
            </div>

            {/* Agreements */}
            <div className="bg-[#111] border border-white/5 rounded-[20px] p-5 flex flex-col gap-4 mt-2">
              <label className="flex items-center gap-3 cursor-pointer group select-none">
                  <div className={`w-5 h-5 rounded-md border flex items-center justify-center transition-all ${agreements.all ? 'bg-blue-600 border-blue-600' : 'border-slate-600 group-hover:border-slate-500'}`}>
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
                      <div className={`w-4 h-4 rounded border flex items-center justify-center transition-all flex-shrink-0 ${agreements[key] ? 'bg-blue-600 border-blue-600' : 'border-slate-700 group-hover:border-slate-600'}`}>
                        {agreements[key] && <CheckIcon size={10} />}
                      </div>
                      <input
                        type="checkbox"
                        checked={agreements[key]}
                        onChange={(event) => handleAgreementChange(key, event.target.checked)}
                        className="hidden"
                      />
                      <span className="text-slate-400 group-hover:text-slate-300 transition-colors">{label}</span>
                    </label>
                    <button type="button" className="text-slate-500 hover:text-slate-400 text-xs underline underline-offset-2 bg-transparent border-none cursor-pointer p-0">보기</button>
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
              className="w-full h-[56px] rounded-[16px] bg-white text-black text-[16px] font-bold tracking-tight hover:bg-slate-200 disabled:opacity-50 disabled:hover:bg-white transition-all mt-2 active:scale-[0.98] cursor-pointer border-none"
              type="submit"
              disabled={!isFormValid || isSubmitting}
            >
              {isSubmitting ? '가입하는 중...' : '회원가입'}
            </button>
          </form>

          <div className="flex justify-center gap-2 text-[14px] text-slate-500">
            <span>이미 계정이 있으신가요?</span>
            <Link to="/login" className="text-white hover:underline font-medium">
              로그인
            </Link>
          </div>
        </main>
      </div>
    </div>
  )
}
