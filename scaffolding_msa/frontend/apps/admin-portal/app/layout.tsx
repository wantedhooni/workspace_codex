import type { Metadata } from "next";
import { Space_Grotesk, IBM_Plex_Mono } from "next/font/google";
import { AdminProviders } from "@/components/admin-providers";
import "./globals.css";

const spaceGrotesk = Space_Grotesk({
    subsets: ["latin"],
    variable: "--font-admin-sans",
});

const ibmPlexMono = IBM_Plex_Mono({
    subsets: ["latin"],
    variable: "--font-admin-mono",
    weight: ["400", "500"],
});

export const metadata: Metadata = {
    title: "Admin Portal",
    description: "refine.dev based operations portal for scaffolding-msa",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="ko">
            <body className={`${spaceGrotesk.variable} ${ibmPlexMono.variable}`}>
                <AdminProviders>{children}</AdminProviders>
            </body>
        </html>
    );
}

