// Cutting a dendrogram at a similarity (or distance) level to define clusters, e.g. PFGE pulsotypes.
import type { ClusterNode } from './cluster';

export interface TreeCut {
  /** Cluster number (1-based, numbered in dendrogram order) of every leaf, indexed by leaf index. */
  groups: number[];
  /** Number of clusters, singletons included. */
  count: number;
  /** Number of leaves of each cluster (index = cluster number - 1). */
  sizes: number[];
}

/**
 * Cuts the tree at distance `height`: every maximal subtree whose merge height is at most `height`
 * becomes a cluster (single leaves above the cut are singletons).
 */
export function cutTree(tree: ClusterNode, height: number): TreeCut {
  const groups: number[] = [];
  const sizes: number[] = [];
  const assign = (n: ClusterNode, g: number) => {
    if (!n.left || !n.right) groups[n.index] = g;
    else {
      assign(n.left, g);
      assign(n.right, g);
    }
  };
  const rec = (n: ClusterNode) => {
    if (!n.left || !n.right || n.height <= height + 1e-12) {
      sizes.push(n.size);
      assign(n, sizes.length);
      return;
    }
    rec(n.left);
    rec(n.right);
  };
  rec(tree);
  return { groups, count: sizes.length, sizes };
}

/** Distance corresponding to a similarity cut-off in percent (for coefficient measures). */
export function similarityToHeight(percent: number): number {
  return 1 - percent / 100;
}
