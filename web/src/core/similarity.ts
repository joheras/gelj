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

/**
 * Maximum number of one-to-one pairs of bands (one of `a`, one of `b`) whose weights differ by at most
 * `tolerance`. Scanning both sorted lists is optimal for interval matching, and symmetric.
 */
export function commonBands(a: number[], b: number[], tolerance: number, optimisation = 0): number {
  return sortedCommonBands(sortedBands(a), sortedBands(b), tolerance, optimisation);
}

/** Finite band weights sorted ascending, as expected by sortedCommonBands. */
export function sortedBands(mw: number[]): number[] {
  return mw.filter(Number.isFinite).sort((x, y) => x - y);
}

/** commonBands on lists already sorted by sortedBands (avoids sorting again in repeated computations). */
export function sortedCommonBands(sa: number[], sb: number[], tolerance: number, optimisation = 0): number {
  return optimisation > 0 ? shiftedCommon(sa, sb, tolerance, optimisation) : sortedCommon(sa, sb, tolerance, 0);
}

/** commonBands on lists already sorted ascending, with the bands of `b` shifted by `shift`. */
function sortedCommon(sa: number[], sb: number[], tolerance: number, shift: number): number {
  let common = 0;
  // A small slack so that a shift computed as a difference of weights still matches exactly.
  const tol = shift === 0 ? tolerance : tolerance + 1e-9 * (tolerance + Math.abs(shift));
  for (let i = 0, j = 0; i < sa.length && j < sb.length; ) {
    const v = sb[j] + shift;
    if (Math.abs(sa[i] - v) <= tol) {
      common++;
      i++;
      j++;
    } else if (sa[i] < v) i++;
    else j++;
  }
  return common;
}

/**
 * Optimisation (as in GelCompar II / BioNumerics): the best number of common bands over every global
 * shift of `b` relative to `a` of at most `optimisation`. The number of matches only changes where the
 * shift brings two bands exactly `tolerance` apart, so it is enough to try those shifts (and 0, ±max).
 * Symmetric: shifting `b` by s is shifting `a` by -s.
 */
function shiftedCommon(sa: number[], sb: number[], tolerance: number, optimisation: number): number {
  let best = sortedCommon(sa, sb, tolerance, 0);
  const limit = Math.min(sa.length, sb.length);
  if (best === limit) return best;
  const tried = new Set<number>([0]);
  const tryShift = (s: number) => {
    if (s < -optimisation || s > optimisation || tried.has(s)) return;
    tried.add(s);
    const c = sortedCommon(sa, sb, tolerance, s);
    if (c > best) best = c;
  };
  tryShift(-optimisation);
  tryShift(optimisation);
  const reach = optimisation + tolerance;
  for (let i = 0, j0 = 0; i < sa.length && best < limit; i++) {
    // Only pairs of bands that can be brought within the tolerance give useful shifts.
    while (j0 < sb.length && sb[j0] < sa[i] - reach) j0++;
    for (let j = j0; j < sb.length && sb[j] <= sa[i] + reach && best < limit; j++) {
      const d = sa[i] - sb[j];
      tryShift(d - tolerance);
      tryShift(d + tolerance);
    }
  }
  return best;
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
  const den = Math.sqrt(Math.max(0, sxx - (sx * sx) / n)) * Math.sqrt(Math.max(0, syy - (sy * sy) / n));
  return den > 0 ? (sxy - (sx * sy) / n) / den : sameValues(x, y) ? 1 : 0;
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
  return den > 0 ? sxy / den : sameValues(x, y) ? 1 : 0;
}

/** Undefined correlations (flat or empty profiles) count as a perfect match only for identical profiles. */
function sameValues(x: number[], y: number[]): boolean {
  return x.every((v, i) => v === y[i]);
}

/** Distance of a band-based coefficient for lanes with `b1` and `b2` bands (both > 0) and `c` common bands. */
export function bandDistance(method: SimilarityMethod, b1: number, b2: number, c: number): number {
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

export function laneDistance(
  method: SimilarityMethod,
  l1: ComparableLane,
  l2: ComparableLane,
  tolerance: number,
  /** Maximum global shift between the two lanes (band-based measures), in the units of `tolerance`. */
  optimisation = 0,
): number {
  if (isBandBased(method)) {
    const b1 = l1.mw.length;
    const b2 = l2.mw.length;
    if (b1 === 0 && b2 === 0) return 0;
    if (b1 === 0 || b2 === 0) return 1;
    return bandDistance(method, b1, b2, commonBands(l1.mw, l2.mw, tolerance, optimisation));
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
  let min = Infinity;
  let max = -Infinity;
  for (const l of lanes)
    for (const v of l.mw) {
      if (!Number.isFinite(v)) continue;
      if (v < min) min = v;
      if (v > max) max = v;
    }
  return max >= min ? ((max - min) * percent) / 100 : 0;
}

export function distanceMatrix(method: SimilarityMethod, lanes: ComparableLane[], tolerance: number, optimisation = 0): number[][] {
  const n = lanes.length;
  const d = Array.from({ length: n }, () => new Array<number>(n).fill(0));
  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      // Every measure is symmetric (band matching included), so each pair is computed once.
      const v = laneDistance(method, lanes[i], lanes[j], tolerance, optimisation);
      d[i][j] = v;
      d[j][i] = v;
    }
  }
  return d;
}
