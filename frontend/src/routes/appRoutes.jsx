import HomePage from '../pages/HomePage'
import { authAdminNavigation, authAdminRoutes } from './authAdminRoutes'
import { communityNavigation, communityRoutes } from './communityRoutes'
import { programReservationNavigation, programReservationRoutes } from './programReservationRoutes'

export const appRoutes = [
  { path: '/', element: <HomePage /> },
  ...programReservationRoutes,
  ...communityRoutes,
  ...authAdminRoutes,
]

export const navigationItems = [
  ...programReservationNavigation,
  ...communityNavigation,
  ...authAdminNavigation,
]
