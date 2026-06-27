export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

function apiBaseUrl() {
  if (typeof window === "undefined") {
    return process.env.API_INTERNAL_BASE_URL ?? API_BASE_URL;
  }
  return API_BASE_URL;
}

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

export type AdminDashboard = {
  month: string;
  activeMemberCount: number;
  activeReadingRecordCount: number;
  meetingCount: number;
  activeReviewCount: number;
  unverifiedBookCount: number;
  participationTargetCount: number;
  participationCompletedCount: number;
  participationIncompleteCount: number;
};

export type AdminMember = {
  memberId: number;
  role: "MEMBER" | "ADMIN";
  displayName: string;
  realName: string | null;
  nickname: string;
  oneLineIntro: string;
  job: string | null;
  profileImageUrl: string | null;
  participationStartMonth: string;
  interestTags: string[];
  inviteVerifiedAt: string | null;
  termsAgreedAt: string | null;
  privacyAgreedAt: string | null;
  onboardingCompletedAt: string | null;
  deactivatedAt: string | null;
  createdAt: string;
};

export type AdminInviteCode = {
  codePreview: string | null;
};

export type AdminInterestTag = {
  id: number;
  name: string;
  slug: string;
  displayOrder: number;
  active: boolean;
};

export type InterestTag = {
  id: number;
  name: string;
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
  role: "MEMBER" | "ADMIN";
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
  birthDate: string | null;
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

export type MeetingAttendee = {
  memberId: number;
  displayName: string;
  profileImageUrl: string | null;
  profileHref: string;
};

export type MeetingSummary = {
  id: number;
  meetingType: "REGULAR_READING" | "REGULAR_ACTION" | "SMALL";
  title: string;
  description: string | null;
  meetingAt: string;
  locationRegion: string;
  capacity: number | null;
  feeAmount: number;
  coverImageUrl: string | null;
  status: "SCHEDULED" | "HELD" | "CANCELED" | "HIDDEN" | "DELETED";
  attendeeCount: number;
  attendeePreviewImageUrls: Array<string | null>;
};

export type MeetingDetail = MeetingSummary & {
  coverImageId: number | null;
  exactLocation: string | null;
  hostMemberId: number | null;
  hostDisplayName: string | null;
  targetMonth: string | null;
  autoGenerated: boolean;
  attendedByMe: boolean;
  canEdit: boolean;
  attendees: MeetingAttendee[];
};

export type ReviewImage = {
  imageId: number;
  imageUrl: string | null;
  displayOrder: number;
};

export type UploadedImage = {
  imageId: number;
  imageUrl: string | null;
};

export type UploadedSingleImage = {
  imageId: number;
  url: string | null;
  width: number | null;
  height: number | null;
};

export type MeetingReviewSummary = {
  id: number;
  meetingId: number;
  meetingTitle: string;
  memberId: number;
  memberDisplayName: string;
  memberProfileImageUrl: string | null;
  title: string;
  contentSummary: string;
  representativeImageUrl: string | null;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
  createdAt: string;
};

export type MeetingReviewDetail = {
  id: number;
  meetingId: number;
  meetingTitle: string;
  memberId: number;
  memberDisplayName: string;
  memberProfileImageUrl: string | null;
  title: string;
  content: string;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
  createdAt: string;
  updatedAt: string;
  canEdit: boolean;
  images: ReviewImage[];
};

export async function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    credentials: "include",
    cache: "no-store",
  });
  return response.json();
}

export async function apiPost<T>(path: string, body?: unknown): Promise<ApiResponse<T>> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
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
  const response = await fetch(`${apiBaseUrl()}${path}`, {
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
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    method: "DELETE",
    credentials: "include",
  });
  return response.json();
}

export async function apiPostForm<T>(path: string, formData: FormData): Promise<ApiResponse<T>> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    method: "POST",
    credentials: "include",
    body: formData,
  });
  return response.json();
}

export function uploadImage(file: File, purpose: "PROFILE" | "READING_RECORD" | "MEETING" | "REVIEW" | "BOOK") {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("purpose", purpose);
  return apiPostForm<UploadedSingleImage>("/uploads/images", formData);
}

export function kakaoLoginUrl() {
  return `${API_BASE_URL}/auth/kakao/login`;
}
