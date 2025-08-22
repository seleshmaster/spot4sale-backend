
package com.spot4sale.repository;

import com.spot4sale.entity.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, java.util.UUID> {

  java.util.List<Booking> findByUserIdOrderByStartDateDesc(java.util.UUID userId);

  @Query("""
    select (count(b) > 0) from Booking b
    where b.spotId = :spotId
      and b.status <> 'CANCELLED'
      and b.startDate <= :endDate
      and b.endDate >= :startDate
  """)
  boolean existsOverlapping(@Param("spotId") java.util.UUID spotId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

  @Query("""
    from Booking b
    where b.spotId = :spotId
      and b.status <> 'CANCELLED'
      and b.startDate <= :endDate
      and b.endDate >= :startDate
    order by b.startDate desc
  """)
  java.util.List<Booking> findOverlapping(@Param("spotId") java.util.UUID spotId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    List<Booking> findByUserId(UUID userId);


    @Query("""
  select count(b) from Booking b
  where b.spotId = :spotId
    and b.status <> 'CANCELLED'
    and b.startDate <= :endDate
    and b.endDate   >= :startDate
""")
    long countOverlapping(UUID spotId, LocalDate startDate, LocalDate endDate);


    @Query("""
  select count(b) from Booking b
  where b.userId = :userId
    and b.spotId = :spotId
    and b.status in ('CONFIRMED','PENDING','PAID')
    and b.startDate <= :endDate
    and b.endDate   >= :startDate
""")
    long countUserOverlap(UUID userId, UUID spotId, LocalDate startDate, LocalDate endDate);



    List<Booking> findBySpotIdInOrderByStartDateDesc(List<UUID> spotIds);



}
