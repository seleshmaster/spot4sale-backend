package com.spot4sale.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "host")
public class Host extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "host_amenities",
            joinColumns = @JoinColumn(name = "host_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities;

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

    @ManyToOne
    @JoinColumn(name = "host_type_id")
    private HostType hostType;

    @ManyToOne
    @JoinColumn(name = "host_category_id")
    private HostCategory hostCategory;

    // --- NEW FIELDS ---
    @Nullable
    private Double defaultPrice;

    @Column(columnDefinition = "text")
    @Nullable
    private String defaultAmenities;

    @Nullable
    private Integer maxBooths;

    @Column(columnDefinition = "jsonb")
    @Nullable
    private String operatingHours; // stored as JSON string

    @Nullable
    private String contactEmail;

    @Nullable
    private String contactPhone;

    @Column(name = "tags", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] tags; //comma separated or JSON array

    @Nullable
    private Integer footTrafficEstimate;

    @Column(columnDefinition = "text")
    @Nullable
    private String cancellationPolicy;

    @Nullable
    private Integer bookingWindowDays;

    @Column(nullable = false)
    private Boolean active = true;


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
