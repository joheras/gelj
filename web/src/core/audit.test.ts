// Regression tests for the issues found in the code audit (see docs/audit.md).
import { describe, expect, it } from 'vitest';
import { cluster, cophenetic } from './cluster';
import { BUILTIN_MARKERS } from './markers';
import { migrationModel, normaliseLanes } from './normalise';
import { commonBands, distanceMatrix, laneDistance, mwTolerance } from './similarity';
import { naturalSpline } from './spline';
import { createImage } from './image';
import { makeLanes } from './detect';
import type { Linkage } from './types';

describe('spline extrapolation', () => {
  it('extends the natural spline linearly beyond the knots, as SSJ does', () => {
    const s = naturalSpline([0, 10, 20, 30], [0, 5, -3, 2]);
    const d0 = s(0) - s(-1);
    const d1 = s(31) - s(30);
    expect(s(-100) - s(0)).toBeCloseTo(-100 * d0, 6);
    expect(s(130) - s(30)).toBeCloseTo(100 * d1, 6);
    // Smooth (C1) at the boundary knots.
    expect(s(1e-6) - s(0)).toBeCloseTo(d0 * 1e-6, 9);
  });
});

describe('molecular weights', () => {
  const lambda = BUILTIN_MARKERS[1]; // y = a + b·ln(x - c): negative beyond the last band

  it('never produces non-finite or non-positive molecular weights', () => {
    const img = createImage(40, 700, 0);
    const [ref, sample] = makeLanes([
      { x: 0, y: 0, w: 20, h: 600 },
      { x: 20, y: 0, w: 20, h: 600 },
    ]);
    ref.isReference = true;
    ref.bands = lambda.refPositions.slice();
    sample.bands = [2, 300, 590, 599];
    const [, r] = normaliseLanes(img, true, [ref, sample], lambda, lambda.mw);
    expect(migrationModel(lambda).evaluate(590)).toBeLessThan(0);
    for (const v of r.mw) {
      expect(Number.isFinite(v)).toBe(true);
      expect(v).toBeGreaterThan(0);
    }
    // Monotone: lower bands have smaller molecular weights.
    for (let i = 1; i < r.mw.length; i++) expect(r.mw[i]).toBeLessThan(r.mw[i - 1]);
  });

  it('ignores non-finite weights when computing the tolerance', () => {
    const t = mwTolerance(
      [
        { mw: [1000, 100], profile: [] },
        { mw: [NaN, 500], profile: [] },
      ],
      10,
    );
    expect(t).toBeCloseTo(90);
  });
});

describe('similarity edge cases', () => {
  it('gives distance 0 to identical flat or empty profiles', () => {
    const flat = { mw: [], profile: [5, 5, 5, 5] };
    const zero = { mw: [], profile: [0, 0, 0, 0] };
    expect(laneDistance('Pearson correlation', flat, { ...flat }, 0)).toBe(0);
    expect(laneDistance('Cosine coefficient', zero, { ...zero }, 0)).toBe(0);
    expect(laneDistance('Pearson correlation', flat, { mw: [], profile: [1, 9, 1, 9] }, 0)).toBe(1);
  });

  it('matches bands optimally (and symmetrically) within the tolerance', () => {
    expect(commonBands([10, 11], [11, 9.5], 1)).toBe(2);
    expect(commonBands([11, 9.5], [10, 11], 1)).toBe(2);
    expect(commonBands([100, 200], [100, 200], 0)).toBe(2);
    expect(commonBands([], [1], 5)).toBe(0);
  });
});

describe('clustering', () => {
  function randomMatrix(n: number, seed: number): number[][] {
    let s = seed;
    const rnd = () => ((s = (s * 1664525 + 1013904223) >>> 0) / 4294967296);
    const pts = Array.from({ length: n }, () => [rnd(), rnd()]);
    return pts.map((a) => pts.map((b) => Math.hypot(a[0] - b[0], a[1] - b[1])));
  }

  /** Brute-force mean linkage: mean of all pairwise distances inside the union. */
  function meanLinkageHeights(d: number[][]): number[] {
    let clusters = d.map((_, i) => [i]);
    const heights: number[] = [];
    const within = (m: number[]) => {
      let s = 0;
      for (let a = 0; a < m.length; a++) for (let b = a + 1; b < m.length; b++) s += d[m[a]][m[b]];
      return s / ((m.length * (m.length - 1)) / 2);
    };
    while (clusters.length > 1) {
      let best = Infinity;
      let bi = 0;
      let bj = 1;
      for (let i = 0; i < clusters.length; i++)
        for (let j = i + 1; j < clusters.length; j++) {
          const v = within([...clusters[i], ...clusters[j]]);
          if (v < best) [best, bi, bj] = [v, i, j];
        }
      heights.push(best);
      clusters = [...clusters.filter((_, k) => k !== bi && k !== bj), [...clusters[bi], ...clusters[bj]]];
    }
    return heights.sort((a, b) => a - b);
  }

  function heights(node: ReturnType<typeof cluster>): number[] {
    const out: number[] = [];
    const rec = (n: typeof node) => {
      if (!n.left || !n.right) return;
      out.push(n.height);
      rec(n.left);
      rec(n.right);
    };
    rec(node);
    return out.sort((a, b) => a - b);
  }

  it('implements mean linkage as the mean distance inside the merged cluster', () => {
    const d = randomMatrix(14, 3);
    const got = heights(cluster(d, 'Mean linkage'));
    meanLinkageHeights(d).forEach((h, i) => expect(got[i]).toBeCloseTo(h, 10));
  });

  it('clusters identical lanes at height 0 with every linkage', () => {
    const lanes = [
      { mw: [500, 300], profile: [] },
      { mw: [500, 300], profile: [] },
      { mw: [500, 300], profile: [] },
      { mw: [900], profile: [] },
    ];
    const d = distanceMatrix('Dice', lanes, 0);
    for (const l of ['UPGMA', 'UPGMC', 'Single linkage', 'Complete linkage', 'Mean linkage', 'Ward'] as Linkage[]) {
      const c = cophenetic(cluster(d, l), 4);
      expect(c[0][1]).toBe(0);
      expect(c[1][2]).toBe(0);
      expect(c[0][3]).toBeGreaterThan(0);
    }
  });
});
