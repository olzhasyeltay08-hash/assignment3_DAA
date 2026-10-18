package algorithms;

import graph.Graph;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class DagShortestPath {
    public static final long INF = Long.MAX_VALUE / 4;

    public record Result(long[] distances, int[] parent, long sum) {}

    private DagShortestPath() {}

    /** Shortest distances from a virtual source joined to every vertex with in-degree zero. */
    public static Result calculate(Graph graph, Metrics metrics) {
        int n = graph.size();
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++)
            for (Graph.Arc arc : graph.neighbors(u)) indegree[arc.to()]++;
        long[] distance = new long[n];
        Arrays.fill(distance, INF);
        for (int i = 0; i < n; i++) if (indegree[i] == 0) distance[i] = 0;
        return relax(graph, distance, metrics);
    }

    /** Single-source shortest distances; vertices that are unreachable keep distance INF. */
    public static Result calculate(Graph graph, int source, Metrics metrics) {
        if (source < 0 || source >= graph.size()) throw new IllegalArgumentException("Invalid source: " + source);
        long[] distance = new long[graph.size()];
        Arrays.fill(distance, INF);
        distance[source] = 0;
        return relax(graph, distance, metrics);
    }

    private static Result relax(Graph graph, long[] distance, Metrics metrics) {
        List<Integer> order = Topological.kahn(graph, metrics);
        int[] parent = new int[graph.size()];
        Arrays.fill(parent, -1);
        for (int u : order) {
            if (distance[u] == INF) continue;
            for (Graph.Arc arc : graph.neighbors(u)) {
                metrics.edgeScans++;
                if (distance[u] + arc.weight() < distance[arc.to()]) {
                    distance[arc.to()] = distance[u] + arc.weight();
                    parent[arc.to()] = u;
                }
            }
        }
        long sum = 0;
        for (long d : distance) if (d != INF) sum += d;
        return new Result(distance, parent, sum);
    }

    public static List<Integer> restorePath(int target, int[] parent) {
        LinkedList<Integer> path = new LinkedList<>();
        for (int v = target; v != -1; v = parent[v]) path.addFirst(v);
        return path;
    }
}
