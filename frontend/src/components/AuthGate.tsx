"use client";

import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { apiGet, type CurrentUser } from "@/lib/api";

const accessRank = {
  PUBLIC: 0,
  AUTHENTICATED: 1,
  INVITE_VERIFIED: 2,
  MEMBER: 3,
  ADMIN: 4,
};

type AuthGateProps = {
  required: keyof typeof accessRank;
  children: (user: CurrentUser) => ReactNode;
};

export function AuthGate({ required, children }: AuthGateProps) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiGet<CurrentUser>("/auth/me")
      .then((result) => {
        if (!result.success) {
          setError(result.error?.message ?? "인증 상태를 확인하지 못했습니다.");
          return;
        }
        setUser(result.data);
      })
      .catch(() => setError("인증 상태를 확인하지 못했습니다."));
  }, []);

  if (error) {
    return <p className="text-sm text-red-700">{error}</p>;
  }

  if (!user) {
    return <p className="text-sm text-[var(--color-charcoal)]">확인 중입니다.</p>;
  }

  if (accessRank[user.accessLevel] < accessRank[required]) {
    const destination = !user.authenticated ? "/login" : "/onboarding";
    window.location.replace(destination);
    return <p className="text-sm text-[var(--color-charcoal)]">이동 중입니다.</p>;
  }

  return children(user);
}
