import axios from 'axios'
import { AUTH_STORAGE_KEYS, clearStoredAuth } from '../auth/authStorage'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

const AUTH_PATHS = [
  '/auth/signup',
  '/auth/login',
  '/auth/refresh',
  '/auth/logout',
]

const isAuthRequest = (url = '') => AUTH_PATHS.some((path) => url.endsWith(path))

apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem(AUTH_STORAGE_KEYS.accessToken)

  // 회원가입·로그인·재발급·로그아웃 요청에는 기존 Access Token을 전달하지 않는다.
  if (accessToken && !isAuthRequest(config.url)) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

let refreshRequest

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const request = error.config
    const refreshToken = localStorage.getItem(AUTH_STORAGE_KEYS.refreshToken)

    // 인증 API의 401은 화면에 그대로 전달한다.
    // 특히 /auth/refresh의 실패를 다시 재발급하려 하면 순환 요청이 발생한다.
    if (
      error.response?.status !== 401
      || request?._retry
      || !refreshToken
      || isAuthRequest(request?.url)
    ) {
      return Promise.reject(error)
    }

    request._retry = true
    refreshRequest ??= apiClient
      .post('/auth/refresh', { refreshToken })
      .then(({ data }) => {
        localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, data.accessToken)
        localStorage.removeItem(AUTH_STORAGE_KEYS.user)
        window.dispatchEvent(new Event('auth:changed'))
        return data.accessToken
      })
      .catch((refreshError) => {
        clearStoredAuth()
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
