"use client";

import Link from "next/link";
import { useMemo } from "react";
import { AlertTriangle, CalendarDays, ChevronRight, RefreshCcw, TrendingUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useBatchJobs } from "@/features/batch/api/queries";
import { ApiError } from "@/lib/api-client";
import { useGenerateReport, useRecentReports, useTodayReport } from "@/features/dashboard/api/queries";

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "short",
  day: "numeric",
  weekday: "short",
});

const dateTimeFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

function formatReportDate(value: string) {
  return dateFormatter.format(new Date(`${value}T00:00:00`));
}

function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value));
}

function getActionTone(action: "BUY" | "WATCH" | "AVOID") {
  switch (action) {
    case "BUY":
      return "border-success/20 bg-success/10";
    case "WATCH":
      return "border-accent/20 bg-accent/10";
    default:
      return "border-danger/15 bg-danger/10";
  }
}

export function DashboardView() {
  const reportQuery = useTodayReport();
  const recentReportsQuery = useRecentReports(5);
  const generateMutation = useGenerateReport();
  const batchJobsQuery = useBatchJobs();
  const report = reportQuery.data;
  const recentReports = recentReportsQuery.data ?? [];
  const batchJobs = batchJobsQuery.data ?? [];
  const averageScore = report
    ? Math.round(report.topSignals.reduce((sum, signal) => sum + signal.score, 0) / Math.max(report.topSignals.length, 1))
    : 0;
  const snapshotLagDays = useMemo(() => {
    if (!report) {
      return 0;
    }
    const reportDate = new Date(`${report.reportDate}T00:00:00`).getTime();
    const snapshotDate = new Date(`${report.snapshotDate}T00:00:00`).getTime();
    return Math.max(0, Math.round((reportDate - snapshotDate) / (1000 * 60 * 60 * 24)));
  }, [report]);

  if (reportQuery.isLoading) {
    return (
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_360px]">
        <Card>
          <CardHeader>
            <CardTitle>오늘의 리포트를 불러오는 중입니다</CardTitle>
            <CardDescription>시장 스냅샷과 시그널 요약을 정리하고 있습니다.</CardDescription>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>최근 브리핑</CardTitle>
            <CardDescription>최근 생성된 리포트 이력을 함께 가져오는 중입니다.</CardDescription>
          </CardHeader>
        </Card>
      </div>
    );
  }

  if (reportQuery.isError || !report) {
    const message =
      reportQuery.error instanceof ApiError ? reportQuery.error.message : "오늘 데이터가 아직 생성되지 않았습니다.";
    return (
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_360px]">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-3">
              <AlertTriangle className="h-5 w-5 text-danger" />
              리포트 준비 필요
            </CardTitle>
            <CardDescription>{message}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm leading-6 text-foreground/70">
              장 시작 전이나 초기 데이터가 비어 있는 경우, 시그널 엔진을 직접 실행해 바로 대시보드를 채울 수 있습니다.
            </p>
            <Button onClick={() => generateMutation.mutate()} disabled={generateMutation.isPending}>
              {generateMutation.isPending ? "생성 중..." : "오늘의 리포트 생성"}
            </Button>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>최근 브리핑</CardTitle>
            <CardDescription>과거 리포트를 통해 최근 시장 톤의 변화를 빠르게 확인할 수 있습니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {recentReports.map((item) => (
                <div key={item.reportDate} className="rounded-3xl border border-border px-4 py-4">
                  <div className="flex items-center justify-between gap-3">
                    <p className="text-sm font-semibold">{formatReportDate(item.reportDate)}</p>
                    <Badge>{item.marketRegime}</Badge>
                  </div>
                <p className="mt-3 text-sm text-foreground/70">{item.summary}</p>
              </div>
            ))}
            {recentReports.length === 0 && <p className="text-sm text-foreground/65">누적된 리포트 이력이 없습니다.</p>}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_360px]">
        <Card className="overflow-hidden">
          <CardContent className="p-0">
            <div className="grid gap-0 lg:grid-cols-[minmax(0,1fr)_320px]">
              <div className="px-6 py-6 lg:px-8 lg:py-8">
                <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-accent">Today&apos;s Market Pulse</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  <Badge>{report.marketRegime}</Badge>
                  <Badge>{snapshotLagDays === 0 ? "LIVE SNAPSHOT" : `SNAPSHOT D-${snapshotLagDays}`}</Badge>
                </div>
                <h3 className="mt-4 text-2xl font-semibold leading-tight text-foreground lg:text-[2rem]">{report.summary}</h3>
                <p className="mt-3 text-sm leading-7 text-foreground/68">
                  시장 데이터 기준일은 {formatReportDate(report.snapshotDate)} 이고, 리포트는 {formatDateTime(report.generatedAt)} 에 생성되었습니다.
                </p>
                <div className="mt-5 flex flex-wrap gap-2">
                  {report.leadingSectors.map((sector) => (
                    <Badge key={sector}>{sector}</Badge>
                  ))}
                </div>
                <div className="mt-6 grid gap-3 sm:grid-cols-3">
                  <div className="rounded-[24px] border border-border bg-white/70 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Market Regime</p>
                    <p className="mt-2 text-lg font-semibold">{report.marketRegime}</p>
                  </div>
                  <div className="rounded-[24px] border border-border bg-white/70 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Average Score</p>
                    <p className="mt-2 text-lg font-semibold">{averageScore}</p>
                  </div>
                  <div className="rounded-[24px] border border-border bg-white/70 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Actionable</p>
                    <p className="mt-2 text-lg font-semibold">{report.topSignals.filter((signal) => signal.action !== "AVOID").length}개</p>
                  </div>
                </div>
              </div>
              <div className="border-t border-border/70 bg-foreground px-6 py-6 text-white lg:border-l lg:border-t-0">
                <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-white/60">Execution Board</p>
                <div className="mt-4 space-y-3">
                  {report.topSignals.slice(0, 3).map((signal) => (
                    <div key={signal.ticker} className="rounded-[24px] border border-white/10 bg-white/8 px-4 py-4">
                      <div className="flex items-center justify-between gap-3">
                        <div>
                          <p className="text-base font-semibold">{signal.ticker}</p>
                          <p className="mt-1 text-xs uppercase tracking-[0.14em] text-white/55">{signal.action}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-[11px] uppercase tracking-[0.18em] text-white/45">Score</p>
                          <p className="mt-1 text-2xl font-semibold">{signal.score}</p>
                        </div>
                      </div>
                      <p className="mt-3 text-sm leading-6 text-white/72">
                        {signal.reasons.slice(0, 2).join(" · ") || "추가 근거 없음"}
                      </p>
                    </div>
                  ))}
                </div>
                <Button className="mt-6 w-full" variant="accent" onClick={() => generateMutation.mutate()} disabled={generateMutation.isPending}>
                  <RefreshCcw className="mr-2 h-4 w-4" />
                  {generateMutation.isPending ? "재생성 중..." : "오늘 리포트 새로 계산"}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardDescription>Recent Briefings</CardDescription>
            <CardTitle className="flex items-center gap-3">
              <CalendarDays className="h-5 w-5 text-accent" />
              최근 리포트 이력
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {recentReports.map((item) => (
              <div key={item.reportDate} className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="text-sm font-semibold">{formatReportDate(item.reportDate)}</p>
                  <Badge>{item.marketRegime}</Badge>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {item.leadingSectors.slice(0, 2).map((sector) => (
                    <Badge key={sector}>{sector}</Badge>
                  ))}
                </div>
                <p className="mt-3 text-sm leading-6 text-foreground/70">{item.summary}</p>
              </div>
            ))}
            {recentReports.length === 0 && !recentReportsQuery.isLoading && (
              <p className="text-sm text-foreground/65">최근 리포트 이력이 아직 없습니다.</p>
            )}
          </CardContent>
        </Card>
      </section>
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_360px]">
        <Card>
          <CardHeader>
            <CardDescription>Actionable Setups</CardDescription>
            <CardTitle>오늘 바로 볼 종목</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4">
            {report.topSignals.map((signal, index) => (
              <div
                key={signal.ticker}
                className={`rounded-[26px] border px-5 py-5 ${getActionTone(signal.action)}`}
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-white/70 text-sm font-semibold text-foreground">
                        {index + 1}
                      </span>
                      <h3 className="text-xl font-semibold text-foreground">{signal.ticker}</h3>
                      <Badge>{signal.action}</Badge>
                    </div>
                    <div className="mt-4 flex flex-wrap gap-2">
                      {signal.reasons.map((reason) => (
                        <span
                          key={`${signal.ticker}-${reason}`}
                          className="inline-flex items-center rounded-full border border-foreground/10 bg-white/70 px-3 py-1 text-xs font-medium text-foreground/72"
                        >
                          {reason}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div className="flex items-center gap-4 lg:flex-col lg:items-end">
                    <div>
                      <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Score</p>
                      <p className="mt-1 text-3xl font-semibold text-foreground">{signal.score}</p>
                    </div>
                    <div className="inline-flex items-center gap-2 text-sm font-semibold text-foreground/62">
                      상세 리포트에서 확인
                      <ChevronRight className="h-4 w-4" />
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardDescription>Data Freshness</CardDescription>
              <CardTitle className="flex items-center gap-3">
                <TrendingUp className="h-5 w-5 text-accent" />
                운영 체크포인트
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-foreground/45">Snapshot</p>
                <p className="mt-2 text-lg font-semibold">{formatReportDate(report.snapshotDate)}</p>
                <p className="mt-2 text-sm leading-6 text-foreground/64">
                  기준일과 현재 날짜 차이는 {snapshotLagDays}일입니다. 레짐 해석 시 데이터 지연을 함께 고려하세요.
                </p>
              </div>
              <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-foreground/45">Leading Sectors</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {report.leadingSectors.map((sector) => (
                    <Badge key={sector}>{sector}</Badge>
                  ))}
                </div>
              </div>
              <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-foreground/45">Generated</p>
                <p className="mt-2 text-lg font-semibold">{formatDateTime(report.generatedAt)}</p>
                <p className="mt-2 text-sm leading-6 text-foreground/64">장전 보고용으로 재생성 버튼을 눌러 최신 시그널을 즉시 계산할 수 있습니다.</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardDescription>Batch Ops</CardDescription>
              <CardTitle>배치 운영 요약</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {batchJobs.slice(0, 2).map((job) => (
                <div key={job.jobName} className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                  <div className="flex items-center justify-between gap-3">
                    <p className="text-sm font-semibold text-foreground">{job.title}</p>
                    <Badge>{job.scheduleState}</Badge>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-foreground/64">
                    다음 실행 {job.nextFireTime ? formatDateTime(job.nextFireTime) : "수동 실행 전용"}
                  </p>
                  <p className="mt-1 text-sm leading-6 text-foreground/56">
                    최근 실행 {job.lastExecution?.startedAt ? formatDateTime(job.lastExecution.startedAt) : "기록 없음"}
                  </p>
                </div>
              ))}
              {batchJobs.length === 0 && !batchJobsQuery.isLoading && (
                <p className="rounded-[24px] border border-border bg-white/74 px-4 py-4 text-sm text-foreground/60">
                  배치 운영 메타데이터를 아직 불러오지 못했습니다.
                </p>
              )}
              <Link
                href="/operations/batch"
                className="inline-flex items-center rounded-full border border-border bg-white px-4 py-2 text-sm font-semibold text-foreground/72 transition hover:border-primary hover:text-primary"
              >
                배치 운영 열기
              </Link>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
}
