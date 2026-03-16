import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { ReportTable } from "@/features/report/report-table";

export default function TodayReportPage() {
  return (
    <AuthGuard>
      <AppShell title="오늘의 리포트" description="시장 레짐과 상위 종목 시그널을 표 형식으로 검토합니다.">
        <ReportTable />
      </AppShell>
    </AuthGuard>
  );
}
