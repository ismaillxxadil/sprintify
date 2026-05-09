import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type UserRole = 'ADMIN' | 'USER' | 'PO' | 'SM' | 'DEV'

export interface AuthUser {
  token: string
  email: string
  role: UserRole
  userId?: string
}

interface AuthStore {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (user: AuthUser) => void
  logout: () => void
  setUser: (user: AuthUser) => void
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      login: (user: AuthUser) => {
        set({ user, isAuthenticated: true })
        if (typeof window !== 'undefined') {
          localStorage.setItem('token', user.token)
          localStorage.setItem('role', user.role)
          localStorage.setItem('email', user.email)
        }
      },
      logout: () => {
        set({ user: null, isAuthenticated: false })
        if (typeof window !== 'undefined') {
          localStorage.removeItem('token')
          localStorage.removeItem('role')
          localStorage.removeItem('email')
          localStorage.removeItem('userId')
        }
      },
      setUser: (user: AuthUser) => {
        set({ user, isAuthenticated: true })
      },
    }),
    {
      name: 'auth-store',
    }
  )
)
