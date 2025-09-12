package com.spot4sale.repository;

import com.spot4sale.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    // No extra methods needed because findAllById(Iterable<ID>) is already provided by JpaRepository
}
