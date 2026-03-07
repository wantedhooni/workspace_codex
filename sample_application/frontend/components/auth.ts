export const TOKEN_KEY = "securities_access_token";
const ROLE_KEY = "securities_roles";

export function getToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function setRoles(roles: string[]): void {
  localStorage.setItem(ROLE_KEY, JSON.stringify(roles));
}

export function getRoles(): string[] {
  if (typeof window === "undefined") {
    return [];
  }

  const raw = localStorage.getItem(ROLE_KEY);
  if (!raw) {
    return [];
  }

  try {
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter((v): v is string => typeof v === "string");
  } catch {
    return [];
  }
}

export function hasRole(role: string): boolean {
  return getRoles().includes(role);
}
