'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { AppShell } from '@/components/app-shell'
import { LoadingScreen } from '@/components/loading-screen'
import { StatusPill } from '@/components/status-pill'
import { getApiErrorMessage, projectApi } from '@/lib/api'
import { formatDateTime } from '@/lib/format'
import { useRequiredAuth } from '@/lib/guards'
import { InviteResponse, MyProjectResponse } from '@/lib/types'

export default function ProjectsPage() {
  const router = useRouter()
  const { hydrated, user, ready } = useRequiredAuth()
  const [loading, setLoading] = useState(true)
  const [projects, setProjects] = useState<MyProjectResponse[]>([])
  const [invites, setInvites] = useState<InviteResponse[]>([])
  const [creating, setCreating] = useState(false)
  const [formData, setFormData] = useState({ name: '', description: '' })

  const loadData = async () => {
    setLoading(true)
    try {
      const [loadedProjects, loadedInvites] = await Promise.all([
        projectApi.listMyProjects(),
        projectApi.getMyInvites(),
      ])
      setProjects(loadedProjects)
      setInvites(loadedInvites)
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

    if (user?.role === 'ADMIN') {
      router.replace('/admin')
      return
    }

    void loadData()
  }, [ready, router, user?.role])

  const handleCreateProject = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!formData.name.trim()) {
      toast.error('Project name is required.')
      return
    }

    setCreating(true)
    try {
      await projectApi.createProject({
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
      })
      setFormData({ name: '', description: '' })
      toast.success('Project created successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setCreating(false)
    }
  }

  const handleInviteDecision = async (projectId: string, decision: 'ACCEPT' | 'REJECT') => {
    try {
      await projectApi.respondToInvite(projectId, decision)
      setInvites((current) => current.filter((invite) => invite.projectId !== projectId))
      toast.success(decision === 'ACCEPT' ? 'Invite accepted.' : 'Invite rejected.')
      if (decision === 'ACCEPT') {
        await loadData()
      }
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    }
  }

  if (!hydrated || !ready || user?.role === 'ADMIN') {
    return <LoadingScreen label="Loading projects..." />
  }

  return (
    <AppShell
      title="Your projects"
      description="See every project where you are an active member, create a new project, and respond to pending invites."
    >
      <section className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="panel">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-slate-900">Current projects</h2>
              <p className="text-sm text-slate-500">Open a project dashboard to manage backlog, sprints, and members.</p>
            </div>
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
              {projects.length} total
            </span>
          </div>
          {loading ? (
            <p className="text-sm text-slate-500">Loading projects...</p>
          ) : projects.length === 0 ? (
            <p className="text-sm text-slate-500">You do not have active projects yet.</p>
          ) : (
            <div className="grid gap-4">
              {projects.map((project) => (
                <Link
                  key={project.id}
                  href={`/projects/${project.id}`}
                  className="rounded-2xl border border-slate-200 p-4 transition hover:border-slate-900"
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-semibold text-slate-900">{project.name}</h3>
                      <p className="mt-1 text-sm text-slate-500">{project.description || 'No description provided.'}</p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <StatusPill label={project.state} />
                      <StatusPill label={project.currentUserRole} />
                    </div>
                  </div>
                  <div className="mt-4 grid gap-2 text-xs text-slate-500 md:grid-cols-3">
                    <p>Project ID: {project.id}</p>
                    <p>Members: {project.memberCount}</p>
                    <p>Created: {formatDateTime(project.createdAt)}</p>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
        <div className="space-y-6">
          <section className="panel">
            <h2 className="text-lg font-semibold text-slate-900">Create project</h2>
            <p className="mt-1 text-sm text-slate-500">Create a project and become its PO automatically.</p>
            <form className="mt-4 space-y-4" onSubmit={handleCreateProject}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Name</label>
                <input
                  className="input"
                  value={formData.name}
                  onChange={(event) => setFormData((current) => ({ ...current, name: event.target.value }))}
                  placeholder="Platform redesign"
                  disabled={creating}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Description</label>
                <textarea
                  className="textarea"
                  rows={4}
                  value={formData.description}
                  onChange={(event) =>
                    setFormData((current) => ({ ...current, description: event.target.value }))
                  }
                  placeholder="Optional project summary"
                  disabled={creating}
                />
              </div>
              <button type="submit" className="button-primary w-full" disabled={creating}>
                {creating ? 'Creating project...' : 'Create project'}
              </button>
            </form>
          </section>
          <section className="panel">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Pending invites</h2>
                <p className="text-sm text-slate-500">Accept an invite to add the project to your workspace.</p>
              </div>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                {invites.length} pending
              </span>
            </div>
            {loading ? (
              <p className="text-sm text-slate-500">Loading invites...</p>
            ) : invites.length === 0 ? (
              <p className="text-sm text-slate-500">You have no pending invites.</p>
            ) : (
              <div className="space-y-4">
                {invites.map((invite) => (
                  <div key={invite.projectId} className="rounded-2xl border border-slate-200 p-4">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="font-semibold text-slate-900">{invite.projectName}</h3>
                        <p className="mt-1 text-sm text-slate-500">Role: {invite.role}</p>
                        <p className="mt-1 text-xs text-slate-500">Invited: {formatDateTime(invite.invitedAt)}</p>
                      </div>
                      <StatusPill label={invite.status} />
                    </div>
                    <div className="mt-4 flex flex-wrap gap-2">
                      <button
                        type="button"
                        className="button-primary"
                        onClick={() => void handleInviteDecision(invite.projectId, 'ACCEPT')}
                      >
                        Accept
                      </button>
                      <button
                        type="button"
                        className="button-secondary"
                        onClick={() => void handleInviteDecision(invite.projectId, 'REJECT')}
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      </section>
    </AppShell>
  )
}
