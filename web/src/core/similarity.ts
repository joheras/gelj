// Lane similarity measures (port of GelJ's LaneCompare package).
// Every function returns a distance: 0 for identical lanes.
import type { SimilarityMethod } from './types';

export interface ComparableLane {
  /** Molecular weight of each band. */
  mw: number[];
  /** Normalised intensity profile. */
  profile: number[];
}

export const SIMILARITY_METHODS: SimilarityMethod[] = [
  'Dice',
  'Jaccard',
  'Band difference',
  "Jeffrey's X",
  'Ochiai',
  'Pearson correlation',
  'Cosine coefficient',
  'Euclidean distance',
  'Manhattan distance',
];

export function isBandBased(m: SimilarityMethod): boolean {
  return SIMILARITY_METHODS.indexOf(m) < 5;
}

/** Whether the measure is a similarity coefficient in [0,1] (shown as a percentage). */
export function isCoefficient(m: SimilarityMethod): boolean {
  return m !== 'Euclidean distance' && m !== 'Manhattan distance';
}

/** Number of bands of `a` matched (greedily, one-to-one) by a band of `b` within `tolerance`. */
export function commonBands(a: number[], b: number[], tolerance: number): number {
  const rest = b.slice();
  let common = 0;
  for (const v of a) {
    const i = rest.findIndex((w) => v - tolerance <= w && w <= v + tolerance);
    if (i >= 0) {
      common++;
      rest.splice(i, 1);
    }
  }
  return common;
}

/** Resample a profile to `n` points (linear interpolation). */
export function resample(p: number[], n: number): number[] {
  if (p.length === n) return p;
  if (p.length === 0) return new Array<number>(n).fill(0);
  const out = new Array<number>(n);
  for (let i = 0; i < n; i++) {
    const t = n === 1 ? 0 : (i * (p.length - 1)) / (n - 1);
    const i0 = Math.floor(t);
    const i1 = Math.min(p.length - 1, i0 + 1);
    out[i] = p[i0] + (p[i1] - p[i0]) * (t - i0);
  }
  return out;
}

function pearson(x: number[], y: number[]): number {
  const n = x.length;
  let sx = 0;
  let sy = 0;
  let sxx = 0;
  let syy = 0;
  let sxy = 0;
  for (let i = 0; i < n; i++) {
    sx += x[i];
    sy += y[i];
    sxx += x[i] * x[i];
    syy += y[i] * y[i];
    sxy += x[i] * y[i];
  }
  const den = Math.sqrt(sxx - (sx * sx) / n) * Math.sqrt(syy - (sy * sy) / n);
  return den > 0 ? (sxy - (sx * sy) / n) / den : 0;
}

function cosine(x: number[], y: number[]): number {
  let sxx = 0;
  let syy = 0;
  let sxy = 0;
  for (let i = 0; i < x.length; i++) {
    sxx += x[i] * x[i];
    syy += y[i] * y[i];
    sxy += x[i] * y[i];
  }
  const den = Math.sqrt(sxx) * Math.sqrt(syy);
  return den > 0 ? sxy / den : 0;
}

export function laneDistance(
  method: SimilarityMethod,
  l1: ComparableLane,
  l2: ComparableLane,
  tolerance: number,
): number {
  if (isBandBased(method)) {
    const b1 = l1.mw.length;
    const b2 = l2.mw.length;
    if (b1 === 0 && b2 === 0) return 0;
    if (b1 === 0 || b2 === 0) return 1;
    const c = commonBands(l1.mw, l2.mw, tolerance);
    let s: number;
    switch (method) {
      case 'Dice':
        s = (2 * c) / (b1 + b2);
        break;
      case 'Jaccard':
        s = c / (b1 + b2 - c);
        break;
      case "Jeffrey's X":
        s = (c / b1 + c / b2) / 2;
        break;
      case 'Ochiai':
        s = c / Math.sqrt(b1 * b2);
        break;
      default: // Band difference
        s = 1 - (b1 + b2 - 2 * c) / (b1 + b2 - c);
    }
    return 1 - s;
  }
  const n = Math.max(l1.profile.length, l2.profile.length);
  const x = resample(l1.profile, n);
  const y = resample(l2.profile, n);
  switch (method) {
    case 'Pearson correlation':
      return 1 - Math.max(0, pearson(x, y));
    case 'Cosine coefficient':
      return 1 - Math.max(0, cosine(x, y));
    case 'Euclidean distance': {
      let s = 0;
      for (let i = 0; i < n; i++) s += ((x[i] - y[i]) / 255) ** 2;
      return Math.sqrt(s / n);
    }
    default: {
      let s = 0;
      for (let i = 0; i < n; i++) s += Math.abs(x[i] - y[i]) / 255;
      return s / n;
    }
  }
}

/**
 * Tolerance for band matching: a percentage of the molecular-weight range of
 * the compared lanes (GelJ used the range of the first lane).
 */
export function mwTolerance(lanes: ComparableLane[], percent: number): number {
  const all = lanes.flatMap((l) => l.mw);
  if (all.length === 0) return 0;
  return ((Math.max(...all) - Math.min(...all)) * percent) / 100;
}

export function distanceMatrix(method: SimilarityMethod, lanes: ComparableLane[], tolerance: number): number[][] {
  const n = lanes.length;
  const d = Array.from({ length: n }, () => new Array<number>(n).fill(0));
  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      // Band-matching is not symmetric with a greedy match; average both directions.
      const v =
        isBandBased(method)
          ? (laneDistance(method, lanes[i], lanes[j], tolerance) + laneDistance(method, lanes[j], lanes[i], tolerance)) / 2
          : laneDistance(method, lanes[i], lanes[j], tolerance);
      d[i][j] = v;
      d[j][i] = v;
    }
  }
  return d;
}
