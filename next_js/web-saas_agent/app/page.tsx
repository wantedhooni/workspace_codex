import Link from "next/link";
import { LogoutButton } from "@/components/auth/logout-button";
import { AuthCookieStore } from "@/lib/auth-service";

const features = [
  ["인증 프록시", "브라우저는 내부 API만 호출하고, Next.js 서버가 백엔드 인증 API와 통신합니다."],
  ["토큰 보호", "액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 저장해 노출 면적을 줄입니다."],
  ["운영 확장성", "로그인 이후 워크스페이스, 구독, 사용량 화면으로 자연스럽게 확장할 수 있습니다."],
];

export default async function Home() {
  const { accessToken } = await AuthCookieStore.getTokens();
  const isAuthenticated = Boolean(accessToken);

  return (
    <main className="min-h-screen overflow-hidden bg-[#f7f9fc] text-slate-950">
      <header className="mx-auto flex h-20 max-w-6xl items-center justify-between px-6">
        <Link href="/" className="flex items-center gap-3 text-lg font-bold">
          <span className="grid size-9 place-items-center rounded-xl bg-slate-950 text-sm text-white">S</span>
          SaaS Agent
        </Link>
        <nav className="hidden items-center gap-8 text-sm font-medium text-slate-600 md:flex">
          <a href="#features">기능</a>
          <a href="#security">보안</a>
          <a href="#demo">데모</a>
        </nav>
        {isAuthenticated ? (
          <div className="flex items-center gap-3">
            <Link className="text-sm font-semibold text-slate-700" href="/dashboard">
              대시보드
            </Link>
            <LogoutButton />
          </div>
        ) : (
          <Link
            href="/login"
            className="rounded-full bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            로그인
          </Link>
        )}
      </header>

      <section className="mx-auto grid max-w-6xl gap-12 px-6 pb-20 pt-12 lg:grid-cols-[1fr_460px] lg:items-center">
        <div>
          <h1 className="max-w-3xl text-5xl font-semibold leading-[1.05] tracking-normal sm:text-6xl">
            SaaS 운영의 인증 시작점을 단단하게 만듭니다.
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-600">
            로그인, 로그아웃, 토큰 갱신, 내 정보 조회까지 실제 API 명세에 맞춰 연결한 SaaS 랜딩 기반입니다.
            팀은 이 위에 구독, 결제, 워크스페이스 기능을 바로 확장할 수 있습니다.
          </p>
          <div className="mt-9 flex flex-col gap-3 sm:flex-row">
            <Link
              href={isAuthenticated ? "/dashboard" : "/login"}
              className="inline-flex h-12 items-center justify-center rounded-full bg-slate-950 px-6 text-base font-semibold text-white transition hover:bg-slate-800"
            >
              {isAuthenticated ? "대시보드 보기" : "데모 로그인"}
            </Link>
            <a
              href="#security"
              className="inline-flex h-12 items-center justify-center rounded-full border border-slate-300 bg-white px-6 text-base font-semibold text-slate-800 transition hover:border-slate-400"
            >
              인증 구조 보기
            </a>
          </div>
        </div>
        <div className="relative">
          <div className="absolute -inset-6 rounded-[32px] bg-cyan-200/30 blur-3xl" />
          <div className="relative rounded-[28px] border border-slate-200 bg-white p-5 shadow-[0_30px_90px_rgba(15,23,42,0.13)]">
            <div className="rounded-2xl bg-slate-950 p-5 text-white">
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <span className="text-sm font-semibold">Auth Console</span>
                <span className="rounded-full bg-emerald-400/15 px-3 py-1 text-xs font-medium text-emerald-200">Ready</span>
              </div>
              <div className="mt-6 grid gap-3">
                {["POST /api/v1/auth/login", "POST /api/v1/auth/refresh", "GET /api/v1/auth/me", "POST /api/v1/auth/logout"].map(
                  (item) => (
                    <div key={item} className="flex items-center justify-between rounded-xl bg-white/7 px-4 py-3">
                      <span className="text-sm text-slate-200">{item}</span>
                      <span className="size-2 rounded-full bg-cyan-300" />
                    </div>
                  ),
                )}
              </div>
            </div>
            <div id="demo" className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p className="text-sm font-semibold text-slate-950">데모 계정</p>
              <p className="mt-2 text-sm text-slate-600">id: demo@example.com</p>
              <p className="text-sm text-slate-600">pw: Qwer1234!</p>
            </div>
          </div>
        </div>
      </section>

      <section id="features" className="border-y border-slate-200 bg-white">
        <div className="mx-auto grid max-w-6xl gap-5 px-6 py-16 md:grid-cols-3">
          {features.map(([title, description]) => (
            <article key={title} className="rounded-2xl border border-slate-200 p-6">
              <h2 className="text-xl font-semibold tracking-normal">{title}</h2>
              <p className="mt-4 text-sm leading-6 text-slate-600">{description}</p>
            </article>
          ))}
        </div>
      </section>

      <section id="security" className="mx-auto grid max-w-6xl gap-10 px-6 py-20 lg:grid-cols-[380px_1fr]">
        <div>
          <h2 className="text-3xl font-semibold tracking-normal">인증 구현 방식</h2>
          <p className="mt-4 text-base leading-7 text-slate-600">
            외부 API 서버는 `SAAS_API_BASE_URL` 환경 변수로 교체할 수 있으며, 기본값은 명세의 `http://localhost:8091`입니다.
          </p>
        </div>
        <div className="grid gap-3">
          {[
            ["1", "사용자가 `/login`에서 이메일과 비밀번호를 제출합니다."],
            ["2", "Next.js `/api/auth/login` 라우트가 백엔드 로그인 API를 호출합니다."],
            ["3", "발급된 토큰은 HttpOnly 쿠키에 저장되어 클라이언트 스크립트에 노출되지 않습니다."],
            ["4", "로그아웃 시 백엔드 API 호출 후 쿠키를 정리합니다."],
          ].map(([step, text]) => (
            <div key={step} className="flex gap-4 rounded-2xl border border-slate-200 bg-white p-5">
              <span className="grid size-8 shrink-0 place-items-center rounded-full bg-cyan-100 text-sm font-bold text-cyan-900">
                {step}
              </span>
              <p className="text-sm leading-6 text-slate-600">{text}</p>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
