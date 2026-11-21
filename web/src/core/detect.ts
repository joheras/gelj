// Automatic lane and band detection (ports of GelJ's LaneSegmentation and BandSegmentationLane).
import { columnProfile, rowProfile, signalFn } from './image';
import { findMaxima, findMinima, movingAverage } from './peaks';
import type { GrayImage, Lane, Rect } from './types';
import { EMPTY_META } from './types';
import { uid } from './uid';

function firstAbove(v: number[], value: number): number {
  for (let i = 0; i < v.length; i++) if (v[i] > value) return i;
  return 0;
}

function lastAbove(v: number[], value: number): number {
  for (let i = v.length - 1; i >= 0; i--) if (v[i] > value) return i;
  return v.length - 1;
}

/**
 * Lane detection: lanes are separated by the local minima of the smoothed
 * horizontal intensity profile of the image.
 */
export function detectLanes(img: GrayImage, darkBackground: boolean, smoothing = 11, minimaTolerance = 1): Rect[] {
  const signal = signalFn(darkBackground);
  const full = { x: 0, y: 0, w: img.width, h: img.height };
  const profile = columnProfile(img, full, signal);
  const profileV = rowProfile(img, full, signal);

  // Remove empty borders.
  const cropLeft = firstAbove(profile, 1);
  const cropRight = lastAbove(profile, 1);
  const cropTop = firstAbove(profileV, 1);
  const cropBottom = lastAbove(profileV, 1);

  const cropped = profile.slice(cropLeft, cropRight);
  const avg = movingAverage(cropped, smoothing);
  const minima = findMinima(avg, minimaTolerance, true).sort((a, b) => a - b);

  const top = cropTop;
  const height = Math.max(1, cropBottom - cropTop);
  const lanes: Rect[] = [];
  const pushLane = (x0: number, x1: number) => {
    if (x1 - x0 >= 2) lanes.push({ x: x0, y: top, w: x1 - x0, h: height });
  };
  for (let j = 0; j < minima.length; j++) {
    if (j === 0) {
      pushLane(cropLeft + 2, cropLeft + minima[0] - 2);
    }
    if (j > 0) {
      pushLane(cropLeft + minima[j - 1] + 2, cropLeft + minima[j] - 2);
    }
    if (j === minima.length - 1) {
      pushLane(cropLeft + minima[j] + 2, cropRight - 2);
    }
  }
  if (lanes.length === 0) pushLane(cropLeft + 2, cropRight - 2);
  return lanes;
}

export function makeLanes(rects: Rect[]): Lane[] {
  return rects.map((rect, i) => ({
    id: uid(),
    name: `Lane ${i + 1}`,
    rect,
    isReference: false,
    threshold: 128,
    bands: [],
    meta: { ...EMPTY_META },
  }));
}

/** Vertical intensity profile of a lane (bands = high values). */
export function laneProfile(img: GrayImage, lane: Rect, darkBackground: boolean): number[] {
  return rowProfile(img, lane, signalFn(darkBackground));
}

/**
 * Band detection: local maxima of the lane profile whose intensity is above
 * `threshold`. Returns positions (px from the lane top) sorted top to bottom.
 */
export function detectBands(profile: number[], threshold: number, tolerance = 2): number[] {
  return findMaxima(profile, tolerance, true)
    .filter((p) => profile[p] > threshold)
    .sort((a, b) => a - b);
}

/** A reasonable starting threshold for a set of profiles. */
export function suggestThreshold(profiles: number[][]): number {
  const all = profiles.flat();
  if (all.length === 0) return 128;
  const mean = all.reduce((a, b) => a + b, 0) / all.length;
  const sd = Math.sqrt(all.reduce((a, b) => a + (b - mean) ** 2, 0) / all.length);
  return Math.round(Math.min(254, Math.max(1, mean + 0.5 * sd)));
}
