const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const storage = {
  tokenKey: "derivops_token",
  roleKey: "derivops_role",
};

export function getToken(): string | null {
  return localStorage.getItem(storage.tokenKey);
}

export function getRole(): string | null {
  return localStorage.getItem(storage.roleKey);
}

export function setAuth(token: string, role: string) {
  localStorage.setItem(storage.tokenKey, token);
  localStorage.setItem(storage.roleKey, role);
}

export function clearAuth() {
  localStorage.removeItem(storage.tokenKey);
  localStorage.removeItem(storage.roleKey);
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init?.headers as Record<string, string> | undefined),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  get: <T,>(path: string) => request<T>(path),
  post: <T,>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T,>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
};
