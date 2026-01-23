import { useMemo, useState, type CSSProperties, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import type { SocialProvider } from '../../services/authApi'
import { useAuth } from './AuthContextDefinition'
import { AuthInput } from './components/AuthInput'
import { AuthPasswordInput } from './components/AuthPasswordInput'
import { ArrowRightIcon, GoogleIcon, MailIcon } from './components/AuthIcons'
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
    <div className="min-h-screen bg-black flex justify-center pt-16 px-6 font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
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
                  <ArrowRightIcon className="text-inherit" />
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
              <AuthInput
                icon={<MailIcon />}
                type="email"
                inputMode="email"
                placeholder="example@email.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="email"
                disabled={isLoading}
                required
              />
            </label>

            <label className="flex flex-col gap-2">
              <span className="text-sm font-medium text-white tracking-[-0.15px]">비밀번호</span>
              <AuthPasswordInput
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                disabled={isLoading}
                minLength={6}
                required
              />
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
