import type { LoginResponseData } from './authApi'

export const AUTH_STORAGE_KEY = 'keyfeed:auth'

export type AuthPersistence = 'local' | 'session'

export type StoredAuthUser = Omit<LoginResponseData, 'accessToken'>

export type StoredAuthState = {
  token: string
  user: StoredAuthUser
  persist: AuthPersistence
}

type AuthChangeListener = (state: StoredAuthState | null) => void

const authListeners = new Set<AuthChangeListener>()

export function getStoredAuth(): StoredAuthState | null {
  if (typeof window === 'undefined') {
    return null
  }

  return readFromStorage(window.sessionStorage, 'session') ?? readFromStorage(window.localStorage, 'local')
}

export function subscribeToAuthChanges(listener: AuthChangeListener) {
  authListeners.add(listener)
  return () => {
    authListeners.delete(listener)
  }
}

export function saveStoredAuth(state: StoredAuthState) {
  if (typeof window === 'undefined') {
    return
  }

  const target = state.persist === 'local' ? window.localStorage : window.sessionStorage
  const other = state.persist === 'local' ? window.sessionStorage : window.localStorage

  target.setItem(
    AUTH_STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      user: state.user,
    }),
  )
  other.removeItem(AUTH_STORAGE_KEY)
  notifyAuthListeners(state)
}

export function clearStoredAuth() {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY)
  window.sessionStorage.removeItem(AUTH_STORAGE_KEY)
  notifyAuthListeners(null)
}

export function getAccessToken(): string | null {
  return getStoredAuth()?.token ?? null
}

function readFromStorage(storage: Storage, persist: AuthPersistence): StoredAuthState | null {
  const raw = storage.getItem(AUTH_STORAGE_KEY)
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw)
    if (!parsed?.token || !parsed?.user) return null

    return {
      token: parsed.token as string,
      user: parsed.user as StoredAuthUser,
      persist,
    }
  } catch (error) {
    console.warn('Failed to parse stored auth state', error)
    storage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

function notifyAuthListeners(state: StoredAuthState | null) {
  authListeners.forEach((listener) => {
    try {
      listener(state)
    } catch (error) {
      console.error('Failed to notify auth listener', error)
    }
  })
}
