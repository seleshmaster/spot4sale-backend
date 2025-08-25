// StoreBlackout.java
package com.spot4sale.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StoreBlackout {
    @Id @GeneratedValue private UUID id;
    @Column(nullable=false) private UUID storeId;
    @Column(nullable=false, name="day") private LocalDate date;
    private String reason;
    @Column(nullable=false) private OffsetDateTime createdAt = OffsetDateTime.now();

}


