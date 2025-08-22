
package com.spot4sale.repository;

import com.spot4sale.entity.Spot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.*;

public interface SpotRepository extends JpaRepository<Spot, java.util.UUID> {
  List<Spot> findByStoreId(java.util.UUID storeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Spot s where s.id = :id")
  Optional<Spot> findByIdForUpdate(@Param("id") java.util.UUID id);


}
