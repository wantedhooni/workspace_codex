import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { AuthPermissions, authProvider, getAuthorizationHeader, hasMenuPermission } from "./authProvider";

function createLocalStorageMock() {
  const store = new Map<string, string>();
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => {
      store.set(key, value);
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
    clear: () => {
      store.clear();
    }
  } as Storage;
}

describe("authProvider", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    Object.defineProperty(globalThis, "localStorage", {
      value: createLocalStorageMock(),
      configurable: true
    });
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should save basic token after successful login", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
      const url = String(input);
      if (url.includes("/users/me/menu-permissions")) {
        return Promise.resolve(
          new Response(
            JSON.stringify({
              data: [],
              total: 0
            }),
            { status: 200, headers: { "Content-Type": "application/json" } }
          )
        );
      }

      return Promise.resolve(
        new Response(
          JSON.stringify({
            data: {
              username: "user",
              roles: ["ROLE_USER"]
            }
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      );
    });

    await authProvider.login({ username: "user", password: "user1234" });

    expect(getAuthorizationHeader()).toMatch(/^Basic /);
    await expect(authProvider.checkAuth()).resolves.toBeUndefined();
    await expect(authProvider.getPermissions()).resolves.toEqual({
      roles: ["ROLE_USER"],
      menuPermissions: []
    });
  });

  it("should not save token when login validation fails", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(() =>
      Promise.resolve(
        new Response(
          JSON.stringify({
            error: {
              message: "Unauthorized"
            }
          }),
          { status: 401, headers: { "Content-Type": "application/json" } }
        )
      )
    );

    await expect(authProvider.login({ username: "user", password: "wrong" })).rejects.toThrow(
      "Authentication failed"
    );
    expect(getAuthorizationHeader()).toBeNull();
  });

  it("should clear token when checkError receives 401", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(() =>
      Promise.resolve(
        new Response(
          JSON.stringify({
            data: {
              username: "admin",
              roles: ["ROLE_ADMIN"]
            }
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      )
    );

    await authProvider.login({ username: "admin", password: "admin1234" });
    expect(getAuthorizationHeader()).not.toBeNull();

    await expect(authProvider.checkError({ status: 401 })).rejects.toBeTruthy();
    expect(getAuthorizationHeader()).toBeNull();
  });

  it("should load user menu permissions", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
      const url = String(input);
      if (url.includes("/users/me/menu-permissions")) {
        return Promise.resolve(
          new Response(
            JSON.stringify({
              data: [
                {
                  menuKey: "portfolios",
                  canList: true,
                  canCreate: false,
                  canEdit: true,
                  canDelete: false
                }
              ],
              total: 1
            }),
            { status: 200, headers: { "Content-Type": "application/json" } }
          )
        );
      }
      return Promise.resolve(
        new Response(
          JSON.stringify({
            data: {
              username: "user",
              roles: ["ROLE_USER"]
            }
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      );
    });

    await authProvider.login({ username: "user", password: "user1234" });

    await expect(authProvider.getPermissions()).resolves.toEqual({
      roles: ["ROLE_USER"],
      menuPermissions: [
        {
          menuKey: "portfolios",
          canList: true,
          canCreate: false,
          canEdit: true,
          canDelete: false
        }
      ]
    });
  });
});

describe("hasMenuPermission", () => {
  it("should allow admin by default when menu rule is missing", () => {
    const permissions: AuthPermissions = {
      roles: ["ROLE_ADMIN"],
      menuPermissions: []
    };

    expect(hasMenuPermission(permissions, "portfolios", "list")).toBe(true);
    expect(hasMenuPermission(permissions, "portfolios", "create")).toBe(true);
  });

  it("should follow configured menu permissions for user role", () => {
    const permissions: AuthPermissions = {
      roles: ["ROLE_USER"],
      menuPermissions: [
        {
          menuKey: "transactions",
          canList: true,
          canCreate: false,
          canEdit: false,
          canDelete: false
        }
      ]
    };

    expect(hasMenuPermission(permissions, "transactions", "list")).toBe(true);
    expect(hasMenuPermission(permissions, "transactions", "create")).toBe(false);
    expect(hasMenuPermission(permissions, "portfolios", "list")).toBe(false);
  });

  it("should hide admin-only menus when user has no explicit menu permissions", () => {
    const permissions: AuthPermissions = {
      roles: ["ROLE_USER"],
      menuPermissions: []
    };

    expect(hasMenuPermission(permissions, "portfolios", "list")).toBe(true);
    expect(hasMenuPermission(permissions, "users", "list")).toBe(false);
    expect(hasMenuPermission(permissions, "menu-permissions", "list")).toBe(false);
  });
});
