// StoreWeeklyOpen.java (optional)
package com.spot4sale.entity;
import jakarta.persistence.*; import lombok.*; import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HostWeeklyOpen {
    @Id @GeneratedValue private UUID id;
    @Column(nullable=false) private UUID storeId;
    @Column(nullable=false) private int dayOfWeek; // 1=Mon..7=Sun
    @Column(nullable=false) private boolean open = true;
}