import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { LoginResponseData } from '../../services/authApi'
import {
  AUTH_STORAGE_KEY,
  clearStoredAuth,
  getStoredAuth,
  saveStoredAuth,
  subscribeToAuthChanges,
  type AuthPersistence,
  type StoredAuthState,
} from '../../services/authStorage'
import { AuthContext, type AuthContextValue } from './AuthContextDefinition'

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
