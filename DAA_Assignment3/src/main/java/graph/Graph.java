package graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public final class Graph {
    public record Arc(int to, int weight) {}
    private final int n;
    private final boolean directed;
    private final List<List<Arc>> adjacency;
    private final List<Edge> edges;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        this.n = n;
        this.directed = directed;
        adjacency = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adjacency.add(new ArrayList<>());
        edges = new ArrayList<>();
    }

    public void addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);
        edges.add(new Edge(u, v, w));
        adjacency.get(u).add(new Arc(v, w));
        if (!directed && u != v) adjacency.get(v).add(new Arc(u, w));
    }

    public int size() { return n; }
    public boolean isDirected() { return directed; }
    public List<Arc> neighbors(int v) { checkVertex(v); return adjacency.get(v); }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public int edgeCount() { return edges.size(); }

    public Graph reverse() {
        Graph result = new Graph(n, true);
        for (Edge e : edges) result.addEdge(e.to(), e.from(), e.weight());
        return result;
    }

    public static Graph read(Path path) throws IOException {
        try (Scanner scanner = new Scanner(Files.newBufferedReader(path))) {
            int n = scanner.nextInt();
            int m = scanner.nextInt();
            String type = scanner.next();
            Graph graph = new Graph(n, type.equalsIgnoreCase("directed"));
            for (int i = 0; i < m; i++) graph.addEdge(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
            return graph;
        }
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("Invalid vertex: " + v);
    }
}
