
package com.spot4sale.repository;

import com.spot4sale.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface StoreRepository extends JpaRepository<Store, java.util.UUID> {
    List<Store> findByZipCode(String zip);

    List<Store> findByCityIgnoreCase(String city);


    List<Store> findByOwnerId(UUID ownerId);

}
