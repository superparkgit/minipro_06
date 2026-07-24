import { useEffect, useState } from 'react'
import { getMyProfile } from '../api/userApi'
import { AUTH_STORAGE_KEYS } from '../auth/authStorage'

let cachedToken = null
let cachedRequest = null

export function clearCurrentUserCache() {
  cachedToken = null
  cachedRequest = null
}

const getCurrentUserRequest = (token) => {
  if (cachedToken !== token || !cachedRequest) {
    cachedToken = token
    cachedRequest = getMyProfile()
      .then(({ data }) => data)
      .catch(() => null)
  }

  return cachedRequest
}

export function useCurrentUser() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    const loadCurrentUser = () => {
      const token = localStorage.getItem(AUTH_STORAGE_KEYS.accessToken)

      if (!token) {
        clearCurrentUserCache()
        if (active) {
          setUser(null)
          setLoading(false)
        }
        return
      }

      setLoading(true)

      getCurrentUserRequest(token).then((profile) => {
        if (
          active
          && localStorage.getItem(AUTH_STORAGE_KEYS.accessToken) === token
        ) {
          setUser(profile)
          setLoading(false)
        }
      })
    }

    const handleAuthChanged = () => {
      loadCurrentUser()
    }

    loadCurrentUser()

    window.addEventListener('auth:changed', handleAuthChanged)
    window.addEventListener('auth:expired', handleAuthChanged)
    window.addEventListener('storage', handleAuthChanged)

    return () => {
      active = false
      window.removeEventListener('auth:changed', handleAuthChanged)
      window.removeEventListener('auth:expired', handleAuthChanged)
      window.removeEventListener('storage', handleAuthChanged)
    }
  }, [])

  return { user, loading }
}

export const hasRole = (user, role) => Boolean(user?.roles?.includes(role))
