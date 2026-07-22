import AdminUsersPage from '../pages/admin/AdminUsersPage'
import LoginPage from '../pages/auth/LoginPage'
import SignupPage from '../pages/auth/SignupPage'

export const authAdminRoutes = [
  { path: '/login', element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  { path: '/admin/users', element: <AdminUsersPage /> },
]

export const authAdminNavigation = [
  { to: '/admin/users', label: '관리자' },
  { to: '/login', label: '로그인' },
]
