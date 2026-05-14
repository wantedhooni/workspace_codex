"use client";

import { useMemo, useState, useSyncExternalStore } from "react";
import {
  BarChart3,
  Database,
  LayoutDashboard,
  LogOut,
  RefreshCw,
  ShieldCheck,
  Users,
  WalletCards,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AccountService, AccountTransactionService, AdminService, ApiClient, AuthService, AuthStorageService } from "@/services";
import type { BaseCrudService } from "@/services/base-crud-service";
import type { AuthSession, DomainKey, NavigationKey } from "@/types/admin-api";
import { cn } from "@/lib/utils";
import { domainConfigs } from "./domain-config";
import { DomainCrudView } from "./domain-crud-view";

const navigationItems: Array<{
  key: NavigationKey;
  label: string;
  icon: typeof LayoutDashboard;
}> = [
  { key: "dashboard", label: "대시보드", icon: LayoutDashboard },
  { key: "admin", label: "관리자", icon: Users },
  { key: "account", label: "계좌", icon: WalletCards },
  { key: "accounttransaction", label: "계좌 거래", icon: Database },
];

/**
 * 관리자 콘솔 전체 화면을 구성하는 최상위 클라이언트 컴포넌트입니다.
 * 인증 상태에 따라 로그인 페이지와 운영 대시보드를 전환합니다.
 */
