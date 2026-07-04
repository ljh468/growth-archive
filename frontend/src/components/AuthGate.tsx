"use client";

import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { apiGetCurrentUser, type CurrentUser } from "@/lib/api";

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
    apiGetCurrentUser()
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
    return <AuthSpinner />;
  }

  if (accessRank[user.accessLevel] < accessRank[required]) {
    const destination = !user.authenticated ? "/login" : "/onboarding";
    window.location.replace(destination);
    return <AuthSpinner />;
  }

  return children(user);
}

function AuthSpinner() {
  return (
    <div className="grid min-h-24 place-items-center" aria-label="처리 중">
      <span className="size-6 animate-spin rounded-full border-2 border-[rgba(47,90,67,0.18)] border-t-[var(--color-deep-green)]" />
    </div>
  );
}
