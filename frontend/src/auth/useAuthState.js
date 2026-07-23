import { useEffect, useState } from 'react'
import { getStoredAuthState } from './authStorage'

function useAuthState() {
  const [authState, setAuthState] = useState(getStoredAuthState)

  useEffect(() => {
    const updateAuthState = () => setAuthState(getStoredAuthState())

    window.addEventListener('auth:changed', updateAuthState)
    window.addEventListener('auth:expired', updateAuthState)
    window.addEventListener('storage', updateAuthState)

    return () => {
      window.removeEventListener('auth:changed', updateAuthState)
      window.removeEventListener('auth:expired', updateAuthState)
      window.removeEventListener('storage', updateAuthState)
    }
  }, [])

  return authState
}

export default useAuthState
