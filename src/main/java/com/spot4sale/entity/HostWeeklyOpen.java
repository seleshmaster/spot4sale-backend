// StoreWeeklyOpen.java (optional)
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostWeeklyOpen extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private UUID storeId;
    @Column(nullable = false)
    private int dayOfWeek; // 1=Mon..7=Sun
    @Column(nullable = false)
    private boolean open = true;


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