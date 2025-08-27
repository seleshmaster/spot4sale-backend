package com.spot4sale.service;

import com.spot4sale.entity.Review;
import com.spot4sale.entity.Review.TargetType;
import com.spot4sale.entity.User;
import com.spot4sale.repository.ReviewRepository;
import com.spot4sale.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository users;

    // Add a review
    public Review addReview(Review review, Authentication auth) {
        User user = AuthUtils.requireUser(users, auth);
        review.setReviewerId(user.getId());
        return reviewRepository.save(review);
    }

    // Get reviews for a target (store or seller) with pagination
    public Page<Review> getReviews(TargetType targetType, UUID targetId, Pageable pageable) {
        return reviewRepository.findByTargetTypeAndTargetId(targetType, targetId, pageable);
    }

    // Optional: get reviews by reviewer
    public Page<Review> getReviewsByReviewer(UUID reviewerId, Pageable pageable, Authentication auth) {
        User reviewer = AuthUtils.requireUser(users, auth);
        return reviewRepository.findByReviewerId(reviewer.getId(), pageable);
    }

    // Compute average rating for a target
    public Double getAverageRating(TargetType targetType, UUID targetId) {
        return reviewRepository.findByTargetTypeAndTargetId(targetType, targetId, Pageable.unpaged())
                .getContent()                    // get the List<Review> from the Page
                .stream()                         // convert to Stream<Review>
                .mapToDouble(Review::getRating)  // extract rating
                .average()                        // compute average
                .orElse(0.0);                     // default if no reviews
    }

    public Long getReviewCount(TargetType targetType, UUID targetId) {
        return reviewRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }
}

