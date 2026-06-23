import type { Metadata } from "next";
import { SiteShell } from "@/components/shell/SiteShell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Growth Archive",
  description: "읽고, 실행하고, 성장한 기록을 남기는 사람들",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
