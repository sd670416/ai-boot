import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api'
import type { UserProfile } from '@/types'

const TOKEN_KEY = 'ai-boot-token'
const USER_KEY = 'ai-boot-user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserProfile | null>(loadUser())

  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.userType === 'BACKEND')
  const isSuperAdmin = computed(() => user.value?.userType === 'BACKEND' && user.value?.roleCode === 'SUPER_ADMIN')

  async function login(mode: 'admin' | 'frontend', payload: { username: string; password: string }) {
    const loginResult = mode === 'admin' ? await api.adminLogin(payload) : await api.frontendLogin(payload)
    localStorage.setItem(TOKEN_KEY, loginResult.token)
    token.value = loginResult.token
    const profile = await api.getMe()
    user.value = profile
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    return profile
  }

  async function refreshMe() {
    if (!token.value) return null
    const profile = await api.getMe()
    user.value = profile
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    return profile
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function loadUser() {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as UserProfile
    } catch {
      return null
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    login,
    logout,
    refreshMe,
  }
})
