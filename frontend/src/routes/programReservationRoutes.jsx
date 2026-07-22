import ProgramDetailPage from '../pages/program/ProgramDetailPage'
import ProgramFormPage from '../pages/program/ProgramFormPage'
import ProgramListPage from '../pages/program/ProgramListPage'
import ProgramManagePage from '../pages/program/ProgramManagePage'
import MyReservationsPage from '../pages/reservation/MyReservationsPage'
import TrainerReservationsPage from '../pages/reservation/TrainerReservationsPage'
import TrainerStatsPage from '../pages/reservation/TrainerStatsPage'

export const programReservationRoutes = [
  { path: '/programs', element: <ProgramListPage /> },
  { path: '/programs/new', element: <ProgramFormPage /> },
  { path: '/programs/:programId/edit', element: <ProgramFormPage /> },
  { path: '/programs/:programId', element: <ProgramDetailPage /> },
  { path: '/programs/:programId/manage', element: <ProgramManagePage /> },
  { path: '/programs/:programId/reservations', element: <TrainerReservationsPage /> },
  { path: '/reservations/me', element: <MyReservationsPage /> },
  { path: '/trainer/stats', element: <TrainerStatsPage /> },
]

export const programReservationNavigation = [
  { to: '/programs', label: '프로그램' },
  { to: '/reservations/me', label: '내 예약' },
  { to: '/trainer/stats', label: '트레이너' },
]
