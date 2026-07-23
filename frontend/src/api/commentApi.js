import apiClient from './apiClient'

export const createComment = (postId, payload) => apiClient.post(`/posts/${postId}/comments`, payload)
export const getComments = (postId) => apiClient.get(`/posts/${postId}/comments`)
export const updateComment = (commentId, payload) => apiClient.put(`/comments/${commentId}`, payload)
export const deleteComment = (commentId) => apiClient.delete(`/comments/${commentId}`)
