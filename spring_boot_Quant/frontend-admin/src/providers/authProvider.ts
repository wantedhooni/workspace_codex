import type { AuthProvider } from "react-admin";
import { appProfile, isLocalProfile } from "../config/profile";

type SessionUser = {
  id: string;
  fullName: string;
  username: string;
  accessToken: string;
  expiresAtEpochMs: number;
};

type DemoAccount = {
  username: string;
  password: string;
  fullName: string;
};

type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: number;
  email: string;
  name: string;
  roleCodes: string[];
};

type AccountMeResponse = {
  userId: number;
  email: string;
  name: string;
  status: string;
  roleCodes: string[];
};

type AccountMenuItem = {
  menuId: number;
  menuKey: string;
  menuLabel: string;
  path: string;
  icon: string;
  sortOrder: number;
  canRead: boolean;
  canCreate: boolean;
  canUpdate: boolean;
  canDelete: boolean;
};

type AccountMenusResponse = {
  items?: AccountMenuItem[];
};

type ApiError = {
  message?: string;
};

export type MenuPermissionMap = Record<string, AccountMenuItem>;

export type AppPermissions = {
  username: string;
  userId: number;
  roleCodes: string[];
  menuPermissions: MenuPermissionMap;
};

const SESSION_STORAGE_KEY = "quant.admin.session";
const API_BASE = "http://127.0.0.1:8088";

export const DEMO_LOCAL_ACCOUNTS: DemoAccount[] = [
  { username: "admin@quant.io", password: "demo1234", fullName: "System Admin" },
  { username: "trader@quant.io", password: "trader1234", fullName: "Execution Trader" },
  { username: "risk@quant.io", password: "risk1234", fullName: "Risk Officer" },
  { username: "viewer@quant.io", password: "viewer1234", fullName: "Read Only" }
];

export const DEMO_ADMIN_CREDENTIALS = {
  username: "admin@quant.io",
  password: "demo1234"
};

let cachedPermissions: AppPermissions | null = null;

export const isLocalRuntime = () => {
  if (typeof window === "undefined") {
    return isLocalProfile;
  }
  const host = window.location.hostname.toLowerCase();
  const isLocalHost = host === "localhost" || host === "127.0.0.1";
  return isLocalProfile || isLocalHost;
};

const readApiMessage = async (res: Response) => {
  try {
    const body = (await res.json()) as ApiError;
    if (body?.message) {
      return body.message;
    }
  } catch {
    // ignore json parse failure
  }
  return `${res.status} ${res.statusText}`;
};

export const getSessionUser = (): SessionUser | null => {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as SessionUser;
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
};

const isSessionExpired = (session: SessionUser | null): boolean => {
  if (!session) {
    return true;
  }
  return Date.now() >= Number(session.expiresAtEpochMs ?? 0);
};

export const getAuthHeaders = (): Record<string, string> => {
  const session = getSessionUser();
  if (!session || isSessionExpired(session) || !session.accessToken) {
    return {};
  }
  return {
    Authorization: `Bearer ${session.accessToken}`
  };
};

const setSessionUser = (user: SessionUser) => {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
};

const clearSessionUser = () => {
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
  }
};

const buildPermissionMap = (items: AccountMenuItem[]) => {
  const map: MenuPermissionMap = {};
  items.forEach((item) => {
    map[item.menuKey] = item;
  });
  return map;
};

