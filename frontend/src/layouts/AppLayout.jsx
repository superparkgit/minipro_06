import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { getMyProfile, logout } from '../api/authApi'
import {
  AUTH_STORAGE_KEYS,
  USER_ROLES,
  clearStoredAuth,
  storeAuthenticatedUser,
} from '../auth/authStorage'
import useAuthState from '../auth/useAuthState'
import { navigationItems } from '../routes/appRoutes'

function AppLayout() {
  const navigate = useNavigate()
  const { isAuthenticated, isProfileLoaded, name, roles } = useAuthState()
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const isAdmin = roles.includes(USER_ROLES.admin)

  useEffect(() => {
    if (!isAuthenticated || isProfileLoaded) return undefined

    let isActive = true

    getMyProfile()
      .then(({ data }) => {
        if (!isActive) return
        storeAuthenticatedUser(data)
        window.dispatchEvent(new Event('auth:changed'))
      })
      .catch(() => {
        // 401은 apiClient가 재발급 또는 세션 만료로 처리한다.
      })

    return () => { isActive = false }
  }, [isAuthenticated, isProfileLoaded])

  const handleLogout = async () => {
    if (isLoggingOut) return

    setIsLoggingOut(true)
    const refreshToken = localStorage.getItem(AUTH_STORAGE_KEYS.refreshToken)

    try {
      if (refreshToken) await logout(refreshToken)
    } catch {
      // 서버 연결이 끊겨도 현재 브라우저의 인증 정보는 제거해 사용자를 화면에 남겨두지 않는다.
    } finally {
      clearStoredAuth()
      setIsLoggingOut(false)
      window.dispatchEvent(new Event('auth:changed'))
      navigate('/login', {
        replace: true,
        state: { logoutComplete: true },
      })
    }
  }

  return (
    <>
      <header className="app-header">
        <Link className="brand" to="/">PT 예약 커뮤니티</Link>
        <nav>
          {navigationItems
            .filter(({ to }) => {
              if (to === '/login') return false
              if (to === '/admin/users') return isAdmin
              return true
            })
            .map(({ to, label }) => <NavLink key={to} to={to}>{label}</NavLink>)}

          {isAuthenticated ? (
            <div className="header-auth">
              <span className="header-user-name">{name ? `${name} 님` : '회원님'}</span>
              <button
                className="header-auth-button"
                type="button"
                onClick={handleLogout}
                disabled={isLoggingOut}
              >
                {isLoggingOut ? '로그아웃 중…' : '로그아웃'}
              </button>
            </div>
          ) : (
            <NavLink to="/login">로그인</NavLink>
          )}
        </nav>
      </header>
      <main className="app-main"><Outlet /></main>
    </>
  )
}

export default AppLayout
