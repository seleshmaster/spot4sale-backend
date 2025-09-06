// StoreBlackoutRepository.java
package com.spot4sale.repository;
import com.spot4sale.entity.HostBlackout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface HostBlackoutRepository extends JpaRepository<HostBlackout, UUID> {
    List<HostBlackout> findByStoreIdAndDateBetween(UUID storeId, LocalDate from, LocalDate to);
    List<HostBlackout> findByStoreId(UUID storeId);
    // boolean existsByStoreIdAndDate(UUID storeId, LocalDate date);
}


