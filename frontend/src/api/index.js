import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth } from '../utils/auth'

const AUTH_EXPIRED_MESSAGE = '登录状态已失效，请重新登录'
const AUTH_EXPIRED_KEYWORDS = ['请先登录', '登录状态无效', '登录已过期']

let authExpiredHandled = false

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

const isAuthExpired = (message, status) => {
  if (status === 401) {
    return true
  }
  return AUTH_EXPIRED_KEYWORDS.some((keyword) => message?.includes(keyword))
}

const handleAuthExpired = () => {
  if (authExpiredHandled) {
    return
  }

  authExpiredHandled = true
  clearAuth()
  ElMessage.error(AUTH_EXPIRED_MESSAGE)

  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  if (!window.location.pathname.endsWith('/login')) {
    window.location.assign(`/login?redirect=${encodeURIComponent(currentPath)}`)
  }

  window.setTimeout(() => {
    authExpiredHandled = false
  }, 1000)
}

const stopCurrentRequest = () => new Promise(() => {})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }

      const message = body.message || '接口请求失败'
      if (isAuthExpired(message, response.status)) {
        handleAuthExpired()
        return stopCurrentRequest()
      }

      return Promise.reject(new Error(message))
    }

    return body
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      '网络请求失败'

    if (isAuthExpired(message, error.response?.status)) {
      handleAuthExpired()
      return stopCurrentRequest()
    }

    return Promise.reject(new Error(message))
  },
)

export default api
