import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { BatchControlView } from "@/features/batch/components/batch-control-view";

export default function BatchOperationsPage() {
  return (
    <AuthGuard>
      <AppShell
        title="배치 운영"
        description="Quartz JDBC 스케줄과 Spring Batch JDBC 실행 이력을 제어하고 운영 상태를 확인합니다."
      >
        <BatchControlView />
      </AppShell>
    </AuthGuard>
  );
}
