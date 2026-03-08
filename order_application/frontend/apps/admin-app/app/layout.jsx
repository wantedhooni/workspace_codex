import "./globals.css";

export const metadata = {
  title: "Order Admin",
  description: "Order application admin console"
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
