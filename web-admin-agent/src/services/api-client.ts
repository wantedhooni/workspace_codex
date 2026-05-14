import axios, {
  AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";

import type {
  AdminAuthResponse,
  ApiResponse,
  AuthSession,
  JsonRecord,
  LoginRequest,
} from "@/types/admin-api";

const TOKEN_STORAGE_KEY = "web-admin-agent.session";
const TOKEN_CHANGE_EVENT = "web-admin-agent.session-change";

type HttpMethod = "GET" | "POST" | "PATCH" | "DELETE";

type ApiRequestConfig = {
  method?: HttpMethod;
  data?: object;
  skipAuth?: boolean;
  skipRefresh?: boolean;
};

type RetryableAxiosConfig = InternalAxiosRequestConfig & {
  skipAuth?: boolean;
  skipRefresh?: boolean;
  retried?: boolean;
};

/**
 * 브라우저 localStorage에 JWT 세션을 저장하고 복원하는 인증 저장소 서비스입니다.
 */
export class AuthStorageService {
  /**
   * 저장소 변경을 React external store로 구독합니다.
   */
  subscribe(onStoreChange: () => void): () => void {
    window.addEventListener("storage", onStoreChange);
    window.addEventListener(TOKEN_CHANGE_EVENT, onStoreChange);

    return () => {
      window.removeEventListener("storage", onStoreChange);
      window.removeEventListener(TOKEN_CHANGE_EVENT, onStoreChange);
    };
  }

  /**
   * 저장된 인증 세션 문자열을 읽어옵니다.
   */
  readRaw(): string | null {
    if (typeof window === "undefined") {
      return null;
    }

    return window.localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  /**
   * 저장된 인증 세션을 읽어옵니다.
   */
  read(): AuthSession | null {
    const raw = this.readRaw();

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      this.clear();
      return null;
    }
  }

  /**
   * 로그인 또는 토큰 갱신 성공 후 인증 세션을 저장합니다.
   */
  write(session: AuthSession): void {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(session));
    window.dispatchEvent(new Event(TOKEN_CHANGE_EVENT));
  }

  /**
   * 로그아웃 또는 인증 만료 시 인증 세션을 제거합니다.
   */
  clear(): void {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    window.dispatchEvent(new Event(TOKEN_CHANGE_EVENT));
  }
}

/**
 * ADMIN-API HTTP 요청과 JWT/refresh token 갱신을 담당하는 공통 axios 클라이언트입니다.
 */
export class ApiClient {
  private readonly http: AxiosInstance;
  private readonly authStorage: AuthStorageService;
  private refreshPromise: Promise<AuthSession> | null = null;

  constructor({
    baseUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081",
    authStorage = new AuthStorageService(),
  }: {
    baseUrl?: string;
    authStorage?: AuthStorageService;
  } = {}) {
    this.authStorage = authStorage;
    this.http = axios.create({
      baseURL: baseUrl.replace(/\/$/, ""),
      headers: { Accept: "application/json" },
    });
    this.setupInterceptors();
  }

  /**
   * 로그인 API를 호출하고 accessToken, refreshToken을 저장합니다.
   */
  async login(payload: LoginRequest): Promise<AuthSession> {
    const response = await this.request<AdminAuthResponse>("/api/v1/auth/login", {
      method: "POST",
      data: payload,
      skipAuth: true,
      skipRefresh: true,
    });
    const session = this.toSession(payload.email, response.data);

    this.authStorage.write(session);

    return session;
  }

  /**
   * refreshToken으로 accessToken을 재발급하고 저장된 세션을 갱신합니다.
   */
  async refresh(): Promise<AuthSession> {
    if (!this.refreshPromise) {
      this.refreshPromise = this.refreshAccessToken().finally(() => {
        this.refreshPromise = null;
      });
    }

    return this.refreshPromise;
  }

