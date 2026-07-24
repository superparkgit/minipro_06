import { NavLink } from 'react-router-dom'
import './AdminUsersPage.css'

const adminSections = [
  { to: '/admin/users', label: '회원 관리' },
  { to: '/admin/reviews', label: '리뷰 심사' },
]

function AdminSectionNav() {
  return (
    <nav className="admin-section-nav" aria-label="관리자 기능">
      {adminSections.map(({ to, label }) => (
        <NavLink key={to} to={to} end>
          {label}
        </NavLink>
      ))}
    </nav>
  )
}

export default AdminSectionNav
