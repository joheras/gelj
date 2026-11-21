// Natural cubic interpolating spline (equivalent to SSJ's SmoothingCubicSpline with rho = 1,
// which GelJ used to warp lanes onto the marker reference).

export function naturalSpline(xs: number[], ys: number[]): (x: number) => number {
  // Sort and drop duplicated abscissae.
  const pts = xs.map((x, i) => [x, ys[i]] as const).sort((a, b) => a[0] - b[0]);
  const x: number[] = [];
  const y: number[] = [];
  for (const [px, py] of pts) {
    if (x.length && px === x[x.length - 1]) continue;
    x.push(px);
    y.push(py);
  }
  const n = x.length;
  if (n === 0) return () => 0;
  if (n === 1) return () => y[0];
  if (n === 2) {
    const b = (y[1] - y[0]) / (x[1] - x[0]);
    return (v) => y[0] + b * (v - x[0]);
  }
  // Second derivatives M with M[0] = M[n-1] = 0 (tridiagonal system).
  const h = x.slice(1).map((v, i) => v - x[i]);
  const alpha = new Array<number>(n).fill(0);
  for (let i = 1; i < n - 1; i++) {
    alpha[i] = (3 / h[i]) * (y[i + 1] - y[i]) - (3 / h[i - 1]) * (y[i] - y[i - 1]);
  }
  const l = new Array<number>(n).fill(1);
  const mu = new Array<number>(n).fill(0);
  const z = new Array<number>(n).fill(0);
  for (let i = 1; i < n - 1; i++) {
    l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
    mu[i] = h[i] / l[i];
    z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
  }
  const c = new Array<number>(n).fill(0);
  const b = new Array<number>(n - 1).fill(0);
  const d = new Array<number>(n - 1).fill(0);
  for (let j = n - 2; j >= 0; j--) {
    c[j] = z[j] - mu[j] * c[j + 1];
    b[j] = (y[j + 1] - y[j]) / h[j] - (h[j] * (c[j + 1] + 2 * c[j])) / 3;
    d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
  }
  return (v: number) => {
    // Outside the data the boundary polynomials are extended.
    let i = 0;
    if (v >= x[n - 1]) i = n - 2;
    else if (v > x[0]) {
      let lo = 0;
      let hi = n - 1;
      while (hi - lo > 1) {
        const mid = (lo + hi) >> 1;
        if (x[mid] <= v) lo = mid;
        else hi = mid;
      }
      i = lo;
    }
    const dx = v - x[i];
    return y[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
  };
}
