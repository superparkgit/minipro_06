import apiClient from './apiClient'

export const getPosts = (params) => apiClient.get('/posts', { params })
export const createPost = (payload) => apiClient.post('/posts', payload)
export const getPost = (postId) => apiClient.get(`/posts/${postId}`)
export const getMyPosts = (params) => apiClient.get('/posts/me', { params })
export const updatePost = (postId, payload) => apiClient.put(`/posts/${postId}`, payload)
export const deletePost = (postId) => apiClient.delete(`/posts/${postId}`)
