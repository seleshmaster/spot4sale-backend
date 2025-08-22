
package com.spot4sale.controller;

import com.spot4sale.dto.CreateReviewRequest;
import com.spot4sale.entity.Review;
import com.spot4sale.repository.ReviewRepository;
import com.spot4sale.service.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewRepository reviews;
  private final AuthUtils authUtils;

  @PostMapping
  public Review create(@RequestBody @Valid CreateReviewRequest r, org.springframework.security.core.Authentication auth){
    UUID reviewerId = authUtils.currentUserId(auth);
    return reviews.save(new Review(null, reviewerId, r.targetType(), r.targetId(), r.rating(), r.comment(), Instant.now()));
  }

  @GetMapping
  public List<Review> list(@RequestParam String targetType, @RequestParam UUID targetId){
    return reviews.findByTargetTypeAndTargetId(targetType, targetId);
  }
}
