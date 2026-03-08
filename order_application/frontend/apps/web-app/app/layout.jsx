import "./globals.css";

export const metadata = {
  title: "Order Web",
  description: "Order application customer web"
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
