package com.growtharchive.service.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.config.properties.AppProperties;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SupabaseStorageServiceTest {

    @Test
    void rejectsUploadWhenSupabaseIsMissingAndLocalFallbackIsDisabled() {
        AppProperties properties = new AppProperties();
        properties.getStorage().setLocalFallbackEnabled(false);
        SupabaseStorageService service = new SupabaseStorageService(properties);
        OptimizedImage image = new OptimizedImage(new byte[] {1, 2, 3}, "image/jpeg", 3, 1, 1);

        assertThatThrownBy(() -> service.storeImage(1L, "PROFILE", image))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Supabase Storage is not configured");
    }
}
