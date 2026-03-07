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
    <section className="panel" style={{ maxWidth: 420, margin: "0 auto" }}>
      <h2>로그인</h2>
      <div className="row" style={{ marginBottom: 10 }}>
        <button type="button" className="secondary" onClick={fillOperatorDemo}>
          운영자 데모 자동입력
        </button>
        <button type="button" className="secondary" onClick={fillViewerDemo}>
          조회자 데모 자동입력
        </button>
      </div>
      <form className="grid" onSubmit={onSubmit}>
        <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="아이디" />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" />
        <button type="submit">로그인</button>
      </form>
      <p style={{ color: "#5c6b7a", fontSize: 13 }}>운영자: admin / admin1234</p>
      <p style={{ color: "#5c6b7a", fontSize: 13 }}>조회자: auditor / audit1234</p>
      {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
    </section>
  );
}
