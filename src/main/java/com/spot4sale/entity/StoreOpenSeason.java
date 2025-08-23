package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StoreOpenSeason {
    @Id @GeneratedValue private UUID id;
    @Column(nullable=false) private UUID storeId;
    @Column(nullable=false) private LocalDate startDate;
    @Column(nullable=false) private LocalDate endDate;
    // store as int[] (1..7) or null for all days
    @Column(name="open_weekdays") private int[] openWeekdays;
    private String note;
    // import java.time.OffsetDateTime;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false,                          // 👈 don't include in INSERT
            columnDefinition = "timestamptz default now()"
    )
    private OffsetDateTime createdAt;

}
