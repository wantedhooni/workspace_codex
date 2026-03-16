"use client";

import Link from "next/link";
import { type ReactNode } from "react";
import { usePathname } from "next/navigation";
import { ActivitySquare, ArrowRight, BarChart3, Newspaper, Radar, ShieldCheck, Sparkles, UserCircle2 } from "lucide-react";
import { cn } from "@/lib/utils";

const navigation = [
  { href: "/dashboard", label: "대시보드", icon: Radar, hint: "오늘의 시장 톤과 우선 대응" },
  { href: "/watchlist", label: "관심종목", icon: BarChart3, hint: "보유 후보와 시그널 상태" },
  { href: "/reports/today", label: "리포트", icon: ShieldCheck, hint: "상세 시그널 근거와 이력" },
  { href: "/news/analyze", label: "뉴스 AI", icon: Newspaper, hint: "뉴스 해석과 영향도 판단" },
  { href: "/operations/batch", label: "배치 운영", icon: ActivitySquare, hint: "Quartz와 Batch 실행 제어" },
  { href: "/profile", label: "프로필", icon: UserCircle2, hint: "개인 계정과 소개 관리" },
];

export function AppShell({
  children,
  title,
  description,
}: {
  children: ReactNode;
  title: string;
  description: string;
}) {
  const pathname = usePathname();

  return (
    <div className="min-h-screen bg-market-grid bg-[size:38px_38px]">
      <div className="mx-auto flex min-h-screen w-full max-w-[1500px] gap-6 px-4 py-5 lg:px-6">
        <aside className="sticky top-6 hidden h-[calc(100vh-3rem)] w-80 shrink-0 rounded-[32px] border border-white/70 bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(248,241,229,0.96))] p-6 shadow-panel backdrop-blur lg:flex lg:flex-col">
          <div className="space-y-6">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.26em] text-accent">Market Signal</p>
              <h1 className="mt-3 text-3xl font-semibold leading-tight text-foreground">Tape before thesis.</h1>
              <p className="mt-3 text-sm leading-6 text-foreground/68">
                시장 해석보다 먼저 봐야 할 데이터와 실행 신호를 정리하는 워크스페이스입니다.
              </p>
            </div>
            <div className="rounded-[28px] border border-foreground/8 bg-foreground px-5 py-5 text-white">
              <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-white/70">
                <Sparkles className="h-4 w-4" />
                Daily Workflow
              </div>
              <div className="mt-4 space-y-3 text-sm text-white/82">
                <p>1. 대시보드에서 오늘의 레짐과 신호 우선순위를 확인합니다.</p>
                <p>2. 관심종목에서 내 추적 리스트와 최신 액션을 점검합니다.</p>
                <p>3. 뉴스 분석에서 헤드라인 해석과 영향도를 빠르게 교차검증합니다.</p>
              </div>
            </div>
          </div>
          <nav className="mt-8 space-y-2">
            {navigation.map((item) => {
              const Icon = item.icon;
              const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex items-start gap-3 rounded-[24px] border px-4 py-4 transition",
                    active
                      ? "border-primary/25 bg-primary text-white shadow-panel"
                      : "border-transparent bg-transparent text-foreground/74 hover:border-border hover:bg-white/70",
                  )}
                >
                  <Icon className="mt-0.5 h-4 w-4 shrink-0" />
                  <div>
                    <div className="text-sm font-semibold">{item.label}</div>
                    <p className={cn("mt-1 text-xs leading-5", active ? "text-white/75" : "text-foreground/50")}>{item.hint}</p>
                  </div>
                </Link>
              );
            })}
          </nav>
          <div className="mt-auto rounded-[28px] border border-border bg-white/72 px-5 py-5">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-foreground/48">Quick Exit</p>
            <p className="mt-2 text-sm leading-6 text-foreground/65">현재 세션을 종료하고 로그인 화면으로 돌아갑니다.</p>
            <Link
              href="/logout"
              className="mt-4 inline-flex items-center gap-2 rounded-full border border-border px-4 py-2 text-sm font-semibold text-foreground/72 transition hover:border-primary hover:text-primary"
            >
              로그아웃
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
        </aside>
        <main className="min-w-0 flex-1 pb-24 lg:pb-10">
          <div className="mb-4 rounded-[28px] border border-white/70 bg-white/78 px-5 py-5 shadow-panel backdrop-blur lg:hidden">
            <p className="text-xs font-semibold uppercase tracking-[0.26em] text-accent">Market Signal</p>
            <h1 className="mt-2 text-2xl font-semibold text-foreground">Tape before thesis.</h1>
            <p className="mt-2 text-sm leading-6 text-foreground/68">모바일에서도 대시보드와 관심종목, 뉴스 판단을 빠르게 넘겨볼 수 있게 정리했습니다.</p>
            <Link
              href="/logout"
              className="mt-4 inline-flex items-center gap-2 rounded-full border border-border px-4 py-2 text-sm font-semibold text-foreground/70"
            >
              로그아웃
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          <header className="mb-6 overflow-hidden rounded-[32px] border border-white/70 bg-[radial-gradient(circle_at_top_left,_rgba(200,116,56,0.2),_transparent_36%),linear-gradient(135deg,_rgba(255,255,255,0.95),_rgba(247,238,221,0.96))] shadow-panel">
            <div className="flex flex-col gap-6 px-6 py-6 lg:px-8 lg:py-7">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="max-w-3xl">
                  <p className="text-xs font-semibold uppercase tracking-[0.26em] text-accent">Signal Workspace</p>
                  <h2 className="mt-3 text-3xl font-semibold text-foreground lg:text-[2.15rem]">{title}</h2>
                  <p className="mt-3 text-sm leading-7 text-foreground/70 lg:text-base">{description}</p>
                </div>
                <div className="grid gap-3 sm:grid-cols-2 lg:min-w-[320px]">
                  <div className="rounded-[24px] border border-foreground/8 bg-white/72 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Focus</p>
                    <p className="mt-2 text-sm font-semibold text-foreground">오늘 가장 먼저 확인할 데이터와 후보를 위에 배치했습니다.</p>
                  </div>
                  <div className="rounded-[24px] border border-foreground/8 bg-white/72 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Flow</p>
                    <p className="mt-2 text-sm font-semibold text-foreground">대시보드 → 관심종목 → 뉴스 검증 순서로 이어집니다.</p>
                  </div>
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                {navigation.slice(0, 5).map((item) => {
                  const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={cn(
                        "inline-flex items-center rounded-full border px-4 py-2 text-sm font-semibold transition",
                        active
                          ? "border-primary bg-primary text-white"
                          : "border-border bg-white/72 text-foreground/70 hover:border-primary hover:text-primary",
                      )}
                    >
                      {item.label}
                    </Link>
                  );
                })}
              </div>
            </div>
          </header>
          <div>{children}</div>
        </main>
      </div>
      <nav className="fixed inset-x-4 bottom-4 z-30 rounded-[28px] border border-white/80 bg-white/92 px-3 py-3 shadow-panel backdrop-blur lg:hidden">
        <div className="grid grid-cols-6 gap-2">
          {navigation.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex flex-col items-center gap-1 rounded-[20px] px-2 py-2 text-[11px] font-semibold transition",
                  active ? "bg-primary text-white" : "text-foreground/62",
                )}
              >
                <Icon className="h-4 w-4" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
