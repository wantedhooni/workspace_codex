"use client";

import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import { getRoles, getToken } from "./auth";

export default function TopbarStatus() {
  const pathname = usePathname();
  const [roles, setRoles] = useState<string[]>([]);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    setRoles(getRoles());
    setAuthenticated(Boolean(getToken()));
  }, [pathname]);

  return (
    <>
      <span>증권/계좌 통합 운영 시스템</span>
      <div className="topbar-meta">
        <span className={`topbar-pill${authenticated ? " is-live" : ""}`}>{authenticated ? "인증됨" : "미인증"}</span>
        <span className="topbar-pill">권한: {roles.join(", ") || "없음"}</span>
        <span className="topbar-pill">Mode: Admin Backoffice</span>
      </div>
    </>
  );
}
