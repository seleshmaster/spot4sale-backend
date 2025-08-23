// StoreBlackoutRepository.java
package com.spot4sale.repository;
import com.spot4sale.entity.StoreBlackout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface StoreBlackoutRepository extends JpaRepository<StoreBlackout, UUID> {
    List<StoreBlackout> findByStoreIdAndDayBetween(UUID storeId, LocalDate from, LocalDate to);
    boolean existsByStoreIdAndDay(UUID storeId, LocalDate day);
}

