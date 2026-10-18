package algorithms;

import graph.Graph;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraphAlgorithmsTest {
    @Test void dsuUnions() {
        DSU dsu = new DSU(5);
        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));
        assertFalse(dsu.union(0, 2));
        assertEquals(3, dsu.components());
        assertEquals(dsu.find(0), dsu.find(2));
    }

    @Test void dsuRejectsInvalidElement() {
        assertThrows(IllegalArgumentException.class, () -> new DSU(3).find(3));
    }

    @Test void mstAlgorithmsAgreeOnDisconnectedGraph() {
        Graph graph = new Graph(5, false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 8);
        graph.addEdge(3, 4, 1);
        MSTResult k = MST.kruskal(graph, new Metrics()), p = MST.prim(graph, new Metrics());
        assertEquals(7, k.weight());
        assertEquals(k.weight(), p.weight());
        assertEquals(2, k.components());
        assertEquals(2, p.components());
        assertEquals(3, k.edges().size());   // V - components
    }

    @Test void mstOnConnectedGraphHasVMinusOneEdges() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 2); graph.addEdge(2, 3, 3);
        graph.addEdge(0, 3, 10); graph.addEdge(0, 2, 9);
        MSTResult k = MST.kruskal(graph, new Metrics());
        assertEquals(6, k.weight());
        assertEquals(1, k.components());
        assertEquals(3, k.edges().size());
        assertEquals(6, MST.prim(graph, new Metrics()).weight());
    }

    @Test void mstRejectsDirectedGraph() {
        Graph graph = new Graph(2, true);
        assertThrows(IllegalArgumentException.class, () -> MST.kruskal(graph, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> MST.prim(graph, new Metrics()));
    }

    @Test void kruskalCountsOperations() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 2); graph.addEdge(0, 2, 3);
        Metrics metrics = new Metrics();
        MST.kruskal(graph, metrics);
        assertEquals(3, metrics.edgeScans);
        assertTrue(metrics.dsuOps >= 6);
        assertTrue(metrics.comparisons > 0);
    }

    @Test void kosarajuFindsComponents() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 0, 1);
        graph.addEdge(1, 2, 1); graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1); graph.addEdge(3, 4, 1);
        List<List<Integer>> sccs = SCC.kosaraju(graph, new Metrics());
        assertEquals(3, sccs.size());
        Set<Set<Integer>> sets = new HashSet<>();
        for (List<Integer> c : sccs) sets.add(new HashSet<>(c));
        assertEquals(Set.of(Set.of(0, 1), Set.of(2, 3), Set.of(4)), sets);
    }

    @Test void kosarajuOnCycleGivesOneComponent() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 1); graph.addEdge(2, 0, 1);
        assertEquals(1, SCC.kosaraju(graph, new Metrics()).size());
    }

    @Test void kahnProducesValidOrder() {
        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 1); graph.addEdge(0, 2, 1); graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1); graph.addEdge(3, 4, 1);
        List<Integer> order = Topological.kahn(graph, new Metrics());
        assertEquals(5, order.size());
        int[] pos = new int[5];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (var e : graph.edges()) assertTrue(pos[e.from()] < pos[e.to()]);
    }

    @Test void kahnDetectsCycle() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 1); graph.addEdge(2, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> Topological.kahn(graph, new Metrics()));
    }

    @Test void dagShortestPathsAllowNegativeWeights() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 4); graph.addEdge(0, 2, 5);
        graph.addEdge(1, 2, -10); graph.addEdge(2, 3, 2);
        var result = DagShortestPath.calculate(graph, new Metrics());
        assertEquals(-4, result.distances()[3]);
        assertEquals(List.of(0, 1, 2, 3), DagShortestPath.restorePath(3, result.parent()));
    }

    @Test void singleSourceLeavesUnreachableAtInfinity() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 2); graph.addEdge(2, 3, 1);
        var result = DagShortestPath.calculate(graph, 0, new Metrics());
        assertEquals(2, result.distances()[1]);
        assertEquals(DagShortestPath.INF, result.distances()[2]);
        assertEquals(DagShortestPath.INF, result.distances()[3]);
    }

    @Test void iterativeSccHandlesLongPath() {
        Graph graph = new Graph(100000, true);
        for (int i = 0; i < 99999; i++) graph.addEdge(i, i + 1, 1);
        assertEquals(100000, SCC.kosaraju(graph, new Metrics()).size());
    }

    @Test void readsDataFiles() throws IOException {
        Graph dag = Graph.read(Path.of("data", "dag.txt"));
        assertTrue(dag.isDirected());
        assertEquals(5, dag.size());
        var result = DagShortestPath.calculate(dag, new Metrics());
        assertEquals(List.of(0, 1, 2, 4), DagShortestPath.restorePath(4, result.parent()));
        assertEquals(6, result.distances()[4]);

        Graph cyclic = Graph.read(Path.of("data", "cyclic.txt"));
        assertThrows(IllegalArgumentException.class, () -> Topological.kahn(cyclic, new Metrics()));
        assertEquals(2, SCC.kosaraju(cyclic, new Metrics()).size());

        Graph disc = Graph.read(Path.of("data", "disconnected.txt"));
        assertEquals(2, MST.kruskal(disc, new Metrics()).components());
        assertEquals(7, MST.prim(disc, new Metrics()).weight());
    }
}
