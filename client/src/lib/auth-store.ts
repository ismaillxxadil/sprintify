'use client'

import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { buildSessionUser } from '@/lib/jwt'
import { AuthResponse, SessionUser } from '@/lib/types'

interface AuthState {
  hydrated: boolean
  user: SessionUser | null
  setSession: (response: AuthResponse) => void
  logout: () => void
  setHydrated: (hydrated: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      hydrated: false,
      user: null,
      setSession: (response) => {
        set({ hydrated: true, user: buildSessionUser(response) })
      },
      logout: () => {
        set({ hydrated: true, user: null })
      },
      setHydrated: (hydrated) => {
        set({ hydrated })
      },
    }),
    {
      name: 'sprintify-auth',
      onRehydrateStorage: () => (state) => {
        state?.setHydrated(true)
      },
    }
  )
)
