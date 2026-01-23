import { createContext, useContext } from 'react'
import type { LoginResponseData } from '../../services/authApi'
import type { AuthPersistence, StoredAuthUser } from '../../services/authStorage'

export type AuthContextValue = {
  user: StoredAuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (data: LoginResponseData, persist: AuthPersistence) => void
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
