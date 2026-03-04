import type { Metadata } from "next";
import { Sora, Cormorant_Garamond } from "next/font/google";
import "./globals.css";

const sora = Sora({
    subsets: ["latin"],
    variable: "--font-web-sans",
});

const cormorant = Cormorant_Garamond({
    subsets: ["latin"],
    variable: "--font-web-serif",
    weight: ["500", "600", "700"],
});

export const metadata: Metadata = {
    title: "Web Application",
    description: "Default customer-facing web application for scaffolding-msa",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="ko">
            <body className={`${sora.variable} ${cormorant.variable}`}>{children}</body>
        </html>
    );
}

