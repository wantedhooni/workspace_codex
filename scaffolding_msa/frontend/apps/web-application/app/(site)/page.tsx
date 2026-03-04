"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

type Overview = {
    users: Array<{
        id: number;
        name: string;
        email: string;
        status: string;
    }>;
    orders: Array<{
        id: number;
        orderNo: string;
        userId: number;
        amount: number;
        status: string;
    }>;
    config: {
        name: string;
        propertySources: Array<{ name: string }>;
    } | null;
    authIssuer: string | null;
};

export default function WebApplicationPage() {
    const [overview, setOverview] = useState<Overview | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetch("/api/overview", { cache: "no-store" })
            .then(async (response) => {
                if (!response.ok) {
                    throw new Error("overview fetch failed");
                }
                return response.json() as Promise<Overview>;
            })
            .then((payload) => {
                setOverview(payload);
                setError(null);
            })
            .catch(() => {
                setError("백엔드 연결 전이라 샘플 웹 레이어만 렌더링 중입니다.");
            });
    }, []);

    const users = overview?.users ?? [];
    const orders = overview?.orders ?? [];

    return (
        <main className="site-shell">
            <header className="site-topbar">
                <div className="site-mark">
                    <strong>Web Application</strong>
                    <span className="muted">Next.js / React</span>
                </div>
                <div className="site-actions">
                    <Link href="http://127.0.0.1:3001" className="primary">
                        Admin Portal
                    </Link>
                    <Link href="http://127.0.0.1:8000">API Gateway</Link>
                </div>
            </header>

            <section className="hero">
                <div className="hero-copy">
                    <span className="eyebrow">Scaffolding Frontend</span>
                    <h1>고객용 웹 앱의 기본 표정까지 같이 세팅한다.</h1>
                    <p>
                        이 앱은 Spring Cloud MSA 백엔드 위에 얹는 기본 대외 채널이다. Config, Auth, Gateway, Domain API를
                        모두 느슨하게 연결하고, 실제 서비스로 확장할 수 있는 첫 화면을 만든다.
                    </p>
                </div>

                <div className="hero-side">
                    <div className="stack">
                        <article>
                            <p>Users Detected</p>
                            <strong>{users.length}</strong>
                        </article>
                        <article>
                            <p>Orders Detected</p>
                            <strong>{orders.length}</strong>
                        </article>
                        <article>
                            <p>OIDC Issuer</p>
                            <strong>{overview?.authIssuer ?? "pending"}</strong>
                        </article>
                    </div>
                </div>
            </section>

            {error && <div className="offline">{error}</div>}

            <section className="signal-grid">
                <article className="signal-card">
                    <h3>Runtime Signal</h3>
                    <p>
                        Gateway, Auth, Config Server의 연결 상태를 웹 레이어에서 읽을 수 있도록 설계했다. 운영 화면과 대외
                        화면이 같은 백엔드 계약을 공유한다.
                    </p>
                </article>
                <article className="signal-card">
                    <h3>Customer Layer</h3>
                    <p>
                        기본 색감은 과도하게 기술적이지 않게 잡고, 마케팅 페이지와 운영 지표가 자연스럽게 공존하도록 구성했다.
                    </p>
                </article>
                <article className="signal-card">
                    <h3>Expansion Ready</h3>
                    <p>
                        BFF 라우트, 인증 연결, 주문 흐름, 콘텐츠 페이지를 같은 Next.js 앱에서 확장할 수 있다.
                    </p>
                </article>
            </section>

            <section className="strip-grid">
                <article className="strip-card">
                    <h3>Recent Users</h3>
                    <ul className="data-list">
                        {users.map((user) => (
                            <li key={user.id}>
                                <span>{user.name}</span>
                                <span className="muted">{user.email}</span>
                            </li>
                        ))}
                    </ul>
                </article>
                <article className="strip-card">
                    <h3>Recent Orders</h3>
                    <ul className="data-list">
                        {orders.map((order) => (
                            <li key={order.id}>
                                <span>{order.orderNo}</span>
                                <span className="muted">₩{Number(order.amount).toLocaleString("ko-KR")}</span>
                            </li>
                        ))}
                    </ul>
                </article>
                <article className="strip-card">
                    <h3>Config Sources</h3>
                    <ul className="data-list">
                        {(overview?.config?.propertySources ?? []).map((source) => (
                            <li key={source.name}>
                                <span>{source.name}</span>
                            </li>
                        ))}
                    </ul>
                </article>
            </section>
        </main>
    );
}

