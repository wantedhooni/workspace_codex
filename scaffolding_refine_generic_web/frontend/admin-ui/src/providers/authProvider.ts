import { AuthBindings } from "@refinedev/core";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8090";
const AUTH_COOKIE_NAME = "ADMIN_TOKEN";

const request = async (path: string, options?: RequestInit) => {
  const res = await fetch(`${API_URL}${path}`, {
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    throw new Error(await res.text());
  }
  return res;
};

const setAuthCookie = (token: string) => {
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${AUTH_COOKIE_NAME}=${encodeURIComponent(token)}; Path=/; SameSite=Lax${secure}`;
};

const clearAuthCookie = () => {
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${AUTH_COOKIE_NAME}=; Path=/; Max-Age=0; SameSite=Lax${secure}`;
};

export const authProvider: AuthBindings = {
  login: async ({ username, password }) => {
    const res = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    const payload = (await res.json()) as { token?: string };
    if (payload.token && payload.token.trim().length > 0) {
      setAuthCookie(payload.token);
    }
    return { success: true, redirectTo: "/" };
  },
  logout: async () => {
    await request("/auth/logout", { method: "POST" });
    clearAuthCookie();
    return { success: true, redirectTo: "/login" };
  },
  check: async () => {
    try {
      await request("/auth/me");
      return { authenticated: true };
    } catch {
      return { authenticated: false, redirectTo: "/login" };
    }
  },
  onError: async (error) => {
    return { error, logout: true, redirectTo: "/login" };
  },
  getIdentity: async () => {
    const res = await request("/auth/me");
    return res.json();
  },
};
