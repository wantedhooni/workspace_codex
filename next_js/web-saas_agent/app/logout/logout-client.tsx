"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export function LogoutClient() {
  const router = useRouter();

  useEffect(() => {
    async function logout() {
      await fetch("/api/auth/logout", { method: "POST" });
      router.replace("/");
      router.refresh();
    }

    void logout();
  }, [router]);

  return <p className="text-sm text-slate-600">로그아웃 중입니다.</p>;
}
