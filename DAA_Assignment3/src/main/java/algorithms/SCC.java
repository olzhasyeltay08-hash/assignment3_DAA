package algorithms;

import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class SCC {
    private SCC() {}
    public static List<List<Integer>> kosaraju(Graph graph, Metrics metrics) {
        int n = graph.size();
        boolean[] seen = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            Deque<int[]> stack = new ArrayDeque<>();
            seen[s] = true;
            stack.push(new int[]{s, 0});
            while (!stack.isEmpty()) {
                metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
                int[] top = stack.peek();
                List<Graph.Arc> adj = graph.neighbors(top[0]);
                if (top[1] < adj.size()) {
                    Graph.Arc arc = adj.get(top[1]++);
                    metrics.edgeScans++;
                    if (!seen[arc.to()]) {
                        seen[arc.to()] = true;
                        stack.push(new int[]{arc.to(), 0});
                    }
                } else {
                    order.add(top[0]);
                    stack.pop();
                }
            }
        }
        Graph reversed = graph.reverse();
        Collections.reverse(order);
        java.util.Arrays.fill(seen, false);
        List<List<Integer>> result = new ArrayList<>();
        for (int s : order) {
            if (seen[s]) continue;
            List<Integer> component = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(s);
            seen[s] = true;
            while (!stack.isEmpty()) {
                metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
                int v = stack.pop();
                component.add(v);
                for (Graph.Arc arc : reversed.neighbors(v)) {
                    metrics.edgeScans++;
                    if (!seen[arc.to()]) { seen[arc.to()] = true; stack.push(arc.to()); }
                }
            }
            result.add(component);
        }
        return result;
    }
}
