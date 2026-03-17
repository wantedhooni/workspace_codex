import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { WatchlistView } from "@/features/watchlist/components/watchlist-view";

export default function WatchlistPage() {
  return (
    <AuthGuard>
      <AppShell title="관심 종목" description="추적할 티커를 등록하고 제거하며 우선순위를 유지합니다.">
        <WatchlistView />
      </AppShell>
    </AuthGuard>
  );
}
