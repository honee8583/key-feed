import { useMemo, useState, type CSSProperties, type FormEvent, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import type { SocialProvider } from '../../services/authApi'

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
    border: 'rgba(15,23,42,0.08)',
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
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [agreements, setAgreements] = useState({
    all: false,
    terms: false,
    privacy: false,
    marketing: false,
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSendingCode, setIsSendingCode] = useState(false)
  const [feedback, setFeedback] = useState<'idle' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')

  const isEmailValid = useMemo(() => /\S+@\S+\.\S+/.test(email), [email])
  const passwordsMatch = password.trim().length >= 8 && password === confirmPassword
  const requiredAccepted = agreements.terms && agreements.privacy

  const isFormValid = useMemo(
    () =>
      name.trim().length > 0 &&
      isEmailValid &&
      password.trim().length >= 8 &&
      passwordsMatch &&
      requiredAccepted,
    [name, isEmailValid, password, passwordsMatch, requiredAccepted],
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
      setMessage('가입이 완료되었어요! 로그인 화면으로 이동해주세요.')
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
    <div className="relative min-h-screen py-6 px-4 pb-12 bg-gradient-to-b from-[#7b3fe4] via-[#4267ff] to-[#021651] flex justify-center overflow-hidden min-[768px]:pt-12">
      <div
        className="absolute w-[420px] h-[420px] rounded-full blur-[140px] opacity-35 pointer-events-none top-[-140px] left-[-120px] bg-white/65"
        aria-hidden
      />
      <div
        className="absolute w-[420px] h-[420px] rounded-full blur-[140px] opacity-35 pointer-events-none bottom-[-140px] right-[-120px] bg-[rgba(194,122,255,0.7)]"
        aria-hidden
      />
      <div className="relative w-full max-w-[420px] flex flex-col gap-6 text-white z-10">
        <header className="flex flex-col gap-5 mt-2">
          <Link to="/login" className="inline-flex items-center gap-1.5 w-fit text-white/95 no-underline text-[15px] hover:opacity-80">
            <ArrowLeftIcon />
            <span>돌아가기</span>
          </Link>
          <div className="flex flex-col items-center gap-4 text-center">
            <div
              className="w-20 h-20 rounded-3xl bg-white/20 border border-white/35 flex items-center justify-center text-4xl shadow-[0_10px_15px_rgba(15,23,42,0.1)]"
              aria-hidden
            >
              <span>📰</span>
            </div>
            <div>
              <h1 className="m-0 text-2xl font-bold tracking-[-0.4px] text-white">회원가입</h1>
              <p className="m-0 text-sm text-[rgba(239,246,255,0.9)]">정보의 홍수 속에서 진짜 필요한 것만</p>
            </div>
          </div>
        </header>

        <section
          className="bg-white/96 rounded-t-[32px] rounded-b-3xl p-8 px-6 pb-7 shadow-[0_25px_50px_-12px_rgba(15,23,42,0.25)] text-[#101828] flex flex-col gap-5 min-[768px]:rounded-[40px]"
          aria-label="회원가입 폼"
        >
          <div className="text-center text-sm text-[#475467]">간편 회원가입</div>
          <div className="flex flex-col gap-3">
            {SOCIAL_BUTTONS.map(
              ({ id, label, background, border, textColor, iconBackground, iconColor, icon }) => (
                <button
                  key={id}
                  type="button"
                  className="h-12 flex items-center justify-center gap-3 rounded-xl border-none text-sm font-normal tracking-[-0.31px] cursor-pointer transition-opacity duration-200 disabled:opacity-60 disabled:cursor-not-allowed hover:opacity-90"
                  style={{ background, borderColor: border, color: textColor } as CSSProperties}
                  onClick={() => handleSocialLogin(id)}
                  disabled={isSubmitting}
                >
                  <span
                    className="inline-flex items-center justify-center w-6 h-6 rounded-[10px] text-xs font-normal"
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

          <div className="my-2 relative text-center h-5">
            <div className="absolute top-1/2 left-0 right-0 h-[1.5px] bg-[#1e2939] -translate-y-1/2" />
            <span className="relative inline-block px-4 bg-white/96 text-sm text-[#6a7282]">또는 이메일로 가입</span>
          </div>

          <form className="flex flex-col gap-[18px]" onSubmit={handleSubmit}>
            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">이름</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <UserIcon />
                <input
                  type="text"
                  placeholder="홍길동"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  disabled={isSubmitting}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
              </div>
            </label>

            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">이메일</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <MailIcon />
                <input
                  type="email"
                  placeholder="example@email.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  disabled={isSubmitting}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
              </div>
            </label>

            <button
              type="button"
              className="h-10 rounded-xl border border-slate-400/50 bg-[#f8f9fb] text-[#334155] font-semibold cursor-pointer transition-colors duration-200 disabled:opacity-60 disabled:cursor-not-allowed hover:bg-slate-100"
              onClick={handleSendVerification}
              disabled={isSendingCode || !isEmailValid}
            >
              {isSendingCode ? '발송 중...' : '인증번호 발송'}
            </button>

            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">비밀번호</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <LockIcon />
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="8자 이상 입력하세요"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  disabled={isSubmitting}
                  minLength={8}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
                <button
                  type="button"
                  className="absolute right-4 top-1/2 -translate-y-1/2 border-none bg-transparent p-0 flex items-center justify-center cursor-pointer text-[#6a7282] hover:text-[#99a1af]"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
            </label>

            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">비밀번호 확인</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <LockIcon />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 다시 입력하세요"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  disabled={isSubmitting}
                  minLength={8}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
                <button
                  type="button"
                  className="absolute right-4 top-1/2 -translate-y-1/2 border-none bg-transparent p-0 flex items-center justify-center cursor-pointer text-[#6a7282] hover:text-[#99a1af]"
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  aria-label={showConfirmPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
                >
                  {showConfirmPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
            </label>

            <div className="border border-slate-400/35 rounded-2xl p-4 flex flex-col gap-3 bg-[#fdfdff]">
              <label className="flex items-center gap-2.5 font-semibold text-[#111827]">
                <input
                  type="checkbox"
                  checked={agreements.all}
                  onChange={(event) => handleAllAgreements(event.target.checked)}
                  disabled={isSubmitting}
                  className="w-4 h-4"
                />
                <span>전체 동의</span>
              </label>
              <div className="flex flex-col gap-2.5">
                {AGREEMENT_ITEMS.map(({ key, label, required }) => (
                  <div key={key} className="flex items-center justify-between gap-2 text-sm text-[#475467]">
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={agreements[key]}
                        onChange={(event) => handleAgreementChange(key, event.target.checked)}
                        disabled={isSubmitting}
                        className="w-4 h-4 accent-[#6f4bff]"
                      />
                      <span>{label}</span>
                    </label>
                    <button
                      type="button"
                      className="text-xs text-[#94a3b8] bg-none border-none cursor-pointer p-0"
                    >
                      보기
                      {required && <span className="absolute w-px h-px p-0 m-[-1px] overflow-hidden clip-rect-[0,0,0,0] border-0">
                        {' '}
                        (필수)
                      </span>}
                    </button>
                  </div>
                ))}
              </div>
            </div>

            <button
              className="h-14 rounded-2xl border-none bg-white text-black text-sm font-medium cursor-pointer tracking-[-0.15px] transition-opacity duration-200 disabled:opacity-60 disabled:cursor-not-allowed hover:opacity-90"
              type="submit"
              disabled={!isFormValid || isSubmitting}
            >
              {isSubmitting ? '가입 중...' : '가입하기'}
            </button>
          </form>

          {feedback !== 'idle' && (
            <p
              className={`mt-2 px-3.5 py-3 rounded-xl text-sm ${
                feedback === 'error'
                  ? 'bg-[rgba(248,113,113,0.12)] text-[#b91c1c]'
                  : 'bg-[rgba(16,185,129,0.1)] text-[#047857]'
              }`}
            >
              {message}
            </p>
          )}

          <footer className="flex justify-center gap-1.5 text-sm text-[#475467]">
            <span>이미 계정이 있으신가요?</span>
            <Link to="/login" className="text-sm text-[#475467] no-underline hover:underline">
              로그인
            </Link>
          </footer>
        </section>
      </div>
    </div>
  )
}

function ArrowLeftIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="w-5 h-5">
      <path
        d="M15 5 8 12l7 7"
        stroke="currentColor"
        strokeWidth="1.8"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function UserIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <circle cx="12" cy="8" r="3.5" stroke="currentColor" strokeWidth="1.6" fill="none" />
      <path
        d="M5.5 19.5a6.5 6.5 0 0 1 13 0"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
        strokeLinecap="round"
      />
    </svg>
  )
}

function MailIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <path
        d="M4 6.8A2.8 2.8 0 0 1 6.8 4h10.4A2.8 2.8 0 0 1 20 6.8v10.4A2.8 2.8 0 0 1 17.2 20H6.8A2.8 2.8 0 0 1 4 17.2z"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="m5 8 7 5 7-5"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <rect
        x="4.5"
        y="10"
        width="15"
        height="10"
        rx="2.5"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
      />
      <path
        d="M8.5 10V7.5A3.5 3.5 0 0 1 12 4a3.5 3.5 0 0 1 3.5 3.5V10"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
      />
      <circle cx="12" cy="15" r="1.2" fill="currentColor" />
    </svg>
  )
}

function EyeIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <path
        d="M2.5 12s3.2-6 9.5-6 9.5 6 9.5 6-3.2 6-9.5 6-9.5-6-9.5-6Z"
        stroke="currentColor"
        strokeWidth="1.6"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="12" cy="12" r="2.5" stroke="currentColor" strokeWidth="1.6" fill="none" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <path
        d="M3 3.5 21 20"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
      <path
        d="M6.5 6.8C4.5 8.5 3 12 3 12s3.2 6 9.5 6a11 11 0 0 0 4.1-.8m3.4-3c1.3-1.4 2-2.2 2-2.2s-3.2-6-9.5-6a11 11 0 0 0-3.2.4"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
      <path
        d="M9.5 9.6a3 3 0 0 1 4.2 4.2"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  )
}

function GoogleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="w-6 h-6">
      <path
        d="M21.6 12.2c0-.7-.1-1.3-.2-1.9H12v3.7h5.4a4.6 4.6 0 0 1-2 3.1v2.6h3.2c1.9-1.7 3-4.2 3-7.5Z"
        fill="#4285F4"
      />
      <path
        d="M12 22c2.7 0 4.9-.9 6.6-2.4l-3.2-2.6c-.9.6-2 .9-3.4.9a5.9 5.9 0 0 1-5.6-4.2H3.1v2.7A10 10 0 0 0 12 22Z"
        fill="#34A853"
      />
      <path
        d="M6.4 13.7a6 6 0 0 1 0-3.7V7.3H3.1a10 10 0 0 0 0 9.4l3.3-2.9Z"
        fill="#FBBC05"
      />
      <path
        d="M12 5.5c1.5 0 2.8.5 3.8 1.5l2.8-2.8C16.8 2.5 14.6 1.6 12 1.6A10 10 0 0 0 3.1 7.3l3.3 2.7A5.9 5.9 0 0 1 12 5.5Z"
        fill="#EA4335"
      />
    </svg>
  )
}
