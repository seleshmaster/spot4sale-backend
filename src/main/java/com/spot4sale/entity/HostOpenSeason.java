// src/main/java/com/spot4sale/entity/StoreOpenSeason.java
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "host_open_season")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostOpenSeason extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // stored as CSV string in DB (e.g. "1,2,3,4,5")
    @Column(name = "open_weekdays") // VARCHAR
    private String openWeekdaysCsv;

    @Column(name = "note")
    private String note;

    @Transient
    public List<Integer> getOpenWeekdays() {
        if (openWeekdaysCsv == null || openWeekdaysCsv.isBlank()) return List.of();
        return Arrays.stream(openWeekdaysCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void setOpenWeekdays(List<Integer> days) {
        if (days == null || days.isEmpty()) {
            this.openWeekdaysCsv = null;
        } else {
            this.openWeekdaysCsv = days.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
    }


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
