import apiClient from './apiClient'

export const createReservation = (payload) => apiClient.post('/reservations', payload)
export const getMyReservations = () => apiClient.get('/reservations/me')
export const cancelReservation = (reservationId) => apiClient.patch(`/reservations/${reservationId}/cancel`)

export const getProgramReservations = (programId) => apiClient.get(`/programs/${programId}/reservations`)
export const approveReservation = (reservationId) => apiClient.patch(`/reservations/${reservationId}/approve`)
export const rejectReservation = (reservationId) => apiClient.patch(`/reservations/${reservationId}/reject`)
export const updateAttendance = (reservationId, payload) => apiClient.patch(`/reservations/${reservationId}/attendance`, payload)

export const getEmptyPrograms = () => apiClient.get('/stats/programs/empty')
export const getMonthlyTrainerStats = (params) => apiClient.get('/stats/trainers/monthly', { params })
export const getPopularPrograms = () => apiClient.get('/stats/programs/popular')
