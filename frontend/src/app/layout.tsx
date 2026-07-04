import type { Metadata, Viewport } from "next";
import { SiteShell } from "@/components/shell/SiteShell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Growth Archive",
  description: "읽고, 실행하고, 성장한 기록을 남기는 사람들",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <head>
        <link as="font" crossOrigin="anonymous" href="/fonts/nanum-square.woff" rel="preload" type="font/woff" />
        <link as="font" crossOrigin="anonymous" href="/fonts/caveat.ttf" rel="preload" type="font/ttf" />
        <link as="font" crossOrigin="anonymous" href="/fonts/nanum-beomsom.ttf" rel="preload" type="font/ttf" />
      </head>
      <body>
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
