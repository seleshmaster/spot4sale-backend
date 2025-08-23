
package com.spot4sale.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "store")
public class Store {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false) private UUID ownerId;
  @Column(nullable=false) private String name;
  private String description;
  private String address; private String city; private String zipCode;
  private Double latitude; private Double longitude;
  @Nullable private Integer cancellationCutoffHours;
}
