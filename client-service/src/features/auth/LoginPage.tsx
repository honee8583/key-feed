import { useMemo, useState, type CSSProperties, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './LoginPage.css'
import { authApi } from '../../services/authApi'
import type { SocialProvider } from '../../services/authApi'
import { useAuth } from './AuthContext'

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
    label: '카카오로 시작하기',
    background: '#fee500',
    border: 'rgba(253,199,0,0.3)',
    textColor: '#101828',
    iconBackground: '#3c1e1e',
    iconColor: '#fee500',
    icon: 'K',
  },
  {
    id: 'naver',
    label: '네이버로 시작하기',
    background: '#03c75a',
    border: 'rgba(0,201,80,0.3)',
    textColor: '#ffffff',
    iconBackground: '#ffffff',
    iconColor: '#03c75a',
    icon: 'N',
  },
  {
    id: 'google',
    label: '구글로 시작하기',
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
    <div className="login-page">
      <div className="login-page__blur login-page__blur--one" aria-hidden />
      <div className="login-page__blur login-page__blur--two" aria-hidden />
      <div className="login-page__container">
        <section className="login-page__hero">
          <div className="login-page__hero-icon" aria-hidden="true">
            <span>📰</span>
          </div>
          <div className="login-page__hero-text">
            <h1>
              콘텐츠 큐레이터
              <span className="login-page__sparkle" role="img" aria-label="sparkles">
                ✨
              </span>
            </h1>
            <p>나만의 정보 피드를 만들어보세요</p>
          </div>
        </section>

        <section className="login-card" aria-label="로그인 폼">
          <header className="login-card__header">
            <p>환영합니다! 👋</p>
          </header>

          <div className="login-card__socials">
            {SOCIAL_BUTTONS.map(
              ({ id, label, background, border, textColor, iconBackground, iconColor, icon }) => (
                <button
                  key={id}
                  type="button"
                  className="social-button"
                  style={
                    {
                      '--social-bg': background,
                      '--social-border': border,
                      '--social-text': textColor,
                      '--social-icon-bg': iconBackground,
                      '--social-icon-text': iconColor,
                    } as CSSProperties
                  }
                  onClick={() => handleSocialLogin(id)}
                  disabled={isLoading}
                >
                  <span className="social-button__icon" aria-hidden>
                    {icon}
                  </span>
                  <span>{label}</span>
                </button>
              ),
            )}
          </div>

          <div className="login-card__divider">
            <span>또는 이메일로 계속하기</span>
          </div>

          <form className="login-form" onSubmit={handleSubmit}>
            <label className="form-field">
              <span className="form-field__label">이메일</span>
              <div className="form-input">
                <MailIcon />
                <input
                  type="email"
                  inputMode="email"
                  placeholder="example@email.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  autoComplete="email"
                  disabled={isLoading}
                  required
                />
              </div>
            </label>

            <label className="form-field">
              <span className="form-field__label">비밀번호</span>
              <div className="form-input">
                <LockIcon />
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력하세요"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete="current-password"
                  disabled={isLoading}
                  minLength={6}
                  required
                />
                <button
                  type="button"
                  className="ghost-icon-button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
            </label>

            <div className="login-form__actions">
              <label className="stay-signed-in">
                <input
                  type="checkbox"
                  checked={staySignedIn}
                  onChange={(event) => setStaySignedIn(event.target.checked)}
                  disabled={isLoading}
                />
                <span>로그인 유지</span>
              </label>
              <button type="button" className="text-link">비밀번호 찾기</button>
            </div>

            <button className="primary-button" type="submit" disabled={!isFormValid || isLoading}>
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          {feedback !== 'idle' && (
            <p className={`login-card__feedback ${feedback === 'error' ? 'is-error' : 'is-success'}`}>
              {message}
            </p>
          )}

          <footer className="login-card__footer">
            <span>아직 계정이 없으신가요?</span>
            <Link to="/signup" className="text-link">
              회원가입
            </Link>
          </footer>
        </section>
      </div>
    </div>
  )
}

function MailIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="form-icon">
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
    <svg viewBox="0 0 24 24" aria-hidden className="form-icon">
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
    <svg viewBox="0 0 24 24" aria-hidden className="form-icon">
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
    <svg viewBox="0 0 24 24" aria-hidden className="form-icon">
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
    <svg viewBox="0 0 24 24" aria-hidden className="google-icon">
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
