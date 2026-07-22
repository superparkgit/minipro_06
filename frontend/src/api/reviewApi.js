import apiClient from './apiClient'

export const createReview = (reservationId, payload) => apiClient.post(`/reservations/${reservationId}/reviews`, payload)
export const updateReview = (reviewId, payload) => apiClient.put(`/reviews/${reviewId}`, payload)
export const deleteReview = (reviewId) => apiClient.delete(`/reviews/${reviewId}`)
export const getProgramReviews = (programId, params) => apiClient.get(`/programs/${programId}/reviews`, { params })
export const createReviewReply = (reviewId, payload) => apiClient.post(`/reviews/${reviewId}/replies`, payload)
export const updateReviewReply = (reviewId, payload) => apiClient.put(`/reviews/${reviewId}/replies`, payload)
export const reportReview = (reviewId, payload) => apiClient.post(`/reviews/${reviewId}/reports`, payload)
export const getAdminReviews = (params) => apiClient.get('/admin/reviews', { params })
export const decideReviewReport = (reviewId, payload) => apiClient.patch(`/admin/reviews/${reviewId}/decision`, payload)
export const getTrainerRating = (trainerId) => apiClient.get(`/trainers/${trainerId}/rating`)
export const getProgramRating = (programId) => apiClient.get(`/programs/${programId}/rating`)
