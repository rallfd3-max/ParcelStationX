import { defineStore } from 'pinia'
import { apiRequest, setTokenProvider } from '@/api/client'
import type { LoginResponse, User } from '@/types/api'

const storage = typeof localStorage === 'undefined' ? null : localStorage

export const useSessionStore = defineStore('session', {
  state: () => ({
    token: storage?.getItem('parcelstationx.token') ?? '',
    currentUser: null as User | null,
    loading: false,
    error: '',
  }),
  getters: {
    authenticated: (state) => Boolean(state.token && state.currentUser),
    isAdmin: (state) => state.currentUser?.role === 'ADMIN',
  },
  actions: {
    initializeClient() { setTokenProvider(() => this.token) },
    async login(username: string, password: string) {
      this.loading = true
      this.error = ''
      try {
        const result = await apiRequest<LoginResponse>('/api/auth/login', {
          method: 'POST', body: JSON.stringify({ username, password }),
        })
        this.token = result.token
        this.currentUser = result.user
        storage?.setItem('parcelstationx.token', result.token)
      } catch (error) {
        this.error = error instanceof Error ? error.message : '登录失败。'
        throw error
      } finally { this.loading = false }
    },
    async loadMe() {
      if (!this.token) return false
      try {
        this.currentUser = await apiRequest<User>('/api/auth/me')
        return true
      } catch {
        this.clear()
        return false
      }
    },
    async logout() {
      try { if (this.token) await apiRequest('/api/auth/logout', { method: 'POST' }) }
      finally { this.clear() }
    },
    clear() {
      this.token = ''
      this.currentUser = null
      storage?.removeItem('parcelstationx.token')
    },
  },
})
