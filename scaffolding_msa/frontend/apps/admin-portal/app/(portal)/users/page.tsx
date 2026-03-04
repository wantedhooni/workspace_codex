"use client";

import { useList } from "@refinedev/core";
import { UserSummary } from "@/lib/types";

export default function UsersPage() {
    const { result, query } = useList<UserSummary>({ resource: "users" });
    const users = result.data ?? [];
    const isLoading = query.isLoading;
    const isError = query.isError;

    return (
        <section className="page-grid">
            <div className="hero-panel">
                <p className="eyebrow">User Resource</p>
                <h3>사용자 서비스 조회</h3>
                <p className="panel-subtitle">Gateway를 통해 `user-service` 목록 API를 읽는다.</p>
            </div>

            <div className="data-panel">
                {isLoading && <p>사용자 목록을 불러오는 중...</p>}
                {isError && <p>사용자 목록을 불러오지 못했습니다.</p>}
                {!isLoading && !isError && (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((user) => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td>{user.name}</td>
                                    <td>{user.email}</td>
                                    <td>
                                        <span className="status-pill">{user.status}</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </section>
    );
}
