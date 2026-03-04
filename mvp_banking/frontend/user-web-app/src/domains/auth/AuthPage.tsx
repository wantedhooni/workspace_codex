import { useState, type FormEvent } from "react";
import { USER_DEMO_ACCOUNT } from "../../shared/session";

type AuthPageProps = {
  loading: boolean;
  error: string | null;
  onLogin: (email: string, password: string) => Promise<void>;
  onSignup: (email: string, password: string, fullName: string) => Promise<void>;
};

export function AuthPage({ loading, error, onLogin, onSignup }: AuthPageProps) {
  const [mode, setMode] = useState<"login" | "signup">("login");
  const [form, setForm] = useState(USER_DEMO_ACCOUNT);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (mode === "login") {
      await onLogin(form.email, form.password);
      return;
    }
    await onSignup(form.email, form.password, form.fullName);
  }

  function fillDemoLogin() {
    setMode("login");
    setForm(USER_DEMO_ACCOUNT);
  }

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">User Web Application</p>
          <h1>내 계좌와 거래를 확인하는 사용자 채널</h1>
          <p className="description">
            실제 백엔드에 연결된 로그인/회원가입 흐름입니다. 도메인별 페이지 구조로 분리된 사용자 전용 웹 애플리케이션입니다.
          </p>
          <div className="demo-credential-card">
            <div>
              <p className="eyebrow">Demo Account</p>
              <strong>{USER_DEMO_ACCOUNT.email}</strong>
              <p className="description demo-password-text">{USER_DEMO_ACCOUNT.password}</p>
            </div>
            <button type="button" className="secondary-button demo-fill-button" onClick={fillDemoLogin}>
              데모 계정 채우기
            </button>
          </div>
        </div>

        <form className="auth-form" onSubmit={onSubmit}>
          <div className="mode-switch">
            <button type="button" className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>
              로그인
            </button>
            <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>
              회원가입
            </button>
          </div>

          {mode === "signup" ? (
            <input
              name="fullName"
              placeholder="이름"
              value={form.fullName}
              onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))}
              required
            />
          ) : null}
          <input
            name="email"
            type="email"
            placeholder="이메일"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            required
          />
          <input
            name="password"
            type="password"
            placeholder="비밀번호"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            required
          />
          {error ? <p className="error-text">{error}</p> : null}
          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? "처리 중..." : mode === "login" ? "로그인" : "회원가입"}
          </button>
        </form>
      </section>
    </main>
  );
}
