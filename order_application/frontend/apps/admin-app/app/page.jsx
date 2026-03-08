import { DashboardCard } from "../components/dashboard-card";

const cards = [
  { title: "주문 이상 건", value: "12", description: "UNKNOWN 상태 주문 추적" },
  { title: "Outbox 적체", value: "3", description: "재시도 예정 이벤트" },
  { title: "취소 실패", value: "1", description: "운영 확인 필요" }
];

export default function AdminPage() {
  return (
    <main className="shell">
      <section className="hero">
        <p className="eyebrow">ADMIN APP</p>
        <h1>운영 상태를 추적하는 관리자 콘솔</h1>
        <p className="body">
          주문, 결제, Outbox 처리 상태를 분리해서 확인할 수 있는 초기 대시보드입니다.
        </p>
      </section>
      <section className="grid">
        {cards.map((card) => (
          <DashboardCard key={card.title} {...card} />
        ))}
      </section>
    </main>
  );
}
