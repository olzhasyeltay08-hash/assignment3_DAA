package algorithms;

import graph.Edge;
import java.util.List;

public record MSTResult(long weight, int components, List<Edge> edges) {}
