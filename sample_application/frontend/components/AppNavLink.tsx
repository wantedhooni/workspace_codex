"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

type AppNavLinkProps = {
  href: string;
  label: string;
  exact?: boolean;
};

export default function AppNavLink({ href, label, exact = false }: AppNavLinkProps) {
  const pathname = usePathname();
  const isActive = exact ? pathname === href : pathname === href || pathname.startsWith(`${href}/`);

  return (
    <Link className={`nav-link${isActive ? " is-active" : ""}`} href={href}>
      {label}
    </Link>
  );
}
