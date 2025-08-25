package com.spot4sale.repository;

import com.spot4sale.entity.StoreOpenSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StoreOpenSeasonRepository extends JpaRepository<StoreOpenSeason, UUID> {
    List<StoreOpenSeason> findByStoreId(UUID storeId);
    List<StoreOpenSeason> findByStoreIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID storeId, LocalDate dateStart, LocalDate dateEnd);


    boolean existsByStoreIdAndEndDateGreaterThanEqual(UUID storeId, LocalDate date);


    @Query("""
         select s from StoreOpenSeason s
         where s.storeId = :storeId
           and s.endDate >= :from
           and s.startDate <= :to
         order by s.startDate asc
         """)
    List<StoreOpenSeason> findOverlapping(UUID storeId, LocalDate from, LocalDate to);



}
