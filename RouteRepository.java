package com.logistics.repository;

import com.logistics.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByActiveTrue();
    List<Route> findBySourceName(String sourceName);
    List<Route> findByDestinationName(String destinationName);
    Optional<Route> findByRouteCode(String routeCode);
}
