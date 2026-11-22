import { describe, expect, it } from 'vitest';
import { bandMatrixCsv, bandMatrixNexus, matchBands } from './bandmatch';
import { binaryDistance, Bootstrap, bootstrapData, replicateDistances, seededRandom } from './bootstrap';
import { cluster, leafOrder, toNewick, type ClusterNode } from './cluster';
import { gamma, median, rollingBallBackground, subtractBackground } from './filters';
import { createImage } from './image';
import { distanceMatrix, laneDistance, mwTolerance } from './similarity';
import { SAMPLE_PROFILES } from './synthetic';
import { cutTree, similarityToHeight } from './treecut';

/** Dark-background image with a linear background ramp and one bright 5x5 spot. */
function rampWithSpot(w: number, h: number) {
  const img = createImage(w, h);
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) img.data[y * w + x] = 20 + (60 * x) / w;
  for (let y = 20; y < 25; y++) for (let x = 30; x < 35; x++) img.data[y * w + x] += 120;
  return img;
}

describe('filters', () => {
  it('removes a smooth background and keeps small bands (dark background)', () => {
    const img = rampWithSpot(80, 50);
    const out = subtractBackground(img, 15, false);
    expect(out.data[5 * 80 + 5]).toBeLessThanOrEqual(4);
    expect(out.data[5 * 80 + 75]).toBeLessThanOrEqual(4);
    expect(out.data[22 * 80 + 32]).toBeGreaterThan(110);
  });

  it('handles light backgrounds and never puts the background above the image', () => {
    const dark = rampWithSpot(60, 40);
    const light = createImage(60, 40);
    for (let i = 0; i < light.data.length; i++) light.data[i] = 255 - dark.data[i];
    const bg = rollingBallBackground(light, 8, true);
    for (let i = 0; i < light.data.length; i++) expect(bg.data[i]).toBeGreaterThanOrEqual(light.data[i]);
    const out = subtractBackground(light, 8, true);
    expect(out.data[2 * 60 + 2]).toBeGreaterThanOrEqual(253);
    expect(out.data[22 * 60 + 32]).toBeLessThan(140);
  });

  it('applies gamma and median', () => {
    const img = createImage(3, 3, 0);
    img.data[4] = 255; // single hot pixel
    expect(median(img).data[4]).toBe(0);
    const g = createImage(1, 1, 64);
    expect(gamma(g, 1).data[0]).toBe(64);
    expect(gamma(g, 0.5).data[0]).toBe(Math.round(255 * Math.sqrt(64 / 255)));
  });
});

describe('band matching', () => {
  it('groups bands within the tolerance into classes, one band per lane', () => {
    const m = matchBands([[1000, 500], [1004, 498, 200], [503]], 10);
    expect(m.classes.length).toBe(3);
    expect(m.classes[0]).toBeCloseTo(1002);
    expect(m.matrix).toEqual([
      [1, 1, 0],
      [1, 1, 1],
      [0, 1, 0],
    ]);
  });

  it('never puts two bands of the same lane in one class', () => {
    const m = matchBands([[100, 98]], 10);
    expect(m.matrix).toEqual([[1, 1]]);
  });

  it('agrees with the pairwise band-matching coefficients on the sample profiles', () => {
    const lanes = SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] as number[] }));
    const tol = mwTolerance(lanes, 1);
    const m = matchBands(SAMPLE_PROFILES, tol);
    for (let i = 0; i < lanes.length; i++)
      for (let j = 0; j < lanes.length; j++)
        expect(binaryDistance('Dice', m.matrix[i], m.matrix[j])).toBeCloseTo(laneDistance('Dice', lanes[i], lanes[j], tol));
  });

  it('exports CSV and NEXUS', () => {
    const m = matchBands([[1000, 500], [500]], 5);
    const csv = bandMatrixCsv(m, ['a', 'b "x"'], [{ header: 'Strain', values: ['S1', 'S2'] }]);
    expect(csv.split('\n')).toEqual(['"Lane","Strain",1000,500', '"a","S1",1,1', '"b ""x""","S2",0,1']);
    const nex = bandMatrixNexus(m, ['a', "b's"]);
    expect(nex).toContain('DIMENSIONS NTAX=2;');
    expect(nex).toContain('DIMENSIONS NCHAR=2;');
    expect(nex).toContain("'b''s' 01");
  });
});

