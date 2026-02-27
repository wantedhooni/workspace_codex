import { AuthProvider } from 'react-admin';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export const authProvider: AuthProvider = {
  login: async ({ username, password }) => {
    const response = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: username, password }),
    });

    if (!response.ok) {
      throw new Error('Invalid credentials');
    }

    const data = await response.json();
    localStorage.setItem('tm_token', data.token);
    localStorage.setItem('tm_user', JSON.stringify({
      displayName: data.displayName,
      role: data.role,
      email: username,
    }));
  },
  logout: async () => {
    localStorage.removeItem('tm_token');
    localStorage.removeItem('tm_user');
  },
  checkAuth: async () => {
    if (localStorage.getItem('tm_token')) {
      return;
    }
    throw new Error('Not authenticated');
  },
  checkError: async (error) => {
    if (error.status === 401 || error.status === 403) {
      localStorage.removeItem('tm_token');
      localStorage.removeItem('tm_user');
      throw new Error('Session expired');
    }
  },
  getIdentity: async () => {
    const raw = localStorage.getItem('tm_user');
    if (!raw) {
      throw new Error('No user');
    }
    const user = JSON.parse(raw);
    return {
      id: user.email,
      fullName: user.displayName,
    };
  },
  getPermissions: async () => {
    const raw = localStorage.getItem('tm_user');
    if (!raw) {
      return [];
    }
    const user = JSON.parse(raw);
    return user.role ? [user.role] : [];
  },
};
