'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { AppShell } from '@/components/app-shell'
import { LoadingScreen } from '@/components/loading-screen'
import { StatusPill } from '@/components/status-pill'
import { getApiErrorMessage, projectApi } from '@/lib/api'
import { formatDate, formatDateTime } from '@/lib/format'
import { useRequiredAuth } from '@/lib/guards'
import {
  BacklogItem,
  BacklogItemStatus,
  BacklogItemType,
  Difficulty,
  MyProjectResponse,
  Priority,
  ProjectMemberResponse,
  ProjectMemberRole,
  SprintResponse,
} from '@/lib/types'

const projectRoles: ProjectMemberRole[] = ['PO', 'SM', 'DEV']
const backlogStatuses: BacklogItemStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']
const priorities: Priority[] = ['HIGH', 'MEDIUM', 'LOW']
const difficulties: Difficulty[] = ['EASY', 'MEDIUM', 'HARD']

export default function ProjectDashboardPage() {
  const params = useParams<{ projectId: string }>()
  const router = useRouter()
  const projectId = params.projectId
  const { hydrated, user, ready } = useRequiredAuth()

  // Validate projectId
  if (!projectId || !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(projectId)) {
    return (
      <AppShell>
        <div className="container mx-auto px-4 py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-red-600 mb-4">Invalid Project ID</h1>
            <p className="text-gray-600 mb-4">The project ID provided is not valid.</p>
            <button
              onClick={() => router.push('/projects')}
              className="btn btn-primary"
            >
              Back to Projects
            </button>
          </div>
        </div>
      </AppShell>
    )
  }
  const [loading, setLoading] = useState(true)
  const [project, setProject] = useState<MyProjectResponse | null>(null)
  const [members, setMembers] = useState<ProjectMemberResponse[]>([])
  const [backlog, setBacklog] = useState<BacklogItem[]>([])
  const [sprints, setSprints] = useState<SprintResponse[]>([])
  const [activeTab, setActiveTab] = useState<'overview' | 'members' | 'backlog' | 'sprints'>('overview')
  const [busyId, setBusyId] = useState<string | null>(null)
  const [inviteForm, setInviteForm] = useState({ email: '', role: 'DEV' as ProjectMemberRole })
  const [backlogForm, setBacklogForm] = useState({
    type: 'EPIC' as BacklogItemType,
    title: '',
    description: '',
    priority: 'MEDIUM' as Priority,
    difficulty: 'MEDIUM' as Difficulty,
    estimatedPoints: '',
    parentId: '',
  })
  const [sprintForm, setSprintForm] = useState({ title: '', startDate: '', endDate: '', sprintGoal: '' })
  const [addItemInputs, setAddItemInputs] = useState<Record<string, string>>({})

  const canManageMembers = project?.currentUserRole === 'PO'
  const canManageSprints = project?.currentUserRole === 'PO' || project?.currentUserRole === 'SM'
  const canCreateEpics = project?.currentUserRole === 'PO' || project?.currentUserRole === 'SM'
  const canCreateTasks = project?.currentUserRole === 'DEV'

  const allowedBacklogTypes = useMemo<BacklogItemType[]>(() => {
    if (canCreateEpics) {
      return ['EPIC', 'USER_STORY']
    }

    if (canCreateTasks) {
      return ['TASK', 'BUG']
    }

    return ['EPIC']
  }, [canCreateEpics, canCreateTasks])

  useEffect(() => {
    if (!allowedBacklogTypes.includes(backlogForm.type)) {
      setBacklogForm((current) => ({ ...current, type: allowedBacklogTypes[0] }))
    }
  }, [allowedBacklogTypes, backlogForm.type])

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      console.log('Loading project data for:', projectId)

      // Load projects first
      const projects = await projectApi.listMyProjects()
      console.log('Projects loaded:', projects.length)

      const currentProject = projects.find((entry) => entry.id === projectId)
      if (!currentProject) {
        console.log('Project not found:', projectId)
        setProject(null)
        setMembers([])
        setBacklog([])
        setSprints([])
        return
      }

      console.log('Current project found:', currentProject.name)
      setProject(currentProject)

      // Load other data in parallel
      const [projectMembers, backlogPage, sprintList] = await Promise.all([
        projectApi.getProjectMembers(projectId).catch((error) => {
          console.error('Error loading members:', error)
          return []
        }),
        projectApi.listBacklogItems(projectId).catch((error) => {
          console.error('Error loading backlog:', error)
          return { content: [] }
        }),
        projectApi.listSprints(projectId).catch((error) => {
          console.error('Error loading sprints:', error)
          return []
        }),
      ])

      setMembers(projectMembers)
      setBacklog(backlogPage.content || [])
      setSprints(sprintList)
    } catch (error) {
      console.error('Error in loadData:', error)
      toast.error(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    if (!ready) {
      return
    }

    if (user?.role === 'ADMIN') {
      router.replace('/admin')
      return
    }

    void loadData()
  }, [loadData, ready, router, user?.role])

  const sprintLookup = useMemo(
    () => Object.fromEntries(sprints.map((sprint) => [sprint.id, sprint.title])),
    [sprints]
  )

  const parentCandidates = backlog.filter((item) =>
    backlogForm.type === 'USER_STORY' ? item.type === 'EPIC' : item.type === 'USER_STORY'
  )

  const handleInvite = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!inviteForm.email.trim()) {
      toast.error('Enter an email to invite.')
      return
    }

    setBusyId('invite')
    try {
      await projectApi.inviteMember(projectId, {
        email: inviteForm.email.trim(),
        role: inviteForm.role,
      })
      setInviteForm({ email: '', role: 'DEV' })
      toast.success('Invite sent successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleRemoveMember = async (memberId: string) => {
    setBusyId(memberId)
    try {
      await projectApi.removeMember(projectId, memberId)
      toast.success('Member removed successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleCreateBacklogItem = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!backlogForm.title.trim()) {
      toast.error('Backlog title is required.')
      return
    }

    if ((backlogForm.type === 'TASK' || backlogForm.type === 'BUG') && !backlogForm.parentId) {
      toast.error('Tasks and bugs must have a parent user story.')
      return
    }

    setBusyId('backlog-create')
    try {
      await projectApi.createBacklogItem(projectId, {
        type: backlogForm.type,
        title: backlogForm.title.trim(),
        description: backlogForm.description.trim() || undefined,
        priority: backlogForm.priority,
        difficulty: backlogForm.difficulty,
        parentId: backlogForm.parentId || undefined,
        estimatedPoints: backlogForm.estimatedPoints ? Number(backlogForm.estimatedPoints) : undefined,
      })
      setBacklogForm({
        type: allowedBacklogTypes[0],
        title: '',
        description: '',
        priority: 'MEDIUM',
        difficulty: 'MEDIUM',
        estimatedPoints: '',
        parentId: '',
      })
      toast.success('Backlog item created successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleStatusChange = async (itemId: string, status: BacklogItemStatus) => {
    setBusyId(itemId)
    try {
      await projectApi.updateBacklogStatus(projectId, itemId, status)
      toast.success('Backlog status updated.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleSelfAssign = async (itemId: string) => {
    setBusyId(itemId)
    try {
      await projectApi.assignBacklogItemToSelf(projectId, itemId)
      toast.success('Item assigned to you.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleMoveToSprint = async (itemId: string, sprintId: string | null) => {
    setBusyId(itemId)
    try {
      await projectApi.moveBacklogItemToSprint(projectId, itemId, sprintId)
      toast.success('Backlog item updated.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleDeleteItem = async (itemId: string) => {
    setBusyId(itemId)
    try {
      await projectApi.deleteBacklogItem(projectId, itemId)
      toast.success('Backlog item deleted.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleCreateSprint = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!sprintForm.title.trim() || !sprintForm.startDate || !sprintForm.endDate) {
      toast.error('Sprint title and dates are required.')
      return
    }

    setBusyId('sprint-create')
    try {
      await projectApi.createSprint(projectId, {
        title: sprintForm.title.trim(),
        startDate: sprintForm.startDate,
        endDate: sprintForm.endDate,
        sprintGoal: sprintForm.sprintGoal.trim() || undefined,
      })
      setSprintForm({ title: '', startDate: '', endDate: '', sprintGoal: '' })
      toast.success('Sprint created successfully.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleSprintTransition = async (sprintId: string, action: 'start' | 'complete' | 'delete') => {
    setBusyId(sprintId)
    try {
      if (action === 'start') {
        await projectApi.startSprint(projectId, sprintId)
        toast.success('Sprint started successfully.')
      } else if (action === 'complete') {
        await projectApi.completeSprint(projectId, sprintId)
        toast.success('Sprint completed successfully.')
      } else {
        await projectApi.deleteSprint(projectId, sprintId)
        toast.success('Sprint deleted successfully.')
      }
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleAddItemsToSprint = async (sprintId: string) => {
    const raw = addItemInputs[sprintId] || ''
    const itemIds = raw
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)

    if (itemIds.length === 0) {
      toast.error('Enter one or more backlog item IDs.')
      return
    }

    setBusyId(sprintId)
    try {
      await projectApi.addItemsToSprint(projectId, sprintId, itemIds)
      setAddItemInputs((current) => ({ ...current, [sprintId]: '' }))
      toast.success('Items added to sprint.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  const handleRemoveItemFromSprint = async (sprintId: string, itemId: string) => {
    setBusyId(itemId)
    try {
      await projectApi.removeItemFromSprint(projectId, sprintId, itemId)
      toast.success('Item removed from sprint.')
      await loadData()
    } catch (error) {
      toast.error(getApiErrorMessage(error))
    } finally {
      setBusyId(null)
    }
  }

  if (!hydrated || !ready || user?.role === 'ADMIN') {
    return <LoadingScreen label="Loading project dashboard..." />
  }

  if (loading) {
    return (
      <AppShell title="Project dashboard" description="Loading project data.">
        <div className="panel text-sm text-slate-500">Loading project details...</div>
      </AppShell>
    )
  }

  if (!project) {
    return (
      <AppShell title="Project not found" description="This project is not part of your current workspace.">
        <div className="panel space-y-4">
          <p className="text-sm text-slate-600">You may not be an active member, or the project no longer exists.</p>
          <button type="button" className="button-primary" onClick={() => router.replace('/projects')}>
            Back to projects
          </button>
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell
      title={project.name}
      description={project.description || 'Project dashboard for backlog, members, and sprint planning.'}
      actions={
        <div className="flex flex-wrap gap-2">
          <StatusPill label={project.state} />
          <StatusPill label={project.currentUserRole} />
        </div>
      }
    >
      <div className="flex flex-wrap gap-2">
        {(['overview', 'members', 'backlog', 'sprints'] as const).map((tab) => (
          <button
            key={tab}
            type="button"
            className={activeTab === tab ? 'button-primary' : 'button-secondary'}
            onClick={() => setActiveTab(tab)}
          >
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === 'overview' ? (
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="panel">
            <p className="text-sm text-slate-500">Your role</p>
            <p className="mt-2 text-2xl font-semibold text-slate-900">{project.currentUserRole}</p>
          </div>
          <div className="panel">
            <p className="text-sm text-slate-500">Members</p>
            <p className="mt-2 text-2xl font-semibold text-slate-900">{members.length}</p>
          </div>
          <div className="panel">
            <p className="text-sm text-slate-500">Backlog items</p>
            <p className="mt-2 text-2xl font-semibold text-slate-900">{backlog.length}</p>
          </div>
          <div className="panel">
            <p className="text-sm text-slate-500">Sprints</p>
            <p className="mt-2 text-2xl font-semibold text-slate-900">{sprints.length}</p>
          </div>
          <div className="panel md:col-span-2 xl:col-span-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Project details</h2>
                <dl className="mt-4 grid gap-3 text-sm text-slate-600">
                  <div>
                    <dt className="font-medium text-slate-900">Project ID</dt>
                    <dd>{project.id}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-slate-900">Owner ID</dt>
                    <dd>{project.ownerId}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-slate-900">Created</dt>
                    <dd>{formatDateTime(project.createdAt)}</dd>
                  </div>
                </dl>
              </div>
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Sprint snapshot</h2>
                <div className="mt-4 space-y-3">
                  {sprints.length === 0 ? (
                    <p className="text-sm text-slate-500">No sprints created yet.</p>
                  ) : (
                    sprints.map((sprint) => (
                      <div key={sprint.id} className="rounded-2xl border border-slate-200 p-4">
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          <div>
                            <p className="font-semibold text-slate-900">{sprint.title}</p>
                            <p className="text-xs text-slate-500">
                              {formatDate(sprint.startDate)} → {formatDate(sprint.endDate)}
                            </p>
                          </div>
                          <StatusPill label={sprint.status} />
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          </div>
        </section>
      ) : null}

      {activeTab === 'members' ? (
        <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
          <section className="panel">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Team members</h2>
                <p className="text-sm text-slate-500">Active members and pending invites inside this project.</p>
              </div>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                {members.length} records
              </span>
            </div>
            <div className="space-y-4">
              {members.map((member) => (
                <div key={`${member.userId}-${member.joinedAt}`} className="rounded-2xl border border-slate-200 p-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold text-slate-900">{member.userId}</p>
                      <p className="mt-1 text-xs text-slate-500">Joined: {formatDateTime(member.joinedAt)}</p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <StatusPill label={member.role} />
                      <StatusPill label={member.status} />
                    </div>
                  </div>
                  {canManageMembers && member.role !== 'PO' ? (
                    <div className="mt-4">
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => void handleRemoveMember(member.userId)}
                        disabled={busyId === member.userId}
                      >
                        Remove member
                      </button>
                    </div>
                  ) : null}
                </div>
              ))}
            </div>
          </section>
          <section className="panel">
            <h2 className="text-lg font-semibold text-slate-900">Invite teammate</h2>
            <p className="mt-1 text-sm text-slate-500">
              Only the project PO can invite members through this form.
            </p>
            {canManageMembers ? (
              <form className="mt-4 space-y-4" onSubmit={handleInvite}>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Email</label>
                  <input
                    className="input"
                    value={inviteForm.email}
                    onChange={(event) => setInviteForm((current) => ({ ...current, email: event.target.value }))}
                    placeholder="teammate@example.com"
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Project role</label>
                  <select
                    className="select"
                    value={inviteForm.role}
                    onChange={(event) =>
                      setInviteForm((current) => ({ ...current, role: event.target.value as ProjectMemberRole }))
                    }
                  >
                    {projectRoles.map((role) => (
                      <option key={role} value={role}>
                        {role}
                      </option>
                    ))}
                  </select>
                </div>
                <button type="submit" className="button-primary" disabled={busyId === 'invite'}>
                  Send invite
                </button>
              </form>
            ) : (
              <p className="mt-4 rounded-2xl bg-slate-100 p-4 text-sm text-slate-600">
                Your current project role is {project.currentUserRole}. Invites are limited to the project PO.
              </p>
            )}
          </section>
        </div>
      ) : null}

      {activeTab === 'backlog' ? (
        <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
          <section className="panel">
            <h2 className="text-lg font-semibold text-slate-900">Create backlog item</h2>
            <p className="mt-1 text-sm text-slate-500">
              PO/SM can create epics and stories. DEV members can create tasks and bugs.
            </p>
            {canCreateEpics || canCreateTasks ? (
              <form className="mt-4 space-y-4" onSubmit={handleCreateBacklogItem}>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Type</label>
                  <select
                    className="select"
                    value={backlogForm.type}
                    onChange={(event) =>
                      setBacklogForm((current) => ({ ...current, type: event.target.value as BacklogItemType }))
                    }
                  >
                    {allowedBacklogTypes.map((type) => (
                      <option key={type} value={type}>
                        {type.replaceAll('_', ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Title</label>
                  <input
                    className="input"
                    value={backlogForm.title}
                    onChange={(event) => setBacklogForm((current) => ({ ...current, title: event.target.value }))}
                    placeholder="Define dashboard metrics"
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Description</label>
                  <textarea
                    className="textarea"
                    rows={4}
                    value={backlogForm.description}
                    onChange={(event) =>
                      setBacklogForm((current) => ({ ...current, description: event.target.value }))
                    }
                    placeholder="Optional details"
                  />
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">Priority</label>
                    <select
                      className="select"
                      value={backlogForm.priority}
                      onChange={(event) =>
                        setBacklogForm((current) => ({ ...current, priority: event.target.value as Priority }))
                      }
                    >
                      {priorities.map((priority) => (
                        <option key={priority} value={priority}>
                          {priority}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">Difficulty</label>
                    <select
                      className="select"
                      value={backlogForm.difficulty}
                      onChange={(event) =>
                        setBacklogForm((current) => ({ ...current, difficulty: event.target.value as Difficulty }))
                      }
                    >
                      {difficulties.map((difficulty) => (
                        <option key={difficulty} value={difficulty}>
                          {difficulty}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Estimated points</label>
                  <input
                    className="input"
                    type="number"
                    min="0"
                    value={backlogForm.estimatedPoints}
                    onChange={(event) =>
                      setBacklogForm((current) => ({ ...current, estimatedPoints: event.target.value }))
                    }
                    placeholder="0"
                  />
                </div>
                {backlogForm.type !== 'EPIC' ? (
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">Parent item</label>
                    <select
                      className="select"
                      value={backlogForm.parentId}
                      onChange={(event) =>
                        setBacklogForm((current) => ({ ...current, parentId: event.target.value }))
                      }
                    >
                      <option value="">Select parent</option>
                      {parentCandidates.map((item) => (
                        <option key={item.id} value={item.id}>
                          {item.title} ({item.id})
                        </option>
                      ))}
                    </select>
                  </div>
                ) : null}
                <button type="submit" className="button-primary" disabled={busyId === 'backlog-create'}>
                  Create backlog item
                </button>
              </form>
            ) : (
              <p className="mt-4 rounded-2xl bg-slate-100 p-4 text-sm text-slate-600">
                Your current project role cannot create new backlog items.
              </p>
            )}
          </section>
          <section className="panel">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Backlog items</h2>
                <p className="text-sm text-slate-500">Update status, self-assign work, or move items into sprints.</p>
              </div>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                {backlog.length} items
              </span>
            </div>
            <div className="space-y-4">
              {backlog.map((item) => (
                <div key={item.id} className="rounded-2xl border border-slate-200 p-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h3 className="font-semibold text-slate-900">{item.title}</h3>
                      <p className="mt-1 text-sm text-slate-500">{item.description || 'No description provided.'}</p>
                      <p className="mt-2 text-xs text-slate-500">Item ID: {item.id}</p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <StatusPill label={item.type} />
                      <StatusPill label={item.status} />
                    </div>
                  </div>
                  <div className="mt-4 grid gap-3 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
                    <p>Priority: {item.priority}</p>
                    <p>Difficulty: {item.difficulty}</p>
                    <p>Points: {item.estimatedPoints ?? '—'}</p>
                    <p>Sprint: {item.sprintId ? sprintLookup[item.sprintId] || item.sprintId : 'Backlog'}</p>
                    <p>Assignee: {item.assigneeId || 'Unassigned'}</p>
                    <p>Parent: {item.parentId || 'None'}</p>
                  </div>
                  <div className="mt-4 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
                    <div className="flex flex-wrap items-center gap-2">
                      <select
                        className="select max-w-[190px]"
                        value={item.status}
                        onChange={(event) => void handleStatusChange(item.id, event.target.value as BacklogItemStatus)}
                        disabled={busyId === item.id}
                      >
                        {backlogStatuses.map((status) => (
                          <option key={status} value={status}>
                            {status.replaceAll('_', ' ')}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        className="button-secondary"
                        onClick={() => void handleSelfAssign(item.id)}
                        disabled={busyId === item.id}
                      >
                        Assign to me
                      </button>
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => void handleDeleteItem(item.id)}
                        disabled={busyId === item.id}
                      >
                        Delete
                      </button>
                    </div>
                    {canManageSprints ? (
                      <select
                        className="select max-w-[240px]"
                        value={item.sprintId || ''}
                        onChange={(event) =>
                          void handleMoveToSprint(item.id, event.target.value ? event.target.value : null)
                        }
                        disabled={busyId === item.id}
                      >
                        <option value="">Backlog</option>
                        {sprints.map((sprint) => (
                          <option key={sprint.id} value={sprint.id}>
                            {sprint.title}
                          </option>
                        ))}
                      </select>
                    ) : null}
                  </div>
                </div>
              ))}
              {backlog.length === 0 ? <p className="text-sm text-slate-500">No backlog items created yet.</p> : null}
            </div>
          </section>
        </div>
      ) : null}

      {activeTab === 'sprints' ? (
        <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
          <section className="panel">
            <h2 className="text-lg font-semibold text-slate-900">Create sprint</h2>
            <p className="mt-1 text-sm text-slate-500">PO and SM members can create and manage sprint plans.</p>
            {canManageSprints ? (
              <form className="mt-4 space-y-4" onSubmit={handleCreateSprint}>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Title</label>
                  <input
                    className="input"
                    value={sprintForm.title}
                    onChange={(event) => setSprintForm((current) => ({ ...current, title: event.target.value }))}
                    placeholder="Sprint 1"
                  />
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">Start date</label>
                    <input
                      className="input"
                      type="date"
                      value={sprintForm.startDate}
                      onChange={(event) =>
                        setSprintForm((current) => ({ ...current, startDate: event.target.value }))
                      }
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">End date</label>
                    <input
                      className="input"
                      type="date"
                      value={sprintForm.endDate}
                      onChange={(event) =>
                        setSprintForm((current) => ({ ...current, endDate: event.target.value }))
                      }
                    />
                  </div>
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">Sprint goal</label>
                  <textarea
                    className="textarea"
                    rows={4}
                    value={sprintForm.sprintGoal}
                    onChange={(event) =>
                      setSprintForm((current) => ({ ...current, sprintGoal: event.target.value }))
                    }
                    placeholder="Deliver first release candidate"
                  />
                </div>
                <button type="submit" className="button-primary" disabled={busyId === 'sprint-create'}>
                  Create sprint
                </button>
              </form>
            ) : (
              <p className="mt-4 rounded-2xl bg-slate-100 p-4 text-sm text-slate-600">
                Your current project role cannot create or manage sprints.
              </p>
            )}
          </section>
          <section className="space-y-4">
            {sprints.length === 0 ? (
              <div className="panel text-sm text-slate-500">No sprints created yet.</div>
            ) : (
              sprints.map((sprint) => (
                <div key={sprint.id} className="panel">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h2 className="text-lg font-semibold text-slate-900">{sprint.title}</h2>
                      <p className="mt-1 text-sm text-slate-500">{sprint.sprintGoal || 'No sprint goal provided.'}</p>
                    </div>
                    <StatusPill label={sprint.status} />
                  </div>
                  <div className="mt-4 grid gap-3 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
                    <p>Start: {formatDate(sprint.startDate)}</p>
                    <p>End: {formatDate(sprint.endDate)}</p>
                    <p>Total points: {sprint.totalPoints ?? 0}</p>
                    <p>Completed points: {sprint.completedPoints ?? 0}</p>
                  </div>
                  {canManageSprints ? (
                    <div className="mt-4 flex flex-wrap gap-2">
                      {sprint.status === 'PLANNING' ? (
                        <button
                          type="button"
                          className="button-primary"
                          onClick={() => void handleSprintTransition(sprint.id, 'start')}
                          disabled={busyId === sprint.id}
                        >
                          Start sprint
                        </button>
                      ) : null}
                      {sprint.status === 'ACTIVE' ? (
                        <button
                          type="button"
                          className="button-secondary"
                          onClick={() => void handleSprintTransition(sprint.id, 'complete')}
                          disabled={busyId === sprint.id}
                        >
                          Complete sprint
                        </button>
                      ) : null}
                      {sprint.status === 'PLANNING' ? (
                        <button
                          type="button"
                          className="button-danger"
                          onClick={() => void handleSprintTransition(sprint.id, 'delete')}
                          disabled={busyId === sprint.id}
                        >
                          Delete sprint
                        </button>
                      ) : null}
                    </div>
                  ) : null}
                  {canManageSprints ? (
                    <div className="mt-5 rounded-2xl border border-slate-200 p-4">
                      <label className="mb-2 block text-sm font-medium text-slate-700">Add item IDs to sprint</label>
                      <div className="flex flex-col gap-2 md:flex-row">
                        <input
                          className="input"
                          value={addItemInputs[sprint.id] || ''}
                          onChange={(event) =>
                            setAddItemInputs((current) => ({ ...current, [sprint.id]: event.target.value }))
                          }
                          placeholder="Comma-separated backlog item IDs"
                        />
                        <button
                          type="button"
                          className="button-primary"
                          onClick={() => void handleAddItemsToSprint(sprint.id)}
                          disabled={busyId === sprint.id}
                        >
                          Add items
                        </button>
                      </div>
                    </div>
                  ) : null}
                  <div className="mt-5 space-y-3">
                    <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Sprint items</h3>
                    {sprint.backlogItems.length === 0 ? (
                      <p className="text-sm text-slate-500">No items in this sprint.</p>
                    ) : (
                      sprint.backlogItems.map((item) => (
                        <div key={item.id} className="rounded-2xl border border-slate-200 p-4">
                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div>
                              <p className="font-semibold text-slate-900">{item.title}</p>
                              <p className="mt-1 text-xs text-slate-500">{item.id}</p>
                            </div>
                            <div className="flex flex-wrap gap-2">
                              <StatusPill label={item.type} />
                              <StatusPill label={item.status} />
                            </div>
                          </div>
                          {canManageSprints && sprint.status === 'PLANNING' ? (
                            <div className="mt-4">
                              <button
                                type="button"
                                className="button-secondary"
                                onClick={() => void handleRemoveItemFromSprint(sprint.id, item.id)}
                                disabled={busyId === item.id}
                              >
                                Remove from sprint
                              </button>
                            </div>
                          ) : null}
                        </div>
                      ))
                    )}
                  </div>
                </div>
              ))
            )}
          </section>
        </div>
      ) : null}
    </AppShell>
  )
}
