import Link from "next/link";
import { SignupForm } from "@/features/auth/signup-form";

const signupBenefits = [
  "리포트, 관심 종목, 뉴스 분석을 보호된 워크스페이스에서 관리",
  "토큰 자동 갱신으로 세션 만료 흐름을 최소화",
  "프로필과 운영 문맥을 함께 저장해 화면 이해도를 높임",
];

export default function SignupPage() {
  return (
    <main className="min-h-screen bg-background bg-[radial-gradient(circle_at_top_left,_rgba(18,53,36,0.11),_transparent_33%),radial-gradient(circle_at_bottom_right,_rgba(200,116,56,0.16),_transparent_30%)] px-4 py-16">
      <div className="mx-auto grid max-w-6xl gap-10 lg:grid-cols-[minmax(0,1fr)_minmax(420px,520px)] lg:items-start">
        <div className="space-y-8">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-accent">Create Workspace</p>
            <h1 className="mt-4 text-5xl font-semibold leading-tight text-foreground">
              개인 분석 워크스페이스를 생성하고 시장 신호를 구조적으로 관리하세요.
            </h1>
            <p className="mt-4 text-base leading-7 text-foreground/72">
              처음 사용하는 사용자도 가입 직후 어떤 화면을 먼저 봐야 하는지 이해할 수 있도록 온보딩 흐름을 단순하게 구성했습니다.
            </p>
          </div>

          <div className="space-y-3">
            {signupBenefits.map((item, index) => (
              <div key={item} className="flex items-start gap-4 rounded-[1.75rem] border border-white/65 bg-white/76 px-5 py-4 shadow-[0_22px_70px_rgba(28,35,28,0.08)]">
                <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-white">
                  {index + 1}
                </div>
                <p className="text-sm leading-6 text-foreground/72">{item}</p>
              </div>
            ))}
          </div>

          <div className="rounded-[2rem] border border-white/65 bg-white/74 p-6 shadow-[0_28px_80px_rgba(28,35,28,0.08)]">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-accent">Already Have Access?</p>
            <p className="mt-3 text-base leading-7 text-foreground/72">
              기존 계정이 있다면 로그인 후 대시보드에서 현재 시장 상태와 종목 시그널을 바로 확인할 수 있습니다.
            </p>
            <Link className="mt-5 inline-flex items-center rounded-full bg-primary px-5 py-3 text-sm font-semibold text-white transition hover:bg-primary/90" href="/login">
              로그인으로 이동
            </Link>
          </div>
        </div>

        <div className="space-y-5">
          <SignupForm />
          <p className="text-center text-sm text-foreground/70">
            이미 계정이 있다면 <Link className="font-semibold text-primary" href="/login">로그인</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