  /**
   * 저장된 accessToken을 자동 첨부해 ADMIN-API 요청을 보냅니다.
   */
  async request<T>(
    url: string,
    config: ApiRequestConfig = {}
  ): Promise<ApiResponse<T>> {
    if (!config.skipAuth && !this.authStorage.read()?.accessToken) {
      throw new Error("로그인이 필요합니다.");
    }

    const response = await this.http.request<ApiResponse<T>>({
      url,
      method: config.method ?? "GET",
      data: config.data,
      skipAuth: config.skipAuth,
      skipRefresh: config.skipRefresh,
    });
    const body = response.data || {};

    if (body.success === false) {
      throw new Error(body.message || "API 요청에 실패했습니다.");
    }

    return body;
  }

  /**
   * 로그아웃 API 호출 후 저장된 JWT 세션을 제거합니다.
   */
  async logout(): Promise<void> {
    const session = this.authStorage.read();

    try {
      if (session?.accessToken) {
        await this.request<void>("/api/v1/auth/logout", {
          method: "POST",
          data: { refreshToken: session.refreshToken },
          skipRefresh: true,
        });
      }
    } finally {
      this.authStorage.clear();
    }
  }

  private setupInterceptors(): void {
    this.http.interceptors.request.use((config) => {
      const requestConfig = config as RetryableAxiosConfig;
      const session = this.authStorage.read();

      if (!requestConfig.skipAuth && session?.accessToken) {
        requestConfig.headers.Authorization = `Bearer ${session.accessToken}`;
      }

      return requestConfig;
    });

    this.http.interceptors.response.use(
      (response) => response,
      async (error: AxiosError<ApiResponse<unknown>>) => {
        const config = error.config as RetryableAxiosConfig | undefined;

        if (
          error.response?.status !== 401 ||
          !config ||
          config.skipRefresh ||
          config.retried
        ) {
          throw this.toError(error);
        }

        config.retried = true;

        try {
          const session = await this.refresh();
          config.headers.Authorization = `Bearer ${session.accessToken}`;

          return this.http.request(config);
        } catch (refreshError) {
          this.authStorage.clear();
          throw refreshError instanceof Error
            ? refreshError
            : new Error("인증이 만료되었습니다. 다시 로그인하세요.");
        }
      }
    );
  }

  private async refreshAccessToken(): Promise<AuthSession> {
    const currentSession = this.authStorage.read();

    if (!currentSession?.refreshToken) {
      this.authStorage.clear();
      throw new Error("refreshToken이 없어 다시 로그인해야 합니다.");
    }

    const response = await this.request<AdminAuthResponse>("/api/v1/auth/refresh", {
      method: "POST",
      data: { refreshToken: currentSession.refreshToken },
      skipAuth: true,
      skipRefresh: true,
    });
    const nextSession = this.toSession(currentSession.email, response.data);

    this.authStorage.write(nextSession);

    return nextSession;
  }

  private toSession(email: string, data?: AdminAuthResponse): AuthSession {
    if (!data?.accessToken) {
      throw new Error("인증 응답에 accessToken이 없습니다.");
    }

    return {
      email,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      tokenType: data.tokenType || "Bearer",
    };
  }

  private toError(error: AxiosError<ApiResponse<unknown>>): Error {
    const status = error.response?.status;
    const serverMessage = error.response?.data?.message;

    if (status === 401) {
      return new Error(serverMessage || "인증이 필요합니다.");
    }

    return new Error(serverMessage || `API 요청 실패: ${status ?? error.message}`);
  }
}

declare module "axios" {
  export interface AxiosRequestConfig {
    skipAuth?: boolean;
    skipRefresh?: boolean;
    retried?: boolean;
  }

  export interface InternalAxiosRequestConfig {
    skipAuth?: boolean;
    skipRefresh?: boolean;
    retried?: boolean;
  }
}

/**
 * query object를 REST API query string으로 변환하는 유틸리티 서비스입니다.
 */
export class QueryStringService {
  /**
   * 빈 값은 제외하고 URLSearchParams 문자열로 변환합니다.
   */
  stringify(value: JsonRecord): string {
    const params = new URLSearchParams();

    Object.entries(value).forEach(([key, fieldValue]) => {
      if (fieldValue === "" || fieldValue === undefined || fieldValue === null) {
        return;
      }

      params.set(key, String(fieldValue));
    });

    return params.toString();
  }
}
