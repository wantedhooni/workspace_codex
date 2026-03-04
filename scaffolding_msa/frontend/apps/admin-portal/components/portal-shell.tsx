"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { PropsWithChildren } from "react";

const navigation = [
    { href: "/dashboard", label: "Dashboard", kicker: "운영 지표" },
    { href: "/users", label: "Users", kicker: "사용자 자원" },
    { href: "/orders", label: "Orders", kicker: "주문 추적" },
    { href: "/config", label: "Config", kicker: "원격 설정" },
];

export function PortalShell({ children }: PropsWithChildren) {
    const pathname = usePathname();

    return (
        <div className="portal-shell">
            <aside className="portal-nav">
                <div className="portal-brand">
                    <span className="portal-badge">refine.dev</span>
                    <h1>Admin Portal</h1>
                    <p>Spring Cloud MSA의 운영 흐름을 읽는 관리 콘솔</p>
                </div>

                <nav className="portal-links">
                    {navigation.map((item) => {
                        const active = pathname.startsWith(item.href);

                        return (
                            <Link key={item.href} href={item.href} className={active ? "portal-link active" : "portal-link"}>
                                <span>{item.kicker}</span>
                                <strong>{item.label}</strong>
                            </Link>
                        );
                    })}
                </nav>

                <div className="portal-footer">
                    <p>Backend entrypoint</p>
                    <code>{process.env.NEXT_PUBLIC_GATEWAY_URL ?? "http://127.0.0.1:8000"}</code>
                </div>
            </aside>

            <main className="portal-main">
                <div className="portal-topbar">
                    <div>
                        <p className="eyebrow">Operations Console</p>
                        <h2>Scaffolding MSA</h2>
                    </div>
                    <div className="portal-pulse">
                        <span />
                        route health by gateway
                    </div>
                </div>
                {children}
            </main>
        </div>
    );
}

