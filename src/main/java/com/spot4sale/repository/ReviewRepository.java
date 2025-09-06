package com.spot4sale.repository;

import com.spot4sale.entity.Review;
import com.spot4sale.entity.Review.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Fetch reviews for a specific store or seller
    Optional<List<Review>> findByTargetId(UUID id);

    Page<Review> findByTargetTypeAndTargetId(TargetType targetType, UUID targetId, Pageable pageable);

    // Optional: fetch all reviews by a specific reviewer
    Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);

    // Optional: average rating for a target
    Double findAverageRatingByTargetTypeAndTargetId(TargetType targetType, UUID targetId);

    Long countByTargetTypeAndTargetId(TargetType targetType, UUID targetId);
}
