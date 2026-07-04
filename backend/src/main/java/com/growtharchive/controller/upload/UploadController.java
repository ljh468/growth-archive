package com.growtharchive.controller.upload;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.service.review.UploadedImageView;
import com.growtharchive.service.storage.StorageService;
import com.growtharchive.service.storage.StoredLocalImage;
import com.growtharchive.service.upload.ImageUploadService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final ImageUploadService imageUploadService;
    private final StorageService storageService;

    public UploadController(ImageUploadService imageUploadService, StorageService storageService) {
        this.imageUploadService = imageUploadService;
        this.storageService = storageService;
    }

    @PostMapping("/images")
    public ApiResponse<ImageUploadService.UploadedImageResponse> uploadImage(
        HttpServletRequest request,
        @RequestParam("file") MultipartFile file,
        @RequestParam("purpose") String purpose
    ) {
        return ApiResponse.success(imageUploadService.uploadImage(request, file, purpose));
    }

    @PostMapping("/review-images")
    public ApiResponse<List<UploadedImageView>> uploadReviewImages(
        HttpServletRequest request,
        @RequestParam("files") List<MultipartFile> files
    ) {
        return ApiResponse.success(imageUploadService.uploadReviewImages(request, files));
    }

    @GetMapping("/local")
    public ResponseEntity<byte[]> localImage(@RequestParam("key") String key) throws IOException {
        StoredLocalImage image = storageService.loadLocalImage(key)
            .orElseThrow(() -> new ApiException(ErrorCode.IMAGE_NOT_FOUND));
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .contentType(MediaType.parseMediaType(image.mimeType()))
            .body(image.bytes());
    }
}
