const TOKEN_KEY = 'adminAccessToken'
const IDENTITY_KEY = 'adminIdentity'

export const authProvider = {
  login: async ({ username, password }) => {
    const baseUrl = import.meta.env.VITE_ADMIN_API_BASE_URL || ''
    const response = await fetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    })

    if (!response.ok) {
      throw new Error('Invalid credentials')
    }

    const data = await response.json()
    if (!data?.accessToken) {
      throw new Error('Missing access token')
    }

    localStorage.setItem(TOKEN_KEY, data.accessToken)
    localStorage.setItem(IDENTITY_KEY, JSON.stringify({ id: username, fullName: username }))
  },

  logout: async () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(IDENTITY_KEY)
  },

  checkAuth: async () => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
      throw new Error('Not authenticated')
    }
  },

  checkError: async (error) => {
    const status = error?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(IDENTITY_KEY)
      throw new Error('Unauthorized')
    }
  },

  getIdentity: async () => {
    const raw = localStorage.getItem(IDENTITY_KEY)
    if (!raw) {
      return { id: 'admin', fullName: 'admin' }
    }
    return JSON.parse(raw)
  },

  getPermissions: async () => {
    return []
  },
}
