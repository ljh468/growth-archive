import { decodeHeicImage, isHeicFile } from "./heicImage";
import { imageUploadProfiles, TARGET_MAX_IMAGE_BYTES, type ImageUploadPurpose } from "./imageUploadConfig";

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
  targetMonth: string;
  status: "ACTIVE" | "HIDDEN" | "DELETED";
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
  withdrawnAt: string | null;
  createdAt: string;
};

export type AdminInviteCode = {
  memberCodePreview: string | null;
  adminCodePreview: string | null;
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

export type OnboardingProfileDraft = {
  nickname: string | null;
  oneLineIntro: string | null;
  displayNameType: "REAL_NAME" | "NICKNAME";
  realName: string | null;
  birthDate: string | null;
  profileImageId: number | null;
  futureMeAt50: string | null;
  joinReason: string | null;
  currentConcern: string | null;
  threeYearGoal: string | null;
  interestTagIds: number[];
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

export type MonthlyActionPlanShowcase = {
  id: number;
  memberId: number;
  displayName: string;
  profileImageUrl: string | null;
  targetMonth: string;
  title: string | null;
  content: string;
  updatedAt: string;
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
    role: "MEMBER" | "ADMIN";
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
  currentReadingRecord: {
    id: number;
    bookId: number;
    bookTitle: string;
    oneLineReview: string;
    blogUrl: string;
    recordedAt: string;
  } | null;
  currentReadingRecords: Array<{
    id: number;
    bookId: number;
    bookTitle: string;
    oneLineReview: string;
    blogUrl: string;
    recordedAt: string;
  }>;
  currentActionPlan: {
    id: number;
    targetMonth: string;
    title: string | null;
    content: string;
    updatedAt: string;
  } | null;
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
  manuallyCompleted: boolean;
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

let currentUserRequest: Promise<ApiResponse<CurrentUser>> | null = null;

export function apiGetCurrentUser() {
  currentUserRequest ??= apiGet<CurrentUser>("/auth/me");
  return currentUserRequest;
}

export function clearCurrentUserCache() {
  currentUserRequest = null;
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

export async function apiPostForm<T>(path: string, formData: FormData, options?: { signal?: AbortSignal }): Promise<ApiResponse<T>> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    method: "POST",
    credentials: "include",
    body: formData,
    signal: options?.signal,
  });
  return response.json();
}

const UPLOAD_TIMEOUT_MS = 25_000;

export async function uploadImage(file: File, purpose: ImageUploadPurpose) {
  let uploadFile: File;
  try {
    uploadFile = await optimizeImageInWorker(file, purpose);
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : "이미지를 처리하지 못했습니다. jpg, png, webp, heic 이미지로 다시 선택해 주세요.";
    return clientError<UploadedSingleImage>(message);
  }
  const formData = new FormData();
  formData.append("file", uploadFile);
  formData.append("purpose", purpose);

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), UPLOAD_TIMEOUT_MS);
  try {
    return await apiPostForm<UploadedSingleImage>("/uploads/images", formData, { signal: controller.signal });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      return clientError<UploadedSingleImage>("업로드 시간이 초과되었습니다. 저장 후 수정 화면에서 다시 추가해 주세요.");
    }
    return clientError<UploadedSingleImage>("이미지 업로드에 실패했습니다. 저장 후 수정 화면에서 다시 시도해 주세요.");
  } finally {
    window.clearTimeout(timeoutId);
  }
}

export function kakaoLoginUrl() {
  return `${API_BASE_URL}/auth/kakao/login`;
}

