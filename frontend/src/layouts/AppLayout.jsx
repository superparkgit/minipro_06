import { Link, NavLink, Outlet } from 'react-router-dom'

function AppLayout() {
  return (
    <>
      <header className="app-header">
        <Link className="brand" to="/">PT 예약 커뮤니티</Link>
        <nav>
          <NavLink to="/programs">프로그램</NavLink>
          <NavLink to="/reservations/me">내 예약</NavLink>
          <NavLink to="/trainer/stats">트레이너</NavLink>
          <NavLink to="/posts">게시판</NavLink>
          <NavLink to="/admin/users">관리자</NavLink>
          <NavLink to="/login">로그인</NavLink>
        </nav>
      </header>
      <main className="app-main"><Outlet /></main>
    </>
  )
}

export default AppLayout
