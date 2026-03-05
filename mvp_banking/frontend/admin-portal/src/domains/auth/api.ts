import { request } from "../../shared/api/http";
import type { AuthResponse, Profile } from "../../types";

export const adminAuthApi = {
  login(email: string, password: string) {
    return request<AuthResponse>("/api/admin/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },
  me(token: string) {
    return request<Profile>("/api/admin/me", {}, token);
  },
};
