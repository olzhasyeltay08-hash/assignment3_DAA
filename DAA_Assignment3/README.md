# DAA Assignment 3: Graph Algorithms

Implemented in Java 17: DSU, Kruskal, Prim, Kosaraju SCC, Kahn topological sort and DAG shortest paths,
with a benchmark, operation counters, plots and a report.

## Requirements
- JDK 17 or newer
- Maven 3.9 or newer
- Python 3 with `pandas` and `matplotlib` (only for the plots)

## Project layout
```
src/main/java/graph        Graph, Edge
src/main/java/algorithms   DSU, MST, SCC, Topological, DagShortestPath, Metrics, MSTResult
src/main/java/app          Main (runs all algorithms on the files in data/)
src/main/java/benchmark    Benchmark (generates graphs, writes results/results.csv)
src/test/java/algorithms   JUnit 5 tests
data/                      Sample graphs: dag.txt, cyclic.txt, disconnected.txt
scripts/plot_results.py    Builds the plots from results/results.csv
results/                   results.csv and plots/*.png (already generated)
REPORT.md                  Complexity, correctness arguments, results
```

## Build and test
```bash
mvn clean test
```

## Run the sample graphs
```bash
mvn -q -DskipTests package
java -cp target/classes app.Main data
```

## Run the benchmark
```bash
mvn -q -DskipTests package
java -Xmx2g -cp target/classes benchmark.Benchmark
```
The benchmark writes `results/results.csv` (50 rows: 5 sizes x 2 densities x 5 algorithm runs). It also checks that
Kruskal and Prim agree on the minimum spanning forest weight and component count for every generated graph.
The largest case (n = 100000, m = 2000000) needs about 1-2 GB of heap, hence `-Xmx2g`.

## Generate plots
```bash
python -m pip install pandas matplotlib
python scripts/plot_results.py
```
Plots are written to `results/plots/`.

## Input graph format
The first line contains `n m directed|undirected`. Each following line contains `u v w` (vertices are 0-based).

## Algorithms
- DSU with union by rank and path compression
- Kruskal and Prim minimum spanning forest (work on disconnected graphs)
- Iterative Kosaraju SCC (no recursion, safe for 100000-vertex paths)
- Kahn topological ordering with cycle detection
- DAG shortest paths (all-sources or single-source) with parent-based path restoration; negative weights allowed

## Metrics (CSV columns)
`time_ms` median of 5 runs after one warm-up; `dsu_ops` DSU find calls plus successful unions; `edge_scans` adjacency
or edge-list entries examined; `comparisons` comparator calls (sorting in Kruskal, priority queue in Prim);
`max_depth` maximum explicit stack size (Kosaraju); `result` MST weight, SCC count, topological order length or sum of
finite shortest distances.

## AI-use acknowledgement
AI assistance was used to help prepare and review the project structure and implementation. The submitted code should
be reviewed and understood by the student before defense.