const fallbackPermissions = (session: SessionUser): AppPermissions => ({
  username: session.username,
  userId: Number(session.id || 0) || 1,
  roleCodes: ["ADMIN"],
  menuPermissions: {
    orders: { menuId: 0, menuKey: "orders", menuLabel: "주문", path: "/#/orders", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: true },
    orderAudits: { menuId: 0, menuKey: "orderAudits", menuLabel: "주문감사", path: "/#/orderAudits", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: false, canDelete: false },
    trades: { menuId: 0, menuKey: "trades", menuLabel: "체결", path: "/#/trades", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: false },
    positions: { menuId: 0, menuKey: "positions", menuLabel: "포지션", path: "/#/positions", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: false, canDelete: false },
    portfolios: { menuId: 0, menuKey: "portfolios", menuLabel: "포트폴리오", path: "/#/portfolios", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: false, canDelete: false },
    savedViews: { menuId: 0, menuKey: "savedViews", menuLabel: "저장 뷰", path: "/#/savedViews", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: false, canDelete: true },
    portfolioSummaries: { menuId: 0, menuKey: "portfolioSummaries", menuLabel: "포트폴리오 요약", path: "/#/portfolioSummaries", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: false, canDelete: false },
    orderHealth: { menuId: 0, menuKey: "orderHealth", menuLabel: "주문건전성", path: "/#/orderHealth", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: true, canDelete: false },
    riskAlerts: { menuId: 0, menuKey: "riskAlerts", menuLabel: "리스크 경보", path: "/#/riskAlerts", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: true, canDelete: false },
    executionQualities: { menuId: 0, menuKey: "executionQualities", menuLabel: "체결품질", path: "/#/executionQualities", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: false, canDelete: false },
    riskLimits: { menuId: 0, menuKey: "riskLimits", menuLabel: "리스크 한도", path: "/#/riskLimits", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: false },
    users: { menuId: 0, menuKey: "users", menuLabel: "사용자", path: "/#/users", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: true },
    roles: { menuId: 0, menuKey: "roles", menuLabel: "권한", path: "/#/roles", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: true },
    menus: { menuId: 0, menuKey: "menus", menuLabel: "메뉴", path: "/#/menus", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: true },
    menuPermissions: { menuId: 0, menuKey: "menuPermissions", menuLabel: "메뉴권한", path: "/#/menuPermissions", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: true },
    journalVouchers: { menuId: 0, menuKey: "journalVouchers", menuLabel: "전표", path: "/#/journalVouchers", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: false },
    ledgerEntries: { menuId: 0, menuKey: "ledgerEntries", menuLabel: "원장", path: "/#/ledgerEntries", icon: "", sortOrder: 0, canRead: true, canCreate: false, canUpdate: true, canDelete: false },
    accountProfile: { menuId: 0, menuKey: "accountProfile", menuLabel: "내 계정", path: "/#/accountProfile", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: false },
    accountSessions: { menuId: 0, menuKey: "accountSessions", menuLabel: "내 세션", path: "/#/accountSessions", icon: "", sortOrder: 0, canRead: true, canCreate: true, canUpdate: true, canDelete: false }
  }
});

const loadPermissions = async (session: SessionUser): Promise<AppPermissions> => {
  const headers = getAuthHeaders();

  const [meRes, menusRes] = await Promise.all([
    fetch(`${API_BASE}/api/account/me`, { headers }),
    fetch(`${API_BASE}/api/account/menus`, { headers })
  ]);

  if (!meRes.ok || !menusRes.ok) {
    return fallbackPermissions(session);
  }

  const me = (await meRes.json()) as AccountMeResponse;
  const menus = (await menusRes.json()) as AccountMenusResponse;

  return {
    username: session.username,
    userId: Number(me.userId),
    roleCodes: Array.isArray(me.roleCodes) ? me.roleCodes : [],
    menuPermissions: buildPermissionMap(Array.isArray(menus.items) ? menus.items : [])
  };
};

export const authProvider: AuthProvider = {
  login: async (params) => {
    const email = typeof params?.username === "string" ? params.username.trim().toLowerCase() : "";
    const password = typeof params?.password === "string" ? params.password : "";

    if (!email || !password) {
      throw new Error("아이디와 비밀번호를 입력해 주세요.");
    }

    const loginRes = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });

    if (!loginRes.ok) {
      throw new Error(await readApiMessage(loginRes));
    }

    const loginData = (await loginRes.json()) as LoginResponse;
    const matched = DEMO_LOCAL_ACCOUNTS.find((account) => account.username === loginData.email);

    const expiresAtEpochMs = Date.now() + Math.max(1, Number(loginData.expiresInSeconds ?? 0)) * 1000;
    setSessionUser({
      id: String(loginData.userId),
      fullName: matched?.fullName ?? loginData.name ?? "Quant User",
      username: loginData.email,
      accessToken: loginData.accessToken,
      expiresAtEpochMs
    });

    cachedPermissions = null;
    return Promise.resolve();
  },

  logout: async () => {
    clearSessionUser();
    cachedPermissions = null;
    return Promise.resolve();
  },

  checkAuth: async () => {
    const session = getSessionUser();
    if (!session || isSessionExpired(session)) {
      clearSessionUser();
      cachedPermissions = null;
      return Promise.reject();
    }
    return Promise.resolve();
  },

  checkError: async () => Promise.resolve(),

  getIdentity: async () => {
    const session = getSessionUser();
    if (!session) {
      return { id: "anonymous", fullName: "Anonymous" };
    }
    return { id: session.id, fullName: session.fullName };
  },

  getPermissions: async () => {
    const session = getSessionUser();
    if (!session || isSessionExpired(session)) {
      clearSessionUser();
      cachedPermissions = null;
      return Promise.reject();
    }

    if (cachedPermissions && cachedPermissions.username === session.username) {
      return cachedPermissions;
    }

    try {
      const loaded = await loadPermissions(session);
      cachedPermissions = loaded;
      return loaded;
    } catch (error) {
      throw new Error(error instanceof Error ? error.message : `[${appProfile}] 권한 정보를 불러오지 못했습니다.`);
    }
  }
};
