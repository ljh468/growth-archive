import type { NextConfig } from "next";

const apiProxyTarget = (
  process.env.API_PROXY_TARGET ??
  process.env.API_INTERNAL_BASE_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

const nextConfig: NextConfig = {
  output: "standalone",
  async rewrites() {
    return [
      {
        source: "/api/v1/:path*",
        destination: `${apiProxyTarget}/:path*`,
      },
    ];
  },
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "contents.kyobobook.co.kr",
      },
      {
        protocol: "https",
        hostname: "covers.openlibrary.org",
      },
      {
        protocol: "https",
        hostname: "**.kakaocdn.net",
      },
      {
        protocol: "https",
        hostname: "**.daumcdn.net",
      },
      {
        protocol: "https",
        hostname: "**.supabase.co",
      },
    ],
  },
};

export default nextConfig;
