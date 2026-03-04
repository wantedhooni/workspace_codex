"use client";

import { useEffect, useMemo, useState } from "react";
import { useList } from "@refinedev/core";
import { ConfigEnvironment, OrderSummary, UserSummary } from "@/lib/types";

export default function DashboardPage() {
    const { result: userList, query: userQuery } = useList<UserSummary>({ resource: "users" });
    const { result: orderList, query: orderQuery } = useList<OrderSummary>({ resource: "orders" });
    const [config, setConfig] = useState<ConfigEnvironment | null>(null);

    useEffect(() => {
        fetch("/api/config?application=application&profile=default", { cache: "no-store" })
            .then(async (response) => {
                if (!response.ok) {
                    throw new Error("Failed to load config");
                }
                return response.json() as Promise<ConfigEnvironment>;
            })
            .then(setConfig)
            .catch(() => setConfig(null));
    }, []);

    const users = userList.data ?? [];
    const orders = orderList.data ?? [];
    const usersLoading = userQuery.isLoading;
    const ordersLoading = orderQuery.isLoading;

    const totalAmount = useMemo(
        () => orders.reduce((sum, item) => sum + Number(item.amount ?? 0), 0),
        [orders]
    );

    return (
        <section className="page-grid">
            <div className="hero-panel">
                <p className="eyebrow">Scaffolding Operations</p>
                <h3>분산 서비스 운영 신호를 한 화면에서 읽는 기본 포털</h3>
                <p className="panel-subtitle">
                    refine.dev를 데이터 계층으로 두고 `users`, `orders`, `config-server`를 운영 관점으로 묶었다.
                </p>
            </div>

            <div className="metric-grid">
                <article>
                    <p>Users</p>
                    <strong>{usersLoading ? "..." : users.length}</strong>
                </article>
                <article>
                    <p>Orders</p>
                    <strong>{ordersLoading ? "..." : orders.length}</strong>
                </article>
                <article>
                    <p>Order Amount</p>
                    <strong>{ordersLoading ? "..." : `₩${totalAmount.toLocaleString("ko-KR")}`}</strong>
                </article>
                <article>
                    <p>Config Sources</p>
                    <strong>{config ? config.propertySources.length : "0"}</strong>
                </article>
            </div>

            <div className="data-panel">
                <h3>최근 사용자</h3>
                <p className="panel-subtitle">기본 스캐폴딩 데이터와 실제 Gateway 응답을 그대로 보여준다.</p>
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td>{user.name}</td>
                                <td>{user.email}</td>
                                <td>
                                    <span className="status-pill">{user.status}</span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
