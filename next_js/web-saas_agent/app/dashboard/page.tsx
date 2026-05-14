import Link from "next/link";
import { redirect } from "next/navigation";
import { LogoutButton } from "@/components/auth/logout-button";
import { AuthCookieStore } from "@/lib/auth-service";

export default async function DashboardPage() {
  const { accessToken } = await AuthCookieStore.getTokens();

  if (!accessToken) {
    redirect("/login");
  }

  return (
    <main className="min-h-screen bg-[#f7f9fc] text-slate-950">
      <header className="border-b border-slate-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-6">
          <Link href="/" className="flex items-center gap-3 text-lg font-bold">
            <span className="grid size-9 place-items-center rounded-xl bg-slate-950 text-sm text-white">S</span>
            SaaS Agent
          </Link>
          <LogoutButton />
        </div>
      </header>
      <section className="mx-auto grid max-w-6xl gap-8 px-6 py-12 lg:grid-cols-[1fr_360px]">
        <div>
          <h1 className="text-4xl font-semibold tracking-normal">데모 대시보드</h1>
          <p className="mt-4 max-w-2xl text-lg leading-8 text-slate-600">
            로그인 세션이 확인되었습니다. 실제 서비스 화면에서는 이 영역에 구독, 사용량, 결제, 워크스페이스 지표를 연결할 수 있습니다.
          </p>
          <div className="mt-10 grid gap-4 sm:grid-cols-3">
            {[
              ["활성 워크스페이스", "12", "+18%"],
              ["이번 달 API 호출", "48.2K", "+9%"],
              ["자동화 성공률", "99.1%", "정상"],
            ].map(([label, value, trend]) => (
              <div key={label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <p className="text-sm text-slate-500">{label}</p>
                <strong className="mt-3 block text-3xl font-semibold">{value}</strong>
                <span className="mt-3 inline-block text-sm font-medium text-emerald-700">{trend}</span>
              </div>
            ))}
          </div>
        </div>
        <aside className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">인증 상태</h2>
          <p className="mt-3 text-sm leading-6 text-slate-600">
            액세스 토큰은 서버 쿠키에 저장되어 있으며 클라이언트 JavaScript에서 직접 읽을 수 없습니다.
          </p>
          <div className="mt-6 rounded-xl bg-slate-50 p-4 text-sm text-slate-600">
            API 서버: <span className="font-medium text-slate-950">http://localhost:8091</span>
          </div>
        </aside>
      </section>
    </main>
  );
}
