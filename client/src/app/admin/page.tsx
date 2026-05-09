'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/lib/auth-store'
import { adminApi, logApi } from '@/lib/api'
import { LogOut, Trash2, Lock, Unlock } from 'lucide-react'

interface User {
  id: string
  email: string
  role: string
  banned?: boolean
  banExpiry?: string
}

interface LogEntry {
  id: string
  actorId: string
  actionType: string
  entityType: string
  entityId: string
  timestamp: string
  details: Record<string, any>
}

export default function AdminDashboard() {
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [users, setUsers] = useState<User[]>([])
  const [logs, setLogs] = useState<LogEntry[]>([])
  const [activeTab, setActiveTab] = useState<'users' | 'logs'>('users')
  const [banDays, setBanDays] = useState<{ [key: string]: number }>({})
  const [deleting, setDeleting] = useState<{ [key: string]: boolean }>({})

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      router.push('/login')
      return
    }

    fetchData()
  }, [user, router])

  const fetchData = async () => {
    try {
      setLoading(true)
      const [usersRes, logsRes] = await Promise.all([
        adminApi.getAllUsers(),
        logApi.getUserLogs('SYSTEM'),
      ])

      setUsers(usersRes.data || [])
      setLogs(logsRes.data || [])
    } catch (error: any) {
      console.error('Error fetching data:', error)
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteUser = async (userId: string, email: string) => {
    if (!confirm(`Are you sure you want to delete ${email}?`)) return

    try {
      setDeleting({ ...deleting, [userId]: true })
      await adminApi.deleteUser(userId)
      toast.success('User deleted successfully')
      setUsers(users.filter((u) => u.id !== userId))
    } catch (error: any) {
      toast.error('Failed to delete user')
    } finally {
      setDeleting({ ...deleting, [userId]: false })
    }
  }

  const handleBanUser = async (userId: string) => {
    const days = banDays[userId]
    if (!days || days < 1) {
      toast.error('Please enter number of days')
      return
    }

    try {
      await adminApi.banUser(userId, days)
      toast.success(`User banned for ${days} days`)
      setBanDays({ ...banDays, [userId]: 0 })
      fetchData()
    } catch (error: any) {
      toast.error('Failed to ban user')
    }
  }

  const handleLogout = () => {
    logout()
    router.push('/login')
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-primary">Sprintify - Admin</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">{user?.email}</span>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
            >
              <LogOut size={18} />
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Tabs */}
        <div className="flex gap-4 mb-8 border-b border-gray-200">
          {(['users', 'logs'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-3 font-semibold border-b-2 transition ${
                activeTab === tab
                  ? 'text-primary border-primary'
                  : 'text-gray-600 border-transparent hover:text-gray-900'
              }`}
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-gray-500">Loading...</div>
          </div>
        ) : (
          <>
            {/* Users Tab */}
            {activeTab === 'users' && (
              <div className="card overflow-x-auto">
                {users.length === 0 ? (
                  <p className="text-center py-12 text-gray-500">No users found</p>
                ) : (
                  <table className="w-full">
                    <thead className="border-b bg-gray-50">
                      <tr>
                        <th className="text-left py-4 px-6 font-semibold">Email</th>
                        <th className="text-left py-4 px-6 font-semibold">Role</th>
                        <th className="text-left py-4 px-6 font-semibold">Status</th>
                        <th className="text-left py-4 px-6 font-semibold">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {users.map((u) => (
                        <tr key={u.id} className="hover:bg-gray-50">
                          <td className="py-4 px-6">{u.email}</td>
                          <td className="py-4 px-6">
                            <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-semibold">
                              {u.role}
                            </span>
                          </td>
                          <td className="py-4 px-6">
                            <span
                              className={`px-3 py-1 rounded-full text-sm font-semibold ${
                                u.banned
                                  ? 'bg-red-100 text-red-800'
                                  : 'bg-green-100 text-green-800'
                              }`}
                            >
                              {u.banned ? 'Banned' : 'Active'}
                            </span>
                          </td>
                          <td className="py-4 px-6">
                            <div className="flex gap-2 items-center">
                              <div className="flex gap-1">
                                <input
                                  type="number"
                                  min="1"
                                  placeholder="Days"
                                  value={banDays[u.id] || ''}
                                  onChange={(e) =>
                                    setBanDays({ ...banDays, [u.id]: parseInt(e.target.value) })
                                  }
                                  className="w-16 px-2 py-1 border border-gray-300 rounded text-sm"
                                />
                                <button
                                  onClick={() => handleBanUser(u.id)}
                                  className="flex items-center gap-1 px-3 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600 transition text-sm"
                                >
                                  <Lock size={16} />
                                  Ban
                                </button>
                              </div>
                              <button
                                onClick={() => handleDeleteUser(u.id, u.email)}
                                disabled={deleting[u.id]}
                                className="flex items-center gap-1 px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 transition disabled:opacity-50 text-sm"
                              >
                                <Trash2 size={16} />
                                {deleting[u.id] ? 'Deleting...' : 'Delete'}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            )}

            {/* Logs Tab */}
            {activeTab === 'logs' && (
              <div className="card overflow-x-auto">
                {logs.length === 0 ? (
                  <p className="text-center py-12 text-gray-500">No logs found</p>
                ) : (
                  <table className="w-full text-sm">
                    <thead className="border-b bg-gray-50">
                      <tr>
                        <th className="text-left py-4 px-6 font-semibold">Actor</th>
                        <th className="text-left py-4 px-6 font-semibold">Action</th>
                        <th className="text-left py-4 px-6 font-semibold">Entity</th>
                        <th className="text-left py-4 px-6 font-semibold">Time</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {logs.map((log) => (
                        <tr key={log.id} className="hover:bg-gray-50">
                          <td className="py-4 px-6 max-w-xs truncate">{log.actorId}</td>
                          <td className="py-4 px-6">{log.actionType}</td>
                          <td className="py-4 px-6">
                            <span className="px-2 py-1 bg-gray-100 rounded text-xs">
                              {log.entityType}
                            </span>
                          </td>
                          <td className="py-4 px-6 text-gray-500">
                            {new Date(log.timestamp).toLocaleString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
