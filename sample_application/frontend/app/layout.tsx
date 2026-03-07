import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "증권/계좌 서비스 운영 콘솔",
  description: "채널계/계정계/정보계/대외계 통합 운영 화면"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <main>
          <h1>증권/계좌 서비스 운영 콘솔</h1>
          <nav>
            <Link href="/">대시보드</Link>
            <Link href="/channel">채널계</Link>
            <Link href="/account">계정계</Link>
            <Link href="/information">정보계</Link>
            <Link href="/external">대외계</Link>
            <Link href="/login">로그인</Link>
          </nav>
          {children}
        </main>
      </body>
    </html>
  );
}
