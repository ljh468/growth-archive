import { decodeHeicImage, isHeicFile } from "./heicImage";
import { imageUploadProfiles, TARGET_MAX_IMAGE_BYTES, type ImageUploadPurpose } from "./imageUploadConfig";

type OptimizeRequest = {
  file: File;
  purpose: ImageUploadPurpose;
};

self.onmessage = async (event: MessageEvent<OptimizeRequest>) => {
  try {
    const { file, purpose } = event.data;
    const optimized = await optimizeImageForUpload(file, purpose);
    self.postMessage({ ok: true, file: optimized });
  } catch (error) {
    self.postMessage({ ok: false, message: error instanceof Error ? error.message : "Image optimization failed" });
  }
};

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
      ? await renderSquareWebp(bitmap, width, height, profile.maxDimension, quality)
      : await renderWebp(bitmap, width, height, quality);
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
  throw new Error("이미지를 충분히 압축하지 못했습니다. 다른 사진을 선택해 주세요.");
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

async function renderWebp(bitmap: ImageBitmap, width: number, height: number, quality: number): Promise<Blob> {
  const canvas = new OffscreenCanvas(width, height);
  const context = canvas.getContext("2d");
  if (!context) {
    throw new Error("이미지 처리 기능을 사용할 수 없습니다.");
  }
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, 0, 0, width, height);
  return canvas.convertToBlob({ type: "image/webp", quality });
}

async function renderSquareWebp(bitmap: ImageBitmap, width: number, height: number, size: number, quality: number): Promise<Blob> {
  const canvas = new OffscreenCanvas(size, size);
  const context = canvas.getContext("2d");
  if (!context) {
    throw new Error("이미지 처리 기능을 사용할 수 없습니다.");
  }
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, (size - width) / 2, (size - height) / 2, width, height);
  return canvas.convertToBlob({ type: "image/webp", quality });
}

function webpFileName(fileName: string) {
  const baseName = fileName.replace(/\.[^.]+$/, "");
  return `${baseName || "image"}.webp`;
}
