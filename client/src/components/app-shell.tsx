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
    <div className="flex flex-col min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      <header className="border-bottom bg-surface">
        <div className="container flex flex-col gap-4 py-4 md:flex-row md:items-center md:justify-between">
          <div>
            <Link href={user?.role === 'ADMIN' ? '/admin' : '/projects'} className="text-2xl font-bold text-primary">
              Sprintify
            </Link>
            <p className="mt-1 text-sm text-secondary">{user?.email}</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={clsx(
                  'rounded-lg px-3 py-2 text-sm font-medium transition',
                  pathname === item.href
                    ? 'bg-primary text-white'
                    : 'bg-surface-secondary text-secondary hover:bg-surface-tertiary'
                )}
              >
                {item.label}
              </Link>
            ))}
            <button type="button" className="btn btn-secondary" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="container flex flex-col gap-6 py-6 flex-1">
        <section className="flex flex-col gap-4 rounded-2xl border bg-surface p-6 shadow-sm md:flex-row md:items-start md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-primary">{title}</h1>
            <p className="mt-2 max-w-2xl text-sm text-secondary">{description}</p>
          </div>
          {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
        </section>
        {children}
      </main>
    </div>
  )
}
