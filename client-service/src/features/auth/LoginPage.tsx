import { useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../services/authApi'
import { useAuth } from './AuthContextDefinition'
import { LoginInput } from './components/LoginInput'
import { MailIcon } from './components/AuthIcons'
import logo from '../../assets/logo.png'



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



  return (
    <div className="min-h-screen bg-black flex justify-center items-center relative overflow-hidden font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      {/* Background Decor */}
      <div className="absolute top-0 left-[-100px] w-[500px] h-[500px] bg-[rgba(43,127,255,0.1)] rounded-full blur-[100px] pointer-events-none" />
      <div className="absolute bottom-0 right-[-100px] w-[500px] h-[500px] bg-[rgba(173,70,255,0.1)] rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-[393px] px-6 relative z-10 flex flex-col gap-10">
        
        {/* Header Section */}
        <div className="flex flex-col items-center">
          <div className="w-[80px] h-[80px] rounded-[24px] flex items-center justify-center mb-6 overflow-hidden shadow-[0px_10px_15px_0px_rgba(0,0,0,0.5)]">
            <img src={logo} alt="KeyFeed Logo" className="w-full h-full object-cover" />
          </div>
          <h1 className="text-[28px] font-bold text-white tracking-[-0.02em] mb-2">Welcome Back</h1>
          <p className="text-[15px] text-[#94A3B8] tracking-[-0.01em]">나만의 맞춤 콘텐츠 피드를 만나보세요</p>
        </div>

        {/* Form Section */}
        <form className="flex flex-col gap-6" onSubmit={handleSubmit}>
          <div className="flex flex-col gap-4">
            <label className="flex flex-col gap-2">
              <span className="text-[14px] font-semibold text-white ml-1">이메일</span>
              <LoginInput
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
              <span className="text-[14px] font-semibold text-white ml-1">비밀번호</span>
              <LoginInput
                type="password"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                disabled={isLoading}
                minLength={6}
                required
              />
            </label>
          </div>

          <div className="flex justify-between items-center text-[13px]">
            <label className="flex items-center gap-2 cursor-pointer text-[#94A3B8] select-none">
              <input
                type="checkbox"
                checked={staySignedIn}
                onChange={(event) => setStaySignedIn(event.target.checked)}
                disabled={isLoading}
                className="w-4 h-4 rounded-[4px] border-[#334155] bg-[#0F172A] checked:bg-[#2B7FFF] checked:border-[#2B7FFF] transition-colors"
              />
              <span>로그인 유지</span>
            </label>
            <button
              type="button"
              className="text-[#94A3B8] hover:text-white transition-colors"
            >
              비밀번호 찾기
            </button>
          </div>

          <button
            className="w-full h-[56px] rounded-[16px] text-white text-[16px] font-bold tracking-[-0.01em] transition-transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed shadow-[0_4px_14px_0_rgba(43,127,255,0.4)]"
            type="submit"
            disabled={!isFormValid || isLoading}
            style={{ background: 'linear-gradient(90deg, #2B7FFF 0%, #9810FA 100%)' }}
          >
            {isLoading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        {feedback !== 'idle' && (
            <p
              className={`text-center text-[14px] ${
                feedback === 'error' ? 'text-red-400' : 'text-emerald-400'
              }`}
            >
              {message}
            </p>
        )}

        <div className="flex justify-center gap-2 text-[14px] text-[#64748B]">
            <span>계정이 없으신가요?</span>
            <Link 
              to="/signup" 
              className="font-semibold bg-clip-text text-transparent bg-gradient-to-r from-[#2B7FFF] to-[#9810FA] hover:opacity-80 transition-opacity"
            >
              회원가입
            </Link>
        </div>
      </div>
    </div>
  )
}
