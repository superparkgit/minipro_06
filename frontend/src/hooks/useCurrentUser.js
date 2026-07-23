import { useEffect, useState } from 'react'
import { getMyProfile } from '../api/userApi'
import { AUTH_STORAGE_KEYS } from '../auth/authStorage'

let cachedRequest = null

export function clearCurrentUserCache() {
  cachedRequest = null
}

export function useCurrentUser() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem(AUTH_STORAGE_KEYS.accessToken)
    if (!token) {
      setUser(null)
      setLoading(false)
      return undefined
    }

    cachedRequest ??= getMyProfile().then(({ data }) => data).catch(() => null)

    let active = true
    cachedRequest.then((profile) => {
      if (active) {
        setUser(profile)
        setLoading(false)
      }
    })
    return () => { active = false }
  }, [])

  return { user, loading }
}

export const hasRole = (user, role) => Boolean(user?.roles?.includes(role))
