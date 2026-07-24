export const AUTH_STORAGE_KEYS = {
  accessToken: 'accessToken',
  refreshToken: 'refreshToken',
  user: 'fitReserveUser',
}

export const USER_ROLES = {
  user: 'ROLE_USER',
  trainer: 'ROLE_TRAINER',
  admin: 'ROLE_ADMIN',
}

const normalizeUser = (user) => {
  if (!user || typeof user !== 'object') return null

  const roles = Array.isArray(user.roles)
    ? user.roles.filter((role) => typeof role === 'string')
    : []

  if (
    user.id == null
    || typeof user.email !== 'string'
    || typeof user.name !== 'string'
  ) {
    return null
  }

  return {
    id: user.id,
    email: user.email,
    name: user.name.trim(),
    roles,
  }
}

const getStoredUser = () => {
  const serializedUser = localStorage.getItem(AUTH_STORAGE_KEYS.user)
  if (!serializedUser) return null

  try {
    return normalizeUser(JSON.parse(serializedUser))
  } catch {
    return null
  }
}

export const storeAuthenticatedUser = (user) => {
  const normalizedUser = normalizeUser(user)
  if (!normalizedUser) {
    throw new Error('회원 정보 응답 형식이 올바르지 않습니다.')
  }

  localStorage.setItem(AUTH_STORAGE_KEYS.user, JSON.stringify(normalizedUser))
}

export const clearStoredAuth = () => {
  localStorage.removeItem(AUTH_STORAGE_KEYS.accessToken)
  localStorage.removeItem(AUTH_STORAGE_KEYS.refreshToken)
  localStorage.removeItem(AUTH_STORAGE_KEYS.user)
}

const decodeJwtPayload = (token) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padding = '='.repeat((4 - (base64.length % 4)) % 4)
    const decoded = atob(base64 + padding)
    const bytes = Uint8Array.from(decoded, (character) => character.charCodeAt(0))

    return JSON.parse(new TextDecoder().decode(bytes))
  } catch {
    return null
  }
}

export const getStoredAuthState = () => {
  const accessToken = localStorage.getItem(AUTH_STORAGE_KEYS.accessToken)
  const refreshToken = localStorage.getItem(AUTH_STORAGE_KEYS.refreshToken)
  const storedUser = getStoredUser()

  const claims = decodeJwtPayload(accessToken)
  const expiresAt = Number(claims?.exp) * 1000
  const hasValidAccessToken = Boolean(claims)
    && Number.isFinite(expiresAt)
    && expiresAt > Date.now()
  const isAuthenticated = hasValidAccessToken || Boolean(refreshToken)

  return {
    isAuthenticated,
    isProfileLoaded: Boolean(storedUser),
    user: storedUser,
    name: isAuthenticated ? storedUser?.name ?? null : null,
    roles: isAuthenticated ? storedUser?.roles ?? [] : [],
  }
}

export const hasStoredRole = (role) => getStoredAuthState().roles.includes(role)
