"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { API_BASE } from "../../components/api";
import { getToken, setRoles, setToken } from "../../components/auth";

type LoginResponse = {
  token: string;
  username: string;
  roles: string[];
  expiresInSeconds: number;
};

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin1234");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (getToken()) {
      router.replace("/");
    }
  }, [router]);

  const fillOperatorDemo = () => {
    setUsername("admin");
    setPassword("admin1234");
  };

  const fillViewerDemo = () => {
    setUsername("auditor");
    setPassword("audit1234");
  };

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data = (await response.json()) as LoginResponse;
      setToken(data.token);
      setRoles(data.roles ?? []);
      router.replace("/");
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <div className="page" style={{ maxWidth: 520, margin: "0 auto" }}>
      <div>
        <h2 className="page-title">관리자 로그인</h2>
        <p className="page-desc">운영 권한에 따라 조회 전용 모드와 처리 모드가 분리됩니다.</p>
      </div>

      <section className="panel">
        <div className="panel-head">
          <h2>접속 안내</h2>
          <span className="badge badge-warn">관리자 전용</span>
        </div>
        <div className="meta-list">
          <div className="meta-row">
            <span className="meta-label">운영자(OPERATOR)</span>
            <strong>계좌/전문/EOD 처리 가능</strong>
          </div>
          <div className="meta-row">
            <span className="meta-label">조회자(VIEWER)</span>
            <strong>조회 전용, 처리 액션 차단</strong>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>계정 자동입력</h2>
          <span className="badge badge-warn">Demo</span>
        </div>
        <div className="row">
          <button type="button" className="secondary" onClick={fillOperatorDemo}>
            운영자 계정 입력
          </button>
          <button type="button" className="secondary" onClick={fillViewerDemo}>
            조회자 계정 입력
          </button>
        </div>
      </section>

      <section className="panel">
        <h2 style={{ marginBottom: 10 }}>로그인 정보</h2>
        <form className="form-grid" onSubmit={onSubmit}>
          <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디" />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" />
          <button type="submit" style={{ gridColumn: "1 / -1" }}>
            로그인
          </button>
        </form>
        <p className="panel-sub" style={{ marginTop: 10 }}>
          운영자: admin / admin1234 | 조회자: auditor / audit1234
        </p>
      </section>

      {error && <div className="alert">{error}</div>}
    </div>
  );
}
