package com.growtharchive.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.growtharchive.dto.ApiResponse;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void healthReturnsEnvelope() {
        HealthController controller = new HealthController();

        ApiResponse<HealthController.HealthResponse> response = controller.health();

        assertThat(response.success()).isTrue();
        assertThat(response.data().status()).isEqualTo("UP");
        assertThat(response.data().service()).isEqualTo("growth-archive-backend");
        assertThat(response.message()).isNull();
        assertThat(response.error()).isNull();
    }
}
