import type { AuthBindings } from "@refinedev/core";
import { api, clearAuth, getToken, setAuth } from "../api/client";

type LoginResponse = {
  accessToken: string;
  expiresIn: number;
  role: string;
};

export const authProvider: AuthBindings = {
  login: async ({ username, password }) => {
    try {
      const res = await api.post<LoginResponse>("/auth/login", { username, password });
      setAuth(res.accessToken, res.role);
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
    const token = getToken();
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
    return localStorage.getItem("derivops_role") ?? null;
  },
  getIdentity: async () => {
    return {
      id: 1,
      name: "operations",
    };
  },
  onError: async () => {
    return {
      error: {
        name: "AuthError",
        message: "인증 오류가 발생했습니다.",
      },
    };
  },
};