function optimizeImageInWorker(file: File, purpose: ImageUploadPurpose): Promise<File> {
  if (typeof Worker === "undefined") {
    return optimizeImageForUpload(file, purpose);
  }

  return new Promise((resolve, reject) => {
    const worker = new Worker(new URL("./imageUploadWorker.ts", import.meta.url));
    const fallbackTimer = window.setTimeout(() => {
      worker.terminate();
      reject(new Error("이미지 처리 시간이 초과되었습니다. 다른 사진을 선택해 주세요."));
    }, 30_000);

    worker.onmessage = (event: MessageEvent<{ ok: boolean; file?: File; message?: string }>) => {
      window.clearTimeout(fallbackTimer);
      worker.terminate();
      if (event.data.ok && event.data.file) {
        resolve(event.data.file);
        return;
      }
      optimizeImageForUpload(file, purpose)
        .then(resolve)
        .catch(() => reject(new Error(event.data.message ?? "이미지를 처리하지 못했습니다.")));
    };
    worker.onerror = () => {
      window.clearTimeout(fallbackTimer);
      worker.terminate();
      optimizeImageForUpload(file, purpose).then(resolve).catch(reject);
    };
    worker.postMessage({ file, purpose });
  });
}

async function optimizeImageForUpload(file: File, purpose: ImageUploadPurpose): Promise<File> {
  if (!isSupportedInput(file)) {
    throw new Error("지원하지 않는 이미지입니다. jpg, png, webp, heic 이미지를 선택해 주세요.");
  }
  const profile = imageUploadProfiles[purpose];
  const bitmap = await decodeImage(file);
  const orientedWidth = bitmap.width;
  const orientedHeight = bitmap.height;
  const scale = purpose === "PROFILE"
    ? profile.maxDimension / Math.min(orientedWidth, orientedHeight)
    : Math.min(1, profile.maxDimension / Math.max(orientedWidth, orientedHeight));
  let width = Math.max(1, Math.round(orientedWidth * scale));
  let height = Math.max(1, Math.round(orientedHeight * scale));
  let quality = profile.quality;

  for (let attempt = 0; attempt < 8; attempt += 1) {
    const blob = purpose === "PROFILE"
      ? await canvasToSquareWebp(bitmap, width, height, profile.maxDimension, quality)
      : await canvasToWebp(bitmap, width, height, quality);
    if (blob.size <= TARGET_MAX_IMAGE_BYTES || (quality <= 0.54 && Math.max(width, height) <= 900)) {
      bitmap.close();
      return new File([blob], webpFileName(file.name), { type: "image/webp", lastModified: Date.now() });
    }
    if (quality > 0.54) {
      quality -= 0.08;
    } else {
      width = Math.max(1, Math.round(width * 0.86));
      height = Math.max(1, Math.round(height * 0.86));
    }
  }

  bitmap.close();
  throw new Error("Image compression failed");
}

function isSupportedInput(file: File) {
  const name = file.name.toLowerCase();
  return file.type.startsWith("image/")
    || name.endsWith(".heic")
    || name.endsWith(".heif");
}

async function decodeImage(file: File): Promise<ImageBitmap> {
  try {
    return await createImageBitmap(file, { imageOrientation: "from-image" });
  } catch {
    if (isHeicFile(file)) {
      return decodeHeicImage(file);
    }
    return createImageBitmap(file);
  }
}

function canvasToWebp(bitmap: ImageBitmap, width: number, height: number, quality: number): Promise<Blob> {
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d", { alpha: true });
  if (!context) {
    return Promise.reject(new Error("Canvas is not available"));
  }
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, 0, 0, width, height);
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error("WebP conversion is not available"));
        return;
      }
      resolve(blob);
    }, "image/webp", quality);
  });
}

function canvasToSquareWebp(bitmap: ImageBitmap, width: number, height: number, size: number, quality: number): Promise<Blob> {
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const context = canvas.getContext("2d", { alpha: true });
  if (!context) {
    return Promise.reject(new Error("Canvas is not available"));
  }
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, (size - width) / 2, (size - height) / 2, width, height);
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error("WebP conversion is not available"));
        return;
      }
      resolve(blob);
    }, "image/webp", quality);
  });
}

function webpFileName(fileName: string) {
  const baseName = fileName.replace(/\.[^.]+$/, "");
  return `${baseName || "image"}.webp`;
}

function clientError<T>(message: string): ApiResponse<T> {
  return {
    success: false,
    data: null as T,
    message: null,
    error: {
      code: "CLIENT_IMAGE_UPLOAD_FAILED",
      message,
    },
  };
}
