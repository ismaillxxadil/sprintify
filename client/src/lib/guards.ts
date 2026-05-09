'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/auth-store'
import { IdentityRole } from '@/lib/types'

export const useRequiredAuth = (allowedRoles?: IdentityRole[]) => {
  const router = useRouter()
  const { hydrated, user } = useAuthStore()

  useEffect(() => {
    if (!hydrated) {
      return
    }

    if (!user) {
      router.replace('/login')
      return
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
      router.replace('/unauthorized')
    }
  }, [allowedRoles, hydrated, router, user])

  return {
    hydrated,
    user,
    ready: hydrated && !!user && (!allowedRoles || allowedRoles.includes(user.role)),
  }
}
