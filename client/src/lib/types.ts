export type IdentityRole = 'ADMIN' | 'USER'
export type ProjectMemberRole = 'PO' | 'SM' | 'DEV'
export type ProjectMemberStatus = 'PENDING' | 'ACTIVE' | 'REJECTED'
export type BacklogItemType = 'EPIC' | 'USER_STORY' | 'TASK' | 'BUG'
export type BacklogItemStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type Priority = 'HIGH' | 'MEDIUM' | 'LOW'
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type SprintStatus = 'PLANNING' | 'ACTIVE' | 'CLOSED'
export type InviteDecision = 'ACCEPT' | 'REJECT'

export interface AuthResponse {
  token: string
  email: string
  role: IdentityRole
}

export interface SessionUser {
  token: string
  email: string
  role: IdentityRole
  userId: string
  exp?: number
}

export interface ApiErrorResponse {
  status?: number
  error?: string
  message?: string
}

export interface MyProjectResponse {
  id: string
  name: string
  description: string | null
  state: string
  ownerId: string
  createdAt: string
  memberCount: number
  currentUserRole: ProjectMemberRole
}

export interface InviteResponse {
  projectId: string
  projectName: string
  role: ProjectMemberRole
  status: ProjectMemberStatus
  invitedAt: string
}

export interface ProjectMemberResponse {
  userId: string
  role: ProjectMemberRole
  status: ProjectMemberStatus
  joinedAt: string
}

export interface AdminUserResponse {
  id: string
  email: string
  role: IdentityRole
  createdAt: string
  bannedUntil: string | null
}

export interface AuditLog {
  id?: string
  actorId: string
  actionType: string
  entityType: string
  entityId: string
  timestamp: string
  details?: Record<string, unknown> | null
}

export interface BacklogItem {
  id: string
  title: string
  description: string | null
  status: BacklogItemStatus
  type: BacklogItemType
  priority: Priority
  difficulty: Difficulty
  estimatedPoints: number | null
  estimatedHours: number | null
  assigneeId: string | null
  parentId: string | null
  sprintId: string | null
  createdById: string
  createdAt: string
  updatedAt: string
  completedAt: string | null
}

export interface SprintResponse {
  id: string
  title: string
  sprintGoal: string | null
  status: SprintStatus
  startDate: string
  endDate: string
  startedAt: string | null
  closedAt: string | null
  totalPoints: number | null
  completedPoints: number | null
  remainingPoints: number | null
  velocity: number | null
  backlogItems: BacklogItem[]
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
  first: boolean
  last: boolean
}
