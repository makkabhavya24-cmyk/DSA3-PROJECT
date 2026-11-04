package com.logistics.algorithm;

import com.logistics.model.Route;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteComparatorTest {

    @Test
    public void testAlternativeRouteComparison() {
        Route directRoute = new Route("RT-DIR", "Hub-A", 1L, "Depot-B", null, "WAREHOUSE_TO_DESTINATION", 20.0, 50, 500, 8, true);
        directRoute.setId(101L);
        Route hop1 = new Route("RT-HOP1", "Hub-A", 1L, "Hub-Mid", 2L, "WAREHOUSE_TO_WAREHOUSE", 8.0, 100, 200, 3, true);
        hop1.setId(102L);
        Route hop2 = new Route("RT-HOP2", "Hub-Mid", 2L, "Depot-B", null, "WAREHOUSE_TO_DESTINATION", 9.0, 80, 250, 4, true);
        hop2.setId(103L);

        // Path 1 (via Hub-Mid): total unit cost = 8 + 9 = 17, distance = 450, time = 7, bottleneck cap = min(100, 80) = 80
        // Path 2 (direct): total unit cost = 20, distance = 500, time = 8, bottleneck cap = 50

        List<Route> routes = Arrays.asList(directRoute, hop1, hop2);
        RouteAlternativeResult result = RouteComparator.compareRoutes(routes, "Hub-A", "Depot-B");

        assertNotNull(result.getBestRoute());
        assertEquals("Hub-A -> Hub-Mid -> Depot-B", result.getBestRoute().getRouteDescription());
        assertEquals(17.0, result.getBestRoute().getTotalUnitCost(), 0.01);
        assertEquals(80, result.getBestRoute().getBottleneckCapacity());

        assertNotNull(result.getSecondBestRoute());
        assertEquals("Hub-A -> Depot-B", result.getSecondBestRoute().getRouteDescription());
        assertEquals(20.0, result.getSecondBestRoute().getTotalUnitCost(), 0.01);
        assertEquals(50, result.getSecondBestRoute().getBottleneckCapacity());
    }
}
