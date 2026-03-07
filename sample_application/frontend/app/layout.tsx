import type { Metadata } from "next";
import AppNavLink from "../components/AppNavLink";
import TopbarStatus from "../components/TopbarStatus";
import "./globals.css";

export const metadata: Metadata = {
  title: "증권/계좌 Admin 업무 시스템",
  description: "증권/계좌 통합 운영 백오피스"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <div className="app-shell">
          <aside className="app-sidebar">
            <div className="brand">
              <h1>SECURITIES ADMIN</h1>
              <p>업무 효율 중심 백오피스</p>
            </div>
            <div className="sidebar-section">
              <div className="sidebar-label">Core Modules</div>
              <nav className="nav-group">
                <AppNavLink exact href="/" label="운영 대시보드" />
                <AppNavLink href="/account" label="계정계 업무" />
                <AppNavLink href="/channel" label="채널계 접수" />
                <AppNavLink href="/external" label="대외 전문" />
                <AppNavLink href="/information" label="정보계 조회" />
              </nav>
            </div>
            <div className="sidebar-section">
              <div className="sidebar-label">Access</div>
              <nav className="nav-group">
                <AppNavLink href="/login" label="로그인" />
              </nav>
            </div>
            <div className="sidebar-note">
              원장 정합성, 대사 상태, 전문 실패 건을 동일 화면 체계로 처리합니다.
            </div>
          </aside>
          <main className="app-main">
            <div className="topbar">
              <TopbarStatus />
            </div>
            {children}
          </main>
        </div>
      </body>
    </html>
  );
}
