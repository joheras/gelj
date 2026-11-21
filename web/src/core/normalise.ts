// Gel normalisation (port of GelJ's ComputeStandards.computestdlanes).
//
// 1. The position of every marker band is interpolated across the gel from the
//    reference lanes (polynomial in the x coordinate of the lane centre).
// 2. Each lane is resized to the size of the marker reference lane and warped
//    with a cubic spline so that its marker bands land on the reference positions.
// 3. Band positions are converted to molecular weights with the marker's migration model.
import { fitCurve, type FitResult } from './curvefit';
import { createImage, crop, resize, rowProfile, signalFn } from './image';
import { naturalSpline } from './spline';
import type { GrayImage, Lane, Marker } from './types';

export interface NormalisedLaneData {
  laneId: string;
  image: GrayImage;
  profile: number[];
  positions: number[];
  mw: number[];
}

export class NormalisationError extends Error {}

/** Fit models used to propagate marker bands between reference lanes (ImageJ ids). */
const POLY_BY_DEGREE = [0, 0, 1, 2, 3];

/** Positions in the marker reference lane of the given molecular weights. */
export function markerPositionsFor(marker: Marker, mws: number[]): number[] {
  return mws.map((mw) => {
    let best = 0;
    for (let i = 1; i < marker.mw.length; i++) {
      if (Math.abs(marker.mw[i] - mw) < Math.abs(marker.mw[best] - mw)) best = i;
    }
    return marker.refPositions[best];
  });
}

export function migrationModel(marker: Marker) {
  return fitCurve(marker.refPositions, marker.mw, marker.fitMethod);
}

/**
 * Molecular weight at position `pos` of the marker reference lane. Uses the migration model, except where
 * it is undefined or not positive (e.g. log models beyond the last marker band), where the weight is
 * interpolated log-linearly between the marker bands (extrapolated from the nearest pair).
 */
export function molecularWeight(model: FitResult, marker: Marker, pos: number): number {
  const v = model.evaluate(pos);
  if (Number.isFinite(v) && v > 0) return v;
  const pts = marker.refPositions
    .map((p, i) => [p, Math.log(marker.mw[i])] as const)
    .filter(([p, l]) => Number.isFinite(p) && Number.isFinite(l))
    .sort((a, b) => a[0] - b[0]);
  if (pts.length === 0) return NaN;
  if (pts.length === 1) return Math.exp(pts[0][1]);
  let i = 0;
  while (i < pts.length - 2 && pos > pts[i + 1][0]) i++;
  const [p0, l0] = pts[i];
  const [p1, l1] = pts[i + 1];
  return Math.exp(p1 === p0 ? l0 : l0 + ((l1 - l0) * (pos - p0)) / (p1 - p0));
}

/** Absolute y of each marker band at the centre of `lane`, interpolated from the reference lanes. */
export function markerBandsAt(lane: Lane, refs: Lane[]): number[] {
  const k = refs[0].bands.length;
  const cx = lane.rect.x + lane.rect.w / 2;
  if (refs.length === 1 || lane.isReference) {
    const src = lane.isReference ? lane : refs[0];
    return src.bands.map((b) => src.rect.y + b);
  }
  const xs = refs.map((r) => r.rect.x + r.rect.w / 2);
  const degree = Math.min(refs.length - 1, 4);
  const out: number[] = [];
  for (let i = 0; i < k; i++) {
    const ys = refs.map((r) => r.rect.y + r.bands[i]);
    out.push(fitCurve(xs, ys, POLY_BY_DEGREE[degree]).evaluate(cx));
  }
  return out;
}

export function validateReferences(lanes: Lane[]): Lane[] {
  const refs = lanes.filter((l) => l.isReference).sort((a, b) => a.rect.x - b.rect.x);
  if (refs.length === 0) throw new NormalisationError('At least one reference lane is required.');
  const k = refs[0].bands.length;
  if (k < 2) throw new NormalisationError('Reference lanes need at least two bands.');
  if (refs.some((r) => r.bands.length !== k)) {
    throw new NormalisationError('All reference lanes must have the same number of bands.');
  }
  return refs;
}

export function normaliseLanes(
  img: GrayImage,
  darkBackground: boolean,
  lanes: Lane[],
  marker: Marker,
  referenceMw: number[],
): NormalisedLaneData[] {
  const refs = validateReferences(lanes);
  if (referenceMw.length !== refs[0].bands.length) {
    throw new NormalisationError('Each reference band needs a molecular weight.');
  }
  const markerPos = markerPositionsFor(marker, referenceMw);
  const model = migrationModel(marker);
  const W = Math.max(1, Math.round(marker.refWidth));
  const H = Math.max(1, Math.round(marker.refHeight));
  const signal = signalFn(darkBackground);
  const bg = darkBackground ? 0 : 255;

  return lanes.map((lane) => {
    const sf = H / lane.rect.h;
    const laneImg = resize(crop(img, lane.rect), W, H);
    const heights = markerBandsAt(lane, refs).map((y) => (y - lane.rect.y) * sf);
    const shift = naturalSpline(
      heights,
      heights.map((h, i) => markerPos[i] - h),
    );
    const target = (row: number) => Math.trunc(row + shift(row));

    const warped = createImage(W, H, bg);
    for (let i = 0; i < H; i++) {
      const ti = target(i);
      const prev = i > 0 ? target(i - 1) : ti;
      for (let j = 0; j < W; j++) {
        // Fill gaps produced by stretching with the previous row.
        for (let k = prev + 1; k < ti; k++) {
          if (k >= 0 && k < H) warped.data[k * W + j] = laneImg.data[(i - 1) * W + j];
        }
        if (ti >= 0 && ti < H) warped.data[ti * W + j] = laneImg.data[i * W + j];
      }
    }

    const positions = lane.bands.map((b) => b * sf + shift(b * sf));
    const mw = lane.isReference ? referenceMw.slice() : positions.map((p) => molecularWeight(model, marker, p));
    return {
      laneId: lane.id,
      image: warped,
      profile: rowProfile(warped, { x: 0, y: 0, w: W, h: H }, signal),
      positions,
      mw,
    };
  });
}
