package benchmark;

import algorithms.*;
import graph.Graph;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Runs all algorithms on generated graphs and writes results/results.csv. */
public final class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int[] FACTORS = {4, 20};   // m = factor * n
    private static final int RUNS = 5;
    private Benchmark() {}

    public static void main(String[] args) throws Exception {
        Path out = Path.of("results");
        Files.createDirectories(out.resolve("plots"));
        try (BufferedWriter writer = Files.newBufferedWriter(out.resolve("results.csv"))) {
            writer.write("algorithm,graph,n,m,density,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result\n");
            Random random = new Random(42);
            for (int n : SIZES) {
                for (int factor : FACTORS) {
                    int m = n * factor;
                    runUndirected(writer, n, m, random);
                    runDirected(writer, n, m, random);
                    runDag(writer, n, m, random);
                    writer.flush();
                    System.out.println("done n=" + n + " m=" + m);
                }
            }
        }
        System.out.println("Wrote " + out.resolve("results.csv"));
    }

    private static void runUndirected(BufferedWriter writer, int n, int m, Random random) throws Exception {
        Graph graph = generateUndirected(n, m, random);
        MSTResult kruskal = MST.kruskal(graph, new Metrics());
        MSTResult prim = MST.prim(graph, new Metrics());
        if (kruskal.weight() != prim.weight() || kruskal.components() != prim.components())
            throw new IllegalStateException("MST algorithms disagree for n=" + n + ", m=" + m);
        for (boolean useKruskal : new boolean[]{true, false}) {
            double time = median(() -> useKruskal ? MST.kruskal(graph, new Metrics()) : MST.prim(graph, new Metrics()));
            Metrics metrics = new Metrics();
            MSTResult result = useKruskal ? MST.kruskal(graph, metrics) : MST.prim(graph, metrics);
            write(writer, useKruskal ? "Kruskal" : "Prim", "undirected", n, m, time, metrics, result.weight());
        }
    }

    private static void runDirected(BufferedWriter writer, int n, int m, Random random) throws Exception {
        Graph graph = generateDirected(n, m, random);
        double time = median(() -> SCC.kosaraju(graph, new Metrics()));
        Metrics metrics = new Metrics();
        int count = SCC.kosaraju(graph, metrics).size();
        write(writer, "Kosaraju", "directed", n, m, time, metrics, count);
    }

    private static void runDag(BufferedWriter writer, int n, int m, Random random) throws Exception {
        Graph dag = generateDag(n, m, random);
        double topoTime = median(() -> Topological.kahn(dag, new Metrics()));
        Metrics topoMetrics = new Metrics();
        int topoCount = Topological.kahn(dag, topoMetrics).size();
        write(writer, "Kahn", "dag", n, m, topoTime, topoMetrics, topoCount);
        double pathTime = median(() -> DagShortestPath.calculate(dag, new Metrics()));
        Metrics pathMetrics = new Metrics();
        long pathSum = DagShortestPath.calculate(dag, pathMetrics).sum();
        write(writer, "DagShortestPath", "dag", n, m, pathTime, pathMetrics, pathSum);
    }

    /** One warm-up run, then the median of RUNS timed runs, in milliseconds. */
    private static double median(Callable call) throws Exception {
        call.run();
        double[] times = new double[RUNS];
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            call.run();
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
        }
        Arrays.sort(times);
        return times[RUNS / 2];
    }

    @FunctionalInterface private interface Callable { Object run() throws Exception; }

    private static void write(BufferedWriter writer, String algorithm, String graph, int n, int m,
                              double time, Metrics metrics, long result) throws Exception {
        writer.write(String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm, graph, n, m, m / n, time, metrics.dsuOps, metrics.edgeScans,
                metrics.comparisons, metrics.maxDepth, result));
        writer.newLine();
    }

    /** Random connected undirected graph: a random spanning tree plus random extra edges. */
    static Graph generateUndirected(int n, int m, Random random) {
        Graph graph = new Graph(n, false);
        int[] perm = permutation(n, random);
        int count = 0;
        for (int i = 1; i < n && count < m; i++) {
            graph.addEdge(perm[random.nextInt(i)], perm[i], 1 + random.nextInt(1000));
            count++;
        }
        while (count < m) {
            int a = random.nextInt(n), b = random.nextInt(n);
            if (b != a) { graph.addEdge(a, b, 1 + random.nextInt(1000)); count++; }
        }
        return graph;
    }

    static Graph generateDirected(int n, int m, Random random) {
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) graph.addEdge(random.nextInt(n), random.nextInt(n), 1 + random.nextInt(1000));
        return graph;
    }

    /** Random DAG: every edge goes from an earlier to a later vertex of a hidden random order. */
    static Graph generateDag(int n, int m, Random random) {
        Graph graph = new Graph(n, true);
        int[] rank = permutation(n, random), inverse = new int[n];
        for (int i = 0; i < n; i++) inverse[rank[i]] = i;
        int count = 0;
        while (count < m) {
            int a = random.nextInt(n), b = random.nextInt(n);
            if (a == b) continue;
            if (inverse[a] > inverse[b]) { int t = a; a = b; b = t; }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
            count++;
        }
        return graph;
    }

    private static int[] permutation(int n, Random random) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) values[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), t = values[i];
            values[i] = values[j]; values[j] = t;
        }
        return values;
    }
}
