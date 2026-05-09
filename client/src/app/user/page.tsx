'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/lib/auth-store'
import { projectApi, userApi, logApi } from '@/lib/api'
import { LogOut, Plus, CheckCircle, XCircle } from 'lucide-react'

interface Invite {
  id: string
  projectId: string
  projectName: string
  status: 'PENDING'
}

interface Project {
  id: string
  name: string
  description: string
}

interface LogEntry {
  id: string
  actionType: string
  entityType: string
  timestamp: string
  details: Record<string, any>
}

export default function UserDashboard() {
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [invites, setInvites] = useState<Invite[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [logs, setLogs] = useState<LogEntry[]>([])
  const [activeTab, setActiveTab] = useState<'invites' | 'projects' | 'logs'>('invites')
  const [showCreateProject, setShowCreateProject] = useState(false)
  const [projectForm, setProjectForm] = useState({ name: '', description: '' })

  useEffect(() => {
    if (!user || user.role === 'ADMIN') {
      router.push('/login')
      return
    }

    fetchData()
  }, [user, router])

  const fetchData = async () => {
    try {
      setLoading(true)
      const [invitesRes, logsRes] = await Promise.all([
        projectApi.getMyInvites(),
        logApi.getUserLogs(user?.email || ''),
      ])

      setInvites(invitesRes.data || [])
      setLogs(logsRes.data || [])
    } catch (error: any) {
      console.error('Error fetching data:', error)
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const handleRespondToInvite = async (projectId: string, accept: boolean) => {
    try {
      await projectApi.respondToInvite(projectId, accept)
      toast.success(accept ? 'Invite accepted!' : 'Invite declined!')
      setInvites(invites.filter((i) => i.projectId !== projectId))
    } catch (error) {
      toast.error('Failed to respond to invite')
    }
  }

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!projectForm.name) {
      toast.error('Project name is required')
      return
    }

    try {
      await projectApi.createProject(projectForm.name, projectForm.description)
      toast.success('Project created successfully!')
      setProjectForm({ name: '', description: '' })
      setShowCreateProject(false)
      fetchData()
    } catch (error: any) {
      toast.error('Failed to create project')
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
          <h1 className="text-2xl font-bold text-primary">Sprintify</h1>
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
          {(['invites', 'projects', 'logs'] as const).map((tab) => (
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
            {/* Invites Tab */}
            {activeTab === 'invites' && (
              <div className="grid gap-4">
                {invites.length === 0 ? (
                  <div className="card text-center py-12">
                    <p className="text-gray-500">No pending invites</p>
                  </div>
                ) : (
                  invites.map((invite) => (
                    <div key={invite.id} className="card flex items-center justify-between">
                      <div>
                        <h3 className="font-semibold text-lg">{invite.projectName}</h3>
                        <p className="text-sm text-gray-500">Invitation pending</p>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleRespondToInvite(invite.projectId, true)}
                          className="flex items-center gap-2 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition"
                        >
                          <CheckCircle size={18} />
                          Accept
                        </button>
                        <button
                          onClick={() => handleRespondToInvite(invite.projectId, false)}
                          className="flex items-center gap-2 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                        >
                          <XCircle size={18} />
                          Decline
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}

            {/* Projects Tab */}
            {activeTab === 'projects' && (
              <div>
                <div className="mb-6">
                  {!showCreateProject ? (
                    <button
                      onClick={() => setShowCreateProject(true)}
                      className="flex items-center gap-2 px-4 py-2 btn-primary"
                    >
                      <Plus size={20} />
                      Create Project
                    </button>
                  ) : (
                    <div className="card">
                      <h3 className="font-semibold text-lg mb-4">Create New Project</h3>
                      <form onSubmit={handleCreateProject} className="space-y-4">
                        <div>
                          <label className="block text-sm font-semibold mb-2">Project Name *</label>
                          <input
                            type="text"
                            value={projectForm.name}
                            onChange={(e) =>
                              setProjectForm({ ...projectForm, name: e.target.value })
                            }
                            className="input-field"
                            placeholder="Project name"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-semibold mb-2">Description</label>
                          <textarea
                            value={projectForm.description}
                            onChange={(e) =>
                              setProjectForm({ ...projectForm, description: e.target.value })
                            }
                            className="input-field"
                            placeholder="Project description"
                            rows={3}
                          />
                        </div>
                        <div className="flex gap-2">
                          <button type="submit" className="btn-primary">
                            Create
                          </button>
                          <button
                            type="button"
                            onClick={() => setShowCreateProject(false)}
                            className="btn-outline"
                          >
                            Cancel
                          </button>
                        </div>
                      </form>
                    </div>
                  )}
                </div>

                <div className="grid gap-4">
                  {projects.length === 0 ? (
                    <div className="card text-center py-12">
                      <p className="text-gray-500">No projects yet. Create one to get started!</p>
                    </div>
                  ) : (
                    projects.map((project) => (
                      <div key={project.id} className="card">
                        <h3 className="font-semibold text-lg">{project.name}</h3>
                        <p className="text-gray-600">{project.description}</p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {/* Logs Tab */}
            {activeTab === 'logs' && (
              <div className="card overflow-x-auto">
                {logs.length === 0 ? (
                  <p className="text-center py-12 text-gray-500">No logs yet</p>
                ) : (
                  <table className="w-full">
                    <thead className="border-b">
                      <tr>
                        <th className="text-left py-3 px-4 font-semibold">Action</th>
                        <th className="text-left py-3 px-4 font-semibold">Entity</th>
                        <th className="text-left py-3 px-4 font-semibold">Time</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {logs.map((log) => (
                        <tr key={log.id} className="hover:bg-gray-50">
                          <td className="py-3 px-4">{log.actionType}</td>
                          <td className="py-3 px-4">{log.entityType}</td>
                          <td className="py-3 px-4 text-sm text-gray-500">
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
