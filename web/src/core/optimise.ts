// Tolerance and optimisation analysis, as in GelCompar II / BioNumerics: the band-based comparison is
// repeated over a grid of position tolerances and optimisations (maximum global shift between two lanes),
// and every setting gets a quality score — the cophenetic correlation of its dendrogram and, when the
// lanes have a known grouping, the adjusted Rand index between that grouping and the cut-off clusters.
import { cluster, copheneticCorrelation, type ClusterNode } from './cluster';
import { bandDistance, isBandBased, mwTolerance, sortedBands, sortedCommonBands, type ComparableLane } from './similarity';
import { cutTree, similarityToHeight } from './treecut';
import type { Linkage, SimilarityMethod } from './types';

export type ScanCriterion = 'cophenetic' | 'ari';

export interface ScanOptions {
  method: SimilarityMethod;
  linkage: Linkage;
  /** Tolerances to try, in % of the molecular-weight range. */
  tolerances: number[];
  /** Optimisations to try, in % of the molecular-weight range (same units as the tolerance). */
  optimisations: number[];
  /** Known group of every lane (e.g. its MLST type); empty or null = unknown, left out of the index. */
  groups?: (string | null | undefined)[];
  /** Similarity cut-off (%) defining the clusters compared with `groups`. */
  cutoff?: number;
}

export interface ScanPoint {
  tolerance: number;
  optimisation: number;
  /** Cophenetic correlation; null when every distance is equal (nothing to represent). */
  cophenetic: number | null;
  /** Adjusted Rand index with the known grouping; null without a grouping. */
  ari: number | null;
  /** Number of clusters (singletons included) at the cut-off; null without a cut-off. */
  clusters: number | null;
}

/** Values from `min` to `max` (inclusive) every `step`, rounded to avoid floating-point noise. */
export function gridValues(min: number, max: number, step: number, limit = 100): number[] {
  if (!(step > 0) || !Number.isFinite(min) || !Number.isFinite(max) || max < min) return [Math.max(0, min || 0)];
  const out: number[] = [];
  for (let k = 0; out.length < limit; k++) {
    const v = Number((min + k * step).toFixed(6));
    if (v > max + 1e-9) break;
    out.push(v);
  }
  return out;
}

/**
 * Adjusted Rand index (Hubert & Arabie) between two partitions given as labels of the same items:
 * 1 for identical partitions, about 0 for random agreement.
 */
export function adjustedRandIndex(a: (string | number)[], b: (string | number)[]): number {
  const n = Math.min(a.length, b.length);
  if (n < 2) return 1;
  const ka = new Map<string | number, number>();
  const kb = new Map<string | number, number>();
  const cells = new Map<string, number>();
  for (let i = 0; i < n; i++) {
    ka.set(a[i], (ka.get(a[i]) ?? 0) + 1);
    kb.set(b[i], (kb.get(b[i]) ?? 0) + 1);
    const key = JSON.stringify([a[i], b[i]]);
    cells.set(key, (cells.get(key) ?? 0) + 1);
  }
  const pairs = (m: number) => (m * (m - 1)) / 2;
  let index = 0;
  for (const m of cells.values()) index += pairs(m);
  let sa = 0;
  let sb = 0;
  for (const m of ka.values()) sa += pairs(m);
  for (const m of kb.values()) sb += pairs(m);
  const expected = (sa * sb) / pairs(n);
  const max = (sa + sb) / 2;
  // Both partitions trivial (one cluster each, or all singletons): they agree.
  if (max === expected) return 1;
  return (index - expected) / (max - expected);
}

/** Data prepared once for every point of a scan. */
export interface ScanContext {
  options: ScanOptions;
  bands: number[][];
  /** Molecular-weight range (1% of tolerance = range / 100). */
  range: number;
  /** Lanes with a known group, and their groups. */
  known: number[];
  knownGroups: string[];
}

