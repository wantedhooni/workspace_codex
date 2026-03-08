const flow = [
  "Idempotency-Key로 중복 주문 방지",
  "주문 생성 후 Outbox 이벤트 적재",
  "결제 실패와 UNKNOWN 상태 분리 처리"
];

export default function WebPage() {
  return (
    <main className="page">
      <section className="panel">
        <p className="label">WEB APP</p>
        <h1>안전한 주문 흐름을 전제로 시작하는 사용자 앱</h1>
        <p className="desc">
          최종적 일관성과 취소 가능성을 고려한 백엔드 구조를 전제로 프론트엔드 화면을 확장할 수 있게 만든 초기 페이지입니다.
        </p>
        <ul className="flow">
          {flow.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </section>
    </main>
  );
}
