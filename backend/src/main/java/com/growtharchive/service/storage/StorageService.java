package com.growtharchive.service.storage;

import java.io.IOException;

public interface StorageService {

    StoredImage storeReviewImage(Long memberId, OptimizedImage image) throws IOException;
}
