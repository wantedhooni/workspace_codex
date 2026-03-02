const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const storage = {
  tokenKey: "derivops_token",
  refreshTokenKey: "derivops_refresh_token",
  roleKey: "derivops_role",
  expiresAtKey: "derivops_expires_at",
  refreshExpiresAtKey: "derivops_refresh_expires_at",
};

export function getToken(): string | null {
  return localStorage.getItem(storage.tokenKey);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(storage.refreshTokenKey);
}

export function getRole(): string | null {
  return localStorage.getItem(storage.roleKey);
}

export function getAccessExpiresAt(): number | null {
  const value = localStorage.getItem(storage.expiresAtKey);
  return value ? Number(value) : null;
}

export function getRefreshExpiresAt(): number | null {
  const value = localStorage.getItem(storage.refreshExpiresAtKey);
  return value ? Number(value) : null;
}

type AuthPayload = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  role: string;
};

export function setAuth(payload: AuthPayload) {
  localStorage.setItem(storage.tokenKey, payload.accessToken);
  localStorage.setItem(storage.refreshTokenKey, payload.refreshToken);
  localStorage.setItem(storage.expiresAtKey, String(Date.now() + payload.expiresIn * 1000));
  localStorage.setItem(storage.refreshExpiresAtKey, String(Date.now() + payload.refreshExpiresIn * 1000));
  localStorage.setItem(storage.roleKey, payload.role);
}

export function clearAuth() {
  localStorage.removeItem(storage.tokenKey);
  localStorage.removeItem(storage.refreshTokenKey);
  localStorage.removeItem(storage.roleKey);
  localStorage.removeItem(storage.expiresAtKey);
  localStorage.removeItem(storage.refreshExpiresAtKey);
}

type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  role: string;
};

type ApiRequestInit = RequestInit & {
  skipAuthRefresh?: boolean;
  retryOnUnauthorized?: boolean;
};

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

let refreshPromise: Promise<string | null> | null = null;

function isExpired(expiresAt: number | null, bufferMs = 30_000): boolean {
  if (!expiresAt) {
    return true;
  }
  return Date.now() + bufferMs >= expiresAt;
}

async function rawRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    ...(init?.headers as Record<string, string> | undefined),
  };

  if (!headers["Content-Type"] && init?.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    const body = await response.text();
    let message = body || `HTTP ${response.status}`;
    try {
      const parsed = JSON.parse(body) as { message?: string };
      if (parsed.message) {
        message = parsed.message;
      }
    } catch {
      // Keep original text when response body is not JSON.
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json() as Promise<T>;
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  const refreshExpiresAt = getRefreshExpiresAt();
  if (!refreshToken || isExpired(refreshExpiresAt, 0)) {
    clearAuth();
    return null;
  }

  if (!refreshPromise) {
    refreshPromise = rawRequest<LoginResponse>("/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((response) => {
        setAuth(response);
        return response.accessToken;
      })
      .catch((error) => {
        clearAuth();
        throw error;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

export async function ensureValidAccessToken(): Promise<string | null> {
  const token = getToken();
  if (!token) {
    return null;
  }

  if (!isExpired(getAccessExpiresAt())) {
    return token;
  }

  return refreshAccessToken();
}

async function request<T>(path: string, init?: ApiRequestInit): Promise<T> {
  const skipAuthRefresh = init?.skipAuthRefresh ?? false;
  const retryOnUnauthorized = init?.retryOnUnauthorized ?? true;
  let token = getToken();

  if (!skipAuthRefresh) {
    token = await ensureValidAccessToken();
  }

  const headers: Record<string, string> = {
    ...(init?.headers as Record<string, string> | undefined),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    return await rawRequest<T>(path, {
      ...init,
      headers,
    });
  } catch (error) {
    if (
      !skipAuthRefresh &&
      retryOnUnauthorized &&
      error instanceof ApiError &&
      error.status === 401
    ) {
      const refreshedToken = await refreshAccessToken();
      if (!refreshedToken) {
        throw error;
      }

      return rawRequest<T>(path, {
        ...init,
        headers: {
          ...headers,
          Authorization: `Bearer ${refreshedToken}`,
        },
      });
    }

    throw error;
  }
}

export const api = {
  get: <T,>(path: string, init?: ApiRequestInit) => request<T>(path, init),
  post: <T,>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T,>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
};
