"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button } from "@/components/ui/button";

export function LogoutButton() {
  const router = useRouter();
  const [isPending, setIsPending] = useState(false);

  async function handleLogout() {
    setIsPending(true);
    await fetch("/api/auth/logout", { method: "POST" });
    router.push("/");
    router.refresh();
  }

  return (
    <Button
      type="button"
      variant="outline"
      size="lg"
      onClick={handleLogout}
      disabled={isPending}
      className="h-11 rounded-full px-5 text-sm"
    >
      {isPending ? "로그아웃 중" : "로그아웃"}
    </Button>
  );
}
