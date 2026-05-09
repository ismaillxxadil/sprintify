'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { authApi, getApiErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/lib/auth-store'
import { LoadingScreen } from '@/components/loading-screen'

export default function LoginPage() {
  const router = useRouter()
  const { hydrated, user, setSession } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({ email: '', password: '' })

  useEffect(() => {
    if (!hydrated) {
      return
    }

    if (user) {
      router.replace(user.role === 'ADMIN' ? '/admin' : '/projects')
    }
  }, [hydrated, router, user])

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!formData.email || !formData.password) {
      toast.error('Enter your email and password.')
      return
    }

    setLoading(true)
    try {
      const response = await authApi.login(formData.email.trim(), formData.password)
      setSession(response)
      toast.success('Logged in successfully.')
      router.replace(response.role === 'ADMIN' ? '/admin' : '/projects')
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  if (!hydrated) {
    return <LoadingScreen label="Loading login..." />
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-10">
      <div className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-slate-900">Sprintify</h1>
          <p className="mt-2 text-sm text-slate-500">Sign in to manage projects, sprints, and team work.</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">Email</label>
            <input
              className="input"
              type="email"
              value={formData.email}
              onChange={(event) => setFormData((current) => ({ ...current, email: event.target.value }))}
              placeholder="you@example.com"
              disabled={loading}
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">Password</label>
            <input
              className="input"
              type="password"
              value={formData.password}
              onChange={(event) => setFormData((current) => ({ ...current, password: event.target.value }))}
              placeholder="••••••••"
              disabled={loading}
            />
          </div>
          <button className="button-primary w-full" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          Need an account?{' '}
          <Link href="/signup" className="font-semibold text-slate-900 underline-offset-4 hover:underline">
            Create one
          </Link>
        </p>
      </div>
    </div>
  )
}
