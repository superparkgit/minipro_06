import apiClient from './apiClient'

export const getPrograms = () => apiClient.get('/programs')
export const getProgram = (programId) => apiClient.get(`/programs/${programId}`)
export const createProgram = (payload) => apiClient.post('/programs', payload)
export const updateProgram = (programId, payload) => apiClient.patch(`/programs/${programId}`, payload)
export const deleteProgram = (programId) => apiClient.delete(`/programs/${programId}`)
export const cancelProgram = (programId) => apiClient.patch(`/programs/${programId}/cancel`)
export const completeProgram = (programId) => apiClient.patch(`/programs/${programId}/complete`)
export const addTrainer = (programId, payload) => apiClient.post(`/programs/${programId}/trainers`, payload)
export const removeTrainer = (programId, trainerId) => apiClient.delete(`/programs/${programId}/trainers/${trainerId}`)
