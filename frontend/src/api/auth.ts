import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: {
    id: number
    username: string
    employeeNo?: string
    realName: string
    phone?: string
    role: string
    roleName: string
    teamId?: number
    teamName?: string
    avatar?: string
  }
}

export function login(data: LoginParams) {
  return request.post<any, { code: number; message: string; data: LoginResult }>('/auth/login', data)
}

export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh', { refreshToken })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.post('/auth/change-password', { oldPassword, newPassword })
}

export function logout() {
  return request.post('/auth/logout')
}
