# Assignment 3 Report: Graph Algorithms

## Complexity Table

| Algorithm | Time | Extra space | Reason |
|---|---|---|---|
| DSU find/union | Amortized O(α(V)) per operation | O(V) total | Union by rank and path compression keep trees shallow. |
| Kruskal | O(E log E + E α(V)) | O(V + E) | Sorting edges dominates, while DSU processes each edge. |
| Prim with a priority queue | O((V + E) log E) | O(V + E) | Each candidate edge may enter and leave the priority queue. |
| Kosaraju | O(V + E) | O(V + E) | Two iterative traversals scan the graph and its reverse. |
| Kahn | O(V + E) | O(V) | Each vertex enters the queue once and each edge updates one in-degree. |
| DAG shortest paths | O(V + E) | O(V) | Topological ordering and one relaxation pass are linear. |

## Correctness: Cut Property

For any cut that separates the vertices into two sets, the minimum-weight edge crossing that cut is safe for a minimum spanning tree. If a spanning tree does not contain this edge, adding it creates a cycle. That cycle must contain another edge crossing the same cut. Replacing that edge with the minimum crossing edge cannot increase the total weight. Kruskal repeatedly chooses the lightest edge that joins two different components, so it never creates a cycle and follows the cut property. Prim grows one tree by choosing the lightest edge from the current tree to an unvisited vertex. This is also a minimum edge crossing the cut between visited and unvisited vertices. Both algorithms therefore build a minimum spanning forest, including separate trees when the graph is disconnected.

## Correctness: DAG Shortest Paths

A topological order places every vertex before all vertices reachable by a single outgoing edge from it. Every path into a vertex therefore comes from predecessors that appear earlier in the order. Sources are initialized to distance zero, which models a virtual source connected to every zero-in-degree vertex. When a vertex is processed, all shortest distances to its predecessors have already been finalized. Relaxing each outgoing edge consequently propagates the best possible distance to its destination. Since a DAG has no directed cycles, no path can return to an earlier vertex in the order. Negative edge weights are safe because there are no cycles that can be repeated to decrease a path cost without bound.

## Which Algorithm When?

| Situation | Choice | Reason |
|---|---|---|
| Sparse undirected graph, edge list | Kruskal | Sorting edges and using DSU is straightforward. |
| Dense undirected graph, adjacency list | Prim | The priority queue grows the tree from adjacent edges. |
| Connectivity under repeated merges | DSU | Fast amortized component queries and unions. |
| Directed graph where SCCs are needed | Kosaraju | Two linear iterative passes find components. |
| Directed acyclic graph | Kahn | Linear-time ordering and explicit cycle detection. |
| DAG shortest paths, including negative weights | DAG shortest paths | A single topological relaxation pass is sufficient. |
| A directed graph may contain cycles | Do not assume a topological order | Kahn detects cycles instead of returning an invalid order. |

## Counters vs Formulas

The operation counters match the theory exactly on all 50 benchmark rows (see `results/results.csv`):

* Kruskal: `edge_scans / m = 1.00`, and `dsu_ops / m` stays between 2.05 and 2.25 (two `find` calls per edge plus n - 1 successful unions), so DSU work is linear in m.
* Prim: `edge_scans / m = 2.00` for every n, because each undirected edge appears in two adjacency lists.
* Kosaraju: `edge_scans / m = 2.00` (one pass over the graph and one over its reverse).
* Kahn: `edge_scans / m = 1.00`.
* DAG shortest paths: `edge_scans / m = 2.00` (the Kahn pass plus the relaxation pass).

The ratios do not grow with n, which confirms the O(V + E) (and O(E log E) comparisons) bounds. Runtime values are machine-dependent, so the operation counters are the stronger reproducible evidence.

## Benchmark Results (n = 100000)

| Algorithm | m = 4n, time (ms) | m = 20n, time (ms) | edge scans / m | other counter |
|---|---|---|---|---|
| Kruskal | 66.0 | 409.9 | 1 | dsu_ops / m ≈ 2.05–2.25 |
| Prim | 174.0 | 1023.2 | 2 | comparisons / m (see plot) |
| Kosaraju | 169.0 | 609.8 | 2 | max stack depth 59501 / 91922 |
| Kahn | 17.1 | 38.7 | 1 | – |
| DagShortestPath | 29.8 | 73.3 | 2 (Kahn pass + relaxation pass) | – |

Generated graphs: `Random(42)`, sizes n = 1000, 3162, 10000, 31623, 100000 and m = 4n and 20n. The undirected graphs are connected (random spanning tree plus random edges), the directed graphs are random, and the DAGs follow a hidden random vertex order. Full data is in `results/results.csv`.

### Plots (in `results/plots/`)

* `time_density_4.png`, `time_density_20.png`: running time of all algorithms against n.
* `mst_kruskal_vs_prim.png`: Kruskal vs Prim for both densities.
* `edge_scans_per_edge.png`: edge scans per edge stay constant.
* `kruskal_dsu_ops_per_edge.png`: DSU operations per edge for Kruskal.
* `mst_comparisons_per_edge.png`: comparisons per edge for Kruskal (sorting) and Prim (priority queue).
* `kosaraju_max_depth.png`: maximum explicit stack depth of the iterative Kosaraju implementation.
* `linear_time_per_element.png`: time / (n + m) for Kosaraju, Kahn and DAG shortest paths, roughly flat.

### Observations

* Kahn and DAG shortest paths are the fastest, as expected for O(V + E) algorithms with a small constant.
* Kruskal beats Prim in this implementation on these random graphs: Prim pushes many `Candidate` objects into a `PriorityQueue`, while Kruskal's cost is one sort plus cheap DSU calls.
* Kosaraju's stack depth reaches tens of thousands of vertices, which is why the implementation is iterative (a recursive DFS would overflow the Java call stack).

## Runtime Notes

The benchmark does one warm-up run and then records the median of five timed runs. Small graphs can show noisy times because JIT compilation, garbage collection and operating-system scheduling are comparable to the measured work (for example the dips at n = 10000). Different hardware and JVM versions change elapsed time without changing the algorithmic operation counts.

## Limitations

* Results depend on the random generator and seed (`Random(42)`) and on the order in which random numbers are consumed.
* Prim uses a lazy-deletion priority queue (O(E log E)), not an indexed heap with decrease-key.
* The DAG shortest-path routine without a source uses a virtual source attached to all in-degree-zero vertices; the `calculate(graph, source, metrics)` overload gives classic single-source distances.
