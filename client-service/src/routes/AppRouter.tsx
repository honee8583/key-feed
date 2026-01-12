import { Suspense, useEffect, type ReactNode } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { LoginPage, SignupPage, useAuth } from '../features/auth'
import { MainPage } from '../features/home'
import { ExplorePage } from '../features/explore'
import { BookmarksPage } from '../features/bookmarks'
import { FolderManagementPage } from '../features/bookmarks/FolderManagementPage'
import { ProfilePage, SourceManagementPage } from '../features/profile'
import { NotificationPage } from '../features/notifications'
import { BottomNavigation } from '../components/BottomNavigation'

export function AppRouter() {
  const { isAuthenticated } = useAuth()

  return (
    <BrowserRouter>
      <ScrollToTop />
      <Suspense
        fallback={
          <div className="min-h-screen flex flex-col items-center justify-center gap-3 text-base text-[#101828] text-center">
            화면을 불러오는 중...
          </div>
        }
      >
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
            path="/explore"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <ExplorePage />
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
            path="/bookmarks"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <BookmarksPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/bookmarks/folders"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <FolderManagementPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <NotificationPage />
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
    <div className="min-h-screen pt-0 pb-[96px]">
      {children}
      <BottomNavigation />
    </div>
  )
}

function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-3 text-base text-[#101828] text-center">
      <p>요청하신 페이지를 찾지 못했어요.</p>
      <Link to="/login" className="text-[#5a4cf5] font-semibold no-underline hover:underline">
        로그인 화면으로 돌아가기
      </Link>
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

function ScrollToTop() {
  const location = useLocation()

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [location.pathname])

  return null
}
