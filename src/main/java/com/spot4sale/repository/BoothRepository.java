
package com.spot4sale.repository;

import com.spot4sale.entity.Booth;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.*;

public interface BoothRepository extends JpaRepository<Booth, java.util.UUID> {
  List<Booth> findByStoreId(java.util.UUID storeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Booth s where s.id = :id")
  Optional<Booth> findByIdForUpdate(@Param("id") java.util.UUID id);


}
