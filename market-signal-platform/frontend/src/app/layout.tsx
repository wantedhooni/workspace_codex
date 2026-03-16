import type { Metadata } from "next";
import { Manrope } from "next/font/google";
import { Providers } from "@/components/layout/providers";
import "@/app/globals.css";

const manrope = Manrope({
  variable: "--font-manrope",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Market Signal Platform",
  description: "미국 주식 시장 상태, 섹터 강도, 종목 시그널, 뉴스 분석 플랫폼",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className={manrope.variable}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
