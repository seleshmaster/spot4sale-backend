
package com.spot4sale.repository;

import com.spot4sale.dto.StoreSummaryDTO;
import com.spot4sale.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface HostRepository extends JpaRepository<Host, java.util.UUID> {
    List<Host> findByZipCode(String zip);

    List<Host> findByCityIgnoreCase(String city);


    List<Host> findByOwnerId(UUID ownerId);

    @Query("""
        SELECT new com.spot4sale.dto.StoreSummaryDTO(
            s.id,
            s.name,
            s.address,
            s.city,
            s.zipCode,
            s.thumbnail,
            COALESCE(AVG(r.rating), 0)
        )
        FROM Host s
        LEFT JOIN Review r ON r.targetId = s.id
        WHERE (:city IS NULL OR s.city = :city)
          AND (:zip IS NULL OR s.zipCode = :zip)
        GROUP BY s.id, s.name, s.address, s.city, s.zipCode, s.thumbnail
        ORDER BY s.name
        """)
    List<StoreSummaryDTO> findStoresByCityOrZip(
            @Param("city") String city,
            @Param("zip") String zip
    );


    @Query("""
        SELECT new com.spot4sale.dto.StoreSummaryDTO(
            s.id,
            s.name,
            s.address,
            s.city,
            s.zipCode,
            s.thumbnail,
            COALESCE(AVG(r.rating), 0)
        )
        FROM Host s
        LEFT JOIN Review r ON r.targetId = s.id
        WHERE (:city IS NULL OR s.city = :city)
          AND (:zip IS NULL OR s.zipCode = :zip)
        GROUP BY s.id
    """)
    List<StoreSummaryDTO> searchByCityOrZip(
            @Param("city") String city,
            @Param("zip") String zip

    );




    @Query(value = """
        SELECT
          s.id,
          s.name,
          s.address,
          s.city,
          s.zip_code AS "zipCode",
          s.thumbnail AS "thumbnail",
          COALESCE(AVG(r.rating), 0) AS "averageRating",
          (6371000 * acos(LEAST(1, GREATEST(-1,
              cos(radians(:lat)) * cos(radians(s.latitude)) *
              cos(radians(s.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(s.latitude))
          )))) AS "distanceMeters"
        FROM store s
        LEFT JOIN review r ON s.id = r.target_id
        WHERE s.latitude IS NOT NULL AND s.longitude IS NOT NULL
          AND (6371000 * acos(LEAST(1, GREATEST(-1,
              cos(radians(:lat)) * cos(radians(s.latitude)) *
              cos(radians(s.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(s.latitude))
          )))) <= :radiusMeters
        GROUP BY s.id, s.name, s.address, s.city, s.zip_code, s.thumbnail, s.latitude, s.longitude
        ORDER BY "distanceMeters"
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<StoreSummaryDTO> searchNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
