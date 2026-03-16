import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { DashboardView } from "@/features/dashboard/dashboard-view";

export default function DashboardPage() {
  return (
    <AuthGuard>
      <AppShell
        title="시장 대시보드"
        description="오늘의 시장 레짐, 선도 섹터, 상위 종목 시그널을 한 번에 점검합니다."
      >
        <DashboardView />
      </AppShell>
    </AuthGuard>
  );
}
