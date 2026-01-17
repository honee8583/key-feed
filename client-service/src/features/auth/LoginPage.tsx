import { useMemo, useState, type CSSProperties, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import type { SocialProvider } from '../../services/authApi'
import { useAuth } from './AuthContext'
import logo from '../../assets/logo.png'

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

export function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [staySignedIn, setStaySignedIn] = useState(true)
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState<'idle' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')
  const { login: persistAuth } = useAuth()
  const navigate = useNavigate()

  const isFormValid = useMemo(
    () => email.trim().length > 0 && password.trim().length >= 6,
    [email, password],
  )

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!isFormValid) return

    setIsLoading(true)
    setFeedback('idle')
    setMessage('')

    try {
      const response = await authApi.login({ email, password, staySignedIn })
      const persistence = staySignedIn ? 'local' : 'session'
      persistAuth(response.data, persistence)
      setFeedback('success')
      setMessage(response.message || '로그인에 성공했어요.')
      navigate('/home', { replace: true })
    } catch (error) {
      console.error(error)
      setFeedback('error')
      const errorMessage = error instanceof Error ? error.message : '로그인에 실패했어요. 입력 정보를 다시 확인해주세요.'
      setMessage(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSocialLogin = async (provider: SocialProvider) => {
    setIsLoading(true)
    setFeedback('idle')
    setMessage('')

    try {
      await authApi.loginWithProvider(provider)
      setFeedback('success')
      setMessage(`${provider.toUpperCase()} 로그인 링크를 준비했어요.`)
    } catch (error) {
      console.error(error)
      setFeedback('error')
      setMessage('소셜 로그인에 실패했어요. 잠시 후 다시 시도해주세요.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-black flex justify-center pt-16 px-6">
      <div className="w-full max-w-[393px] flex flex-col gap-16">
        <div className="flex flex-col gap-0">
          <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mb-6 overflow-hidden">
            <img src={logo} alt="KeyFeed Logo" className="w-full h-full object-cover" />
          </div>
          <h1 className="m-0 mb-1 text-xl font-bold text-white tracking-[-0.45px]">로그인</h1>
          <p className="m-0 text-sm text-[#99a1af] tracking-[-0.15px]">나만의 맞춤 콘텐츠 피드를 만나보세요</p>
        </div>

        <div className="flex flex-col gap-8">
          <div className="flex flex-col gap-3">
            {SOCIAL_BUTTONS.map(
              ({ id, label, background, border, textColor, iconBackground, iconColor, icon }) => (
                <button
                  key={id}
                  type="button"
                  className="flex items-center justify-between w-full h-14 px-5 rounded-2xl border-none text-base font-normal tracking-[-0.31px] cursor-pointer transition-opacity duration-200 disabled:opacity-60 disabled:cursor-not-allowed hover:opacity-90"
                  style={{ background, borderColor: border, color: textColor } as CSSProperties}
                  onClick={() => handleSocialLogin(id)}
                  disabled={isLoading}
                >
                  <div className="flex items-center gap-3">
                    <span
                      className="inline-flex items-center justify-center w-6 h-6 rounded-[10px] text-xs font-normal"
                      style={{ background: iconBackground, color: iconColor } as CSSProperties}
                      aria-hidden
                    >
                      {icon}
                    </span>
                    <span>{label}</span>
                  </div>
                  <ArrowRightIcon />
                </button>
              ),
            )}
          </div>

          <div className="relative text-center h-5">
            <div className="absolute top-1/2 left-0 right-0 h-[1.5px] bg-[#1e2939] -translate-y-1/2" />
            <span className="relative inline-block px-4 bg-black text-sm text-[#6a7282] tracking-[-0.15px]">
              또는
            </span>
          </div>

          <form className="flex flex-col gap-5" onSubmit={handleSubmit}>
            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">이메일</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <MailIcon />
                <input
                  type="email"
                  inputMode="email"
                  placeholder="example@email.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  autoComplete="email"
                  disabled={isLoading}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
              </div>
            </label>

            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">비밀번호</span>
              <div className="relative h-14 rounded-2xl border-[1.5px] border-[#1e2939] bg-[rgba(229,229,229,0.3)] flex items-center gap-3 px-4 pl-12 text-white focus-within:border-[#3d3d3d]">
                <LockIcon />
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력하세요"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete="current-password"
                  disabled={isLoading}
                  minLength={6}
                  className="flex-1 border-none bg-transparent text-base text-white outline-none tracking-[-0.31px] placeholder:text-[#6a7282]"
                  required
                />
                <button
                  type="button"
                  className="absolute right-4 top-1/2 -translate-y-1/2 border-none bg-transparent p-0 flex items-center justify-center cursor-pointer text-[#6a7282] hover:text-[#99a1af]"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
                >
                  <EyeIcon />
                </button>
              </div>
            </label>

            <div className="flex justify-between items-center text-sm">
              <label className="flex items-center gap-2 font-medium text-[#99a1af] tracking-[-0.15px] cursor-pointer">
                <input
                  type="checkbox"
                  checked={staySignedIn}
                  onChange={(event) => setStaySignedIn(event.target.checked)}
                  disabled={isLoading}
                  className="w-4 h-4 border-[1.5px] border-[#e5e5e5] rounded bg-[rgba(229,229,229,0.3)] cursor-pointer"
                />
                <span>로그인 유지</span>
              </label>
              <button
                type="button"
                className="border-none bg-none text-[#99a1af] text-sm font-normal cursor-pointer p-0 tracking-[-0.15px] no-underline hover:underline"
              >
                비밀번호 찾기
              </button>
            </div>

            <button
              className="h-14 rounded-2xl border-none bg-white text-black text-sm font-medium cursor-pointer tracking-[-0.15px] transition-opacity duration-200 disabled:opacity-60 disabled:cursor-not-allowed hover:opacity-90"
              type="submit"
              disabled={!isFormValid || isLoading}
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          {feedback !== 'idle' && (
            <p
              className={`-mt-3 mb-0 px-4 py-3 rounded-xl text-sm text-center ${
                feedback === 'error'
                  ? 'bg-[rgba(239,68,68,0.1)] text-[#ef4444]'
                  : 'bg-[rgba(16,185,129,0.1)] text-[#10b981]'
              }`}
            >
              {message}
            </p>
          )}

          <div className="pt-[33px] border-t-[1.5px] border-[#1e2939] flex justify-center gap-2 text-sm text-[#99a1af] tracking-[-0.15px]">
            <span>아직 계정이 없으신가요?</span>
            <Link to="/signup" className="text-white text-sm font-normal cursor-pointer p-0 tracking-[-0.15px] no-underline hover:underline">
              회원가입
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

function ArrowRightIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden className="flex-shrink-0 text-inherit">
      <path
        d="M7.5 15L12.5 10L7.5 5"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function MailIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 20 20"
      fill="none"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <rect
        x="3"
        y="5"
        width="14"
        height="10"
        rx="2"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <path
        d="M3 7L10 11L17 7"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 20 20"
      fill="none"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <rect
        x="4"
        y="9"
        width="12"
        height="8"
        rx="2"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <path
        d="M7 9V6.5C7 4.57 8.34 3 10 3C11.66 3 13 4.57 13 6.5V9"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  )
}

function EyeIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 20 20"
      fill="none"
      className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6a7282] flex-shrink-0"
      aria-hidden
    >
      <path
        d="M10 6.25C6.25 6.25 3.75 10 3.75 10C3.75 10 6.25 13.75 10 13.75C13.75 13.75 16.25 10 16.25 10C16.25 10 13.75 6.25 10 6.25Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle
        cx="10"
        cy="10"
        r="2"
        stroke="currentColor"
        strokeWidth="1.5"
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
