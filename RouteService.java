package com.logistics.service;

import com.logistics.algorithm.RouteAlternativeResult;
import com.logistics.algorithm.RouteComparator;
import com.logistics.model.Route;
import com.logistics.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
    }

    @Transactional
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    @Transactional
    public Route updateRoute(Long id, Route updated) {
        Route existing = getRouteById(id);
        existing.setRouteCode(updated.getRouteCode());
        existing.setSourceName(updated.getSourceName());
        existing.setDestinationName(updated.getDestinationName());
        existing.setRouteType(updated.getRouteType());
        existing.setUnitCost(updated.getUnitCost());
        existing.setCapacity(updated.getCapacity());
        existing.setDistanceKm(updated.getDistanceKm());
        existing.setTravelTimeHours(updated.getTravelTimeHours());
        existing.setActive(updated.isActive());
        return routeRepository.save(existing);
    }

    @Transactional
    public Route toggleRoute(Long id) {
        Route route = getRouteById(id);
        route.setActive(!route.isActive());
        return routeRepository.save(route);
    }

    /**
     * Compares the cheapest best route vs second-best alternative route
     * between two nodes in the current network.
     */
    public RouteAlternativeResult compareRoutes(String sourceName, String destinationName) {
        List<Route> allRoutes = routeRepository.findAll();
        return RouteComparator.compareRoutes(allRoutes, sourceName, destinationName);
    }
}
