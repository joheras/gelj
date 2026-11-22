import { describe, expect, it } from 'vitest';
import { parseBackup } from './backup';

const img = 'data:image/png;base64,AAAA';

describe('backup import', () => {
  it('rejects files that are not GelJ Web exports', () => {
    expect(() => parseBackup(null)).toThrow(/not a GelJ Web export/);
    expect(() => parseBackup({ format: 'other' })).toThrow(/not a GelJ Web export/);
  });

  it('names the malformed record instead of failing later', () => {
    expect(() => parseBackup({ format: 'gelj-web', experiments: [{ id: 'a', image: img, lanes: {} }] })).toThrow(/experiments record #1/);
    expect(() =>
      parseBackup({ format: 'gelj-web', experiments: [{ id: 'a', image: img, lanes: [{ id: 'l', rect: { x: 0 } }] }] }),
    ).toThrow(/rectangle/);
    expect(() => parseBackup({ format: 'gelj-web', markers: [{ id: 'm', mw: [1, 2], refPositions: [1] }] })).toThrow(/markers record #1/);
    expect(() => parseBackup({ format: 'gelj-web', experiments: 'x' })).toThrow(/not a list/);
  });

  it('fills in fields missing from older backups', () => {
    const b = parseBackup({
      format: 'gelj-web',
      version: 1,
      experiments: [
        {
          id: 'e',
          name: 'Gel',
          image: img,
          status: 'complete',
          lanes: [{ id: 'l1', rect: { x: 1, y: 2, w: 3, h: 4 }, bands: [9, 3] }],
          normalised: [{ laneId: 'l1', image: img, positions: [3, 9], mw: [500, null] }],
        },
      ],
    });
    const e = b.experiments[0];
    expect(e.labId).toBeNull();
    expect(e.originalImage).toBe(img);
    expect(e.lanes[0].bands).toEqual([3, 9]);
    expect(e.lanes[0].meta.strain).toBe('');
    expect(e.normalised[0].mw[1]).toBeNaN();
    expect(b.labs).toEqual([]);
    expect(b.comparisons).toEqual([]);
  });

  it('marks complete experiments without normalised lanes as drafts and drops built-in markers', () => {
    const b = parseBackup({
      format: 'gelj-web',
      experiments: [{ id: 'e', image: img, status: 'complete', lanes: [] }],
      markers: [{ id: 'm', builtin: true, mw: [2, 1], refPositions: [1, 2], refWidth: 1, refHeight: 1 }],
    });
    expect(b.experiments[0].status).toBe('draft');
    expect(b.markers).toEqual([]);
  });
});
