import { describe, expect, it } from 'vitest';
import { hitsCsv, identify, pairBands, summaryCsv, verdict, type Reference } from './identify';
import { commonBands, laneDistance, mwTolerance } from './similarity';

const lane = (mw: number[], profile: number[] = []) => ({ mw, profile });

const library: Reference<string>[] = [
  { item: 'A1', type: 'A', lane: lane([1000, 800, 500, 300, 100]) },
  { item: 'B1', type: 'B', lane: lane([950, 600, 400, 200]) },
  { item: 'A2', type: 'A', lane: lane([1000, 800, 500, 300]) },
  { item: 'C1', type: 'C', lane: lane([]) },
];

describe('band pairing', () => {
  it('pairs as many bands as commonBands counts and lists the unmatched ones', () => {
    const a = [1000, 805, 500, 120, NaN];
    const b = [100, 498, 800, 990, 700];
    for (const tol of [0, 5, 12, 25, 200]) {
      const p = pairBands(a, b, tol);
      expect(p.pairs.length).toBe(commonBands(a, b, tol));
      expect(p.pairs.length + p.queryOnly.length).toBe(a.length);
      expect(p.pairs.length + p.refOnly.length).toBe(b.length);
      for (const [i, j] of p.pairs) expect(Math.abs(a[i] - b[j])).toBeLessThanOrEqual(tol);
    }
    const p = pairBands(a, b, 12);
    expect(p.pairs).toEqual([
      [0, 3],
      [1, 2],
      [2, 1],
    ]);
    expect(p.queryOnly).toEqual([3, 4]);
    expect(p.refOnly).toEqual([0, 4]);
  });
});

describe('identification', () => {
  it('ranks the library from the closest entry with the chosen coefficient', () => {
    const query = lane([1000, 800, 500, 300]);
    const r = identify(query, library, { similarity: 'Dice', tolerance: 1, cutoff: null });
    expect(r.coefficient).toBe(true);
    expect(r.hits.map((h) => h.item)).toEqual(['A2', 'A1', 'B1', 'C1']);
    expect(r.hits[0].similarity).toBeCloseTo(100);
    expect(r.hits[1].similarity).toBeCloseTo((100 * 8) / 9);
    expect(r.hits[1].bands.refOnly).toEqual([4]);
    expect(r.hits[3].similarity).toBe(0);
    // Tolerance from the range of the query and the whole library, as on the comparison page.
    expect(r.tolerance).toBeCloseTo(mwTolerance([query, ...library.map((l) => l.lane)], 1));
    for (const h of r.hits) expect(h.distance).toBeCloseTo(laneDistance('Dice', query, h.lane, r.tolerance));
  });

  it('uses the tolerance to match bands of lanes normalised with different markers', () => {
    // Molecular weights estimated with another marker differ by a few units.
    const query = lane([1008, 795, 503, 297]);
    const strict = identify(query, library, { similarity: 'Jaccard', tolerance: 0.1, cutoff: null });
    const loose = identify(query, library, { similarity: 'Jaccard', tolerance: 1.5, cutoff: null });
    expect(strict.hits[0].similarity).toBeLessThan(50);
    expect(loose.hits[0].item).toBe('A2');
    expect(loose.hits[0].similarity).toBeCloseTo(100);
  });

  it('applies the cut-off and reports the identified type and the closest other type', () => {
    const query = lane([1000, 800, 500, 305, 100]);
    const r = identify(query, library, { similarity: 'Dice', tolerance: 1, cutoff: 85 });
    expect(r.hits.map((h) => h.passes)).toEqual([true, true, false, false]);
    const v = verdict(r);
    expect(v.type).toBe('A');
    expect(v.best?.item).toBe('A1');
    expect(v.runnerUp?.type).toBe('B');
    expect(v.passing).toBe(2);
    const none = verdict(identify(query, library, { similarity: 'Dice', tolerance: 0, cutoff: 100 }));
    expect(none.type).toBeNull();
    expect(none.best?.item).toBe('A1');
    expect(verdict(identify(query, [], { similarity: 'Dice', tolerance: 1, cutoff: null }))).toEqual({
      best: null,
      type: null,
      runnerUp: null,
      passing: 0,
    });
  });

  it('ranks by distance for curve-based measures with a distance cut-off', () => {
    const refs: Reference<string>[] = [
      { item: 'far', type: 'X', lane: lane([], [0, 255, 0, 255]) },
      { item: 'near', type: 'Y', lane: lane([], [10, 0, 250, 0, 10, 0, 250, 0]) },
    ];
    const r = identify(lane([], [0, 0, 255, 0, 0, 0, 255, 0]), refs, { similarity: 'Euclidean distance', tolerance: 1, cutoff: 0.1 });
    expect(r.coefficient).toBe(false);
    expect(r.hits.map((h) => h.item)).toEqual(['near', 'far']);
    expect(r.hits[0].similarity).toBeNull();
    expect(r.hits.map((h) => h.passes)).toEqual([true, false]);
  });

  it('exports the ranked hits and a batch summary as CSV', () => {
    const queries = [lane([1000, 800, 500, 305, 100]), lane([950, 600, 400, 200])].map((l, i) => ({
      name: `Q"${i + 1}`,
      lane: l,
      result: identify(l, library, { similarity: 'Dice', tolerance: 1, cutoff: 80 }),
    }));
    const hits = hitsCsv(queries, (s) => s, 2).split('\n');
    expect(hits).toHaveLength(5);
    expect(hits[1]).toBe('"Q""1",1,"A","A1",100.00,0.00000,yes,5,5,5,"",""');
    expect(hits[2]).toMatch(/^"Q""1",2,"A","A2",88\.89,[\d.]+,yes,4,5,4,"100",""$/);
    const summary = summaryCsv(queries, (s) => s).split('\n');
    expect(summary).toHaveLength(3);
    expect(summary[0]).toMatch(/^"Query","Identified type"/);
    expect(summary[1]).toMatch(/^"Q""1","A","A1","A",100\.00,0\.00000,5,"B",[\d.]+,[\d.]+,2$/);
    expect(summary[2]).toMatch(/^"Q""2","B","B1","B",100\.00/);
  });
});
