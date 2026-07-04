export type ImageUploadPurpose = "PROFILE" | "READING_RECORD" | "MEETING" | "REVIEW" | "BOOK";

export const TARGET_MAX_IMAGE_BYTES = 950 * 1024;

export const imageUploadProfiles: Record<ImageUploadPurpose, { maxDimension: number; quality: number }> = {
  PROFILE: { maxDimension: 512, quality: 0.82 },
  BOOK: { maxDimension: 900, quality: 0.82 },
  READING_RECORD: { maxDimension: 1280, quality: 0.8 },
  MEETING: { maxDimension: 1600, quality: 0.78 },
  REVIEW: { maxDimension: 1400, quality: 0.78 },
};
