import { describe, expect, it } from 'vitest';
import { fitCurve } from './curvefit';
import { chunk, dataUrlBytes, describeStep, fitParameters, formatDateTime, formatNumber, sha256Hex, sha256OfDataUrl } from './report';

describe('report helpers', () => {
  it('decodes base64 and plain data URLs', () => {
    expect(Array.from(dataUrlBytes('data:application/octet-stream;base64,AAH/'))).toEqual([0, 1, 255]);
    expect(new TextDecoder().decode(dataUrlBytes('data:text/plain,a%20b'))).toBe('a b');
    expect(() => dataUrlBytes('nope')).toThrow();
  });

  it('computes SHA-256 checksums', async () => {
    expect(await sha256Hex(new TextEncoder().encode('abc'))).toBe('ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad');
    // "abc" in base64 is YWJj.
    expect(await sha256OfDataUrl('data:text/plain;base64,YWJj')).toBe('ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad');
  });

  it('describes preprocessing steps', () => {
    expect(describeStep({ op: 'crop', params: { x: 1, y: 2, w: 30, h: 40 } })).toBe('Crop to the rectangle x = 1, y = 2, width 30, height 40 px');
    expect(describeStep({ op: 'subtractBackground', params: { radius: 50, lightBackground: true } })).toContain('radius 50 px, light background');
    expect(describeStep({ op: 'gamma', params: { gamma: 0.8 } })).toBe('Gamma 0.8');
    expect(describeStep({ op: 'invert' })).toBe('Invert');
    expect(describeStep({ op: 'sharpen', params: { amount: 2 } })).toBe('sharpen (amount = 2)');
  });

  it('formats fit parameters', () => {
    const fit = fitCurve([1, 2, 3], [3, 5, 7], 0);
    expect(fitParameters(fit)).toBe('a = 1, b = 2');
  });

  it('formats numbers and dates unambiguously', () => {
    expect(formatNumber(6473860000000000)).toBe('6.47386e+15');
    expect(formatNumber(0.0001234)).toBe('1.23400e-4');
    expect(formatNumber(1445.0912)).toBe('1445.09');
    expect(formatNumber(0)).toBe('0');
    expect(formatDateTime(new Date(2026, 10, 3, 9, 5, 7), true)).toMatch(/^2026-11-03 09:05:07 \(UTC[+-]\d\d:\d\d\)$/);
  });

  it('splits lists in blocks', () => {
    expect(chunk([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]]);
    expect(chunk([], 3)).toEqual([]);
  });
});
