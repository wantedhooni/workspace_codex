import { request } from "../../shared/api/http";
import type { AuthResponse, Profile } from "./types";

export const authApi = {
  login(email: string, password: string) {
    return request<AuthResponse>("/api/user/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },
  signup(email: string, password: string, fullName: string) {
    return request<AuthResponse>("/api/user/auth/signup", {
      method: "POST",
      body: JSON.stringify({ email, password, fullName }),
    });
  },
  me(token: string) {
    return request<Profile>("/api/user/me", {}, token);
  },
};
