import type { Announcement } from "./types";

type AnnouncementsPageProps = {
  loading: boolean;
  announcements: Announcement[];
};

export function AnnouncementsPage({ loading, announcements }: AnnouncementsPageProps) {
  if (loading) {
    return (
      <section className="timeline-panel">
        <p>서비스 공지를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Service Announcements</p>
            <h2>서비스 공지</h2>
            <p className="section-copy">
              거래 점검, 정산 컷오프, 정책 변경 같은 서비스 공지를 운영 배너 형식으로 확인할 수 있습니다.
            </p>
          </div>
        </div>
        <div className="announcement-list">
          {announcements.length ? (
            announcements.map((announcement) => (
              <article key={announcement.id} className={`announcement-card severity-${announcement.severity.toLowerCase()}`}>
                <div className="announcement-card-head">
                  <div>
                    <span className="announcement-kicker">{announcement.severity}</span>
                    <h3>{announcement.title}</h3>
                  </div>
                  {announcement.pinned ? <span className="announcement-pin">PINNED</span> : null}
                </div>
                <p className="announcement-summary">{announcement.summary}</p>
                <p className="announcement-body">{announcement.body}</p>
                <div className="announcement-meta">
                  <span>{announcement.startsAt ? new Date(announcement.startsAt).toLocaleString() : "즉시 적용"}</span>
                  <strong>{announcement.endsAt ? `~ ${new Date(announcement.endsAt).toLocaleString()}` : "별도 공지 시까지"}</strong>
                </div>
              </article>
            ))
          ) : (
            <article className="empty-state-card">
              <strong>현재 게시 중인 공지가 없습니다.</strong>
              <p>중요 공지나 점검 안내가 올라오면 이 화면에 표시됩니다.</p>
            </article>
          )}
        </div>
      </section>
    </>
  );
}
