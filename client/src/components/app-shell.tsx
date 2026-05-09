'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import clsx from 'clsx'
import { ReactNode } from 'react'
import { useAuthStore } from '@/lib/auth-store'

interface AppShellProps {
  title: string
  description: string
  children: ReactNode
  actions?: ReactNode
}

export function AppShell({ title, description, children, actions }: AppShellProps) {
  const pathname = usePathname()
  const router = useRouter()
  const { user, logout } = useAuthStore()

  const navItems = user?.role === 'ADMIN'
    ? [{ href: '/admin', label: 'Admin' }]
    : [{ href: '/projects', label: 'Projects' }]

  const handleLogout = () => {
    logout()
    router.replace('/login')
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-4 py-4 md:flex-row md:items-center md:justify-between md:px-6">
          <div>
            <Link href={user?.role === 'ADMIN' ? '/admin' : '/projects'} className="text-2xl font-bold text-slate-900">
              Sprintify
            </Link>
            <p className="mt-1 text-sm text-slate-500">{user?.email}</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={clsx(
                  'rounded-lg px-3 py-2 text-sm font-medium transition',
                  pathname === item.href ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                )}
              >
                {item.label}
              </Link>
            ))}
            <button type="button" className="button-secondary" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 md:px-6">
        <section className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:flex-row md:items-start md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
            <p className="mt-2 max-w-2xl text-sm text-slate-600">{description}</p>
          </div>
          {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
        </section>
        {children}
      </main>
    </div>
  )
}
