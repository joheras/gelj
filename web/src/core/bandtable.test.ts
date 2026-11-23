import { describe, expect, it } from 'vitest';
import matrixFile from '../../public/samples/band-matching-table.txt?raw';
import longFile from '../../public/samples/bands-one-row-per-band.csv?raw';
import wideFile from '../../public/samples/bands-one-row-per-lane.csv?raw';
import {
  bandPositions,
  detectDelimiter,
  guessMapping,
  montage,
  parseDelimited,
  parseNumber,
  parseNumberList,
  readBandTable,
  renderBandLane,
} from './bandtable';
import { laneDistance } from './similarity';

const IMP01 = [2610, 1395, 948, 612, 331, 181];

describe('delimited text', () => {
  it('detects tabs, semicolons and commas', () => {
    expect(detectDelimiter('a\tb\tc\n1\t2\t3')).toBe('\t');
    expect(detectDelimiter('a;b;c\n1,5;2,5;3')).toBe(';');
    expect(detectDelimiter('a,b,c\n"x; y",2,3')).toBe(',');
  });

  it('handles quotes, BOM, CRLF, blank lines and trailing empty columns', () => {
    const t = parseDelimited('﻿Lane,Note,,\r\n"A, 1","say ""hi""",,\r\n\r\nB,"two\nlines",,\r\n');
    expect(t.rows).toEqual([
      ['Lane', 'Note'],
      ['A, 1', 'say "hi"'],
      ['B', 'two\nlines'],
    ]);
  });

  it('parses numbers with decimal commas and units', () => {
    expect(parseNumber('1200')).toBe(1200);
    expect(parseNumber('2,61')).toBeCloseTo(2.61);
    expect(parseNumber('850 bp')).toBe(850);
    expect(parseNumber('1.5kb')).toBe(1.5);
    expect(parseNumber('1,200,000')).toBe(1200000);
    expect(parseNumber('n/a')).toBeNaN();
    expect(parseNumber('')).toBeNaN();
    expect(parseNumberList('1200; 850 | 400')).toEqual([1200, 850, 400]);
    expect(parseNumberList('1200 850')).toEqual([1200, 850]);
  });
});

describe('band table layouts', () => {
  it('reads one row per band (GelJ band CSV) with the lane country, not the laboratory one', () => {
    const t = parseDelimited(longFile);
    const m = guessMapping(t);
    expect(m.layout).toBe('long');
    expect(t.rows[0][m.bands[0]]).toBe('molecular_weight');
    expect(t.rows[0][m.lane]).toBe('lane');
    expect(m.meta.country).toBe(8);
    const r = readBandTable(t, m);
    expect(r.issues.filter((i) => i.level === 'error')).toEqual([]);
    expect(r.lanes).toHaveLength(6);
    expect(r.lanes[0]).toMatchObject({ name: 'IMP-01', lab: 'Hospital A', isReference: false, values: IMP01 });
    expect(r.lanes[0].meta).toMatchObject({ strain: 'EC-101', genus: 'Escherichia', species: 'coli', country: 'Spain', mlst: 'ST131' });
    expect(r.lanes[4].meta.country).toBe('Portugal');
  });

  it('reads one row per lane with numbered band columns in kb and decimal commas', () => {
    const t = parseDelimited(wideFile);
    expect(t.delimiter).toBe(';');
    const m = guessMapping(t);
    expect(m.layout).toBe('wide');
    expect(m.bands).toHaveLength(6);
    expect(m.scale).toBe(1000);
    expect(m.meta.mlst).toBe(5);
    const r = readBandTable(t, m);
    expect(r.issues).toEqual([]);
    expect(r.lanes.map((l) => l.name)).toEqual(['IMP-01', 'IMP-02', 'IMP-03', 'IMP-04', 'IMP-05', 'IMP-06']);
    r.lanes[0].values.forEach((v, i) => expect(v).toBeCloseTo(IMP01[i]));
    expect(r.lanes[1].values).toHaveLength(5);
  });

  it('reads a band-matching (binary character) table with band classes as headers', () => {
    const t = parseDelimited(matrixFile);
    expect(t.delimiter).toBe('\t');
    const m = guessMapping(t);
    expect(m.layout).toBe('matrix');
    expect(m.values).toBe('mw');
    expect(t.rows[0][m.lane]).toBe('Key');
    const r = readBandTable(t, m);
    expect(r.issues).toEqual([]);
    expect(r.lanes[0].values).toHaveLength(6);
    expect(r.lanes[0].values[0]).toBe(2600);
    expect(r.lanes[0].meta.strain).toBe('EC-101');
  });

  it('gives the same similarities whatever the layout', () => {
    const read = (text: string) => {
      const t = parseDelimited(text);
      return readBandTable(t, guessMapping(t)).lanes.map((l) => ({ mw: l.values, profile: [] }));
    };
    const [a, b, c] = [read(longFile), read(wideFile), read(matrixFile)];
    for (let i = 0; i < 6; i++)
      for (let j = 0; j < 6; j++) {
        const d = laneDistance('Dice', a[i], a[j], 30);
        expect(laneDistance('Dice', b[i], b[j], 30)).toBeCloseTo(d);
        expect(laneDistance('Dice', c[i], c[j], 30)).toBeCloseTo(d);
      }
  });

  it('accepts a single cell listing the bands, and empty lane labels continuing the previous lane', () => {
    const wide = parseDelimited('Sample,Bands\nA,"1200; 800; 400"\nB,"1200 600"\n');
    const mw = guessMapping(wide);
    expect(mw.layout).toBe('wide');
    expect(readBandTable(wide, mw).lanes.map((l) => l.values)).toEqual([
      [1200, 800, 400],
      [1200, 600],
    ]);
    const long = parseDelimited('Key\tSize (kb)\nA\t1.2\n\t0.4\nB\t0.6\nB\t1.2\n');
    const ml = guessMapping(long);
    expect(ml.layout).toBe('long');
    expect(ml.scale).toBe(1000);
    expect(readBandTable(long, ml).lanes.map((l) => [l.name, l.values])).toEqual([
      ['A', [1200, 400]],
      ['B', [1200, 600]],
    ]);
  });

  it('reads GelJ desktop population-genetics matrices (PAST: "Locus1(2600.5)" headers, unnamed lane column)', () => {
    const t = parseDelimited(',Locus1(2600.5),Locus2(1400),Locus3(950.25)\nS1,1,0,1\nS2,0,1,1\n');
    const m = guessMapping(t);
    expect(m.layout).toBe('matrix');
    expect(m.lane).toBe(0);
    expect(readBandTable(t, m).lanes.map((l) => [l.name, l.values])).toEqual([
      ['S1', [2600.5, 950.25]],
      ['S2', [1400, 950.25]],
    ]);
  });

  it('treats band-class headers in % as positions', () => {
    const t = parseDelimited('Key,12.5,40.2,77.9\nA,1,0,1\nB,+,,x\n');
    const m = guessMapping(t);
    expect(m.values).toBe('position');
    expect(readBandTable(t, m).lanes.map((l) => l.values)).toEqual([
      [12.5, 77.9],
      [12.5, 77.9],
    ]);
  });

  it('skips a header preamble', () => {
    const t = parseDelimited('Exported 2020-01-01\n\nLane,MW\nA,500\nA,300\n');
    const m = guessMapping(t);
    expect(m.headerRow).toBe(1);
    expect(readBandTable(t, m).lanes).toEqual([expect.objectContaining({ name: 'A', values: [500, 300] })]);
  });
});

