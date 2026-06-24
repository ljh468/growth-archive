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

export type BookSummary = {
  id: number;
  title: string;
  authorsText: string;
  publisher: string | null;
  publishedDate: string | null;
  thumbnailUrl: string | null;
  status: "VERIFIED" | "UNVERIFIED";
};

export type BookSearchResult = {
  provider: "KAKAO";
  title: string;
  authorsText: string;
  publisher: string | null;
  publishedDate: string | null;
  thumbnailUrl: string | null;
  isbn10: string | null;
  isbn13: string | null;
  sourcePayload: string;
};

export type ReadingRecord = {
  id: number;
  memberId: number;
  memberDisplayName: string;
  memberProfileImageUrl: string | null;
  bookId: number;
  bookTitle: string;
  authorsText: string;
  bookThumbnailUrl: string | null;
  rating: number | null;
  oneLineReview: string;
  blogUrl: string;
  imageId: number | null;
  recordImageUrl: string | null;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
  recordedAt: string;
  createdAt: string;
};

export type LibraryBook = {
  id: number;
  title: string;
  authorsText: string;
  publisher: string | null;
  thumbnailUrl: string | null;
  readingRecordCount: number;
  averageRating: number | null;
};

export type RecommendedBook = {
  id: number;
  bookId: number;
  title: string;
  authorsText: string;
  publisher: string | null;
  thumbnailUrl: string | null;
  reason: string;
  displayOrder: number;
};

export type LibraryResponse = {
  recommendedBooks: RecommendedBook[];
  popularBooks: LibraryBook[];
  recentReadingRecords: ReadingRecord[];
};

export type BookDetailResponse = {
  book: BookSummary & {
    readingRecordCount: number;
    readerCount: number;
    averageRating: number | null;
  };
  readingRecords: ReadingRecord[];
};

export type GrowthStats = {
  readingRecordCount: number;
  actionPlanCount: number;
  monthlyReflectionCount: number;
  meetingReviewCount: number;
  smallMeetingCreatedCount: number;
};

export type ActivitySummary = {
  type: string;
  title: string;
  href: string;
  occurredAt: string;
};

export type ProfileCard = {
  memberId: number;
  displayName: string;
  profileImageUrl: string | null;
  oneLineIntro: string;
  interestTags: string[];
  futureMeAt50Summary: string;
  growthStats: GrowthStats;
  recentPublicActivity: ActivitySummary | null;
};

export type ProfileDetail = {
  memberId: number;
  displayName: string;
  profileImageUrl: string | null;
  oneLineIntro: string;
  interestTags: string[];
  futureMeAt50: string;
  growthStats: GrowthStats;
  recentReadingRecords: ReadingRecord[];
  recentMeetingReviews: ActivitySummary[];
  recentPublicActivities: ActivitySummary[];
  memberOnly: {
    job: string | null;
    joinReason: string | null;
    currentConcern: string | null;
    threeYearGoal: string | null;
    recentActionPlans: ActivitySummary[];
    recentReflections: ActivitySummary[];
  } | null;
};

export type MyProfile = {
  memberId: number;
  nickname: string;
  realName: string | null;
  displayNameType: "REAL_NAME" | "NICKNAME";
  displayName: string;
  profileImageUrl: string | null;
  profileImageId: number | null;
  oneLineIntro: string;
  job: string | null;
  interestTagIds: number[];
  futureMeAt50: string;
  joinReason: string | null;
  currentConcern: string | null;
  threeYearGoal: string | null;
};

export type MyDashboard = {
  profile: {
    memberId: number;
    displayName: string;
    profileImageUrl: string | null;
    oneLineIntro: string;
  };
  participation: {
    month: string;
    readingRecordCount: number;
    actionPlanCount: number;
    completed: boolean;
    coffeeSupportTarget: boolean;
  };
  quickStats: GrowthStats;
  recentActivities: ActivitySummary[];
};

export type MonthlyActionPlan = {
  id: number;
  memberId: number;
  targetMonth: string;
  title: string | null;
  content: string;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
  createdAt: string;
  updatedAt: string;
};

export type MonthlyReflection = {
  id: number;
  memberId: number;
  targetMonth: string;
  wellDone: string | null;
  regret: string | null;
  nextFocus: string | null;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
  createdAt: string;
  updatedAt: string;
};

export type MonthlyReflectionSlot = {
  targetMonth: string;
  writable: boolean;
  reflection: MonthlyReflection | null;
};

export type ParticipationStatus = {
  month: string;
  readingRecordCount: number;
  actionPlanCount: number;
  calculationTarget: boolean;
  completed: boolean;
  coffeeSupportTarget: boolean;
  coffeeSupportItem: string;
};

export type AdminParticipationMember = {
  memberId: number;
  displayName: string;
  nickname: string;
  profileImageUrl: string | null;
  readingRecordCount: number;
  hasActionPlan: boolean;
  calculationTarget: boolean;
  completed: boolean;
  coffeeSupportTarget: boolean;
  coffeeSupportItem: string;
  adminMemo: string | null;
};

export type AdminParticipationSummary = {
  month: string;
  totalTargetMemberCount: number;
  completedCount: number;
  incompleteCount: number;
  completionRate: number;
  members: AdminParticipationMember[];
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

export async function apiPut<T>(path: string, body?: unknown): Promise<ApiResponse<T>> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  return response.json();
}

export async function apiDelete<T>(path: string): Promise<ApiResponse<T>> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    credentials: "include",
  });
  return response.json();
}

export function kakaoLoginUrl() {
  return `${API_BASE_URL}/auth/kakao/login`;
}
