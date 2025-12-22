import api from './api'

const TOKEN_KEY = 'crud_ui_token'

export default {
  async login(username, password) {
    // Intentar con API si existe
    try {
      const res = await api.post('/auth/login', { username, password })
      const token = res.data?.token
      if (!token) throw new Error(res.data?.message || 'No se recibió token')
      localStorage.setItem(TOKEN_KEY, token)
      return token
    } catch (e) {
      // Fallback: autenticación demo local (usuario: admin/admin)
      if (username === 'admin' && password === 'admin') {
        const token = 'demo-token'
        localStorage.setItem(TOKEN_KEY, token)
        return token
      }
      // Re-lanzar error público
      throw new Error(e?.response?.data?.message || e?.message || 'Credenciales inválidas')
    }
  },

  logout() {
    localStorage.removeItem(TOKEN_KEY)
  },

  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  isAuthenticated() {
    return !!localStorage.getItem(TOKEN_KEY)
  }
}
