import simpleRestProvider from 'ra-data-simple-rest'
import { fetchUtils } from 'react-admin'

const TOKEN_KEY = 'adminAccessToken'

const httpClient = (url, options = {}) => {
  const token = localStorage.getItem(TOKEN_KEY)
  const headers = new Headers(options.headers || {})
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  return fetchUtils.fetchJson(url, { ...options, headers })
}

const apiUrl = import.meta.env.VITE_ADMIN_API_BASE_URL || ''

export const dataProvider = simpleRestProvider(apiUrl, httpClient)
