// Synthetic gel generator, used by the tests and by the "Load sample gel" demo.
import { createImage } from './image';
import type { GrayImage, Marker } from './types';

export interface SyntheticGel {
  image: GrayImage;
  /** Lane centres (x). */
  centres: number[];
  /** Band positions (absolute y) of each lane. */
  bands: number[][];
  /** Which lanes are marker lanes. */
  isMarker: boolean[];
  /** True molecular weight of each band. */
  mw: number[][];
}

function rng(seed: number) {
  let s = seed >>> 0;
  return () => {
    s = (s * 1664525 + 1013904223) >>> 0;
    return s / 4294967296;
  };
}

/** Position of `mw` in the marker reference lane (log-linear interpolation between marker bands). */
export function referencePosition(marker: Marker, mw: number): number {
  const pos = marker.refPositions;
  const logMw = marker.mw.map(Math.log);
  const l = Math.log(mw);
  for (let i = 0; i < logMw.length - 1; i++) {
    if (l <= logMw[i] && l >= logMw[i + 1]) {
      const t = (logMw[i] - l) / (logMw[i] - logMw[i + 1]);
      return pos[i] + t * (pos[i + 1] - pos[i]);
    }
  }
  return l > logMw[0] ? pos[0] : pos[pos.length - 1];
}

/**
 * Builds a dark-background gel with `profiles.length` sample lanes, a marker lane
 * at both ends and in the middle, and a slight "smile" distortion.
 */
export function syntheticGel(
  marker: Marker,
  profiles: number[][],
  opts: { laneWidth?: number; gap?: number; height?: number; smile?: number; seed?: number; noise?: number } = {},
): SyntheticGel {
  const laneWidth = opts.laneWidth ?? 34;
  const gap = opts.gap ?? 14;
  const height = opts.height ?? 480;
  const smile = opts.smile ?? 10;
  const noise = opts.noise ?? 6;
  const random = rng(opts.seed ?? 7);
  const top = 30;
  const span = height - 60;

  const mwToRel = (mw: number) => referencePosition(marker, mw) / marker.refHeight;

  const half = Math.floor(profiles.length / 2);
  const lanesMw: number[][] = [marker.mw, ...profiles.slice(0, half), marker.mw, ...profiles.slice(half), marker.mw];
  const isMarker = lanesMw.map((_, i) => i === 0 || i === half + 1 || i === lanesMw.length - 1);
  const width = gap + lanesMw.length * (laneWidth + gap);
  const img = createImage(width, height);
  for (let i = 0; i < img.data.length; i++) img.data[i] = 12 + random() * noise;

  const centres: number[] = [];
  const bands: number[][] = [];
  lanesMw.forEach((mws, li) => {
    const x0 = gap + li * (laneWidth + gap);
    const cx = x0 + laneWidth / 2;
    centres.push(cx);
    const u = (cx / width) * 2 - 1;
    const offset = -smile * (1 - u * u); // centre lanes run a bit faster
    const ys: number[] = [];
    for (const mw of mws) {
      const y = top + mwToRel(mw) * span + offset;
      ys.push(Math.round(y));
      for (let yy = Math.floor(y - 6); yy <= Math.ceil(y + 6); yy++) {
        if (yy < 0 || yy >= height) continue;
        const a = 200 * Math.exp(-((yy - y) ** 2) / (2 * 1.6 * 1.6));
        for (let x = x0 + 2; x < x0 + laneWidth - 2; x++) {
          img.data[yy * width + x] = Math.min(255, img.data[yy * width + x] + a);
        }
      }
    }
    // Faint lane background so that lanes are visible in the horizontal profile.
    for (let y = top - 10; y < top + span + 10; y++) {
      for (let x = x0 + 1; x < x0 + laneWidth - 1; x++) img.data[y * width + x] = Math.min(255, img.data[y * width + x] + 18);
    }
    bands.push(ys);
  });
  return { image: img, centres, bands, isMarker, mw: lanesMw };
}

/** Sample lanes for the demo gel (molecular weights in the 100bp Plus ladder range). */
export const SAMPLE_PROFILES: number[][] = [
  [2600, 1400, 950, 610, 330, 180],
  [2600, 1400, 950, 610, 330, 180],
  [2600, 1400, 880, 610, 330],
  [1800, 1100, 720, 450, 250, 150],
  [1800, 1100, 720, 450, 250],
  [2400, 1300, 820, 520, 260, 130],
];
