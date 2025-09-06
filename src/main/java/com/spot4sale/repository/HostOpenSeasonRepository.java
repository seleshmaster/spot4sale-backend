package com.spot4sale.repository;

import com.spot4sale.entity.HostOpenSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HostOpenSeasonRepository extends JpaRepository<HostOpenSeason, UUID> {
    List<HostOpenSeason> findByStoreId(UUID storeId);
    List<HostOpenSeason> findByStoreIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID storeId, LocalDate dateStart, LocalDate dateEnd);


    boolean existsByStoreIdAndEndDateGreaterThanEqual(UUID storeId, LocalDate date);


    @Query("""
         select s from HostOpenSeason s
         where s.storeId = :storeId
           and s.endDate >= :from
           and s.startDate <= :to
         order by s.startDate asc
         """)
    List<HostOpenSeason> findOverlapping(UUID storeId, LocalDate from, LocalDate to);



}
