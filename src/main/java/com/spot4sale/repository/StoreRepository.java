
package com.spot4sale.repository;

import com.spot4sale.dto.StoreNearbyDTO;
import com.spot4sale.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface StoreRepository extends JpaRepository<Store, java.util.UUID> {
    List<Store> findByZipCode(String zip);

    List<Store> findByCityIgnoreCase(String city);


    List<Store> findByOwnerId(UUID ownerId);


    @Query(value = """
        SELECT
          s.id                         AS id,
          s.name                       AS name,
          s.address                    AS address,
          s.city                       AS city,
          s.zip_code                   AS zipCode,
          s.latitude                   AS latitude,
          s.longitude                  AS longitude,
          (6371000 * acos(LEAST(1, GREATEST(-1,
              cos(radians(:lat)) * cos(radians(s.latitude)) *
              cos(radians(s.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(s.latitude))
          ))))                         AS distanceMeters
        FROM store s
        WHERE s.latitude IS NOT NULL AND s.longitude IS NOT NULL
        AND (6371000 * acos(LEAST(1, GREATEST(-1,
              cos(radians(:lat)) * cos(radians(s.latitude)) *
              cos(radians(s.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(s.latitude))
        )))) <= :radiusMeters
        ORDER BY distanceMeters
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<StoreNearbyDTO> searchNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
