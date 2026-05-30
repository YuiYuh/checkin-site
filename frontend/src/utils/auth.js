const AUTH_EVENT = 'habitlink-auth-changed'

export const DEFAULT_USER = { id: 1, username: 'student01', nickname: '小明' }

export const getToken = () => {
  return localStorage.getItem('token') || ''
}

export const getCurrentUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user')) || null
  } catch {
    return null
  }
}

export const isLoggedIn = () => {
  return Boolean(getToken() && getCurrentUser())
}

export const saveAuth = ({ token, user } = {}) => {
  if (!token || !user) {
    throw new Error('登录信息不完整')
  }
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
  window.dispatchEvent(new Event(AUTH_EVENT))
}

export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.dispatchEvent(new Event(AUTH_EVENT))
}

export const onAuthChange = (handler) => {
  window.addEventListener(AUTH_EVENT, handler)
  window.addEventListener('storage', handler)

  return () => {
    window.removeEventListener('storage', handler)
    window.removeEventListener(AUTH_EVENT, handler)
  }
}
