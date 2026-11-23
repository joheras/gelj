// Identification against a reference library: ranks library lanes by their similarity to a query lane
// (as GelJ desktop's similarity search, BioNumerics identification or CLIQS reference libraries).
import { isCoefficient, laneDistance, mwTolerance, type ComparableLane } from './similarity';
import type { SimilarityMethod } from './types';

/** A lane of a complete experiment kept in the reference library under a type name. */
export interface LibraryEntry {
  /** `${experimentId}/${laneId}`, as the lane references of comparisons. A lane is in the library at most once. */
  id: string;
  /** Pulsotype, ribotype, species… the identification reported for lanes that match this entry. */
  type: string;
  comments: string;
  createdAt: number;
}

export interface IdentifySettings {
  similarity: SimilarityMethod;
  /** Band tolerance, as a percentage of the molecular-weight range of the query and the library. */
  tolerance: number;
  /** Minimum similarity (%) for coefficients, maximum distance otherwise; null = no cut-off. */
  cutoff: number | null;
}

export interface Reference<T> {
  item: T;
  type: string;
  lane: ComparableLane;
}

export interface BandPairing {
  /** Pairs of indices (query band, reference band) matched within the tolerance. */
  pairs: [number, number][];
  /** Indices of the query bands without a match. */
  queryOnly: number[];
  /** Indices of the reference bands without a match. */
  refOnly: number[];
}

export interface Hit<T> extends Reference<T> {
  distance: number;
  /** Similarity in % for coefficients; null for Euclidean and Manhattan distances. */
  similarity: number | null;
  bands: BandPairing;
  /** Whether the hit satisfies the cut-off (always true without one). */
  passes: boolean;
}

export interface Identification<T> {
  /** Library entries sorted from the closest to the farthest. */
  hits: Hit<T>[];
  /** Absolute band tolerance (molecular-weight units) used. */
  tolerance: number;
  coefficient: boolean;
}

/**
 * One-to-one pairing of the bands of two lanes whose molecular weights differ by at most `tolerance`.
 * Same sorted scan as `commonBands`, so the number of pairs is the count the band-based coefficients use.
 */
export function pairBands(a: number[], b: number[], tolerance: number): BandPairing {
  const ia = a.map((_, i) => i).filter((i) => Number.isFinite(a[i])).sort((x, y) => a[x] - a[y]);
  const ib = b.map((_, i) => i).filter((i) => Number.isFinite(b[i])).sort((x, y) => b[x] - b[y]);
  const pairs: [number, number][] = [];
  const usedA = new Set<number>();
  const usedB = new Set<number>();
  for (let i = 0, j = 0; i < ia.length && j < ib.length; ) {
    const va = a[ia[i]];
    const vb = b[ib[j]];
    if (Math.abs(va - vb) <= tolerance) {
      pairs.push([ia[i], ib[j]]);
      usedA.add(ia[i]);
      usedB.add(ib[j]);
      i++;
      j++;
    } else if (va < vb) i++;
    else j++;
  }
  pairs.sort((p, q) => p[0] - q[0]);
  return {
    pairs,
    queryOnly: a.map((_, i) => i).filter((i) => !usedA.has(i)),
    refOnly: b.map((_, i) => i).filter((i) => !usedB.has(i)),
  };
}

function passesCutoff(distance: number, coefficient: boolean, cutoff: number | null): boolean {
  if (cutoff === null || !Number.isFinite(cutoff)) return true;
  // Rounded as displayed, so a hit shown as "80.0%" is not rejected by an 80% cut-off.
  return coefficient ? Math.round(1000 * (1 - distance)) / 10 >= cutoff : distance <= cutoff + 1e-12;
}

/**
 * Ranks the references by similarity to `query`. Lanes normalised with different markers are compared as on
 * the comparison page: band-based measures through molecular weights, with a tolerance taken from the range of
 * the query and every reference; curve-based measures on profiles resampled to a common length.
 */
