// StoreWeeklyOpenRepository.java (optional)
package com.spot4sale.repository;
import com.spot4sale.entity.HostWeeklyOpen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface HostWeeklyOpenRepository extends JpaRepository<HostWeeklyOpen, UUID> {
    List<HostWeeklyOpen> findByStoreId(UUID storeId);
}
