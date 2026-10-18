"""Generate plots from results/results.csv into results/plots/.

Usage (from the project root):
    python -m pip install pandas matplotlib
    python scripts/plot_results.py
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd

ROOT = Path(__file__).resolve().parent.parent
CSV = ROOT / "results" / "results.csv"
OUT = ROOT / "results" / "plots"
OUT.mkdir(parents=True, exist_ok=True)

df = pd.read_csv(CSV)
df["edge_scans_per_m"] = df["edge_scans"] / df["m"]
df["dsu_ops_per_m"] = df["dsu_ops"] / df["m"]
df["comparisons_per_m"] = df["comparisons"] / df["m"]
ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]


def lines(ax, column, algos, density, ylabel, title, loglog=True):
    for algo in algos:
        d = df[(df.algorithm == algo) & (df.density == density)].sort_values("n")
        ax.plot(d["n"], d[column], marker="o", label=algo)
    if loglog:
        ax.set_xscale("log")
        ax.set_yscale("log")
    else:
        ax.set_xscale("log")
    ax.set_xlabel("n (vertices)")
    ax.set_ylabel(ylabel)
    ax.set_title(title)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()


def save(fig, name):
    fig.tight_layout()
    fig.savefig(OUT / name, dpi=150)
    plt.close(fig)
    print("saved", OUT / name)


# 1. Running time vs n (one figure per density)
for density in sorted(df.density.unique()):
    fig, ax = plt.subplots(figsize=(7, 5))
    lines(ax, "time_ms", ALGOS, density, "median time (ms)", f"Running time, m = {density}n")
    save(fig, f"time_density_{density}.png")

# 2. MST: Kruskal vs Prim, time for both densities
fig, axes = plt.subplots(1, 2, figsize=(12, 5), sharey=True)
for ax, density in zip(axes, sorted(df.density.unique())):
    lines(ax, "time_ms", ["Kruskal", "Prim"], density, "median time (ms)", f"MST, m = {density}n")
save(fig, "mst_kruskal_vs_prim.png")

# 3. Edge scans per edge (should stay bounded: ~2 for Kosaraju/Prim/Kahn-based, ~1 for Kruskal)
fig, axes = plt.subplots(1, 2, figsize=(12, 5), sharey=True)
for ax, density in zip(axes, sorted(df.density.unique())):
    lines(ax, "edge_scans_per_m", ALGOS, density, "edge scans / m", f"Edge scans per edge, m = {density}n", loglog=False)
save(fig, "edge_scans_per_edge.png")

# 4. DSU operations per edge for Kruskal
fig, ax = plt.subplots(figsize=(7, 5))
for density in sorted(df.density.unique()):
    d = df[(df.algorithm == "Kruskal") & (df.density == density)].sort_values("n")
    ax.plot(d["n"], d["dsu_ops_per_m"], marker="o", label=f"m = {density}n")
ax.set_xscale("log")
ax.set_xlabel("n (vertices)")
ax.set_ylabel("DSU operations / m")
ax.set_title("Kruskal: DSU operations per edge")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
save(fig, "kruskal_dsu_ops_per_edge.png")

# 5. Priority-queue / sort comparisons per edge (Kruskal vs Prim)
fig, axes = plt.subplots(1, 2, figsize=(12, 5), sharey=True)
for ax, density in zip(axes, sorted(df.density.unique())):
    lines(ax, "comparisons_per_m", ["Kruskal", "Prim"], density, "comparisons / m",
          f"Comparisons per edge, m = {density}n", loglog=False)
save(fig, "mst_comparisons_per_edge.png")

# 6. Kosaraju: maximum explicit stack depth (iterative implementation, no recursion)
fig, ax = plt.subplots(figsize=(7, 5))
for density in sorted(df.density.unique()):
    d = df[(df.algorithm == "Kosaraju") & (df.density == density)].sort_values("n")
    ax.plot(d["n"], d["max_depth"], marker="o", label=f"m = {density}n")
ax.set_xscale("log")
ax.set_xlabel("n (vertices)")
ax.set_ylabel("max stack depth")
ax.set_title("Kosaraju: maximum DFS stack depth")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
save(fig, "kosaraju_max_depth.png")

# 7. Time per (n + m): should be roughly flat for the linear algorithms
fig, ax = plt.subplots(figsize=(7, 5))
for algo in ["Kosaraju", "Kahn", "DagShortestPath"]:
    d = df[(df.algorithm == algo) & (df.density == 20)].sort_values("n")
    ax.plot(d["n"], d["time_ms"] * 1000.0 / (d["n"] + d["m"]), marker="o", label=algo)
ax.set_xscale("log")
ax.set_xlabel("n (vertices)")
ax.set_ylabel("microseconds per (n + m)")
ax.set_title("Linear algorithms: time / (n + m), m = 20n")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
save(fig, "linear_time_per_element.png")
