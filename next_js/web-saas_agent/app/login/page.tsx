import Link from "next/link";
import { LoginForm } from "./login-form";

export default function LoginPage() {
  return (
    <main className="min-h-screen bg-[#f6f8fb] text-slate-950">
      <div className="mx-auto grid min-h-screen w-full max-w-6xl grid-cols-1 lg:grid-cols-[1fr_440px]">
        <section className="flex flex-col justify-between px-6 py-8 sm:px-10 lg:px-12">
          <Link href="/" className="flex items-center gap-3 text-lg font-bold">
            <span className="grid size-9 place-items-center rounded-xl bg-slate-950 text-sm text-white">S</span>
            SaaS Agent
          </Link>
          <div className="max-w-2xl py-16">
            <h1 className="text-4xl font-semibold leading-tight tracking-normal sm:text-5xl">
              운영 데이터를 한 화면에서 확인하고, 팀의 다음 액션까지 빠르게 연결하세요.
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-8 text-slate-600">
              데모 계정으로 로그인하면 인증 토큰이 HttpOnly 쿠키에 저장되고, 이후 API 요청은 Next.js 서버 라우트를 통해 안전하게 전달됩니다.
            </p>
          </div>
          <div className="grid max-w-2xl gap-3 text-sm text-slate-500 sm:grid-cols-3">
            <div className="border-t border-slate-200 pt-4">
              <strong className="block text-slate-950">API 연동</strong>
              로그인, 로그아웃, me, refresh
            </div>
            <div className="border-t border-slate-200 pt-4">
              <strong className="block text-slate-950">보안 기본값</strong>
              HttpOnly 쿠키 기반 토큰 관리
            </div>
            <div className="border-t border-slate-200 pt-4">
              <strong className="block text-slate-950">데모 계정</strong>
              demo@example.com
            </div>
          </div>
        </section>
        <section className="flex items-center px-6 py-10 sm:px-10 lg:px-0">
          <div className="w-full rounded-2xl border border-slate-200 bg-white p-6 shadow-[0_24px_80px_rgba(15,23,42,0.08)] sm:p-8">
            <div className="mb-8">
              <h2 className="text-2xl font-semibold tracking-normal">로그인</h2>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                데모 계정 정보가 기본 입력되어 있습니다.
              </p>
            </div>
            <LoginForm />
          </div>
        </section>
      </div>
    </main>
  );
}
