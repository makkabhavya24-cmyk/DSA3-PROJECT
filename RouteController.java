package com.logistics.controller;

import com.logistics.algorithm.RouteAlternativeResult;
import com.logistics.model.Route;
import com.logistics.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody Route route) {
        try {
            return ResponseEntity.ok(routeService.createRoute(route));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Long id, @RequestBody Route route) {
        try {
            return ResponseEntity.ok(routeService.updateRoute(id, route));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Route> toggleRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.toggleRoute(id));
    }

    @GetMapping("/compare")
    public ResponseEntity<RouteAlternativeResult> compareRoutes(
            @RequestParam String source,
            @RequestParam String destination) {
        return ResponseEntity.ok(routeService.compareRoutes(source, destination));
    }
}
