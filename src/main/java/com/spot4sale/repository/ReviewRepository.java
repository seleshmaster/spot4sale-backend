
package com.spot4sale.repository;

import com.spot4sale.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, java.util.UUID> {
  java.util.List<Review> findByTargetTypeAndTargetId(String targetType, java.util.UUID targetId);
}
