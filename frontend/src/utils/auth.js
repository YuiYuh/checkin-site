const AUTH_EVENT = 'habitlink-auth-changed'

export const DEFAULT_USER = { id: 1, username: 'demo', nickname: '演示用户' }
export const DEFAULT_TOKEN = 'user-1'

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

export const saveAuth = ({ token = DEFAULT_TOKEN, user = DEFAULT_USER } = {}) => {
  localStorage.setItem('token', token || DEFAULT_TOKEN)
  localStorage.setItem('user', JSON.stringify(user || DEFAULT_USER))
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
    window.removeEventListener(AUTH_EVENT, handler)
    window.removeEventListener('storage', handler)
  }
}
