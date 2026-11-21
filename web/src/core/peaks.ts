// 1-D peak finding, ported from ImageJ's MaximumFinder.findMaxima(double[], double, boolean).

/** Returns maximum positions sorted by decreasing value. */
export function findMaxima(xx: number[], tolerance: number, excludeOnEdges: boolean): number[] {
  const includeEdge = !excludeOnEdges;
  const len = xx.length;
  if (len < 2) return [];
  if (tolerance < 0) tolerance = 0;
  const maxPositions: number[] = [];
  let max = xx[0];
  let min = xx[0];
  let maxPos = 0;
  let lastMaxPos = -1;
  let leftValleyFound = includeEdge;
  for (let jj = 1; jj < len; jj++) {
    const val = xx[jj];
    if (val > min + tolerance) leftValleyFound = true;
    if (val > max && leftValleyFound) {
      max = val;
      maxPos = jj;
    }
    if (leftValleyFound) lastMaxPos = maxPos;
    if (val < max - tolerance && leftValleyFound) {
      maxPositions.push(maxPos);
      leftValleyFound = false;
      min = val;
      max = val;
    }
    if (val < min) {
      min = val;
      if (!leftValleyFound) max = val;
    }
  }
  if (includeEdge) {
    if (maxPositions.length > 0 && maxPositions[maxPositions.length - 1] !== lastMaxPos) maxPositions.push(lastMaxPos);
    if (maxPositions.length === 0 && max - min >= tolerance) maxPositions.push(lastMaxPos);
  }
  // Centre of flat maxima.
  const centred = maxPositions.map((p) => {
    let pos = p;
    let mid = p;
    while (pos < len - 1 && xx[pos] === xx[pos + 1]) {
      mid += 0.5;
      pos++;
    }
    return Math.floor(mid);
  });
  return centred
    .map((p, i) => ({ p, v: xx[p], i }))
    .sort((a, b) => b.v - a.v || a.i - b.i)
    .map((e) => e.p);
}

export function findMinima(xx: number[], tolerance: number, excludeOnEdges: boolean): number[] {
  return findMaxima(
    xx.map((v) => -v),
    tolerance,
    excludeOnEdges,
  );
}

/** Centered moving average with a window of `points` samples, clipped at the edges. */
export function movingAverage(v: number[], points: number): number[] {
  const half = Math.floor((points - 1) / 2);
  const res = new Array<number>(v.length);
  for (let i = 0; i < v.length; i++) {
    const sp = Math.max(0, i - half);
    const ep = Math.min(v.length - 1, i + half);
    let s = 0;
    for (let j = sp; j <= ep; j++) s += v[j];
    res[i] = s / (ep - sp + 1);
  }
  return res;
}
