
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking",
        indexes = {@Index(name = "ix_booking_spot_dates", columnList = "spotId,startDate,endDate")})
public class Booking extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private UUID spotId;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false)
    private String status;
    // PENDING | CONFIRMED | CANCELLED
    @Column(name = "total_price")   // <-- add this to be explicit
    private BigDecimal totalPrice;
    @Column(name = "payment_intent_id")
    private String paymentIntentId;
    @Column(name = "refund_id")
    private String refundId;
    @Column(name = "refunded_at")
    private Instant refundedAt;
    @Column(name = "refund_amount_cents")
    private BigDecimal refundAmountCents;
    @Column(name = "cancel_reason")
    private String cancelReason;


    @PrePersist
    protected void onCreate() {
        createdAt = java.time.Instant.now();
        updatedAt = java.time.Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.Instant.now();
    }

}
