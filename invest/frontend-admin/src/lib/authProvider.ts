"use client";

import type { AuthProvider } from "react-admin";

const TOKEN_KEY = "quant_admin_basic_token";
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";
const ADMIN_ONLY_MENU_KEYS = new Set(["users", "menu-permissions"]);

type CurrentUserPayload = {
  username: string;
  roles: string[];
};

export type UserMenuPermissionPayload = {
  menuKey: string;
  canList: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
};

export type AuthPermissions = {
  roles: string[];
  menuPermissions: UserMenuPermissionPayload[];
};

type CurrentUserResponse = {
  data: CurrentUserPayload;
};

type MenuPermissionListResponse = {
  data: UserMenuPermissionPayload[];
  total: number;
};

function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function getAuthorizationHeader(): string | null {
  const token = getToken();
  return token ? `Basic ${token}` : null;
}

async function fetchCurrentUser(token: string): Promise<CurrentUserPayload> {
  const response = await fetch(`${API_BASE_URL}/users/me`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      Authorization: `Basic ${token}`
    }
  });

  if (!response.ok) {
    throw new Error("Authentication failed");
  }

  const body = (await response.json()) as CurrentUserResponse;
  return body.data;
}

async function fetchUserMenuPermissions(token: string): Promise<UserMenuPermissionPayload[]> {
  const response = await fetch(`${API_BASE_URL}/users/me/menu-permissions`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      Authorization: `Basic ${token}`
    }
  });

  if (response.status === 404) {
    return [];
  }

  if (!response.ok) {
    throw new Error("Failed to load menu permissions");
  }

  const body = (await response.json()) as MenuPermissionListResponse;
  return body.data ?? [];
}

async function fetchAuthPermissions(token: string): Promise<AuthPermissions> {
  const currentUser = await fetchCurrentUser(token);
  const menuPermissions = await fetchUserMenuPermissions(token);
  return {
    roles: currentUser.roles,
    menuPermissions
  };
}

export function hasMenuPermission(
  permissions: AuthPermissions | null | undefined,
  menuKey: string,
  action: "list" | "create" | "edit" | "delete" = "list"
): boolean {
  if (!permissions) {
    return true;
  }

  const roles = permissions.roles ?? [];
  if (roles.includes("ROLE_ADMIN")) {
    return true;
  }

  const menuPermissions = permissions.menuPermissions ?? [];
  const target = menuPermissions.find((permission) => permission.menuKey === menuKey);
  if (menuPermissions.length === 0) {
    return !ADMIN_ONLY_MENU_KEYS.has(menuKey);
  }

  if (!target) {
    return false;
  }

  if (action === "create") {
    return target.canCreate;
  }
  if (action === "edit") {
    return target.canEdit;
  }
  if (action === "delete") {
    return target.canDelete;
  }
  return target.canList;
}

export const authProvider: AuthProvider = {
  login: async ({ username, password }) => {
    if (!username || !password) {
      throw new Error("username and password are required");
    }

    const token = btoa(`${username}:${password}`);
    await fetchCurrentUser(token);
    setToken(token);
  },
  logout: async () => {
    clearToken();
  },
  checkAuth: async () => {
    const token = getToken();
    if (!token) {
      throw new Error("Not authenticated");
    }
    await fetchAuthPermissions(token);
  },
  checkError: async (error) => {
    const status = error?.status;
    if (status === 401 || status === 403) {
      clearToken();
      throw error;
    }
  },
  getIdentity: async () => {
    const token = getToken();
    if (!token) {
      throw new Error("Not authenticated");
    }
    const currentUser = await fetchCurrentUser(token);
    return {
      id: currentUser.username,
      fullName: currentUser.username
    };
  },
  getPermissions: async () => {
    const token = getToken();
    if (!token) {
      return {
        roles: [],
        menuPermissions: []
      } as AuthPermissions;
    }
    return fetchAuthPermissions(token);
  }
};
