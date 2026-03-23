import { useEffect, useState, type FormEvent } from "react";

type UserProfile = {
  authenticated: boolean;
  registered: boolean;
  name: string;
  email: string;
  picture: string;
  provider: string;
  displayName: string;
  organization: string;
  jobTitle: string;
  marketingConsent: boolean;
};

type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
};

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8087";
const accessTokenKey = "sample_google_oauth_access_token";
const refreshTokenKey = "sample_google_oauth_refresh_token";

function App() {
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [signupLoading, setSignupLoading] = useState(false);
  const [signupForm, setSignupForm] = useState({
    displayName: "",
    organization: "",
    jobTitle: "",
    marketingConsent: false
  });

  const loginUrl = `${apiBaseUrl}/oauth2/authorization/google`;

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get("accessToken");
    const refreshToken = params.get("refreshToken");
    const loginStatus = params.get("login");

    if (accessToken && refreshToken) {
      persistTokens({ accessToken, refreshToken });
      window.history.replaceState({}, "", "/");
    } else if (loginStatus === "error") {
      setErrorMessage("Google 로그인에 실패했습니다. Redis, OAuth 설정, 리디렉트 URI를 다시 확인하세요.");
      window.history.replaceState({}, "", "/");
    }

    void fetchCurrentUser();
  }, []);

  function persistTokens(tokens: { accessToken: string; refreshToken: string }) {
    window.localStorage.setItem(accessTokenKey, tokens.accessToken);
    window.localStorage.setItem(refreshTokenKey, tokens.refreshToken);
  }

  function clearTokens() {
    window.localStorage.removeItem(accessTokenKey);
    window.localStorage.removeItem(refreshTokenKey);
  }

  function getAccessToken() {
    return window.localStorage.getItem(accessTokenKey);
  }

  function getRefreshToken() {
    return window.localStorage.getItem(refreshTokenKey);
  }

  async function refreshAccessToken() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearTokens();
      return null;
    }

    const response = await fetch(`${apiBaseUrl}/api/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ refreshToken })
    });

    if (!response.ok) {
      clearTokens();
      return null;
    }

    const tokens = (await response.json()) as TokenResponse;
    persistTokens(tokens);
    return tokens.accessToken;
  }

  async function authorizedFetch(input: string, init: RequestInit = {}, allowRetry = true) {
    const accessToken = getAccessToken();
    const headers = new Headers(init.headers ?? {});

    if (accessToken) {
      headers.set("Authorization", `Bearer ${accessToken}`);
    }

    const response = await fetch(input, {
      ...init,
      headers
    });

    if (response.status === 401 && allowRetry) {
      const refreshedAccessToken = await refreshAccessToken();
      if (!refreshedAccessToken) {
        return response;
      }

      const retryHeaders = new Headers(init.headers ?? {});
      retryHeaders.set("Authorization", `Bearer ${refreshedAccessToken}`);

      return fetch(input, {
        ...init,
        headers: retryHeaders
      });
    }

    return response;
  }

  async function fetchCurrentUser() {
    const accessToken = getAccessToken();
    if (!accessToken) {
      setLoading(false);
      setUser(null);
      return;
    }

    try {
      setLoading(true);
      const response = await authorizedFetch(`${apiBaseUrl}/api/auth/me`);

      if (response.status === 401) {
        clearTokens();
        setUser(null);
        return;
      }

      if (!response.ok) {
        throw new Error("로그인 사용자 조회에 실패했습니다.");
      }

      const payload = (await response.json()) as UserProfile;
      setUser(payload);
      setSignupForm({
        displayName: payload.displayName ?? payload.name ?? "",
        organization: payload.organization ?? "",
        jobTitle: payload.jobTitle ?? "",
        marketingConsent: payload.marketingConsent ?? false
      });
    } catch (error) {
      setUser(null);
      clearTokens();
      setErrorMessage(error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  }

  async function logout() {
    try {
      const refreshToken = getRefreshToken();
      if (!refreshToken) {
        clearTokens();
        setUser(null);
        return;
      }

      const response = await authorizedFetch(`${apiBaseUrl}/api/auth/logout`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken })
      }, false);

      if (!response.ok && response.status !== 401) {
        throw new Error("로그아웃에 실패했습니다.");
      }

      clearTokens();
      setUser(null);
      setErrorMessage("");
      window.history.replaceState({}, "", "/");
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "로그아웃 처리 중 오류가 발생했습니다.");
    }
  }

  async function signup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    try {
      setSignupLoading(true);
      setErrorMessage("");

      const response = await authorizedFetch(`${apiBaseUrl}/api/auth/signup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(signupForm)
      });

      if (!response.ok) {
        const payload = (await response.json().catch(() => null)) as { message?: string } | null;
        throw new Error(payload?.message ?? "회원가입 처리에 실패했습니다.");
      }

      const payload = (await response.json()) as UserProfile;
      setUser(payload);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "회원가입 처리 중 오류가 발생했습니다.");
    } finally {
      setSignupLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Spring Boot + Redis + JWT + Google OAuth</p>
          <h1>세션 없이 스케일 아웃 가능한 OAuth 로그인 샘플</h1>
          <p className="description">
            OAuth 승인 요청과 refresh token은 Redis로 관리하고,
            로그인 완료 후에는 JWT Bearer 토큰으로 API를 호출한다.
          </p>
          <div className="actions">
            <a className="primary-button" href={loginUrl}>
              Google로 로그인
            </a>
            <button className="secondary-button" type="button" onClick={() => void fetchCurrentUser()}>
              상태 다시 조회
            </button>
          </div>
          <dl className="meta-grid">
            <div>
              <dt>Frontend</dt>
              <dd>React 19 + Vite</dd>
            </div>
            <div>
              <dt>Backend</dt>
              <dd>Spring Boot 3.4 / JWT</dd>
            </div>
            <div>
              <dt>OAuth 상태</dt>
              <dd>Redis Authorization Request</dd>
            </div>
            <div>
              <dt>API 인증</dt>
              <dd>Bearer Access Token</dd>
            </div>
          </dl>
        </div>

        <div className="status-card">
          <span className="card-label">로그인 상태</span>
          {loading ? <p className="muted">JWT 기반 사용자 상태를 확인하는 중입니다.</p> : null}
          {!loading && user?.registered ? (
            <>
              <div className="profile">
                {user.picture ? <img src={user.picture} alt={user.displayName || user.name} /> : <div className="avatar-fallback">{(user.displayName || user.name).slice(0, 1)}</div>}
                <div>
                  <strong>{user.displayName}</strong>
                  <p>{user.email}</p>
                </div>
              </div>
              <p className="provider">소속: {user.organization} / {user.jobTitle}</p>
              <p className="provider">연동 제공자: {user.provider}</p>
              <button className="secondary-button full-width" type="button" onClick={() => void logout()}>
                로그아웃
              </button>
            </>
          ) : null}
          {!loading && user && !user.registered ? (
            <form className="signup-form" onSubmit={(event) => void signup(event)}>
              <p className="signup-copy">OAuth 로그인 후 기본 회원은 이미 저장되었습니다. 서비스 가입 완료를 위해 추가 프로필을 입력해 주세요.</p>
              <label>
                <span>표시 이름</span>
                <input
                  value={signupForm.displayName}
                  onChange={(event) => setSignupForm((current) => ({ ...current, displayName: event.target.value }))}
                  maxLength={50}
                  required
                />
              </label>
              <label>
                <span>소속 조직</span>
                <input
                  value={signupForm.organization}
                  onChange={(event) => setSignupForm((current) => ({ ...current, organization: event.target.value }))}
                  maxLength={80}
                  required
                />
              </label>
              <label>
                <span>직무</span>
                <input
                  value={signupForm.jobTitle}
                  onChange={(event) => setSignupForm((current) => ({ ...current, jobTitle: event.target.value }))}
                  maxLength={60}
                  required
                />
              </label>
              <label className="checkbox-row">
                <input
                  type="checkbox"
                  checked={signupForm.marketingConsent}
                  onChange={(event) => setSignupForm((current) => ({ ...current, marketingConsent: event.target.checked }))}
                />
                <span>신규 기능 소식과 운영 안내 수신에 동의합니다.</span>
              </label>
              <button className="primary-button full-width" type="submit" disabled={signupLoading}>
                {signupLoading ? "가입 처리 중..." : "회원가입 완료"}
              </button>
            </form>
          ) : null}
          {!loading && !user ? (
            <div className="empty-state">
              <p>아직 로그인되지 않았습니다.</p>
              <p className="muted">로그인 후 access/refresh token이 브라우저에 저장되고 이후 API는 Bearer 방식으로 호출됩니다.</p>
            </div>
          ) : null}
          {errorMessage ? <p className="error-message">{errorMessage}</p> : null}
        </div>
      </section>

      <section className="guide-grid">
        <article className="guide-card">
          <h2>사전 준비</h2>
          <ol>
            <li>Google Cloud Console에서 OAuth Client ID를 생성한다.</li>
            <li>승인된 리디렉션 URI에 <code>http://localhost:8087/login/oauth2/code/google</code> 를 등록한다.</li>
            <li>Redis를 실행하고 <code>.env</code> 의 JWT/Redis 설정을 채운다.</li>
            <li>최초 로그인 후 표시 이름, 조직, 직무를 입력해 가입을 완료한다.</li>
          </ol>
        </article>
        <article className="guide-card">
          <h2>확인 포인트</h2>
          <ol>
            <li>OAuth 성공 후 URL 파라미터로 받은 토큰이 저장되는지 확인한다.</li>
            <li>최초 로그인 계정은 회원가입 폼이 노출되는지 확인한다.</li>
            <li>access token 만료 시 refresh token으로 자동 재발급되는지 확인한다.</li>
            <li>로그아웃 후 Redis의 refresh token이 제거되는지 확인한다.</li>
          </ol>
        </article>
      </section>
    </main>
  );
}

export default App;
