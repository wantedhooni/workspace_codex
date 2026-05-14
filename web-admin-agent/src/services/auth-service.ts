import { ApiClient } from "@/services/api-client";
import type { ApiResponse, AuthSession, JsonRecord, LoginRequest } from "@/types/admin-api";

/**
 * api-docs.json의 auth-controller 엔드포인트를 담당하는 인증 서비스입니다.
 * 로그인, 로그아웃, 토큰 갱신, 현재 인증 주체 조회를 제공합니다.
 */
export class AuthService {
  constructor(private readonly apiClient = new ApiClient()) {}

  /**
   * `POST /api/v1/auth/login`을 호출하고 JWT 세션을 저장합니다.
   */
  async login(payload: LoginRequest): Promise<AuthSession> {
    return this.apiClient.login(payload);
  }

  /**
   * `POST /api/v1/auth/logout`을 호출하고 저장된 JWT 세션을 제거합니다.
   */
  async logout(): Promise<void> {
    await this.apiClient.logout();
  }

  /**
   * `POST /api/v1/auth/refresh`를 호출해 accessToken을 갱신합니다.
   */
  async refresh(): Promise<AuthSession> {
    return this.apiClient.refresh();
  }

  /**
   * `GET /api/v1/auth/me`를 호출해 현재 인증 주체 정보를 조회합니다.
   */
  async me(): Promise<ApiResponse<JsonRecord>> {
    return this.apiClient.request<JsonRecord>("/api/v1/auth/me");
  }
}