export function identify<T>(query: ComparableLane, refs: Reference<T>[], settings: IdentifySettings): Identification<T> {
  const coefficient = isCoefficient(settings.similarity);
  const tolerance = mwTolerance([query, ...refs.map((r) => r.lane)], settings.tolerance);
  const hits = refs.map((r) => {
    const distance = laneDistance(settings.similarity, query, r.lane, tolerance);
    return {
      ...r,
      distance,
      similarity: coefficient ? 100 * (1 - distance) : null,
      bands: pairBands(query.mw, r.lane.mw, tolerance),
      passes: passesCutoff(distance, coefficient, settings.cutoff),
    };
  });
  // Ties: more matched bands first (curve-based measures), then library order (stable sort).
  hits.sort((x, y) => x.distance - y.distance || y.bands.pairs.length - x.bands.pairs.length);
  return { hits, tolerance, coefficient };
}

export interface Verdict<T> {
  /** Closest hit, or null for an empty library. */
  best: Hit<T> | null;
  /** Type reported for the query: the type of the closest hit when it satisfies the cut-off. */
  type: string | null;
  /** Closest hit of a different type than the best one (how clear-cut the identification is). */
  runnerUp: Hit<T> | null;
  /** Number of hits that satisfy the cut-off. */
  passing: number;
}

export function verdict<T>(id: Identification<T>): Verdict<T> {
  const best = id.hits[0] ?? null;
  return {
    best,
    type: best && best.passes ? best.type : null,
    runnerUp: best ? (id.hits.find((h) => h.type !== best.type) ?? null) : null,
    passing: id.hits.filter((h) => h.passes).length,
  };
}

/** Similarity as displayed: a percentage for coefficients, the distance otherwise. */
export function formatScore(h: Pick<Hit<unknown>, 'distance' | 'similarity'>): string {
  return h.similarity !== null ? `${h.similarity.toFixed(1)}%` : h.distance.toPrecision(3);
}

const q = (s: string) => `"${s.replace(/"/g, '""')}"`;
const mw = (v: number) => (Number.isFinite(v) ? String(Number(v.toPrecision(4))) : '');

export interface IdentifiedLane<T> {
  name: string;
  lane: ComparableLane;
  result: Identification<T>;
}

/** CSV with the `top` closest hits of every query: one row per (query, hit), with the band pairing. */
export function hitsCsv<T>(queries: IdentifiedLane<T>[], refName: (item: T) => string, top: number): string {
  const rows = [
    ['Query', 'Rank', 'Reference type', 'Reference lane', 'Similarity (%)', 'Distance', 'Passes cut-off', 'Matched bands', 'Query bands', 'Reference bands', 'Unmatched query bands', 'Unmatched reference bands']
      .map(q)
      .join(','),
  ];
  for (const query of queries) {
    query.result.hits.slice(0, top).forEach((h, k) =>
      rows.push(
        [
          q(query.name),
          k + 1,
          q(h.type),
          q(refName(h.item)),
          h.similarity !== null ? h.similarity.toFixed(2) : '',
          h.distance.toPrecision(6),
          h.passes ? 'yes' : 'no',
          h.bands.pairs.length,
          query.lane.mw.length,
          h.lane.mw.length,
          q(h.bands.queryOnly.map((i) => mw(query.lane.mw[i])).join(' ')),
          q(h.bands.refOnly.map((i) => mw(h.lane.mw[i])).join(' ')),
        ].join(','),
      ),
    );
  }
  return rows.join('\n');
}

/** CSV with one row per query: the identified type, the closest hit and the closest hit of another type. */
export function summaryCsv<T>(queries: IdentifiedLane<T>[], refName: (item: T) => string): string {
  const rows = [
    ['Query', 'Identified type', 'Best match', 'Best match type', 'Similarity (%)', 'Distance', 'Matched bands', 'Next other type', 'Next similarity (%)', 'Next distance', 'Hits passing cut-off']
      .map(q)
      .join(','),
  ];
  for (const query of queries) {
    const v = verdict(query.result);
    const score = (h: Hit<T> | null) => [h?.similarity != null ? h.similarity.toFixed(2) : '', h ? h.distance.toPrecision(6) : ''];
    rows.push(
      [
        q(query.name),
        q(v.type ?? ''),
        q(v.best ? refName(v.best.item) : ''),
        q(v.best?.type ?? ''),
        ...score(v.best),
        v.best ? v.best.bands.pairs.length : '',
        q(v.runnerUp?.type ?? ''),
        ...score(v.runnerUp),
        v.passing,
      ].join(','),
    );
  }
  return rows.join('\n');
}
