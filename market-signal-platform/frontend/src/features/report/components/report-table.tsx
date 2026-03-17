"use client";

import { CalendarDays } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useRecentReports, useTodayReport } from "@/features/dashboard/api/queries";
import { ApiError } from "@/lib/api-client";

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  year: "numeric",
  month: "short",
  day: "numeric",
});

const dateTimeFormatter = new Intl.DateTimeFormat("ko-KR", {
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

function formatReportDate(value: string) {
  return dateFormatter.format(new Date(`${value}T00:00:00`));
}

function formatReportTime(value: string) {
  return dateTimeFormatter.format(new Date(value));
}

export function ReportTable() {
  const reportQuery = useTodayReport();
  const recentReportsQuery = useRecentReports(7);

  if (reportQuery.isLoading) {
    return <div className="text-sm text-foreground/70">리포트를 불러오는 중입니다...</div>;
  }

  if (reportQuery.isError || !reportQuery.data) {
    const message =
      reportQuery.error instanceof ApiError ? reportQuery.error.message : "먼저 대시보드에서 오늘의 리포트를 생성하세요.";
    return <div className="text-sm text-foreground/70">{message}</div>;
  }

  return (
    <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
      <Card>
        <CardHeader>
          <CardDescription>Daily Report</CardDescription>
          <CardTitle>오늘의 리포트</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-6 grid gap-3 md:grid-cols-4">
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Regime</p>
              <div className="mt-2 flex items-center gap-2">
                <Badge>{reportQuery.data.marketRegime}</Badge>
              </div>
            </div>
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Report Date</p>
              <p className="mt-2 text-sm font-semibold">{formatReportDate(reportQuery.data.reportDate)}</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Snapshot</p>
              <p className="mt-2 text-sm font-semibold">{formatReportDate(reportQuery.data.snapshotDate)}</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Generated</p>
              <p className="mt-2 text-sm font-semibold">{formatReportTime(reportQuery.data.generatedAt)}</p>
            </div>
          </div>
          <p className="mb-5 text-sm leading-7 text-foreground/70">{reportQuery.data.summary}</p>
          <div className="mb-5 flex flex-wrap gap-2">
            {reportQuery.data.leadingSectors.map((sector) => (
              <Badge key={sector}>{sector}</Badge>
            ))}
          </div>
          <div className="overflow-hidden rounded-[26px] border border-border bg-white/72">
            <table className="min-w-full">
              <thead className="bg-background/85">
                <tr className="text-left text-xs uppercase tracking-[0.2em] text-foreground/60">
                  <th className="px-5 py-4">Rank</th>
                  <th className="px-5 py-4">Ticker</th>
                  <th className="px-5 py-4">Why</th>
                  <th className="px-5 py-4">Score</th>
                  <th className="px-5 py-4">Action</th>
                </tr>
              </thead>
              <tbody>
                {reportQuery.data.topSignals.map((signal, index) => (
                  <tr key={signal.ticker} className="border-t border-border text-sm transition hover:bg-background/60">
                    <td className="px-5 py-4 text-foreground/60">{index + 1}</td>
                    <td className="px-5 py-4 font-semibold">{signal.ticker}</td>
                    <td className="px-5 py-4">
                      <div className="flex flex-wrap gap-2">
                        {signal.reasons.map((reason) => (
                          <span
                            key={`${signal.ticker}-${reason}`}
                            className="inline-flex items-center rounded-full border border-foreground/10 bg-background/60 px-3 py-1 text-xs font-medium text-foreground/70"
                          >
                            {reason}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-5 py-4">{signal.score}</td>
                    <td className="px-5 py-4">
                      <Badge>{signal.action}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardDescription>Report History</CardDescription>
          <CardTitle className="flex items-center gap-3">
            <CalendarDays className="h-5 w-5 text-accent" />
            최근 리포트
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
            {recentReportsQuery.data?.map((report) => (
              <div key={report.reportDate} className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="text-sm font-semibold">{formatReportDate(report.reportDate)}</p>
                  <Badge>{report.marketRegime}</Badge>
                </div>
              <p className="mt-2 text-xs uppercase tracking-[0.16em] text-foreground/45">snapshot {formatReportDate(report.snapshotDate)}</p>
              <div className="mt-3 flex flex-wrap gap-2">
                {report.leadingSectors.slice(0, 2).map((sector) => (
                  <Badge key={sector}>{sector}</Badge>
                ))}
              </div>
              <p className="mt-3 text-sm leading-6 text-foreground/70">{report.summary}</p>
            </div>
          ))}
          {!recentReportsQuery.data?.length && !recentReportsQuery.isLoading && (
            <p className="text-sm text-foreground/65">누적된 리포트 이력이 없습니다.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
