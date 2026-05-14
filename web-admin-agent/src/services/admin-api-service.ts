import type {
  AdminAuthResponse,
  ApiResponse,
  AuthSession,
  JsonRecord,
  LoginRequest,
  PageResponse,
} from "@/types/admin-api";

const TOKEN_STORAGE_KEY = "web-admin-agent.session";
const TOKEN_CHANGE_EVENT = "web-admin-agent.session-change";

/**
 * ADMIN-API와 통신하는 공통 REST 서비스입니다.
 * JWT 인증 헤더, 공통 응답 포맷, CRUD 요청을 한 곳에서 관리합니다.
 */
export class AdminApiService {
  private readonly baseUrl: string;

  constructor(
    baseUrl =
      process.env.NEXT_PUBLIC_API_BASE_URL ??
      process.env.NEXT_PUBLIC_API_URL ??
      "http://localhost:8081"
  ) {
    this.baseUrl = baseUrl.replace(/\/$/, "");
  }

  /**
   * 로그인 API를 호출하고 accessToken, refreshToken을 포함한 세션을 반환합니다.
   */
  async login(payload: LoginRequest): Promise<AuthSession> {
    const response = await this.request<AdminAuthResponse>("/api/v1/auth/login", {
      method: "POST",
      body: payload,
    });

    if (!response.data?.accessToken) {
      throw new Error(response.message || "로그인 응답에 accessToken이 없습니다.");
    }

    return {
      email: payload.email,
      accessToken: response.data.accessToken,
      refreshToken: response.data.refreshToken,
      tokenType: response.data.tokenType || "Bearer",
    };
  }

  /**
   * refreshToken을 서버에 전달해 현재 세션을 무효화합니다.
   */
  async logout(session: AuthSession): Promise<void> {
    await this.request<void>("/api/v1/auth/logout", {
      method: "POST",
      token: session.accessToken,
      body: { refreshToken: session.refreshToken },
    });
  }

  /**
   * 현재 accessToken으로 인증 주체 정보를 조회합니다.
   */
  async me(token: string): Promise<ApiResponse<JsonRecord>> {
    return this.request<JsonRecord>("/api/v1/auth/me", { token });
  }

  /**
   * 도메인 목록을 pageable과 검색 조건으로 조회합니다.
   */
  async search(
    endpoint: string,
    token: string,
    searchRequest: JsonRecord,
    page = 0,
    size = 20
  ): Promise<PageResponse<JsonRecord>> {
    const query = this.toQueryString({
      page,
      size,
      ...this.prefixObject("searchRequest", searchRequest),
    });
    const response = await this.request<PageResponse<JsonRecord>>(`${endpoint}?${query}`, {
      token,
    });

    return response.data ?? {
      content: [],
      totalElements: 0,
      totalPages: 0,
      page,
      size,
    };
  }

  /**
   * 도메인 단건을 조회합니다.
   */
  async get(endpoint: string, token: string, id: string | number): Promise<JsonRecord> {
    const response = await this.request<JsonRecord>(`${endpoint}/${id}`, { token });
    return response.data ?? {};
  }

  /**
   * 도메인 데이터를 생성합니다.
   */
  async create(endpoint: string, token: string, body: JsonRecord): Promise<void> {
    await this.request<void>(endpoint, { method: "POST", token, body });
  }

  /**
   * 도메인 데이터를 수정합니다.
   */
  async update(
    endpoint: string,
    token: string,
    id: string | number,
    body: JsonRecord
  ): Promise<void> {
    await this.request<void>(`${endpoint}/${id}`, { method: "PATCH", token, body });
  }

  /**
   * 도메인 데이터를 삭제합니다.
   */
  async delete(endpoint: string, token: string, id: string | number): Promise<void> {
    await this.request<void>(`${endpoint}/${id}`, { method: "DELETE", token });
  }

  private async request<T>(
    path: string,
    options: {
      method?: "GET" | "POST" | "PATCH" | "DELETE";
      token?: string;
      body?: object;
    } = {}
  ): Promise<ApiResponse<T>> {
    const headers = new Headers();
    headers.set("Accept", "application/json");

    if (options.body) {
      headers.set("Content-Type", "application/json");
    }

    if (options.token) {
      headers.set("Authorization", `Bearer ${options.token}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      method: options.method ?? "GET",
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
    });

    const text = await response.text();
    const parsed = text ? (JSON.parse(text) as ApiResponse<T>) : {};

    if (!response.ok || parsed.success === false) {
      throw new Error(parsed.message || `API 요청 실패: ${response.status}`);
    }

    return parsed;
  }

  private prefixObject(prefix: string, value: JsonRecord): JsonRecord {
    return Object.fromEntries(
      Object.entries(value)
        .filter(([, fieldValue]) => fieldValue !== "" && fieldValue !== undefined)
        .map(([key, fieldValue]) => [`${prefix}.${key}`, fieldValue])
    );
  }

  private toQueryString(value: JsonRecord): string {
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

/**
 * 브라우저 localStorage에 JWT 세션을 저장하고 복원하는 인증 서비스입니다.
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
    return raw ? (JSON.parse(raw) as AuthSession) : null;
  }

  /**
   * 로그인 성공 후 인증 세션을 저장합니다.
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
