package com.growtharchive.service.storage;

import java.io.IOException;
import java.util.Optional;

public interface StorageService {

    StoredImage storeImage(Long memberId, String imageType, OptimizedImage image) throws IOException;

    Optional<StoredLocalImage> loadLocalImage(String objectKey) throws IOException;
}
