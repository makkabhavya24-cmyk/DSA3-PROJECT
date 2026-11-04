package com.logistics.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class McmfSolverTest {

    @Test
    public void testSimpleMcmfFlow() {
        // Node 0: Source, Node 1: Warehouse A, Node 2: Warehouse B, Node 3: Sink
        FlowNetwork network = new FlowNetwork(4);
        network.registerNode(0, "Source");
        network.registerNode(1, "Warehouse A");
        network.registerNode(2, "Warehouse B");
        network.registerNode(3, "Sink");

        // Edge Source -> Warehouse A (cap 50, cost 0)
        network.addEdge(0, 1, 50, 0, null, "Source", "A", 0, 0);
        // Edge Source -> Warehouse B (cap 50, cost 0)
        network.addEdge(0, 2, 50, 0, null, "Source", "B", 0, 0);

        // Edge A -> Sink (cap 30, cost 10)
        network.addEdge(1, 3, 30, 10.0, 1L, "A", "Sink", 100, 2);
        // Edge B -> Sink (cap 40, cost 15)
        network.addEdge(2, 3, 40, 15.0, 2L, "B", "Sink", 150, 3);

        // Request 50 units
        // Cheapest route is through A (30 units @ $10 = $300), then remaining 20 units through B (20 units @ $15 = $300)
        // Total cost should be $600 for 50 units.
        McmfSolver.McmfResult result = McmfSolver.solve(network, 0, 3, 50);

        assertEquals(50, result.getMaxFlow(), "Should fulfill all 50 units");
        assertEquals(0, result.getUnfulfilledDemand(), "Unfulfilled should be 0");
        assertEquals(600.0, result.getTotalCost(), 0.01, "Total cost should be 30*10 + 20*15 = 600.0");
    }

    @Test
    public void testCapacityBottleneckWithUnfulfilledDemand() {
        // Source -> A (cap 20, cost 0)
        // A -> Sink (cap 20, cost 10)
        // Requested 35 units
        FlowNetwork network = new FlowNetwork(3);
        network.registerNode(0, "Source");
        network.registerNode(1, "Warehouse A");
        network.registerNode(2, "Sink");

        network.addEdge(0, 1, 20, 0, null, "Source", "A", 0, 0);
        network.addEdge(1, 2, 20, 10.0, 1L, "A", "Sink", 100, 2);

        McmfSolver.McmfResult result = McmfSolver.solve(network, 0, 2, 35);

        assertEquals(20, result.getMaxFlow(), "Max flow should be bounded by 20");
        assertEquals(15, result.getUnfulfilledDemand(), "Remaining demand should be 15");
        assertEquals(200.0, result.getTotalCost(), 0.01, "Cost should be 20*10 = 200.0");
        assertFalse(result.isFullyFulfilled(), "Should report not fully fulfilled");
    }
}
