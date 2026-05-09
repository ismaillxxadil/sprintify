import { useRouter } from 'next/navigation'
import { useEffect } from 'react'
import { useAuthStore } from '@/lib/auth-store'

export const useProtectedRoute = () => {
  const router = useRouter()
  const { isAuthenticated, user } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/login')
    }
  }, [isAuthenticated, user, router])

  return { isAuthenticated, user }
}

export const useRoleProtection = (allowedRoles: string[]) => {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/login')
      return
    }

    if (!allowedRoles.includes(user.role)) {
      router.push('/unauthorized')
    }
  }, [isAuthenticated, user, router, allowedRoles])

  return { user, isAuthenticated }
}
