import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { NewsAnalyzeForm } from "@/features/news/news-analyze-form";

export default function NewsAnalyzePage() {
  return (
    <AuthGuard>
      <AppShell title="뉴스 분석" description="헤드라인과 본문을 기반으로 감성, 영향도, 해석을 생성합니다.">
        <NewsAnalyzeForm />
      </AppShell>
    </AuthGuard>
  );
}
