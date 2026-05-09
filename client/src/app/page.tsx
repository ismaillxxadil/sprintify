'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { LoadingScreen } from '@/components/loading-screen'
import { useAuthStore } from '@/lib/auth-store'

export default function HomePage() {
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

    router.replace(user.role === 'ADMIN' ? '/admin' : '/projects')
  }, [hydrated, router, user])

  return <LoadingScreen label="Opening Sprintify..." />
}
