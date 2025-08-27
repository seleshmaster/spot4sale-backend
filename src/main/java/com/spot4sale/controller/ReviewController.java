package com.spot4sale.controller;

import com.spot4sale.entity.Review;
import com.spot4sale.entity.Review.TargetType;
import com.spot4sale.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@RequestBody Review review, Authentication auth) {
        return reviewService.addReview(review, auth);
    }

    @GetMapping("/{targetType}/{targetId}")
    public Page<Review> getReviews(
            @PathVariable String targetType,
            @PathVariable String targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Authentication auth
    ) {
        TargetType enumType = TargetType.valueOf(targetType.toUpperCase());
        UUID id = UUID.fromString(targetId);
        return reviewService.getReviews(enumType, id, PageRequest.of(page, size));
    }

    @GetMapping("/average/{targetType}/{targetId}")
    public Double getAverageRating(
            @PathVariable String targetType,
            @PathVariable String targetId, Authentication auth
    ) {
        TargetType enumType = TargetType.valueOf(targetType.toUpperCase());
        UUID id = UUID.fromString(targetId);
        return reviewService.getAverageRating(enumType, id);
    }

    @GetMapping("/count/{targetType}/{targetId}")
    public Long getReviewCount(
            @PathVariable String targetType,
            @PathVariable String targetId

    ) {
        TargetType enumType = TargetType.valueOf(targetType.toUpperCase());
        UUID id = UUID.fromString(targetId);
        return reviewService.getReviewCount(enumType, id);
    }
}
