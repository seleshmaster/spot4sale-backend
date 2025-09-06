
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "spot")
public class Booth {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false) private UUID storeId;
  @Column(nullable=false) private Double pricePerDay;
  @Column(nullable=false) private Boolean available = true;
}
