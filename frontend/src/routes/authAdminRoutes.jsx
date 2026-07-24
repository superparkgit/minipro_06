import { Navigate } from 'react-router-dom'
import AdminReviewModerationPage from '../pages/admin/AdminReviewModerationPage'
import AdminUsersPage from '../pages/admin/AdminUsersPage'
import LoginPage from '../pages/auth/LoginPage'
import SignupPage from '../pages/auth/SignupPage'

export const authAdminRoutes = [
  { path: '/login', element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  { path: '/admin', element: <Navigate replace to="/admin/users" /> },
  { path: '/admin/users', element: <AdminUsersPage /> },
  { path: '/admin/reviews', element: <AdminReviewModerationPage /> },
]

export const authAdminNavigation = [
  { to: '/admin', label: '관리자' },
  { to: '/login', label: '로그인' },
]
