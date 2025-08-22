
package com.spot4sale.repository;

import com.spot4sale.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface StoreRepository extends JpaRepository<Store, java.util.UUID> {
  java.util.List<Store> findByZipCode(String zip);
  java.util.List<Store> findByCityIgnoreCase(String city);
}
