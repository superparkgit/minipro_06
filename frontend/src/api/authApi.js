import apiClient from './apiClient'

export const signup = (payload) => apiClient.post('/auth/signup', payload)
export const login = (payload) => apiClient.post('/auth/login', payload)
export const refresh = (refreshToken) => apiClient.post('/auth/refresh', { refreshToken })
export const logout = (refreshToken) => apiClient.post('/auth/logout', { refreshToken })
export const getMyProfile = () => apiClient.get('/users/me')
