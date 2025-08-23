// StoreWeeklyOpenRepository.java (optional)
package com.spot4sale.repository;
import com.spot4sale.entity.StoreWeeklyOpen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface StoreWeeklyOpenRepository extends JpaRepository<StoreWeeklyOpen, UUID> {
    List<StoreWeeklyOpen> findByStoreId(UUID storeId);
}
