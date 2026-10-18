package algorithms;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Minimum spanning forest algorithms for undirected graphs. */
public final class MST {
    private MST() {}

    public static MSTResult kruskal(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        List<Edge> edges = new ArrayList<>(graph.edges());
        // every comparison made by the sort is counted
        edges.sort((a, b) -> { metrics.comparisons++; return Integer.compare(a.weight(), b.weight()); });
        DSU dsu = new DSU(graph.size());
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        for (Edge edge : edges) {
            metrics.edgeScans++;
            if (dsu.union(edge.from(), edge.to())) {
                chosen.add(edge);
                total += edge.weight();
            }
        }
        metrics.dsuOps += dsu.findCalls() + dsu.successfulUnions();
        return new MSTResult(total, dsu.components(), List.copyOf(chosen));
    }

    public static MSTResult prim(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        int n = graph.size();
        boolean[] used = new boolean[n];
        record Candidate(int from, int to, int weight) {}
        PriorityQueue<Candidate> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (used[start]) continue;
            components++;
            used[start] = true;
            for (Graph.Arc arc : graph.neighbors(start)) {
                metrics.edgeScans++;
                queue.add(new Candidate(start, arc.to(), arc.weight()));
            }
            while (!queue.isEmpty()) {
                Candidate c = queue.poll();
                if (used[c.to()]) continue;
                used[c.to()] = true;
                chosen.add(new Edge(c.from(), c.to(), c.weight()));
                total += c.weight();
                for (Graph.Arc arc : graph.neighbors(c.to())) {
                    metrics.edgeScans++;
                    if (!used[arc.to()]) queue.add(new Candidate(c.to(), arc.to(), arc.weight()));
                }
            }
        }
        return new MSTResult(total, components, List.copyOf(chosen));
    }

    private static void requireUndirected(Graph graph) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
    }
}
