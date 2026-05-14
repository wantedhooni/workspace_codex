import { apiClient } from "@/lib/api-client";
import type {
  ApiResponse,
  JwtPrincipal,
  LoginRequest,
  LogoutRequest,
  UserAuthResponse,
} from "@/services/auth/auth.types";

export function loginApi(payload: LoginRequest) {
  return apiClient<ApiResponse<UserAuthResponse>>("/api/v1/auth/login", {
    method: "POST",
    body: payload,
    auth: false,
  });
}

export function logoutApi(params: {
  accessToken: string;
  refreshToken: string;
}) {
  const payload: LogoutRequest = {
    refreshToken: params.refreshToken,
  };

  return apiClient<ApiResponse<void>>("/api/v1/auth/logout", {
    method: "POST",
    body: payload,
    auth: false,
    headers: {
      Authorization: `Bearer ${params.accessToken}`,
    },
  });
}

export function meApi() {
  return apiClient<ApiResponse<JwtPrincipal>>("/api/v1/auth/me", {
    method: "GET",
    auth: true,
  });
}