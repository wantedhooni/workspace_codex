"use client";

import { useEffect } from "react";
import { useAuth } from "@/features/auth/context/auth-context";

export default function LogoutPage() {
  const { logout } = useAuth();

  useEffect(() => {
    void logout();
  }, [logout]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-background">
      <div className="rounded-full border border-border bg-card px-6 py-3 text-sm font-semibold text-foreground">
        로그아웃 처리 중입니다...
      </div>
    </main>
  );
}
