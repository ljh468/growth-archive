package com.growtharchive.controller.interest;

import com.growtharchive.dto.ApiResponse;
import com.growtharchive.repository.InterestTagRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interest-tags")
public class InterestTagController {

    private final InterestTagRepository interestTagRepository;

    public InterestTagController(InterestTagRepository interestTagRepository) {
        this.interestTagRepository = interestTagRepository;
    }

    @GetMapping
    public ApiResponse<List<InterestTagView>> list() {
        return ApiResponse.success(interestTagRepository.findActive().stream()
            .map(row -> new InterestTagView(row.id(), row.name()))
            .toList());
    }

    public record InterestTagView(Long id, String name) {
    }
}
