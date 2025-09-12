package com.spot4sale.repository;

import com.spot4sale.entity.HostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostCategoryRepository extends JpaRepository<HostCategory, UUID> {
    Optional<HostCategory> findByName(String name);
}
