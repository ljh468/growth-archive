export function isHeicFile(file: File) {
  const name = file.name.toLowerCase();
  return file.type === "image/heic"
    || file.type === "image/heif"
    || name.endsWith(".heic")
    || name.endsWith(".heif");
}

export async function decodeHeicImage(file: File): Promise<ImageBitmap> {
  try {
    const { heicTo } = await import("heic-to/next");
    return await heicTo({
      blob: file,
      type: "bitmap",
      options: { imageOrientation: "from-image" },
    });
  } catch {
    throw new Error("HEIC 이미지를 변환하지 못했습니다. 사진 앱에서 JPG로 내보낸 뒤 다시 선택해 주세요.");
  }
}
