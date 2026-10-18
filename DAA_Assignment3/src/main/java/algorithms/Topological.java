package algorithms;

import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Topological {
    private Topological() {}
    public static List<Integer> kahn(Graph graph, Metrics metrics) {
        int n = graph.size();
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++)
            for (Graph.Arc arc : graph.neighbors(u)) indegree[arc.to()]++;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) queue.add(i);
        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int u = queue.remove();
            order.add(u);
            for (Graph.Arc arc : graph.neighbors(u)) {
                metrics.edgeScans++;
                if (--indegree[arc.to()] == 0) queue.add(arc.to());
            }
        }
        if (order.size() != n) throw new IllegalArgumentException("Graph contains a cycle");
        return order;
    }
}
