import axios, { AxiosError } from 'axios'
import {
  AdminUserResponse,
  ApiErrorResponse,
  AuditLog,
  AuthResponse,
  BacklogItem,
  InviteDecision,
  InviteResponse,
  MyProjectResponse,
  PageResponse,
  ProjectMemberResponse,
  SprintResponse,
} from '@/lib/types'

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || '/api',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const stored = window.localStorage.getItem('sprintify-auth')
    if (stored) {
      try {
        const parsed = JSON.parse(stored) as { state?: { user?: { token?: string } } }
        const token = parsed.state?.user?.token
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
      } catch {
        window.localStorage.removeItem('sprintify-auth')
      }
    }
  }

  return config
})

export const getApiErrorMessage = (error: unknown) => {
  if (axios.isAxiosError(error)) {
    const responseMessage = (error.response?.data as ApiErrorResponse | undefined)?.message
    if (responseMessage) {
      return responseMessage
    }

    if (error.response?.status === 401) {
      return 'Your session has expired. Please log in again.'
    }
  }

  return error instanceof Error ? error.message : 'Something went wrong'
}

export const authApi = {
  login: async (email: string, password: string) => {
    const response = await api.post<AuthResponse>('/identity-service/api/v1/auth/login', {
      email,
      password,
    })
    return response.data
  },
  signup: async (email: string, password: string) => {
    const response = await api.post<AuthResponse>('/identity-service/api/v1/auth/signup', {
      email,
      password,
    })
    return response.data
  },
}

export const adminApi = {
  getUsers: async () => {
    const response = await api.get<AdminUserResponse[]>('/identity-service/api/v1/admin/users')
    return response.data
  },
  banUser: async (userId: string, days: number) => {
    await api.post(`/identity-service/api/v1/admin/users/${userId}/ban`, { days })
  },
  deleteUser: async (userId: string) => {
    await api.delete(`/identity-service/api/v1/admin/users/${userId}`)
  },
}

export const logApi = {
  getAllLogs: async () => {
    const response = await api.get<AuditLog[]>('/log-analysis-service/api/logs/all')
    return response.data
  },
  getUserLogs: async (actorId: string) => {
    const response = await api.get<AuditLog[]>(`/log-analysis-service/api/logs/user/${actorId}`)
    return response.data
  },
  getEntityLogs: async (entityType: string, entityId: string) => {
    const response = await api.get<AuditLog[]>(
      `/log-analysis-service/api/logs/entity/${entityType}/${entityId}`
    )
    return response.data
  },
}

export const projectApi = {
  listMyProjects: async () => {
    const response = await api.get<MyProjectResponse[]>('/project-service/api/v1/projects/me')
    return response.data
  },
  createProject: async (payload: { name: string; description?: string; state?: string }) => {
    const response = await api.post<MyProjectResponse>('/project-service/api/v1/projects/create', payload)
    return response.data
  },
  getMyInvites: async () => {
    const response = await api.get<InviteResponse[]>('/project-service/api/v1/projects/invites/me')
    return response.data
  },
  respondToInvite: async (projectId: string, decision: InviteDecision) => {
    await api.put(`/project-service/api/v1/projects/${projectId}/invites/respond`, { decision })
  },
  getProjectMembers: async (projectId: string) => {
    const response = await api.get<ProjectMemberResponse[]>(
      `/project-service/api/v1/projects/${projectId}/members`
    )
    return response.data
  },
  inviteMember: async (projectId: string, payload: { email: string; role: string }) => {
    await api.post(`/project-service/api/v1/projects/${projectId}/invites`, payload)
  },
  removeMember: async (projectId: string, memberId: string) => {
    await api.delete(`/project-service/api/v1/projects/${projectId}/members/${memberId}`)
  },
  listBacklogItems: async (projectId: string) => {
    const response = await api.get<PageResponse<BacklogItem>>(
      `/project-service/api/v1/projects/${projectId}/backlog-items`
    )
    return response.data
  },
  createBacklogItem: async (
    projectId: string,
    payload: {
      type: string
      title: string
      description?: string
      priority?: string
      parentId?: string
      difficulty?: string
      estimatedPoints?: number
    }
  ) => {
    const response = await api.post<BacklogItem>(
      `/project-service/api/v1/projects/${projectId}/backlog-items`,
      payload
    )
    return response.data
  },
  updateBacklogItem: async (
    projectId: string,
    itemId: string,
    payload: {
      title?: string
      description?: string
      priority?: string
      difficulty?: string
      estimatedPoints?: number
      estimatedHours?: number
    }
  ) => {
    const response = await api.patch<BacklogItem>(
      `/project-service/api/v1/projects/${projectId}/backlog-items/${itemId}`,
      payload
    )
    return response.data
  },
  updateBacklogStatus: async (projectId: string, itemId: string, status: string) => {
    const response = await api.patch<BacklogItem>(
      `/project-service/api/v1/projects/${projectId}/backlog-items/${itemId}/status`,
      { status }
    )
    return response.data
  },
  assignBacklogItemToSelf: async (projectId: string, itemId: string) => {
    const response = await api.patch<BacklogItem>(
      `/project-service/api/v1/projects/${projectId}/backlog-items/${itemId}/assign`
    )
    return response.data
  },
  moveBacklogItemToSprint: async (projectId: string, itemId: string, sprintId: string | null) => {
    const response = await api.patch<BacklogItem>(
      `/project-service/api/v1/projects/${projectId}/backlog-items/${itemId}/sprint`,
      { sprintId }
    )
    return response.data
  },
  deleteBacklogItem: async (projectId: string, itemId: string) => {
    await api.delete(`/project-service/api/v1/projects/${projectId}/backlog-items/${itemId}`)
  },
  listSprints: async (projectId: string) => {
    const response = await api.get<SprintResponse[]>(
      `/project-service/api/v1/projects/${projectId}/sprints`
    )
    return response.data
  },
  createSprint: async (
    projectId: string,
    payload: { title: string; startDate: string; endDate: string; sprintGoal?: string }
  ) => {
    const response = await api.post<SprintResponse>(
      `/project-service/api/v1/projects/${projectId}/sprints`,
      payload
    )
    return response.data
  },
  addItemsToSprint: async (projectId: string, sprintId: string, itemIds: string[]) => {
    const response = await api.post<SprintResponse>(
      `/project-service/api/v1/projects/${projectId}/sprints/${sprintId}/items`,
      { itemIds }
    )
    return response.data
  },
  removeItemFromSprint: async (projectId: string, sprintId: string, itemId: string) => {
    const response = await api.delete<SprintResponse>(
      `/project-service/api/v1/projects/${projectId}/sprints/${sprintId}/items/${itemId}`
    )
    return response.data
  },
  startSprint: async (projectId: string, sprintId: string, comment?: string) => {
    const response = await api.post<SprintResponse>(
      `/project-service/api/v1/projects/${projectId}/sprints/${sprintId}/start`,
      { comment }
    )
    return response.data
  },
  completeSprint: async (projectId: string, sprintId: string, comment?: string) => {
    const response = await api.post<SprintResponse>(
      `/project-service/api/v1/projects/${projectId}/sprints/${sprintId}/complete`,
      { comment }
    )
    return response.data
  },
  deleteSprint: async (projectId: string, sprintId: string) => {
    await api.delete(`/project-service/api/v1/projects/${projectId}/sprints/${sprintId}`)
  },
}

export const isAxiosError = (error: unknown): error is AxiosError<ApiErrorResponse> =>
  axios.isAxiosError(error)
