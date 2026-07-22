import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('accessToken')
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

let refreshRequest

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const request = error.config
    const refreshToken = localStorage.getItem('refreshToken')

    if (error.response?.status !== 401 || request?._retry || !refreshToken) {
      return Promise.reject(error)
    }

    request._retry = true
    refreshRequest ??= apiClient
      .post('/auth/refresh', { refreshToken })
      .then(({ data }) => {
        localStorage.setItem('accessToken', data.accessToken)
        return data.accessToken
      })
      .catch((refreshError) => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.dispatchEvent(new Event('auth:expired'))
        throw refreshError
      })
      .finally(() => { refreshRequest = undefined })

    const accessToken = await refreshRequest
    request.headers.Authorization = `Bearer ${accessToken}`
    return apiClient(request)
  },
)

export default apiClient
