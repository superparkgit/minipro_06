import { Route, Routes } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import HomePage from './pages/HomePage'
import LoginPage from './pages/auth/LoginPage'
import SignupPage from './pages/auth/SignupPage'
import ProgramDetailPage from './pages/program/ProgramDetailPage'
import ProgramFormPage from './pages/program/ProgramFormPage'
import ProgramListPage from './pages/program/ProgramListPage'
import ProgramManagePage from './pages/program/ProgramManagePage'
import MyReservationsPage from './pages/reservation/MyReservationsPage'
import TrainerStatsPage from './pages/reservation/TrainerStatsPage'
import TrainerReservationsPage from './pages/reservation/TrainerReservationsPage'
import PostsPage from './pages/community/PostsPage'
import ReviewsPage from './pages/review/ReviewsPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        <Route path="/programs" element={<ProgramListPage />} />
        <Route path="/programs/new" element={<ProgramFormPage />} />
        <Route path="/programs/:programId/edit" element={<ProgramFormPage />} />
        <Route path="/programs/:programId" element={<ProgramDetailPage />} />
        <Route path="/programs/:programId/manage" element={<ProgramManagePage />} />
        <Route path="/programs/:programId/reservations" element={<TrainerReservationsPage />} />

        <Route path="/reservations/me" element={<MyReservationsPage />} />
        <Route path="/trainer/stats" element={<TrainerStatsPage />} />
        <Route path="/posts" element={<PostsPage />} />
        <Route path="/reviews" element={<ReviewsPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
      </Route>
    </Routes>
  )
}

export default App