describe('validation', () => {
  it('reports non-numeric and non-positive band values with their line', () => {
    const t = parseDelimited('Lane,MW\nA,500\nA,abc\nA,-3\n');
    const r = readBandTable(t, guessMapping(t));
    expect(r.issues.filter((i) => i.level === 'error').map((i) => i.message)).toEqual([
      'Line 3: "abc" is not a number.',
      'Line 4: molecular weight -3 must be positive.',
    ]);
    expect(r.lanes[0].values).toEqual([500]);
  });

  it('warns about lanes without bands, duplicates, conflicting metadata and unknown cells', () => {
    const t = parseDelimited('Key,Strain,1000,500\nA,s1,1,?\nB,s2,0,0\nA,s3,0,1\n');
    const r = readBandTable(t, guessMapping(t));
    const msgs = r.issues.map((i) => i.message).join('\n');
    expect(r.issues.every((i) => i.level === 'warning')).toBe(true);
    expect(msgs).toMatch(/"B" has no bands/);
    expect(msgs).toMatch(/"A" appears more than once/);
    expect(msgs).toMatch(/several values for strain/);
    expect(msgs).toMatch(/1 cells were neither/);
    expect(r.lanes[0].values).toEqual([1000, 500]);
  });

  it('rejects mappings without band columns or lanes, and positions over 100%', () => {
    const t = parseDelimited('Lane,MW\nA,500\n');
    expect(readBandTable(t, { ...guessMapping(t), bands: [] }).issues[0].level).toBe('error');
    expect(readBandTable(parseDelimited('Lane,MW\n'), guessMapping(t)).issues.some((i) => /No lanes/.test(i.message))).toBe(true);
    const p = readBandTable(t, { ...guessMapping(t), values: 'position' });
    expect(p.issues.some((i) => /0–100/.test(i.message))).toBe(true);
  });
});

describe('synthetic lanes', () => {
  it('places large fragments at the top on a log scale spanning the import', () => {
    const [a, b] = bandPositions([[1000, 100], [316.23]], 'mw', 100);
    expect(a[0]).toBeCloseTo(5);
    expect(a[1]).toBeCloseTo(95);
    expect(b[0]).toBeCloseTo(50, 0);
    expect(bandPositions([[25, 50]], 'position', 200)).toEqual([[50, 100]]);
  });

  it('draws bright bands where the bands are', () => {
    const img = renderBandLane([20], 10, 40);
    expect(img.data[20 * 10 + 5]).toBeGreaterThan(200);
    expect(img.data[5 * 10 + 5]).toBeLessThan(30);
    const m = montage([img, img], 2);
    expect(m.width).toBe(26);
    expect(m.data[20 * 26 + 2 + 5]).toBe(img.data[20 * 10 + 5]);
  });
});
