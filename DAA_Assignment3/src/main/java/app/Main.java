package app;

import algorithms.*;
import graph.Edge;
import graph.Graph;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runs every algorithm that applies to each graph file found in a data directory. */
public final class Main {
    private Main() {}

    public static void main(String[] args) throws IOException {
        Path dir = Path.of(args.length > 0 ? args[0] : "data");
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
            stream.forEach(files::add);
        }
        Collections.sort(files);
        for (Path file : files) run(file);
    }

    private static void run(Path file) throws IOException {
        Graph graph = Graph.read(file);
        System.out.printf("=== %s (n=%d, m=%d, %s)%n", file.getFileName(), graph.size(),
                graph.edgeCount(), graph.isDirected() ? "directed" : "undirected");
        if (!graph.isDirected()) {
            Metrics km = new Metrics(), pm = new Metrics();
            MSTResult k = MST.kruskal(graph, km), p = MST.prim(graph, pm);
            System.out.printf("Kruskal: weight=%d, components=%d, edges=%s%n", k.weight(), k.components(), format(k.edges()));
            System.out.printf("Prim:    weight=%d, components=%d, edges=%s%n", p.weight(), p.components(), format(p.edges()));
            System.out.println("Both algorithms agree: " + (k.weight() == p.weight() && k.components() == p.components()));
            return;
        }
        System.out.println("SCCs (Kosaraju): " + SCC.kosaraju(graph, new Metrics()));
        try {
            System.out.println("Topological order (Kahn): " + Topological.kahn(graph, new Metrics()));
            DagShortestPath.Result r = DagShortestPath.calculate(graph, new Metrics());
            System.out.println("DAG shortest distances: " + java.util.Arrays.toString(r.distances()));
            for (int v = 0; v < graph.size(); v++)
                System.out.println("  path to " + v + ": " + DagShortestPath.restorePath(v, r.parent()));
        } catch (IllegalArgumentException e) {
            System.out.println("Topological order / DAG shortest paths skipped: " + e.getMessage());
        }
    }

    private static String format(List<Edge> edges) {
        StringBuilder sb = new StringBuilder("[");
        for (Edge e : edges) sb.append(e.from()).append('-').append(e.to()).append('(').append(e.weight()).append(") ");
        return sb.toString().trim() + "]";
    }
}
