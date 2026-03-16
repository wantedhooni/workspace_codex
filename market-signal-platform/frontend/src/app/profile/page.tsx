import { AppShell } from "@/components/layout/app-shell";
import { AuthGuard } from "@/components/layout/auth-guard";
import { ProfileForm } from "@/features/profile/profile-form";

export default function ProfilePage() {
  return (
    <AuthGuard>
      <AppShell title="프로필" description="이름과 투자 소개를 관리하고 계정 상태를 한 화면에서 확인합니다.">
        <ProfileForm />
      </AppShell>
    </AuthGuard>
  );
}
