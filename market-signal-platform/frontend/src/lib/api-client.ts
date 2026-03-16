import { clearStoredAccessToken, getStoredAccessToken, setStoredAccessToken } from "@/lib/auth-storage";
import { env } from "@/lib/env";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly payload?: unknown,
  ) {
    super(typeof payload === "object" && payload && "message" in payload ? String(payload.message) : "요청 처리에 실패했습니다.");
  }
}

class ApiClient {
  private accessToken: string | null = null;
  private refreshPromise: Promise<string | null> | null = null;

  constructor() {
    this.accessToken = getStoredAccessToken();
  }

  setAccessToken(token: string | null) {
    this.accessToken = token;
    if (token) {
      setStoredAccessToken(token);
      return;
    }
    clearStoredAccessToken();
  }

  getAccessToken() {
    return this.accessToken;
  }

  async request<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
    const headers = new Headers(init.headers);
    headers.set("Content-Type", "application/json");
    if (this.accessToken) {
      headers.set("Authorization", `Bearer ${this.accessToken}`);
    }

    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      ...init,
      headers,
      credentials: "include",
      cache: "no-store",
    });

    if (response.status === 401 && retry) {
      const refreshed = await this.refreshAccessToken();
      if (refreshed) {
        return this.request<T>(path, init, false);
      }
    }

    if (!response.ok) {
      const payload = await this.parseJson(response);
      throw new ApiError(response.status, payload);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return this.parseJson(response) as Promise<T>;
  }

  async refreshAccessToken() {
    if (!this.refreshPromise) {
      this.refreshPromise = fetch(`${env.apiBaseUrl}/auth/refresh`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      })
        .then(async (response) => {
          if (!response.ok) {
            this.setAccessToken(null);
            return null;
          }
          const payload = (await response.json()) as { accessToken: string };
          this.setAccessToken(payload.accessToken);
          return payload.accessToken;
        })
        .catch(() => {
          this.setAccessToken(null);
          return null;
        })
        .finally(() => {
          this.refreshPromise = null;
        });
    }

    return this.refreshPromise;
  }

  private async parseJson(response: Response) {
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  }
}

export const apiClient = new ApiClient();
