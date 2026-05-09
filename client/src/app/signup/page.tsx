'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { authApi, getApiErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/lib/auth-store'
import { LoadingScreen } from '@/components/loading-screen'

export default function SignupPage() {
  const router = useRouter()
  const { hydrated, user, setSession } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({ email: '', password: '', confirmPassword: '' })

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

    if (!formData.email || !formData.password || !formData.confirmPassword) {
      toast.error('Complete all required fields.')
      return
    }

    if (formData.password.length < 8) {
      toast.error('Password must be at least 8 characters long.')
      return
    }

    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match.')
      return
    }

    setLoading(true)
    try {
      const response = await authApi.signup(formData.email.trim(), formData.password)
      setSession(response)
      toast.success('Account created successfully.')
      router.replace(response.role === 'ADMIN' ? '/admin' : '/projects')
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  if (!hydrated) {
    return <LoadingScreen label="Loading signup..." />
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-10">
      <div className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-slate-900">Create account</h1>
          <p className="mt-2 text-sm text-slate-500">Start using Sprintify with a simple functionality-first workspace.</p>
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
              placeholder="At least 8 characters"
              disabled={loading}
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">Confirm password</label>
            <input
              className="input"
              type="password"
              value={formData.confirmPassword}
              onChange={(event) =>
                setFormData((current) => ({ ...current, confirmPassword: event.target.value }))
              }
              placeholder="Repeat your password"
              disabled={loading}
            />
          </div>
          <button className="button-primary w-full" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account?{' '}
          <Link href="/login" className="font-semibold text-slate-900 underline-offset-4 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
