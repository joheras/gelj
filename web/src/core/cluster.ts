// Agglomerative hierarchical clustering on a distance matrix.
import type { Linkage } from './types';

export const LINKAGES: Linkage[] = ['UPGMA', 'UPGMC', 'Single linkage', 'Complete linkage', 'Mean linkage', 'Ward'];

export interface ClusterNode {
  /** Leaf index for leaves, -1 for internal nodes. */
  index: number;
  height: number;
  size: number;
  left?: ClusterNode;
  right?: ClusterNode;
}

export function cluster(dist: number[][], linkage: Linkage): ClusterNode {
  const n = dist.length;
  if (n === 0) throw new Error('Nothing to cluster');
  // Squared distances for centroid and Ward (Lance-Williams formulation).
  const squared = linkage === 'UPGMC' || linkage === 'Ward';
  const D = dist.map((row) => row.map((v) => (squared ? v * v : v)));
  let nodes: (ClusterNode & { members: number[] })[] = dist.map((_, i) => ({ index: i, height: 0, size: 1, members: [i] }));
  let active = nodes.map((_, i) => i);
  // Sum of the pairwise distances within each cluster (for mean linkage).
  const within = new Map<number, number>(active.map((i) => [i, 0]));

  while (active.length > 1) {
    let bi = 0;
    let bj = 1;
    let best = Infinity;
    for (let a = 0; a < active.length; a++) {
      for (let b = a + 1; b < active.length; b++) {
        const v = D[active[a]][active[b]];
        if (v < best) {
          best = v;
          bi = a;
          bj = b;
        }
      }
    }
    const i = active[bi];
    const j = active[bj];
    const ni = nodes[i].size;
    const nj = nodes[j].size;
    const height = squared ? Math.sqrt(Math.max(0, best)) : best;
    const merged = {
      index: -1,
      height,
      size: ni + nj,
      left: nodes[i],
      right: nodes[j],
      members: [...nodes[i].members, ...nodes[j].members],
    };
    // Reuse slot i for the merged cluster.
    for (const k of active) {
      if (k === i || k === j) continue;
      const nk = nodes[k].size;
      const dik = D[i][k];
      const djk = D[j][k];
      let v: number;
      switch (linkage) {
        case 'Single linkage':
          v = Math.min(dik, djk);
          break;
        case 'Complete linkage':
          v = Math.max(dik, djk);
          break;
        case 'UPGMA':
          v = (ni * dik + nj * djk) / (ni + nj);
          break;
        case 'UPGMC':
          v = (ni * dik + nj * djk) / (ni + nj) - (ni * nj * D[i][j]) / ((ni + nj) * (ni + nj));
          break;
        case 'Ward':
          v = ((ni + nk) * dik + (nj + nk) * djk - nk * D[i][j]) / (ni + nj + nk);
          break;
        default: {
          // Mean linkage (as in Weka): mean of all pairwise distances inside the union.
          const members = [...merged.members, ...nodes[k].members];
          const m = members.length;
          const total = (within.get(i) ?? 0) + (within.get(j) ?? 0) + (within.get(k) ?? 0) + crossSum(dist, merged.members, nodes[k].members) + crossSum(dist, nodes[i].members, nodes[j].members);
          v = total / ((m * (m - 1)) / 2);
        }
      }
      D[i][k] = v;
      D[k][i] = v;
    }
    within.set(i, (within.get(i) ?? 0) + (within.get(j) ?? 0) + crossSum(dist, nodes[i].members, nodes[j].members));
    nodes = nodes.slice();
    nodes[i] = merged;
    active = active.filter((k) => k !== j);
  }
  return stripMembers(nodes[active[0]]);
}

function crossSum(dist: number[][], a: number[], b: number[]): number {
  let s = 0;
  for (const i of a) for (const j of b) s += dist[i][j];
  return s;
}

function stripMembers(node: ClusterNode & { members?: number[] }): ClusterNode {
  const { index, height, size, left, right } = node;
  return { index, height, size, left: left && stripMembers(left), right: right && stripMembers(right) };
}

export function leafOrder(node: ClusterNode): number[] {
  if (!node.left || !node.right) return [node.index];
  return [...leafOrder(node.left), ...leafOrder(node.right)];
}

/** Newick representation; branch lengths are half the merge distances (ultrametric tree). */
export function toNewick(node: ClusterNode, names: string[]): string {
  const esc = (s: string) => (/[\s(),:;'[\]]/.test(s) ? `'${s.replace(/'/g, "''")}'` : s);
  const rec = (n: ClusterNode, parentHeight: number): string => {
    const len = Math.max(0, (parentHeight - n.height) / 2);
    const label = !n.left || !n.right ? esc(names[n.index]) : `(${rec(n.left, n.height)},${rec(n.right, n.height)})`;
    return `${label}:${len.toFixed(6)}`;
  };
  if (!node.left || !node.right) return `${esc(names[node.index])};`;
  return `(${rec(node.left, node.height)},${rec(node.right, node.height)});`;
}

/** Cophenetic matrix: merge height at which two leaves are joined. */
export function cophenetic(node: ClusterNode, n: number): number[][] {
  const c = Array.from({ length: n }, () => new Array<number>(n).fill(0));
  const rec = (nd: ClusterNode): number[] => {
    if (!nd.left || !nd.right) return [nd.index];
    const a = rec(nd.left);
    const b = rec(nd.right);
    for (const i of a)
      for (const j of b) {
        c[i][j] = nd.height;
        c[j][i] = nd.height;
      }
    return [...a, ...b];
  };
  rec(node);
  return c;
}

/** Pearson correlation between the original distances and the cophenetic distances. */
export function copheneticCorrelation(dist: number[][], node: ClusterNode): number {
  const n = dist.length;
  if (n < 3) return 1;
  const c = cophenetic(node, n);
  const x: number[] = [];
  const y: number[] = [];
  for (let i = 0; i < n; i++)
    for (let j = i + 1; j < n; j++) {
      x.push(dist[i][j]);
      y.push(c[i][j]);
    }
  const mx = x.reduce((a, b) => a + b, 0) / x.length;
  const my = y.reduce((a, b) => a + b, 0) / y.length;
  let sxy = 0;
  let sxx = 0;
  let syy = 0;
  for (let k = 0; k < x.length; k++) {
    sxy += (x[k] - mx) * (y[k] - my);
    sxx += (x[k] - mx) ** 2;
    syy += (y[k] - my) ** 2;
  }
  return sxx > 0 && syy > 0 ? sxy / Math.sqrt(sxx * syy) : 1;
}
