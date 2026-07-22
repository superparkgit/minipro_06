import { Link, NavLink, Outlet } from 'react-router-dom'
import { navigationItems } from '../routes/appRoutes'

function AppLayout() {
  return (
    <>
      <header className="app-header">
        <Link className="brand" to="/">PT 예약 커뮤니티</Link>
        <nav>
          {navigationItems.map(({ to, label }) => <NavLink key={to} to={to}>{label}</NavLink>)}
        </nav>
      </header>
      <main className="app-main"><Outlet /></main>
    </>
  )
}

export default AppLayout
