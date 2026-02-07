import { apiClient } from './apiClient'

export type LoginPayload = {
  email: string
  password: string
  staySignedIn?: boolean
}

export type SignupPayload = {
  name: string
  email: string
  password: string
  marketingOptIn?: boolean
}

export type SocialProvider = 'kakao' | 'naver' | 'google'

export type LoginResponseData = {
  id: number
  email: string
  name: string
  role: string
  accessToken: string
}

export type LoginResponse = {
  status: number
  message: string
  data: LoginResponseData
}

export type VerificationStatus = 'PENDING' | 'VERIFIED' | 'EXPIRED' | 'LOCKED'

export type VerificationResponseData = {
  status: VerificationStatus
  attempts: number
  retryAt: string | null
  expiresAt: string
}

export type VerificationResponse = {
  status: string
  message: string
  data: VerificationResponseData
}

export const authApi = {
  login(payload: LoginPayload) {
    return apiClient.request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: payload,
    })
  },
  loginWithProvider(provider: SocialProvider) {
    return apiClient.request<{ url: string }>(`/auth/${provider}/login`, {
      method: 'POST',
    })
  },
  sendVerificationCode(email: string) {
    return apiClient.request<{ status: string; message: string; data: null }>('/auth/password-reset/request', {
      method: 'POST',
      body: { email },
    })
  },
  confirmVerificationCode(email: string, code: string) {
    return apiClient.request<VerificationResponse>('/auth/password-reset/verify', {
      method: 'POST',
      body: { email, code },
    })
  },
  signup(payload: SignupPayload) {
    return apiClient.request<{ token: string }>('/auth/join', {
      method: 'POST',
      body: payload,
    })
  },
  resetPassword(payload: { email: string; newPassword: string; confirmPassword: string }) {
    return apiClient.request<{ status: string; message: string; data: null }>('/auth/password-reset/confirm', {
      method: 'POST',
      body: payload,
    })
  },
}
