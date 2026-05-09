import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

// Add token to request headers
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const userId = localStorage.getItem('userId')
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
  }
  return config
})

export const authApi = {
  signup: (email: string, password: string) =>
    api.post('/identity-service/api/v1/auth/signup', { email, password }),
  login: (email: string, password: string) =>
    api.post('/identity-service/api/v1/auth/login', { email, password }),
}

export const userApi = {
  getProfile: () => api.get('/identity-service/api/v1/users/profile'),
}

export const adminApi = {
  getAllUsers: () => api.get('/identity-service/api/v1/admin/users'),
  deleteUser: (id: string) => api.delete(`/identity-service/api/v1/admin/users/${id}`),
  banUser: (id: string, days: number) =>
    api.post(`/identity-service/api/v1/admin/users/${id}/ban`, { days }),
}

export const projectApi = {
  createProject: (name: string, description: string) =>
    api.post('/project-service/api/v1/projects/create', { name, description }),
  getMyInvites: () => api.get('/project-service/api/v1/projects/invites/me'),
  respondToInvite: (projectId: string, decision: boolean) =>
    api.put(`/project-service/api/v1/projects/${projectId}/invites/respond`, { decision }),
}

export const logApi = {
  getUserLogs: (userId: string) => api.get(`/log-analysis-service/api/logs/user/${userId}`),
}

export default api
