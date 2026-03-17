"use client";

import { FormEvent, useEffect, useState } from "react";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8086";

const demoAccounts = [
  { tenantId: "alpha", username: "alpha.admin", password: "demo1234", label: "Alpha 관리자", accent: "alpha" },
  { tenantId: "alpha", username: "alpha.viewer", password: "demo1234", label: "Alpha 조회자", accent: "alpha" },
  { tenantId: "beta", username: "beta.admin", password: "demo1234", label: "Beta 관리자", accent: "beta" },
  { tenantId: "beta", username: "beta.viewer", password: "demo1234", label: "Beta 조회자", accent: "beta" }
] as const;

const statuses = ["DISCOVERY", "ACTIVE", "ON_HOLD", "COMPLETED"] as const;

type ProjectStatus = (typeof statuses)[number];

type LoginResponse = {
  accessToken: string;
  expiresAt: string;
  user: {
    tenantId: string;
    username: string;
    displayName: string;
    role: string;
  };
};

type DashboardResponse = {
  tenant: {
    tenantId: string;
    displayName: string;
    role: string;
  };
  projects: {
    id: number;
    name: string;
    description: string;
    ownerName: string;
    status: ProjectStatus;
    createdAt: string;
    updatedAt: string;
  }[];
  statusCounts: {
    status: ProjectStatus;
    count: number;
  }[];
};

const storageKey = "smaple-multi-tenancy-session";

function accentClass(accent: string) {
  return accent === "beta" ? "beta" : "alpha";
}

