import { Link, NavLink, Outlet } from "react-router-dom";
import type { Announcement } from "../../domains/announcements/types";
import type { Profile } from "../../domains/auth/types";

type UserAppLayoutProps = {
  profile?: Profile;
  loading: boolean;
  error: string | null;
  unreadNotificationCount: number;
  announcements: Announcement[];
  onLogout: () => void;
};

const NAV_ITEMS = [
  { to: "/", label: "Dashboard" },
  { to: "/announcements", label: "Announcements" },
  { to: "/accounts", label: "Accounts" },
  { to: "/linked-bank-accounts", label: "Linked Banks" },
  { to: "/funding-requests", label: "Funding" },
  { to: "/transactions", label: "Transactions" },
  { to: "/fx-rates", label: "FX Rates" },
  { to: "/exchange-requests", label: "Exchange" },
  { to: "/stock-orders", label: "Stock Orders" },
  { to: "/stock-positions", label: "Positions" },
  { to: "/notifications", label: "Notifications" },
];

export function UserAppLayout({ profile, loading, error, unreadNotificationCount, announcements, onLogout }: UserAppLayoutProps) {
  const featuredAnnouncement = announcements.find((item) => item.severity === "CRITICAL")
    ?? announcements.find((item) => item.pinned)
    ?? announcements[0];

  return (
    <main className="page-shell app-shell">
      <section className="app-header">
        <div className="hero-copy">
          <p className="eyebrow">User Web Application</p>
          <h1>{profile ? `${profile.displayName}님의 사용자 채널` : "사용자 채널"}</h1>
          <p className="description">
            사용자 전용 `user API` 기반 화면입니다. 계좌, 입출금, 거래, 환율, 환전, 주식 주문, 포지션, 알림 센터를 도메인별 페이지로 나눠 확인할 수 있습니다.
          </p>
        </div>
        <div className="hero-card session-card">
          <p className="eyebrow">Session</p>
          <strong>{profile?.email ?? "Loading..."}</strong>
          <ul>
            <li>권한: {profile?.roles.join(", ") ?? "USER"}</li>
            <li>상태: {loading ? "동기화 중" : "활성"}</li>
            <li>미확인 알림: {unreadNotificationCount}건</li>
          </ul>
          <button type="button" className="secondary-button" onClick={onLogout}>
            로그아웃
          </button>
        </div>
      </section>

      <nav className="app-nav">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) => `app-nav-link${isActive ? " active" : ""}`}
          >
            <span>{item.label}</span>
            {item.to === "/notifications" && unreadNotificationCount > 0 ? (
              <span className="app-nav-badge">{unreadNotificationCount}</span>
            ) : null}
          </NavLink>
        ))}
      </nav>

      {featuredAnnouncement ? (
        <section className={`service-banner severity-${featuredAnnouncement.severity.toLowerCase()}`}>
          <div>
            <p className="eyebrow">Service Banner</p>
            <strong>{featuredAnnouncement.title}</strong>
            <p>{featuredAnnouncement.summary}</p>
          </div>
          <div className="service-banner-actions">
            <span>{featuredAnnouncement.endsAt ? `종료 ${new Date(featuredAnnouncement.endsAt).toLocaleString()}` : "별도 공지 시까지"}</span>
            <Link to="/announcements">전체 공지 보기</Link>
          </div>
        </section>
      ) : null}

      {error ? (
        <section className="timeline-panel banner-panel">
          <p className="error-text">{error}</p>
        </section>
      ) : null}

      <section className="page-content">
        <Outlet />
      </section>
    </main>
  );
}