export function AdminConsoleApp() {
  const services = useMemo(() => {
    const apiClient = new ApiClient();

    return {
      auth: new AuthService(apiClient),
      domains: {
        admin: new AdminService(apiClient),
        account: new AccountService(apiClient),
        accounttransaction: new AccountTransactionService(apiClient),
      } satisfies Record<DomainKey, BaseCrudService>,
    };
  }, []);
  const authStorage = useMemo(() => new AuthStorageService(), []);
  const sessionRaw = useSyncExternalStore(
    (onStoreChange) => authStorage.subscribe(onStoreChange),
    () => authStorage.readRaw(),
    () => null
  );
  const session = useMemo<AuthSession | null>(
    () => (sessionRaw ? (JSON.parse(sessionRaw) as AuthSession) : null),
    [sessionRaw]
  );
  const [activeMenu, setActiveMenu] = useState<NavigationKey>("dashboard");

  const activeDomain = domainConfigs.find((domain) => domain.key === activeMenu);
  const activeCrudService =
    activeDomain && activeDomain.key in services.domains
      ? services.domains[activeDomain.key]
      : null;

  const handleLogin = (nextSession: AuthSession) => {
    authStorage.write(nextSession);
    setActiveMenu("dashboard");
  };

  const handleLogout = async () => {
    try {
      await services.auth.logout();
    } catch {
      authStorage.clear();
    }
    setActiveMenu("dashboard");
  };

  if (!session?.accessToken) {
    return <LoginPage authService={services.auth} onLogin={handleLogin} />;
  }

  return (
    <div className="flex min-h-screen bg-slate-50 text-slate-950">
      <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white lg:block">
        <div className="flex h-16 items-center gap-3 border-b border-slate-200 px-5">
          <div className="flex size-9 items-center justify-center rounded-lg bg-blue-600 text-white">
            <ShieldCheck className="size-5" />
          </div>
          <div>
            <div className="text-sm font-semibold">ADMIN Console</div>
            <div className="text-xs text-slate-500">JWT 운영 관리자</div>
          </div>
        </div>
        <nav className="space-y-1 p-3">
          {navigationItems.map((item) => {
            const Icon = item.icon;
            const selected = activeMenu === item.key;

            return (
              <button
                key={item.key}
                type="button"
                onClick={() => setActiveMenu(item.key)}
                className={cn(
                  "flex h-10 w-full items-center gap-3 rounded-lg px-3 text-left text-sm font-medium transition-colors",
                  selected
                    ? "bg-blue-50 text-blue-700"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-6">
          <div>
            <div className="text-sm font-semibold">
              {activeMenu === "dashboard" ? "대시보드" : activeDomain?.label}
            </div>
            <div className="text-xs text-slate-500">
              API 서버:{" "}
              {process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081"}
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <div className="text-sm font-medium">{session.email}</div>
              <div className="text-xs text-slate-500">{session.tokenType || "Bearer"} 인증</div>
            </div>
            <Button variant="outline" size="sm" onClick={handleLogout}>
              <LogOut className="size-4" />
              로그아웃
            </Button>
          </div>
        </header>

        <main className="min-w-0 flex-1 p-4 lg:p-6">
          {activeMenu === "dashboard" ? (
            <DashboardIndex session={session} onNavigate={(key) => setActiveMenu(key)} />
          ) : (
            activeDomain && activeCrudService && (
              <DomainCrudView
                key={activeDomain.key}
                config={activeDomain}
                crudService={activeCrudService}
              />
            )
          )}
        </main>
      </div>
    </div>
  );
}

function LoginPage({
  authService,
  onLogin,
}: {
  authService: AuthService;
  onLogin: (session: AuthSession) => void;
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const session = await authService.login({ email, password });
      onLogin(session);
    } catch (errorValue) {
      setError(errorValue instanceof Error ? errorValue.message : "로그인에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-[420px] rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8">
          <div className="mb-4 flex size-11 items-center justify-center rounded-lg bg-blue-600 text-white">
            <ShieldCheck className="size-6" />
          </div>
          <h1 className="text-2xl font-semibold tracking-tight">ADMIN API 로그인</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            JWT 기반 관리자 콘솔입니다. 발급받은 관리자 계정으로 로그인하세요.
          </p>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <label className="block space-y-1.5">
            <span className="text-sm font-medium">이메일</span>
            <Input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="admin@example.com"
              autoComplete="username"
              required
            />
          </label>
          <label className="block space-y-1.5">
            <span className="text-sm font-medium">비밀번호</span>
            <Input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="비밀번호"
              autoComplete="current-password"
              required
            />
          </label>

          {error && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </div>
          )}

          <Button className="h-10 w-full" type="submit" disabled={isLoading}>
            {isLoading ? "로그인 중" : "로그인"}
          </Button>
        </form>
      </div>
    </div>
  );
}

function DashboardIndex({
  session,
  onNavigate,
}: {
  session: AuthSession;
  onNavigate: (key: DomainKey) => void;
}) {
  const metrics = [
    { label: "관리 도메인", value: "3", caption: "Admin, Account, Transaction" },
    { label: "인증 방식", value: "JWT", caption: session.tokenType || "Bearer" },
    { label: "API Base", value: "8081", caption: "localhost 기본 서버" },
  ];

  return (
    <div className="space-y-6">
      <section className="grid gap-3 md:grid-cols-3">
        {metrics.map((metric) => (
          <div key={metric.label} className="rounded-lg border border-slate-200 bg-white p-5">
            <div className="text-sm font-medium text-slate-500">{metric.label}</div>
            <div className="mt-2 text-2xl font-semibold">{metric.value}</div>
            <div className="mt-1 text-xs text-slate-500">{metric.caption}</div>
          </div>
        ))}
      </section>

      <section className="grid gap-4 xl:grid-cols-[1fr_360px]">
        <div className="rounded-lg border border-slate-200 bg-white">
          <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
            <div>
              <h2 className="text-base font-semibold">도메인 작업 바로가기</h2>
              <p className="text-sm text-slate-500">각 메뉴는 별도 검색 필터와 공통 AG Grid 템플릿을 사용합니다.</p>
            </div>
            <BarChart3 className="size-5 text-blue-600" />
          </div>
          <div className="divide-y divide-slate-100">
            {domainConfigs.map((domain) => (
              <button
                key={domain.key}
                type="button"
                onClick={() => onNavigate(domain.key)}
                className="flex w-full items-center justify-between px-5 py-4 text-left hover:bg-slate-50"
              >
                <div>
                  <div className="text-sm font-semibold">{domain.label}</div>
                  <div className="mt-1 text-sm text-slate-500">{domain.description}</div>
                </div>
                <span className="text-xs font-medium text-blue-700">열기</span>
              </button>
            ))}
          </div>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5">
          <div className="mb-4 flex items-center gap-2">
            <RefreshCw className="size-4 text-blue-600" />
            <h2 className="text-base font-semibold">운영 메모</h2>
          </div>
          <ul className="space-y-3 text-sm leading-6 text-slate-600">
            <li>계좌와 거래 API schema는 현재 object로만 제공되어 동적 컬럼을 함께 지원합니다.</li>
            <li>목록 검색은 pageable과 searchRequest query를 조합해 요청합니다.</li>
            <li>생성/수정/삭제 후 목록을 자동으로 다시 조회합니다.</li>
          </ul>
        </div>
      </section>
    </div>
  );
}
