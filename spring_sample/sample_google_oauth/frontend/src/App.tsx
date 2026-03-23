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

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8087";

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
    const loginStatus = params.get("login");

    if (loginStatus === "error") {
      setErrorMessage("Google 로그인에 실패했습니다. 콘솔 설정과 리다이렉트 URI를 다시 확인하세요.");
    }

    void fetchCurrentUser();
  }, []);

  async function fetchCurrentUser() {
    try {
      setLoading(true);
      const response = await fetch(`${apiBaseUrl}/api/auth/me`, {
        credentials: "include"
      });

      if (response.status === 401 || response.status === 302) {
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
      setErrorMessage(error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  }

  async function logout() {
    try {
      const response = await fetch(`${apiBaseUrl}/api/auth/logout`, {
        method: "POST",
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error("로그아웃에 실패했습니다.");
      }

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

      const response = await fetch(`${apiBaseUrl}/api/auth/signup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "include",
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
          <p className="eyebrow">Spring Boot + React + Google OAuth</p>
          <h1>실무형 소셜 로그인 흐름을 바로 검증할 수 있는 데모</h1>
          <p className="description">
            백엔드는 Spring Security OAuth2 Client로 Google 인증을 처리하고,
            프론트엔드는 세션 기반 사용자 상태를 조회해 로그인 결과를 즉시 보여준다.
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
              <dd>Spring Boot 3.4 / Spring Security</dd>
            </div>
            <div>
              <dt>세션</dt>
              <dd>JSESSIONID 기반 유지</dd>
            </div>
            <div>
              <dt>API</dt>
              <dd>{apiBaseUrl}</dd>
            </div>
          </dl>
        </div>

        <div className="status-card">
          <span className="card-label">로그인 상태</span>
          {loading ? <p className="muted">사용자 상태를 확인하는 중입니다.</p> : null}
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
              <p className="signup-copy">Google 인증은 완료되었습니다. 서비스 가입을 마치려면 추가 정보를 입력해 주세요.</p>
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
              <p className="muted">Google Cloud Console에 등록한 테스트 계정으로 로그인해 주세요.</p>
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
            <li><code>.env</code> 와 <code>frontend/.env.local</code> 에 설정을 채운다.</li>
            <li>최초 로그인 후 표시 이름, 조직, 직무를 입력해 가입을 완료한다.</li>
          </ol>
        </article>
        <article className="guide-card">
          <h2>확인 포인트</h2>
          <ol>
            <li>로그인 성공 후 프론트엔드로 복귀하는지 확인한다.</li>
            <li>최초 로그인 계정은 회원가입 폼이 노출되는지 확인한다.</li>
            <li>가입 완료 후 새로고침해도 가입 상태가 유지되는지 확인한다.</li>
            <li>로그아웃 후 <code>/api/auth/me</code> 가 다시 인증을 요구하는지 확인한다.</li>
          </ol>
        </article>
      </section>
    </main>
  );
}

export default App;
