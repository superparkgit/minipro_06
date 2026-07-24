import apiClient from './apiClient'

export const getAdminUsers = () => apiClient.get('/admin/users')
export const updateUserRoles = (userId, roles) => (
  apiClient.patch(`/admin/users/${userId}/roles`, { roles })
)
