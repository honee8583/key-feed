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
    return apiClient.request<{ success: boolean }>('/auth/email/verification-code', {
      method: 'POST',
      body: { email },
    })
  },
  signup(payload: SignupPayload) {
    return apiClient.request<{ token: string }>('/auth/signup', {
      method: 'POST',
      body: payload,
    })
  },
}
