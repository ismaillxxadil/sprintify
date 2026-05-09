import { AuthResponse, IdentityRole, SessionUser } from '@/lib/types'

interface JwtPayload {
  sub?: string
  email?: string
  role?: IdentityRole
  exp?: number
}

const decodeBase64Url = (value: string) => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=')

  if (typeof window !== 'undefined') {
    return window.atob(padded)
  }

  return Buffer.from(padded, 'base64').toString('utf8')
}

export const parseJwt = (token: string): JwtPayload => {
  try {
    const [, payload] = token.split('.')
    if (!payload) {
      return {}
    }

    return JSON.parse(decodeBase64Url(payload)) as JwtPayload
  } catch {
    return {}
  }
}

export const buildSessionUser = (response: AuthResponse): SessionUser => {
  const payload = parseJwt(response.token)

  return {
    token: response.token,
    email: response.email || payload.email || '',
    role: response.role || payload.role || 'USER',
    userId: payload.sub || '',
    exp: payload.exp,
  }
}
