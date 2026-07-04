package com.growtharchive.service.upload;

import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.service.storage.StorageService;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImageAssetCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImageAssetCleanupScheduler.class);
    private static final int BATCH_SIZE = 200;

    private final ImageAssetRepository imageAssetRepository;
    private final StorageService storageService;

    public ImageAssetCleanupScheduler(ImageAssetRepository imageAssetRepository, StorageService storageService) {
        this.imageAssetRepository = imageAssetRepository;
        this.storageService = storageService;
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void cleanupOrphanImages() {
        cleanupOrphanImages(OffsetDateTime.now().minusHours(24));
    }

    @Transactional
    public int cleanupOrphanImages(OffsetDateTime threshold) {
        int deleted = 0;
        for (ImageAssetRepository.OrphanImageAsset image : imageAssetRepository.findUnlinkedImagesCreatedBefore(threshold, BATCH_SIZE)) {
            try {
                storageService.deleteImage(image.bucket(), image.objectKey());
                imageAssetRepository.delete(image.id());
                deleted += 1;
            } catch (IOException exception) {
                log.warn("Failed to delete orphan image asset. imageId={}, objectKey={}", image.id(), image.objectKey(), exception);
            }
        }
        if (deleted > 0) {
            log.info("Deleted orphan image assets. count={}", deleted);
        }
        return deleted;
    }
}
