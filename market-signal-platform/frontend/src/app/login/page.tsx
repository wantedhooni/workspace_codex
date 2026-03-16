import Link from "next/link";
import { LoginForm } from "@/features/auth/login-form";

const valueProps = [
  "시장 레짐과 선도 섹터를 아침 브리핑 카드로 즉시 확인",
  "관심 종목별 최신 액션, 점수, 근거를 커버리지 뷰로 확인",
  "뉴스 분석 결과와 최근 이력을 같은 흐름 안에서 검토",
];

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const params = await searchParams;

  return (
    <main className="min-h-screen bg-background bg-[radial-gradient(circle_at_top_left,_rgba(18,53,36,0.14),_transparent_35%),radial-gradient(circle_at_bottom_right,_rgba(200,116,56,0.18),_transparent_32%)] px-4 py-16">
      <div className="mx-auto grid max-w-6xl gap-10 lg:grid-cols-[minmax(0,1.05fr)_minmax(420px,520px)] lg:items-start">
        <div className="space-y-8">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-accent">Market Signal Platform</p>
            <h1 className="mt-4 text-5xl font-semibold leading-tight text-foreground">
              미국 주식 시장의 변화를, 매일 아침 실행 가능한 신호로 변환합니다.
            </h1>
            <p className="mt-4 text-base leading-7 text-foreground/72">
              실제 시장 스냅샷 시드, 리포트 캐시, 뉴스 분석 이력을 한 워크플로로 연결해 운용 화면에서 바로 판단할 수 있게 설계했습니다.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            {valueProps.map((item, index) => (
              <div key={item} className="rounded-[1.75rem] border border-white/65 bg-white/78 p-5 shadow-[0_24px_80px_rgba(28,35,28,0.08)] backdrop-blur">
                <p className="text-xs font-semibold uppercase tracking-[0.22em] text-accent">0{index + 1}</p>
                <p className="mt-3 text-sm leading-6 text-foreground/72">{item}</p>
              </div>
            ))}
          </div>

          <div className="rounded-[2rem] border border-white/65 bg-[linear-gradient(145deg,rgba(21,49,36,0.96),rgba(33,68,52,0.88))] p-6 text-white shadow-[0_32px_100px_rgba(18,33,26,0.18)]">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-white/65">Demo Access</p>
            <div className="mt-4 grid gap-4 md:grid-cols-[1fr_auto] md:items-end">
              <div>
                <p className="text-lg font-semibold">기본 테스트 계정으로 즉시 검증</p>
                <p className="mt-2 text-sm leading-6 text-white/72">
                  `demo@marketsignal.dev / Demo1234!` 계정으로 로그인하면 실제 시드 데이터 기반 대시보드와 관심 종목 화면을 바로 볼 수 있습니다.
                </p>
              </div>
              <Link
                className="inline-flex items-center justify-center rounded-full border border-white/18 bg-white/10 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/16"
                href="/signup"
              >
                새 계정 만들기
              </Link>
            </div>
          </div>
        </div>

        <div className="space-y-5">
          <LoginForm nextPath={params.next} />
          <p className="text-center text-sm text-foreground/70">
            계정이 없다면 <Link className="font-semibold text-primary" href="/signup">회원가입</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
