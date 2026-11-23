import { describe, expect, it } from 'vitest';
import { seededRandom } from './bootstrap';
import { adjustedRandIndex, bestPoint, evaluatePoint, gridValues, prepareScan, scanTolerance, type ScanPoint } from './optimise';
import { commonBands, distanceMatrix, laneDistance } from './similarity';

/** Brute force: the best common-band count over a fine grid of shifts. */
function bruteShift(a: number[], b: number[], tol: number, opt: number): number {
  let best = 0;
  for (let k = -2000; k <= 2000; k++) best = Math.max(best, commonBands(a, b.map((v) => v + (opt * k) / 2000), tol));
  return best;
}

describe('optimisation (global shift)', () => {
  it('matches a whole lane shifted by less than the optimisation', () => {
    const a = [100, 200, 300, 400];
    const b = a.map((v) => v + 8);
    expect(commonBands(a, b, 2)).toBe(0);
    expect(commonBands(a, b, 2, 5)).toBe(0);
    expect(commonBands(a, b, 2, 6)).toBe(4);
    expect(commonBands(a, b, 2, 10)).toBe(4);
  });

  it('is symmetric, never below the unshifted count, and equals a brute-force search', () => {
    const rnd = seededRandom(7);
    for (let t = 0; t < 200; t++) {
      const a = Array.from({ length: 1 + Math.floor(rnd() * 8) }, () => Math.round(rnd() * 500));
      const b = Array.from({ length: 1 + Math.floor(rnd() * 8) }, () => Math.round(rnd() * 500));
      const tol = rnd() * 10;
      const opt = rnd() * 30;
      const c = commonBands(a, b, tol, opt);
      expect(c).toBe(commonBands(b, a, tol, opt));
      expect(c).toBeGreaterThanOrEqual(commonBands(a, b, tol));
      expect(c).toBeGreaterThanOrEqual(bruteShift(a, b, tol, opt));
      // The exact search can only find more than the grid by hitting a boundary shift the grid misses.
      expect(c).toBeLessThanOrEqual(bruteShift(a, b, tol * 1.001 + 1e-6, opt));
    }
  });

  it('is used by laneDistance and distanceMatrix for band-based measures only', () => {
    const l1 = { mw: [100, 200, 300], profile: [0, 10, 0] };
    const l2 = { mw: [106, 206, 306], profile: [0, 10, 0] };
    expect(laneDistance('Dice', l1, l2, 1)).toBe(1);
    expect(laneDistance('Dice', l1, l2, 1, 5)).toBeCloseTo(0);
    expect(distanceMatrix('Dice', [l1, l2], 1, 5)[0][1]).toBeCloseTo(0);
    expect(laneDistance('Pearson correlation', l1, l2, 1, 5)).toBe(laneDistance('Pearson correlation', l1, l2, 1));
  });
});

describe('adjusted Rand index', () => {
  it('is 1 for identical partitions whatever the labels', () => {
    expect(adjustedRandIndex(['a', 'a', 'b', 'b', 'c'], [3, 3, 1, 1, 2])).toBeCloseTo(1);
  });
  it('matches a hand-computed value', () => {
    // Contingency [[2,1],[0,2]]: index 2, row pairs 3+1, column pairs 1+3, n pairs 10 → (2-1.6)/(4-1.6).
    expect(adjustedRandIndex(['x', 'x', 'x', 'y', 'y'], [1, 1, 2, 2, 2])).toBeCloseTo(0.4 / 2.4);
  });
  it('is negative or near zero for disagreeing partitions', () => {
    expect(adjustedRandIndex(['a', 'a', 'b', 'b'], [1, 2, 1, 2])).toBeLessThan(0);
  });
});

describe('gridValues', () => {
  it('includes both ends and avoids floating-point noise', () => {
    expect(gridValues(0.5, 2, 0.5)).toEqual([0.5, 1, 1.5, 2]);
    expect(gridValues(0.1, 0.3, 0.1)).toEqual([0.1, 0.2, 0.3]);
    expect(gridValues(0, 1, 0)).toEqual([0]);
  });
});

