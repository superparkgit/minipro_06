import apiClient from './apiClient'

export const getMyProfile = () => apiClient.get('/users/me')