export default function Home() {
  const [loginForm, setLoginForm] = useState({
    tenantId: "alpha",
    username: "alpha.admin",
    password: "demo1234"
  });
  const [createForm, setCreateForm] = useState({
    name: "",
    description: "",
    ownerName: "",
    status: "DISCOVERY" as ProjectStatus
  });
  const [token, setToken] = useState<string | null>(null);
  const [session, setSession] = useState<LoginResponse | null>(null);
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string>("데모 계정으로 로그인해 테넌트 격리를 확인하세요.");
  const [statusDrafts, setStatusDrafts] = useState<Record<number, ProjectStatus>>({});

  useEffect(() => {
    const saved = window.localStorage.getItem(storageKey);
    if (!saved) {
      return;
    }

    const parsed = JSON.parse(saved) as { token: string; session: LoginResponse };
    setToken(parsed.token);
    setSession(parsed.session);
  }, []);

  useEffect(() => {
    if (!token) {
      setDashboard(null);
      return;
    }
    void loadDashboard(token);
  }, [token]);

  async function request<T>(path: string, init?: RequestInit, accessToken?: string): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        ...(init?.headers ?? {})
      }
    });

    if (!response.ok) {
      const errorBody = (await response.json().catch(() => ({ message: "요청 처리에 실패했습니다." }))) as {
        message?: string;
      };
      throw new Error(errorBody.message ?? "요청 처리에 실패했습니다.");
    }

    return (await response.json()) as T;
  }

  async function loadDashboard(accessToken: string) {
    try {
      setLoading(true);
      const result = await request<DashboardResponse>("/api/projects/dashboard", { method: "GET" }, accessToken);
      setDashboard(result);
      setStatusDrafts(
        Object.fromEntries(result.projects.map((project) => [project.id, project.status]))
      );
      setMessage(`${result.tenant.tenantId} 테넌트 데이터만 표시 중입니다.`);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "대시보드 조회에 실패했습니다.";
      setMessage(errorMessage);
      logout(false);
    } finally {
      setLoading(false);
    }
  }

  async function submitLogin(event?: FormEvent<HTMLFormElement>, override = loginForm) {
    event?.preventDefault();

    try {
      setLoading(true);
      const result = await request<LoginResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(override)
      });

      setSession(result);
      setToken(result.accessToken);
      window.localStorage.setItem(storageKey, JSON.stringify({ token: result.accessToken, session: result }));
      setLoginForm({
        tenantId: result.user.tenantId,
        username: result.user.username,
        password: override.password
      });
      setMessage(`${result.user.displayName} 계정으로 로그인했습니다.`);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "로그인에 실패했습니다.";
      setMessage(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  async function createProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) {
      return;
    }

    try {
      setLoading(true);
      await request("/api/projects", {
        method: "POST",
        body: JSON.stringify(createForm)
      }, token);
      setCreateForm({
        name: "",
        description: "",
        ownerName: "",
        status: "DISCOVERY"
      });
      setMessage("새 프로젝트를 등록했습니다.");
      await loadDashboard(token);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "프로젝트 등록에 실패했습니다.";
      setMessage(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  async function updateProjectStatus(projectId: number) {
    if (!token) {
      return;
    }

    try {
      setLoading(true);
      await request(`/api/projects/${projectId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: statusDrafts[projectId] })
      }, token);
      setMessage("프로젝트 상태를 변경했습니다.");
      await loadDashboard(token);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "상태 변경에 실패했습니다.";
      setMessage(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  function logout(clearMessage = true) {
    setToken(null);
    setSession(null);
    setDashboard(null);
    setStatusDrafts({});
    window.localStorage.removeItem(storageKey);
    if (clearMessage) {
      setMessage("로그아웃했습니다.");
    }
  }

  return (
    <main className="page-shell">
      <section className="hero-panel">
        <div className="hero-copy">
          <p className="eyebrow">Spring Boot + JPA + JWT + Next.js</p>
          <h1>테넌트가 달라도 같은 서버를 쓰는 운영 데모</h1>
          <p className="hero-description">
            로그인 토큰에 담긴 <code>tenantId</code>를 기준으로 프로젝트 목록과 변경 요청이 분리됩니다.
            Alpha와 Beta 계정을 번갈아 로그인해 데이터가 섞이지 않는지 바로 확인할 수 있습니다.
          </p>
        </div>
        <div className="hero-state">
          <span className={`tenant-pill ${session ? accentClass(session.user.tenantId) : "neutral"}`}>
            {session ? `${session.user.tenantId.toUpperCase()} 세션` : "로그인 전"}
          </span>
          <p>{message}</p>
        </div>
      </section>

      <section className="grid">
        <article className="card">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Quick Login</p>
              <h2>데모 계정</h2>
            </div>
          </div>
          <div className="account-list">
            {demoAccounts.map((account) => (
              <button
                key={`${account.tenantId}-${account.username}`}
                type="button"
                className={`account-button ${accentClass(account.accent)}`}
                onClick={() => {
                  const next = {
                    tenantId: account.tenantId,
                    username: account.username,
                    password: account.password
                  };
                  setLoginForm(next);
                  void submitLogin(undefined, next);
                }}
                disabled={loading}
              >
                <strong>{account.label}</strong>
                <span>{account.username}</span>
              </button>
            ))}
          </div>

          <form className="stack-form" onSubmit={submitLogin}>
            <label>
              <span>Tenant ID</span>
              <input
                value={loginForm.tenantId}
                onChange={(event) => setLoginForm((prev) => ({ ...prev, tenantId: event.target.value }))}
              />
            </label>
            <label>
              <span>Username</span>
              <input
                value={loginForm.username}
                onChange={(event) => setLoginForm((prev) => ({ ...prev, username: event.target.value }))}
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm((prev) => ({ ...prev, password: event.target.value }))}
              />
            </label>
            <div className="button-row">
              <button type="submit" className="primary-button" disabled={loading}>
                로그인
              </button>
              <button type="button" className="secondary-button" onClick={() => logout()} disabled={!session || loading}>
                로그아웃
              </button>
            </div>
          </form>
        </article>

        <article className="card wide-card">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Dashboard</p>
              <h2>테넌트 프로젝트 현황</h2>
            </div>
            <button type="button" className="secondary-button" onClick={() => token && void loadDashboard(token)} disabled={!token || loading}>
              새로고침
            </button>
          </div>

          {dashboard ? (
            <>
              <div className="metric-grid">
                {dashboard.statusCounts.map((item) => (
                  <div key={item.status} className="metric-card">
                    <span>{item.status}</span>
                    <strong>{item.count}</strong>
                  </div>
                ))}
              </div>

              <div className="table-shell">
                <table>
                  <thead>
                    <tr>
                      <th>프로젝트</th>
                      <th>오너</th>
                      <th>상태</th>
                      <th>액션</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboard.projects.map((project) => (
                      <tr key={project.id}>
                        <td>
                          <strong>{project.name}</strong>
                          <p>{project.description}</p>
                        </td>
                        <td>{project.ownerName}</td>
                        <td>
                          <select
                            value={statusDrafts[project.id] ?? project.status}
                            onChange={(event) =>
                              setStatusDrafts((prev) => ({
                                ...prev,
                                [project.id]: event.target.value as ProjectStatus
                              }))
                            }
                          >
                            {statuses.map((status) => (
                              <option key={status} value={status}>
                                {status}
                              </option>
                            ))}
                          </select>
                        </td>
                        <td>
                          <button
                            type="button"
                            className="ghost-button"
                            onClick={() => void updateProjectStatus(project.id)}
                            disabled={loading}
                          >
                            상태 반영
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          ) : (
            <div className="empty-state">로그인 후 테넌트별 프로젝트 목록이 표시됩니다.</div>
          )}
        </article>
      </section>

      <section className="grid bottom-grid">
        <article className="card">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Create</p>
              <h2>프로젝트 등록</h2>
            </div>
          </div>
          <form className="stack-form" onSubmit={createProject}>
            <label>
              <span>프로젝트명</span>
              <input
                value={createForm.name}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, name: event.target.value }))}
                disabled={!token || loading}
              />
            </label>
            <label>
              <span>설명</span>
              <textarea
                rows={4}
                value={createForm.description}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, description: event.target.value }))}
                disabled={!token || loading}
              />
            </label>
            <label>
              <span>오너</span>
              <input
                value={createForm.ownerName}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, ownerName: event.target.value }))}
                disabled={!token || loading}
              />
            </label>
            <label>
              <span>초기 상태</span>
              <select
                value={createForm.status}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, status: event.target.value as ProjectStatus }))}
                disabled={!token || loading}
              >
                {statuses.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" className="primary-button" disabled={!token || loading}>
              프로젝트 추가
            </button>
          </form>
        </article>

        <article className="card">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Isolation Check</p>
              <h2>검증 포인트</h2>
            </div>
          </div>
          <ul className="check-list">
            <li>Alpha 계정으로 로그인하면 Alpha 프로젝트만 보입니다.</li>
            <li>Beta 계정으로 전환하면 같은 URL에서도 다른 데이터 셋이 표시됩니다.</li>
            <li>다른 테넌트 프로젝트 ID를 직접 호출해도 상태 변경이 거부됩니다.</li>
            <li>JWT에는 사용자명, 권한, tenantId가 함께 들어가며 서버는 이를 기준으로 요청을 제한합니다.</li>
          </ul>
        </article>
      </section>
    </main>
  );
}
