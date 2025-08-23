package com.spot4sale.repository;

import com.spot4sale.entity.StoreOpenSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StoreOpenSeasonRepository extends JpaRepository<StoreOpenSeason, UUID> {
    List<StoreOpenSeason> findByStoreId(UUID storeId);
    List<StoreOpenSeason> findByStoreIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID storeId, LocalDate dateEnd, LocalDate dateStart);
}
