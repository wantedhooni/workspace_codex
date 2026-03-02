import type { AuthBindings } from "@refinedev/core";
import { ApiError, api, clearAuth, ensureValidAccessToken, getRole, setAuth } from "../api/client";

type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  role: string;
};

export const authProvider: AuthBindings = {
  login: async ({ username, password }) => {
    try {
      const res = await api.post<LoginResponse>("/auth/login", { username, password });
      setAuth(res);
      return {
        success: true,
        redirectTo: "/",
      };
    } catch {
      return {
        success: false,
        error: {
          name: "LoginError",
          message: "아이디 또는 비밀번호를 확인해주세요.",
        },
      };
    }
  },
  logout: async () => {
    clearAuth();
    return {
      success: true,
      redirectTo: "/login",
    };
  },
  check: async () => {
    const token = await ensureValidAccessToken();
    if (token) {
      return {
        authenticated: true,
      };
    }
    return {
      authenticated: false,
      redirectTo: "/login",
    };
  },
  getPermissions: async () => {
    return getRole();
  },
  getIdentity: async () => {
    return {
      id: 1,
      name: "operations",
    };
  },
  onError: async (error) => {
    if (error instanceof ApiError && error.status === 401) {
      clearAuth();
      return {
        logout: true,
        redirectTo: "/login",
        error: {
          name: "AuthExpired",
          message: "세션이 만료되어 다시 로그인해야 합니다.",
        },
      };
    }

    return {
      error: {
        name: "AuthError",
        message: "인증 오류가 발생했습니다.",
      },
    };
  },
};