export function prepareScan(lanes: ComparableLane[], options: ScanOptions): ScanContext {
  if (!isBandBased(options.method)) throw new Error('The tolerance analysis needs a band-based similarity coefficient.');
  if (lanes.length < 3) throw new Error('Select at least three lanes.');
  const known: number[] = [];
  const knownGroups: string[] = [];
  options.groups?.forEach((g, i) => {
    const v = (g ?? '').trim();
    if (v && i < lanes.length) {
      known.push(i);
      knownGroups.push(v);
    }
  });
  return { options, bands: lanes.map((l) => sortedBands(l.mw)), range: mwTolerance(lanes, 100), known, knownGroups };
}

/** Distance matrix of a band-based coefficient for absolute tolerance and optimisation. */
export function bandDistanceMatrix(method: SimilarityMethod, bands: number[][], tolerance: number, optimisation: number): number[][] {
  const n = bands.length;
  const d = Array.from({ length: n }, () => new Array<number>(n).fill(0));
  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      const b1 = bands[i].length;
      const b2 = bands[j].length;
      const v =
        b1 === 0 && b2 === 0 ? 0 : b1 === 0 || b2 === 0 ? 1 : bandDistance(method, b1, b2, sortedCommonBands(bands[i], bands[j], tolerance, optimisation));
      d[i][j] = v;
      d[j][i] = v;
    }
  }
  return d;
}

function allEqual(d: number[][]): boolean {
  const v = d.length > 1 ? d[0][1] : 0;
  return d.every((row, i) => row.every((x, j) => i === j || Math.abs(x - v) < 1e-12));
}

/** Scores one tolerance/optimisation setting (both in % of the molecular-weight range). */
export function evaluatePoint(ctx: ScanContext, tolerance: number, optimisation: number): ScanPoint {
  const { method, linkage, cutoff } = ctx.options;
  const dist = bandDistanceMatrix(method, ctx.bands, (ctx.range * tolerance) / 100, (ctx.range * optimisation) / 100);
  const tree: ClusterNode = cluster(dist, linkage);
  const cophenetic = allEqual(dist) ? null : copheneticCorrelation(dist, tree);
  let ari: number | null = null;
  let clusters: number | null = null;
  if (cutoff !== undefined && Number.isFinite(cutoff)) {
    const cut = cutTree(tree, similarityToHeight(cutoff));
    clusters = cut.count;
    if (ctx.known.length >= 2) ari = adjustedRandIndex(ctx.knownGroups, ctx.known.map((i) => cut.groups[i]));
  }
  return { tolerance, optimisation, cophenetic, ari, clusters };
}

/** Grid of settings, tolerance varying fastest. */
export function scanGrid(options: ScanOptions): [number, number][] {
  return options.optimisations.flatMap((o) => options.tolerances.map((t): [number, number] => [t, o]));
}

/**
 * Best setting: highest score of `criterion` (the other score breaks ties), then the smallest optimisation
 * and tolerance — the most conservative setting among equally good ones.
 */
export function bestPoint(points: ScanPoint[], criterion: ScanCriterion): ScanPoint | null {
  const other: ScanCriterion = criterion === 'ari' ? 'cophenetic' : 'ari';
  const eps = 1e-9;
  let best: ScanPoint | null = null;
  for (const p of points) {
    const s = p[criterion];
    if (s === null || !Number.isFinite(s)) continue;
    if (!best) {
      best = p;
      continue;
    }
    const bs = best[criterion]!;
    const so = p[other] ?? -Infinity;
    const bo = best[other] ?? -Infinity;
    if (s > bs + eps) best = p;
    else if (s < bs - eps) continue;
    else if (so > bo + eps) best = p;
    else if (so < bo - eps) continue;
    else if (p.optimisation < best.optimisation || (p.optimisation === best.optimisation && p.tolerance < best.tolerance)) best = p;
  }
  return best;
}

/** Runs the whole scan synchronously (the UI runs it in a worker, point by point). */
export function scanTolerance(lanes: ComparableLane[], options: ScanOptions, onProgress?: (done: number, total: number) => void): ScanPoint[] {
  const ctx = prepareScan(lanes, options);
  const grid = scanGrid(options);
  return grid.map(([t, o], k) => {
    const p = evaluatePoint(ctx, t, o);
    onProgress?.(k + 1, grid.length);
    return p;
  });
}