/** Three types of 6 bands; isolates of a type are copies with jitter and a whole-lane shift. */
function typedLanes(seed: number, jitter: number, shift: number) {
  const rnd = seededRandom(seed);
  const types = [
    [50, 120, 210, 330, 480, 700],
    [60, 140, 250, 360, 520, 650],
    [80, 100, 190, 300, 450, 760],
  ];
  const lanes: { mw: number[]; profile: number[] }[] = [];
  const groups: string[] = [];
  types.forEach((t, k) => {
    for (let r = 0; r < 4; r++) {
      const s = (rnd() * 2 - 1) * shift;
      lanes.push({ mw: t.map((v) => v + s + (rnd() * 2 - 1) * jitter), profile: [] });
      groups.push(`T${k}`);
    }
  });
  return { lanes, groups };
}

describe('tolerance scan', () => {
  it('finds a tolerance that recovers the known types', () => {
    // Range 710: 1% = 7.1. Jitter ±3 needs a tolerance of about 1%; a tolerance of 0.1% splits the types.
    const { lanes, groups } = typedLanes(3, 3, 0);
    const points = scanTolerance(lanes, {
      method: 'Dice',
      linkage: 'UPGMA',
      tolerances: gridValues(0.1, 2, 0.1),
      optimisations: [0],
      groups,
      cutoff: 80,
    });
    expect(points).toHaveLength(20);
    expect(points[0].ari!).toBeLessThan(1);
    const best = bestPoint(points, 'ari')!;
    expect(best.ari).toBeCloseTo(1);
    expect(best.clusters).toBe(3);
    expect(best.tolerance).toBeGreaterThanOrEqual(0.8);
    expect(bestPoint(points, 'cophenetic')!.cophenetic).toBeGreaterThan(0.9);
  });

  it('needs the optimisation when whole lanes are shifted', () => {
    const { lanes, groups } = typedLanes(5, 1, 8);
    const opts = { method: 'Dice' as const, linkage: 'UPGMA' as const, tolerances: [0.5], optimisations: [0, 2.5], groups, cutoff: 80 };
    const [noShift, withShift] = scanTolerance(lanes, opts);
    expect(noShift.ari!).toBeLessThan(0.9);
    expect(withShift.ari!).toBeCloseTo(1);
    expect(bestPoint([noShift, withShift], 'ari')).toBe(withShift);
  });

  it('ignores degenerate settings and lanes without a group', () => {
    const { lanes, groups } = typedLanes(9, 2, 0);
    groups[0] = '';
    const ctx = prepareScan(lanes, { method: 'Dice', linkage: 'UPGMA', tolerances: [], optimisations: [], groups, cutoff: 80 });
    expect(ctx.known).toHaveLength(11);
    // A tolerance of the whole range makes every lane identical: no cophenetic correlation.
    expect(evaluatePoint(ctx, 100, 0).cophenetic).toBeNull();
    const pts: ScanPoint[] = [
      { tolerance: 2, optimisation: 0, cophenetic: 0.9, ari: null, clusters: 3 },
      { tolerance: 1, optimisation: 0, cophenetic: 0.9, ari: null, clusters: 3 },
      { tolerance: 3, optimisation: 0, cophenetic: null, ari: null, clusters: 1 },
    ];
    expect(bestPoint(pts, 'cophenetic')!.tolerance).toBe(1);
    expect(bestPoint(pts, 'ari')).toBeNull();
    expect(() => prepareScan(lanes, { method: 'Pearson correlation', linkage: 'UPGMA', tolerances: [], optimisations: [] })).toThrow();
  });

  it('runs a few hundred lanes in reasonable time', () => {
    const rnd = seededRandom(11);
    const lanes = Array.from({ length: 300 }, () => ({ mw: Array.from({ length: 15 }, () => 20 + rnd() * 980), profile: [] }));
    const ctx = prepareScan(lanes, { method: 'Dice', linkage: 'UPGMA', tolerances: [], optimisations: [] });
    const t0 = performance.now();
    evaluatePoint(ctx, 1, 1);
    expect(performance.now() - t0).toBeLessThan(5000);
  });
});
