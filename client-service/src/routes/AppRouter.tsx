import { Suspense, type ReactNode } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage, SignupPage, useAuth } from '../features/auth'
import { MainPage } from '../features/home'
import { ProfilePage, SourceManagementPage } from '../features/profile'
import { BottomNavigation } from '../components/BottomNavigation'

export function AppRouter() {
  const { isAuthenticated } = useAuth()

  return (
    <BrowserRouter>
      <Suspense fallback={<div className="app-loading">화면을 불러오는 중...</div>}>
        <Routes>
          <Route path="/" element={<Navigate to={isAuthenticated ? '/home' : '/login'} replace />} />
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <MainPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <ProfilePage />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile/sources"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <SourceManagementPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route
            path="*"
            element={
              <AppLayout>
                <NotFoundPage />
              </AppLayout>
            }
          />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}

function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      {children}
      <BottomNavigation />
    </div>
  )
}

function NotFoundPage() {
  return (
    <div className="not-found">
      <p>요청하신 페이지를 찾지 못했어요.</p>
      <Link to="/login">로그인 화면으로 돌아가기</Link>
    </div>
  )
}

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}
