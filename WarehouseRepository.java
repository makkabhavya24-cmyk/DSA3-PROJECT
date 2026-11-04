package com.logistics.repository;

import com.logistics.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
    Optional<Warehouse> findByName(String name);
    boolean existsByCode(String code);
}
