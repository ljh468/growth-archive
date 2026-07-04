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
  const orientation = await readExifOrientation(file);
  const bitmap = await decodeImage(file);
  const orientedWidth = swapsDimensions(orientation) ? bitmap.height : bitmap.width;
  const orientedHeight = swapsDimensions(orientation) ? bitmap.width : bitmap.height;
  const scale = purpose === "PROFILE"
    ? profile.maxDimension / Math.min(orientedWidth, orientedHeight)
    : Math.min(1, profile.maxDimension / Math.max(orientedWidth, orientedHeight));
  let width = Math.max(1, Math.round(orientedWidth * scale));
  let height = Math.max(1, Math.round(orientedHeight * scale));
  let quality = profile.quality;

  for (let attempt = 0; attempt < 8; attempt += 1) {
    const blob = purpose === "PROFILE"
      ? await renderSquareWebp(bitmap, orientation, width, height, profile.maxDimension, quality)
      : await renderWebp(bitmap, orientation, width, height, quality);
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
    return await createImageBitmap(file, { imageOrientation: "none" });
  } catch {
    if (isHeicFile(file)) {
      return decodeHeicImage(file);
    }
    return createImageBitmap(file);
  }
}

async function renderWebp(bitmap: ImageBitmap, orientation: number, width: number, height: number, quality: number): Promise<Blob> {
  const canvas = new OffscreenCanvas(width, height);
  const context = canvas.getContext("2d");
  if (!context) {
    throw new Error("이미지 처리 기능을 사용할 수 없습니다.");
  }
  applyOrientationTransform(context, orientation, width, height);
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, 0, 0, swapsDimensions(orientation) ? height : width, swapsDimensions(orientation) ? width : height);
  return canvas.convertToBlob({ type: "image/webp", quality });
}

async function renderSquareWebp(bitmap: ImageBitmap, orientation: number, width: number, height: number, size: number, quality: number): Promise<Blob> {
  const canvas = new OffscreenCanvas(size, size);
  const context = canvas.getContext("2d");
  if (!context) {
    throw new Error("이미지 처리 기능을 사용할 수 없습니다.");
  }
  applyOrientationTransform(context, orientation, size, size);
  const swapped = swapsDimensions(orientation);
  const drawWidth = swapped ? height : width;
  const drawHeight = swapped ? width : height;
  context.drawImage(bitmap, 0, 0, bitmap.width, bitmap.height, (size - drawWidth) / 2, (size - drawHeight) / 2, drawWidth, drawHeight);
  return canvas.convertToBlob({ type: "image/webp", quality });
}

function applyOrientationTransform(context: OffscreenCanvasRenderingContext2D, orientation: number, width: number, height: number) {
  switch (orientation) {
    case 2:
      context.translate(width, 0);
      context.scale(-1, 1);
      break;
    case 3:
      context.translate(width, height);
      context.rotate(Math.PI);
      break;
    case 4:
      context.translate(0, height);
      context.scale(1, -1);
      break;
    case 5:
      context.rotate(0.5 * Math.PI);
      context.scale(1, -1);
      break;
    case 6:
      context.translate(width, 0);
      context.rotate(0.5 * Math.PI);
      break;
    case 7:
      context.translate(width, height);
      context.rotate(0.5 * Math.PI);
      context.scale(-1, 1);
      break;
    case 8:
      context.translate(0, height);
      context.rotate(-0.5 * Math.PI);
      break;
    default:
      break;
  }
}

function swapsDimensions(orientation: number) {
  return orientation >= 5 && orientation <= 8;
}

async function readExifOrientation(file: File): Promise<number> {
  if (!file.type.includes("jpeg") && !file.name.toLowerCase().match(/\.(jpg|jpeg)$/)) {
    return 1;
  }
  const buffer = await file.slice(0, 64 * 1024).arrayBuffer();
  const view = new DataView(buffer);
  if (view.getUint16(0, false) !== 0xffd8) {
    return 1;
  }
  let offset = 2;
  while (offset + 4 < view.byteLength) {
    const marker = view.getUint16(offset, false);
    offset += 2;
    const length = view.getUint16(offset, false);
    offset += 2;
    if (marker === 0xffe1 && offset + length <= view.byteLength) {
      return parseExifOrientation(view, offset);
    }
    offset += length - 2;
  }
  return 1;
}

function parseExifOrientation(view: DataView, offset: number) {
  const exifHeader = [0x45, 0x78, 0x69, 0x66, 0x00, 0x00];
  for (let index = 0; index < exifHeader.length; index += 1) {
    if (view.getUint8(offset + index) !== exifHeader[index]) {
      return 1;
    }
  }
  const tiffOffset = offset + 6;
  const littleEndian = view.getUint16(tiffOffset, false) === 0x4949;
  const firstIfdOffset = view.getUint32(tiffOffset + 4, littleEndian);
  const entriesOffset = tiffOffset + firstIfdOffset;
  const entries = view.getUint16(entriesOffset, littleEndian);
  for (let index = 0; index < entries; index += 1) {
    const entryOffset = entriesOffset + 2 + index * 12;
    if (view.getUint16(entryOffset, littleEndian) === 0x0112) {
      return view.getUint16(entryOffset + 8, littleEndian);
    }
  }
  return 1;
}

function webpFileName(fileName: string) {
  const baseName = fileName.replace(/\.[^.]+$/, "");
  return `${baseName || "image"}.webp`;
}
