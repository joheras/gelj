// Bootstrap support of dendrogram branches: characters (band classes or profile points) are resampled
// with replacement, the distance matrix and the tree are rebuilt, and every branch of the original tree
// gets the percentage of replicates in which the same group of lanes appears.
import { cluster, type ClusterNode } from './cluster';
import { isBandBased, laneDistance, resample } from './similarity';
import type { Linkage, SimilarityMethod } from './types';

/** Characters resampled by the bootstrap. */
export type BootstrapData =
  | { kind: 'binary'; matrix: number[][] }
  | { kind: 'profile'; profiles: number[][] };

export function seededRandom(seed: number): () => number {
  let s = seed >>> 0;
  return () => {
    // mulberry32
    s = (s + 0x6d2b79f5) >>> 0;
    let t = s;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** Band-based coefficient computed from weighted presence/absence counts; returns a distance. */
export function binaryDistance(method: SimilarityMethod, x: number[], y: number[], weights?: number[]): number {
  let a = 0; // both present
  let b = 0; // only in x
  let c = 0; // only in y
  for (let k = 0; k < x.length; k++) {
    const w = weights ? weights[k] : 1;
    if (x[k] && y[k]) a += w;
    else if (x[k]) b += w;
    else if (y[k]) c += w;
  }
  if (a + b === 0 && a + c === 0) return 0;
  if (a + b === 0 || a + c === 0) return 1;
  let s: number;
  switch (method) {
    case 'Jaccard':
      s = a / (a + b + c);
      break;
    case "Jeffrey's X":
      s = (a / (a + b) + a / (a + c)) / 2;
      break;
    case 'Ochiai':
      s = a / Math.sqrt((a + b) * (a + c));
      break;
    case 'Band difference':
      s = 1 - (b + c) / (a + b + c);
      break;
    default: // Dice
      s = (2 * a) / (2 * a + b + c);
  }
  return 1 - s;
}

/** Distance matrix of one bootstrap replicate. */
export function replicateDistances(method: SimilarityMethod, data: BootstrapData, random: () => number): number[][] {
  if (data.kind === 'binary') {
    const m = data.matrix[0]?.length ?? 0;
    const w = new Array<number>(m).fill(0);
    for (let k = 0; k < m; k++) w[Math.floor(random() * m)]++;
    return square(data.matrix.length, (i, j) => binaryDistance(method, data.matrix[i], data.matrix[j], w));
  }
  const len = data.profiles[0]?.length ?? 0;
  const idx = Array.from({ length: len }, () => Math.floor(random() * len));
  const lanes = data.profiles.map((p) => ({ mw: [], profile: idx.map((i) => p[i]) }));
  return square(lanes.length, (i, j) => laneDistance(method, lanes[i], lanes[j], 0));
}

function square(n: number, f: (i: number, j: number) => number): number[][] {
  const d = Array.from({ length: n }, () => new Array<number>(n).fill(0));
  for (let i = 0; i < n; i++)
    for (let j = i + 1; j < n; j++) {
      const v = f(i, j);
      d[i][j] = v;
      d[j][i] = v;
    }
  return d;
}

function leaves(n: ClusterNode): number[] {
  return !n.left || !n.right ? [n.index] : [...leaves(n.left), ...leaves(n.right)];
}

/** Identifier of the set of leaves under every internal node. */
function cladeKeys(tree: ClusterNode): Map<ClusterNode, string> {
  const out = new Map<ClusterNode, string>();
  const rec = (n: ClusterNode) => {
    if (!n.left || !n.right) return;
    out.set(n, leaves(n).sort((a, b) => a - b).join(','));
    rec(n.left);
    rec(n.right);
  };
  rec(tree);
  return out;
}

/** Accumulates bootstrap replicates; can be driven in small batches to keep the UI responsive. */
export class Bootstrap {
  private readonly method: SimilarityMethod;
  private readonly linkage: Linkage;
  private readonly data: BootstrapData;
  private readonly keys: Map<ClusterNode, string>;
  private readonly hits = new Map<string, number>();
  private readonly random: () => number;
  replicates = 0;

  constructor(tree: ClusterNode, method: SimilarityMethod, linkage: Linkage, data: BootstrapData, seed = 1) {
    this.method = method;
    this.linkage = linkage;
    this.data = data;
    this.keys = cladeKeys(tree);
    for (const k of this.keys.values()) this.hits.set(k, 0);
    this.random = seededRandom(seed);
  }

  run(count: number): void {
    for (let r = 0; r < count; r++) {
      const t = cluster(replicateDistances(this.method, this.data, this.random), this.linkage);
      for (const k of cladeKeys(t).values()) {
        const h = this.hits.get(k);
        if (h !== undefined) this.hits.set(k, h + 1);
      }
      this.replicates++;
    }
  }

  /** Support (0..100) of every internal node of the original tree. */
  support(): Map<ClusterNode, number> {
    const out = new Map<ClusterNode, number>();
    for (const [node, k] of this.keys) out.set(node, this.replicates ? (100 * (this.hits.get(k) ?? 0)) / this.replicates : 0);
    return out;
  }
}

/** Characters to resample for a given measure: band classes for band-based ones, profile points otherwise. */
export function bootstrapData(method: SimilarityMethod, bandMatrix: number[][], profiles: number[][]): BootstrapData {
  if (isBandBased(method)) return { kind: 'binary', matrix: bandMatrix };
  const n = Math.max(0, ...profiles.map((p) => p.length));
  return { kind: 'profile', profiles: profiles.map((p) => resample(p, n)) };
}
