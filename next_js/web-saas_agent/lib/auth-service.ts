import { cookies } from "next/headers";

export type LoginRequest = {
  email: string;
  password: string;
};

export type UserAuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
};

export type ApiResponse<T> = {
  success?: boolean;
  data?: T;
  message?: string;
  timestamp?: number;
};

const ACCESS_TOKEN_COOKIE = "saas_access_token";
const REFRESH_TOKEN_COOKIE = "saas_refresh_token";
const API_BASE_URL = process.env.SAAS_API_BASE_URL ?? "http://localhost:8091";

function cookieOptions() {
  return {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
  };
}

/**
 * SaaS 인증 API와 Next.js 앱 사이의 서버 전용 인증 처리를 담당하는 서비스 클래스입니다.
 */
export class AuthService {
  private readonly baseUrl: string;

  /**
   * 인증 API 기본 주소를 주입받아 백엔드 호출 경로를 구성합니다.
   */
  constructor(baseUrl = API_BASE_URL) {
    this.baseUrl = baseUrl.replace(/\/$/, "");
  }

  /**
   * 이메일과 비밀번호로 백엔드 로그인 API를 호출하고 발급받은 토큰을 반환합니다.
   */
  async login(payload: LoginRequest) {
    return this.request<UserAuthResponse>("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  /**
   * 현재 토큰을 이용해 백엔드 로그아웃 API를 호출합니다.
   */
  async logout(accessToken?: string, refreshToken?: string) {
    return this.request<void>("/api/v1/auth/logout", {
      method: "POST",
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined,
      body: JSON.stringify({ refreshToken }),
    });
  }

  /**
   * 리프레시 토큰으로 새 인증 토큰 쌍을 발급받습니다.
   */
  async refresh(refreshToken: string) {
    return this.request<UserAuthResponse>("/api/v1/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
  }

  /**
   * 액세스 토큰으로 현재 인증 주체 정보를 조회합니다.
   */
  async me(accessToken: string) {
    return this.request<unknown>("/api/v1/auth/me", {
      method: "GET",
      headers: { Authorization: `Bearer ${accessToken}` },
    });
  }

  /**
   * 공통 JSON 요청 형식과 API 응답 오류 처리를 캡슐화합니다.
   */
  private async request<T>(path: string, init: RequestInit) {
    const response = await fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(init.headers ?? {}),
      },
      cache: "no-store",
    });

    const json = (await response.json().catch(() => ({}))) as ApiResponse<T>;

    if (!response.ok || json.success === false) {
      throw new Error(json.message ?? "인증 API 요청에 실패했습니다.");
    }

    return json;
  }
}

/**
 * 인증 토큰 쿠키 저장과 삭제를 담당하는 서버 전용 헬퍼 클래스입니다.
 */
export class AuthCookieStore {
  /**
   * 로그인 또는 토큰 갱신으로 받은 토큰을 HttpOnly 쿠키에 저장합니다.
   */
  static async setTokens(tokens: UserAuthResponse) {
    const store = await cookies();
    store.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, {
      ...cookieOptions(),
      maxAge: 60 * 30,
    });
    store.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, {
      ...cookieOptions(),
      maxAge: 60 * 60 * 24 * 7,
    });
  }

  /**
   * 현재 요청의 인증 토큰 쿠키 값을 조회합니다.
   */
  static async getTokens() {
    const store = await cookies();
    return {
      accessToken: store.get(ACCESS_TOKEN_COOKIE)?.value,
      refreshToken: store.get(REFRESH_TOKEN_COOKIE)?.value,
    };
  }

  /**
   * 인증 토큰 쿠키를 만료시켜 브라우저 세션을 정리합니다.
   */
  static async clearTokens() {
    const store = await cookies();
    store.delete(ACCESS_TOKEN_COOKIE);
    store.delete(REFRESH_TOKEN_COOKIE);
  }
}

export const authService = new AuthService();
