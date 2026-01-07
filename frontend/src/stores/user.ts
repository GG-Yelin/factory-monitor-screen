import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
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

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 从localStorage恢复用户信息
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      userInfo.value = JSON.parse(storedUser)
    } catch (e) {
      console.error('Failed to parse stored user info')
    }
  }

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isManager = computed(() => ['ADMIN', 'MANAGER'].includes(userInfo.value?.role || ''))

  function setToken(accessToken: string, refresh: string) {
    token.value = accessToken
    refreshToken.value = refresh
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', refresh)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    isManager,
    setToken,
    setUserInfo,
    logout
  }
})
