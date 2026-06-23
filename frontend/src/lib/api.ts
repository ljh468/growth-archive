export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export type CurrentUser = {
  authenticated: boolean;
  accessLevel: "PUBLIC" | "AUTHENTICATED" | "INVITE_VERIFIED" | "MEMBER" | "ADMIN";
  memberId: number | null;
  role: "MEMBER" | "ADMIN" | null;
  displayName: string | null;
  profileImageUrl: string | null;
  inviteVerified: boolean;
  termsAgreed: boolean;
  privacyAgreed: boolean;
  onboardingCompleted: boolean;
  deactivated: boolean;
};

type ApiResponse<T> = {
  success: boolean;
  data: T;
  message: string | null;
  error?: {
    code: string;
    message: string;
  } | null;
};

export async function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
    cache: "no-store",
  });
  return response.json();
}

export async function apiPost<T>(path: string, body?: unknown): Promise<ApiResponse<T>> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  return response.json();
}

export function kakaoLoginUrl() {
  return `${API_BASE_URL}/auth/kakao/login`;
}
