'use client'

import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { AppShell } from '@/components/app-shell'
import { LoadingScreen } from '@/components/loading-screen'
import { StatusPill } from '@/components/status-pill'
import { adminApi, getApiErrorMessage, logApi } from '@/lib/api'
import { useRequiredAuth } from '@/lib/guards'
import { formatDateTime } from '@/lib/format'
import { AdminUserResponse, AuditLog } from '@/lib/types'

export default function AdminPage() {
  const { hydrated, ready } = useRequiredAuth(['ADMIN'])
  const [loading, setLoading] = useState(true)
  const [users, setUsers] = useState<AdminUserResponse[]>([])
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [activeTab, setActiveTab] = useState<'users' | 'logs'>('users')
  const [banDays, setBanDays] = useState<Record<string, string>>({})
  const [busyUserId, setBusyUserId] = useState<string | null>(null)

  const loadData = async () => {
    setLoading(true)
    try {
      const [loadedUsers, loadedLogs] = await Promise.all([adminApi.getUsers(), logApi.getAllLogs()])
      setUsers(loadedUsers)
      setLogs(loadedLogs)
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!ready) {
      return
    }

    void loadData()
  }, [ready])

  const handleBan = async (userId: string) => {
    const rawDays = banDays[userId]
    if (!rawDays) {
      toast.error('Enter number of days.')
      return
    }

    setBusyUserId(userId)
    try {
      await adminApi.banUser(userId, Number(rawDays))
      toast.success('User updated successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyUserId(null)
    }
  }

  const handleDelete = async (userId: string) => {
    setBusyUserId(userId)
    try {
      await adminApi.deleteUser(userId)
      toast.success('User deleted successfully.')
      setUsers((current) => current.filter((user) => user.id !== userId))
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyUserId(null)
    }
  }

  if (!hydrated || !ready) {
    return <LoadingScreen label="Loading admin workspace..." />
  }

  return (
    <AppShell
      title="Admin workspace"
      description="Review every user, ban or delete accounts, and monitor the complete audit log through the API gateway."
      actions={
        <div className="flex rounded-xl bg-slate-100 p-1">
          {(['users', 'logs'] as const).map((tab) => (
            <button
              key={tab}
              type="button"
              className={tab === activeTab ? 'button-primary' : 'button-secondary'}
              onClick={() => setActiveTab(tab)}
            >
              {tab === 'users' ? 'Users' : 'Logs'}
            </button>
          ))}
        </div>
      }
    >
      {loading ? (
        <div className="panel text-sm text-slate-500">Loading data...</div>
      ) : activeTab === 'users' ? (
        <section className="panel overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead>
              <tr className="text-left text-slate-500">
                <th className="py-3 pr-4 font-medium">Email</th>
                <th className="py-3 pr-4 font-medium">Role</th>
                <th className="py-3 pr-4 font-medium">Status</th>
                <th className="py-3 pr-4 font-medium">Created</th>
                <th className="py-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((user) => {
                const isBusy = busyUserId === user.id
                const banned = !!user.bannedUntil && new Date(user.bannedUntil) > new Date()

                return (
                  <tr key={user.id}>
                    <td className="py-4 pr-4 align-top">
                      <p className="font-medium text-slate-900">{user.email}</p>
                      <p className="mt-1 text-xs text-slate-500">{user.id}</p>
                    </td>
                    <td className="py-4 pr-4 align-top">
                      <StatusPill label={user.role} />
                    </td>
                    <td className="py-4 pr-4 align-top">
                      <StatusPill label={banned ? 'BANNED' : 'ACTIVE'} />
                      <p className="mt-1 text-xs text-slate-500">Until: {formatDateTime(user.bannedUntil)}</p>
                    </td>
                    <td className="py-4 pr-4 align-top text-slate-600">{formatDateTime(user.createdAt)}</td>
                    <td className="py-4 align-top">
                      <div className="flex flex-wrap items-center gap-2">
                        <input
                          className="input max-w-[110px]"
                          type="number"
                          min="1"
                          placeholder="Days"
                          value={banDays[user.id] || ''}
                          onChange={(event) =>
                            setBanDays((current) => ({ ...current, [user.id]: event.target.value }))
                          }
                        />
                        <button type="button" className="button-secondary" onClick={() => void handleBan(user.id)} disabled={isBusy}>
                          Ban
                        </button>
                        <button type="button" className="button-danger" onClick={() => void handleDelete(user.id)} disabled={isBusy}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </section>
      ) : (
        <section className="panel overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead>
              <tr className="text-left text-slate-500">
                <th className="py-3 pr-4 font-medium">Actor</th>
                <th className="py-3 pr-4 font-medium">Action</th>
                <th className="py-3 pr-4 font-medium">Entity</th>
                <th className="py-3 pr-4 font-medium">Time</th>
                <th className="py-3 font-medium">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {logs.map((log, index) => (
                <tr key={`${log.actorId}-${log.timestamp}-${index}`}>
                  <td className="py-4 pr-4 align-top text-slate-700">{log.actorId}</td>
                  <td className="py-4 pr-4 align-top text-slate-700">{log.actionType}</td>
                  <td className="py-4 pr-4 align-top text-slate-700">
                    <p>{log.entityType}</p>
                    <p className="mt-1 text-xs text-slate-500">{log.entityId}</p>
                  </td>
                  <td className="py-4 pr-4 align-top text-slate-600">{formatDateTime(log.timestamp)}</td>
                  <td className="py-4 align-top">
                    <pre className="max-w-xl overflow-auto whitespace-pre-wrap break-words rounded-xl bg-slate-100 p-3 text-xs text-slate-700">
                      {JSON.stringify(log.details || {}, null, 2)}
                    </pre>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </AppShell>
  )
}
