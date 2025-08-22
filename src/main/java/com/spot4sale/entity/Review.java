
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "review")
public class Review {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false) private UUID reviewerId;
  @Column(nullable=false) private String targetType; // STORE | SELLER
  @Column(nullable=false) private UUID targetId;
  @Column(nullable=false) private Integer rating; // 1..5
  @Column(length=2000) private String comment;
  @Column(nullable=false) private Instant createdAt = Instant.now();
}
