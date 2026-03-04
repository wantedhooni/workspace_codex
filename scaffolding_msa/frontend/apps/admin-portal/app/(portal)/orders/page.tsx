"use client";

import { useList } from "@refinedev/core";
import { OrderSummary } from "@/lib/types";

export default function OrdersPage() {
    const { result, query } = useList<OrderSummary>({ resource: "orders" });
    const orders = result.data ?? [];
    const isLoading = query.isLoading;
    const isError = query.isError;

    return (
        <section className="page-grid">
            <div className="hero-panel">
                <p className="eyebrow">Order Resource</p>
                <h3>주문 서비스 조회</h3>
                <p className="panel-subtitle">기본 운영 포털은 읽기 전용이며 주문 현황을 빠르게 보는 데 집중한다.</p>
            </div>

            <div className="data-panel">
                {isLoading && <p>주문 목록을 불러오는 중...</p>}
                {isError && <p>주문 목록을 불러오지 못했습니다.</p>}
                {!isLoading && !isError && (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Order No</th>
                                <th>User ID</th>
                                <th>Amount</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.id}>
                                    <td>{order.id}</td>
                                    <td>{order.orderNo}</td>
                                    <td>{order.userId}</td>
                                    <td>₩{Number(order.amount).toLocaleString("ko-KR")}</td>
                                    <td>
                                        <span className="status-pill">{order.status}</span>
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
