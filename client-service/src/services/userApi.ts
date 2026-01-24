import { apiClient } from './apiClient'

export type PasswordChangeRequest = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export type PasswordChangeResponse = {
  status: number
  message: string
  data: null
}

export const userApi = {
  changePassword(payload: PasswordChangeRequest) {
    return apiClient.request<PasswordChangeResponse>('/users/password', {
      method: 'PATCH',
      body: payload,
    })
  },
}
