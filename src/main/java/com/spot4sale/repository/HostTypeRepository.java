package com.spot4sale.repository;


import com.spot4sale.entity.HostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostTypeRepository extends JpaRepository<HostType, UUID> {
    Optional<HostType> findByName(String name);
}
