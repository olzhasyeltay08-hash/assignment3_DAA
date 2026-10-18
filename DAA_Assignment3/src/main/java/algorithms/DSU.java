package algorithms;

public final class DSU {
    private final int[] parent;
    private final int[] rank;
    private int components;
    private long findCalls;
    private long successfulUnions;

    public DSU(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        parent = new int[n];
        rank = new int[n];
        components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        findCalls++;
        if (x < 0 || x >= parent.length) throw new IllegalArgumentException("Invalid element");
        int root = x;
        while (root != parent[root]) root = parent[root];
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int a = find(x), b = find(y);
        if (a == b) return false;
        if (rank[a] < rank[b]) parent[a] = b;
        else if (rank[a] > rank[b]) parent[b] = a;
        else { parent[b] = a; rank[a]++; }
        components--;
        successfulUnions++;
        return true;
    }

    public int components() { return components; }
    public long findCalls() { return findCalls; }
    public long successfulUnions() { return successfulUnions; }
}
