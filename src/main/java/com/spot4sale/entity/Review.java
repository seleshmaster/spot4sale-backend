package com.spot4sale.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "review",
        indexes = {
                @Index(name = "idx_review_target", columnList = "targetType,targetId")
        }
)
public class Review {

    public enum TargetType { STORE, SELLER }

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID reviewerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @NotNull
    @Column(nullable = false)
    private UUID targetId;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
