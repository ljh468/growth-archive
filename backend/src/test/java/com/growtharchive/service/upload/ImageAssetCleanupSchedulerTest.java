package com.growtharchive.service.upload;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.service.storage.StorageService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImageAssetCleanupSchedulerTest {

    private final ImageAssetRepository imageAssetRepository = mock(ImageAssetRepository.class);
    private final StorageService storageService = mock(StorageService.class);
    private final ImageAssetCleanupScheduler scheduler = new ImageAssetCleanupScheduler(imageAssetRepository, storageService);

    @Test
    void deletesDatabaseRowAfterStorageObjectIsDeleted() throws Exception {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(24);
        when(imageAssetRepository.findUnlinkedImagesCreatedBefore(threshold, 200))
            .thenReturn(List.of(new ImageAssetRepository.OrphanImageAsset(1L, "images", "local/review.webp")));

        scheduler.cleanupOrphanImages(threshold);

        verify(storageService).deleteImage("images", "local/review.webp");
        verify(imageAssetRepository).delete(1L);
    }

    @Test
    void keepsDatabaseRowWhenStorageDeleteFails() throws Exception {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(24);
        when(imageAssetRepository.findUnlinkedImagesCreatedBefore(threshold, 200))
            .thenReturn(List.of(new ImageAssetRepository.OrphanImageAsset(2L, "images", "local/profile.webp")));
        doThrow(new IOException("storage failed")).when(storageService).deleteImage("images", "local/profile.webp");

        scheduler.cleanupOrphanImages(threshold);

        verify(imageAssetRepository, never()).delete(2L);
    }
}
