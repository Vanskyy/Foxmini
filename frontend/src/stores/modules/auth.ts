import { defineStore } from 'pinia'
import { apiLogin, apiRegister } from '../../api/auth'
import type { LoginRequest, RegisterRequest, UserResponse } from '../../types/user'
import { Role } from '../../types/user'

interface AuthState {
  user: UserResponse | null
  loading: boolean
  error: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    loading: false,
    error: null,
  }),
  getters: {
    isLogged: (s) => !!s.user,
    role: (s) => s.user?.role as Role | undefined,
  },
  actions: {
    initFromCache() {
      const raw = localStorage.getItem('user')
      if (raw) {
        try {
          this.user = JSON.parse(raw)
        } catch {
          /* ignore */
        }
      }
    },
    async login(payload: LoginRequest) {
      this.loading = true
      this.error = null
      try {
        this.user = await apiLogin(payload)
        localStorage.setItem('user', JSON.stringify(this.user))
      } catch (e: any) {
        this.error = e.message || '登录失败'
        throw e
      } finally {
        this.loading = false
      }
    },
    async register(payload: RegisterRequest) {
      this.loading = true
      this.error = null
      try {
        this.user = await apiRegister(payload)
        localStorage.setItem('user', JSON.stringify(this.user))
      } catch (e: any) {
        this.error = e.message || '注册失败'
        throw e
      } finally {
        this.loading = false
      }
    },
    logout() {
      this.user = null
      localStorage.removeItem('user')
    },
  },
})