describe('tree cut', () => {
  const lanes = SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] as number[] }));
  const d = distanceMatrix('Dice', lanes, mwTolerance(lanes, 1));
  const tree = cluster(d, 'UPGMA');

  it('defines clusters at a similarity cut-off', () => {
    const all = cutTree(tree, similarityToHeight(100));
    expect(all.groups[0]).toBe(all.groups[1]); // identical lanes
    expect(all.groups[3]).not.toBe(all.groups[4]); // 3 and 4 differ by one band
    const loose = cutTree(tree, similarityToHeight(70)); // lanes 0 and 2 are 72.7% similar
    expect(loose.groups[3]).toBe(loose.groups[4]);
    expect(loose.groups[0]).toBe(loose.groups[2]);
    expect(loose.sizes.reduce((a, b) => a + b, 0)).toBe(6);
    expect(cutTree(tree, similarityToHeight(0)).count).toBe(1);
  });

  it('numbers clusters in dendrogram order', () => {
    const cut = cutTree(tree, similarityToHeight(100));
    const order = leafOrder(tree).map((i) => cut.groups[i]);
    expect(order[0]).toBe(1);
    for (let k = 1; k < order.length; k++) expect(order[k] - order[k - 1]).toBeGreaterThanOrEqual(0);
    expect(cut.count).toBe(Math.max(...cut.groups));
  });
});

describe('bootstrap', () => {
  it('is reproducible and gives full support to clear groups', () => {
    const m = matchBands(SAMPLE_PROFILES, mwTolerance(SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] })), 1));
    const d = distanceMatrix('Dice', SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] })), mwTolerance(SAMPLE_PROFILES.map((mw) => ({ mw, profile: [] })), 1));
    const tree = cluster(d, 'UPGMA');
    const bs = new Bootstrap(tree, 'Dice', 'UPGMA', bootstrapData('Dice', m.matrix, []), 42);
    bs.run(50);
    expect(bs.replicates).toBe(50);
    const support = bs.support();
    const find = (n: ClusterNode, a: number, b: number): ClusterNode | undefined => {
      if (!n.left || !n.right) return undefined;
      const ls = leafOrder(n);
      if (ls.length === 2 && ls.includes(a) && ls.includes(b)) return n;
      return find(n.left, a, b) ?? find(n.right, a, b);
    };
    const pair = find(tree, 0, 1)!;
    expect(support.get(pair)).toBe(100); // identical lanes always cluster together
    expect(support.get(tree)).toBe(100); // the root contains every lane
    for (const v of support.values()) expect(v).toBeGreaterThanOrEqual(0);
    const again = new Bootstrap(tree, 'Dice', 'UPGMA', bootstrapData('Dice', m.matrix, []), 42);
    again.run(50);
    expect([...again.support().values()]).toEqual([...support.values()]);
    expect(toNewick(tree, ['a', 'b', 'c', 'd', 'e', 'f'], support)).toMatch(/\)100;$/);
  });

  it('resamples profile points for curve-based measures', () => {
    const profiles = [
      [0, 10, 50, 10, 0, 0],
      [0, 12, 48, 9, 0, 0],
      [40, 5, 0, 0, 30, 60],
    ];
    const data = bootstrapData('Pearson correlation', [], profiles);
    const d = replicateDistances('Pearson correlation', data, seededRandom(3));
    expect(d.length).toBe(3);
    expect(d[0][0]).toBe(0);
    expect(d[0][1]).toBeLessThan(d[0][2]);
  });
});
