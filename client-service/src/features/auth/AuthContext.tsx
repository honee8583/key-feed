import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { LoginResponseData } from '../../services/authApi'
import {
  AUTH_STORAGE_KEY,
  clearStoredAuth,
  getStoredAuth,
  saveStoredAuth,
  subscribeToAuthChanges,
  type AuthPersistence,
  type StoredAuthState,
  type StoredAuthUser,
} from '../../services/authStorage'

type AuthContextValue = {
  user: StoredAuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (data: LoginResponseData, persist: AuthPersistence) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authState, setAuthState] = useState<StoredAuthState | null>(() => getStoredAuth())

  const login = useCallback((data: LoginResponseData, persist: AuthPersistence) => {
    const nextState: StoredAuthState = {
      token: data.accessToken,
      user: {
        id: data.id,
        email: data.email,
        name: data.name,
        role: data.role,
      },
      persist,
    }

    saveStoredAuth(nextState)
    setAuthState(nextState)
  }, [])

  const logout = useCallback(() => {
    clearStoredAuth()
    setAuthState(null)
  }, [])

  useEffect(() => {
    if (typeof window === 'undefined') return undefined

    const handleStorage = (event: StorageEvent) => {
      if (event.key !== AUTH_STORAGE_KEY) return
      setAuthState(getStoredAuth())
    }

    window.addEventListener('storage', handleStorage)
    return () => window.removeEventListener('storage', handleStorage)
  }, [])

  useEffect(() => subscribeToAuthChanges(setAuthState), [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: authState?.user ?? null,
      token: authState?.token ?? null,
      isAuthenticated: Boolean(authState?.token),
      login,
      logout,
    }),
    [authState, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
