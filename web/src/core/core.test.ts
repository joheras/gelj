import { describe, expect, it } from 'vitest';
import { cluster, copheneticCorrelation, leafOrder, toNewick } from './cluster';
import { fitCurve } from './curvefit';
import { detectBands, detectLanes, laneProfile, makeLanes } from './detect';
import { createImage, crop, enhanceContrast, flipHorizontal, rotate90 } from './image';
import { BUILTIN_MARKERS } from './markers';
import { normaliseLanes } from './normalise';
import { findMaxima } from './peaks';
import { distanceMatrix, laneDistance, mwTolerance } from './similarity';
import { naturalSpline } from './spline';
import { referencePosition, SAMPLE_PROFILES, syntheticGel } from './synthetic';

const ladder = BUILTIN_MARKERS[0];

describe('image', () => {
  it('rotates and flips', () => {
    const img = createImage(3, 2);
    img.data.set([1, 2, 3, 4, 5, 6]);
    expect(Array.from(rotate90(img, true).data)).toEqual([3, 6, 2, 5, 1, 4]);
    expect(Array.from(rotate90(img, false).data)).toEqual([4, 1, 5, 2, 6, 3]);
    expect(Array.from(flipHorizontal(img).data)).toEqual([3, 2, 1, 6, 5, 4]);
    expect(Array.from(crop(img, { x: 1, y: 0, w: 2, h: 2 }).data)).toEqual([2, 3, 5, 6]);
  });

  it('stretches contrast', () => {
    const img = createImage(100, 1);
    for (let i = 0; i < 100; i++) img.data[i] = 50 + i;
    const out = enhanceContrast(img);
    expect(out.data[0]).toBe(0);
    expect(out.data[99]).toBe(255);
  });
});

describe('peaks', () => {
  it('finds maxima like ImageJ, strongest first', () => {
    const p = [0, 1, 5, 1, 0, 0, 9, 0, 0, 3, 0];
    expect(findMaxima(p, 2, true)).toEqual([6, 2, 9]);
  });
});

describe('curve fitting', () => {
  it('fits polynomials exactly', () => {
    const x = [1, 2, 3, 4, 5];
    const y = x.map((v) => 2 - 3 * v + 0.5 * v * v);
    const r = fitCurve(x, y, 1);
    expect(r.rSquared).toBeCloseTo(1, 8);
    expect(r.evaluate(10)).toBeCloseTo(2 - 30 + 50, 6);
  });

  it('fits the built-in marker migration models', () => {
    for (const m of BUILTIN_MARKERS) {
      const r = fitCurve(m.refPositions, m.mw, m.fitMethod);
      expect(r.rSquared).toBeGreaterThan(0.97);
    }
  });
});

describe('spline', () => {
  it('interpolates its knots', () => {
    const s = naturalSpline([0, 10, 25, 40], [1, -2, 4, 0]);
    expect(s(10)).toBeCloseTo(-2);
    expect(s(25)).toBeCloseTo(4);
  });
});

describe('gel pipeline', () => {
  const gel = syntheticGel(ladder, SAMPLE_PROFILES);

  it('detects every lane', () => {
    const lanes = detectLanes(gel.image, true);
    expect(lanes.length).toBe(gel.centres.length);
    lanes.forEach((r, i) => expect(Math.abs(r.x + r.w / 2 - gel.centres[i])).toBeLessThan(6));
  });

  it('detects bands and maps them onto the marker reference after normalisation', () => {
    const lanes = makeLanes(detectLanes(gel.image, true));
    lanes.forEach((lane, i) => {
      lane.isReference = gel.isMarker[i];
      lane.bands = detectBands(laneProfile(gel.image, lane.rect, true), 120);
      expect(lane.bands.length).toBe(gel.bands[i].length);
    });
    const result = normaliseLanes(gel.image, true, lanes, ladder, ladder.mw);
    result.forEach((r, i) => {
      if (gel.isMarker[i]) return;
      r.positions.forEach((p, k) => {
        expect(Math.abs(p - referencePosition(ladder, gel.mw[i][k]))).toBeLessThan(4);
      });
      expect(r.profile.length).toBe(ladder.refHeight);
    });
  });
});

describe('similarity and clustering', () => {
  const lanes = SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] as number[] }));
  const tol = mwTolerance(lanes, 1);

  it('computes Dice', () => {
    expect(laneDistance('Dice', lanes[0], lanes[1], tol)).toBe(0);
    expect(laneDistance('Dice', lanes[0], lanes[2], tol)).toBeCloseTo(1 - (2 * 4) / 11);
  });

  it('clusters identical lanes first', () => {
    const d = distanceMatrix('Dice', lanes, tol);
    for (const link of ['UPGMA', 'UPGMC', 'Single linkage', 'Complete linkage', 'Mean linkage', 'Ward'] as const) {
      const tree = cluster(d, link);
      const order = leafOrder(tree);
      expect(order.slice().sort()).toEqual([0, 1, 2, 3, 4, 5]);
      expect(Math.abs(order.indexOf(0) - order.indexOf(1))).toBe(1);
      expect(Math.abs(order.indexOf(3) - order.indexOf(4))).toBe(1);
    }
    const tree = cluster(d, 'UPGMA');
    expect(copheneticCorrelation(d, tree)).toBeGreaterThan(0.8);
    expect(toNewick(tree, ['a', 'b', 'c', 'd', 'e', 'f'])).toMatch(/;$/);
  });
});
