
package com.spot4sale.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store")
public class Host {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private UUID ownerId;
    @Column(nullable = false)
    private String name;
    private String description;
    private String address;
    private String city;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    @Nullable
    private Integer cancellationCutoffHours;
    // Multiple images (URLs)
    @Column(name = "images", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Nullable
    private String[] images;

    // Single thumbnail image (URL)
    @Nullable
    private String thumbnail;

    // Store characteristics (JSON key-value)
    @Column(columnDefinition = "jsonb")
    @Nullable
    private String characteristics; // stored as JSON string
}
