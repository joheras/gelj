// Curve fitting for the migration model (position -> molecular weight).
// Models and numbering follow ImageJ's CurveFitter, which GelJ used.

export interface FitModel {
  id: number;
  name: string;
  formula: string;
  nParams: number;
  f: (p: number[], x: number) => number;
  /** Polynomial degree when the model is fitted by linear least squares. */
  poly?: number;
  /** Fit by linear regression on transformed data. */
  regression?: 'exp' | 'power';
}

const poly = (id: number, deg: number, name: string): FitModel => ({
  id,
  name,
  formula: 'y = ' + Array.from({ length: deg + 1 }, (_, i) => `${'abcdefghi'[i]}${i ? (i > 1 ? `x^${i}` : 'x') : ''}`).join(' + '),
  nParams: deg + 1,
  poly: deg,
  f: (p, x) => {
    let s = 0;
    for (let i = p.length - 1; i >= 0; i--) s = s * x + p[i];
    return s;
  },
});

export const FIT_MODELS: FitModel[] = [
  poly(0, 1, 'Straight line'),
  poly(1, 2, '2nd degree polynomial'),
  poly(2, 3, '3rd degree polynomial'),
  poly(3, 4, '4th degree polynomial'),
  poly(17, 5, '5th degree polynomial'),
  poly(18, 6, '6th degree polynomial'),
  poly(19, 7, '7th degree polynomial'),
  poly(20, 8, '8th degree polynomial'),
  { id: 4, name: 'Exponential', formula: 'y = a·exp(bx)', nParams: 2, f: (p, x) => p[0] * Math.exp(p[1] * x) },
  {
    id: 15,
    name: 'Exponential (linear regression)',
    formula: 'y = a·exp(bx)',
    nParams: 2,
    regression: 'exp',
    f: (p, x) => p[0] * Math.exp(p[1] * x),
  },
  { id: 5, name: 'Power', formula: 'y = a·x^b', nParams: 2, f: (p, x) => p[0] * Math.pow(x, p[1]) },
  {
    id: 16,
    name: 'Power (linear regression)',
    formula: 'y = a·x^b',
    nParams: 2,
    regression: 'power',
    f: (p, x) => p[0] * Math.pow(x, p[1]),
  },
  { id: 6, name: 'Log', formula: 'y = a·ln(bx)', nParams: 2, f: (p, x) => p[0] * Math.log(p[1] * x) },
  { id: 9, name: 'y = a+b·ln(x-c)', formula: 'y = a + b·ln(x-c)', nParams: 3, f: (p, x) => p[0] + p[1] * Math.log(x - p[2]) },
  {
    id: 7,
    name: 'Rodbard',
    formula: 'y = d + (a-d)/(1+(x/c)^b)',
    nParams: 4,
    f: (p, x) => p[3] + (p[0] - p[3]) / (1 + Math.pow(x / p[2], p[1])),
  },
  {
    id: 10,
    name: 'Rodbard (NIH Image)',
    formula: 'y = c·((x-a)/(d-x))^(1/b)',
    nParams: 4,
    f: (p, x) => p[2] * Math.pow((x - p[0]) / (p[3] - x), 1 / p[1]),
  },
  {
    id: 14,
    name: 'Inverse Rodbard',
    formula: 'y = c·((x-a)/(d-x))^(1/b)',
    nParams: 4,
    f: (p, x) => p[2] * Math.pow((x - p[0]) / (p[3] - x), 1 / p[1]),
  },
  {
    id: 11,
    name: 'Exponential with offset',
    formula: 'y = a·exp(-bx) + c',
    nParams: 3,
    f: (p, x) => p[0] * Math.exp(-p[1] * x) + p[2],
  },
  {
    id: 12,
    name: 'Gaussian',
    formula: 'y = a + (b-a)·exp(-(x-c)²/(2d²))',
    nParams: 4,
    f: (p, x) => p[0] + (p[1] - p[0]) * Math.exp(-((x - p[2]) ** 2) / (2 * p[3] * p[3])),
  },
  {
    id: 21,
    name: 'Gaussian (no offset)',
    formula: 'y = a·exp(-(x-b)²/(2c²))',
    nParams: 3,
    f: (p, x) => p[0] * Math.exp(-((x - p[1]) ** 2) / (2 * p[2] * p[2])),
  },
  {
    id: 13,
    name: 'Exponential recovery',
    formula: 'y = a·(1-exp(-bx)) + c',
    nParams: 3,
    f: (p, x) => p[0] * (1 - Math.exp(-p[1] * x)) + p[2],
  },
  {
    id: 22,
    name: 'Exponential recovery (no offset)',
    formula: 'y = a·(1-exp(-bx))',
    nParams: 2,
    f: (p, x) => p[0] * (1 - Math.exp(-p[1] * x)),
  },
  {
    id: 23,
    name: 'Chapman-Richards',
    formula: 'y = a·(1-exp(-bx))^c',
    nParams: 3,
    f: (p, x) => p[0] * Math.pow(1 - Math.exp(-p[1] * x), p[2]),
  },
  {
    id: 8,
    name: 'Gamma variate',
    formula: 'y = b·(x-a)^c·exp(-(x-a)/d)',
    nParams: 4,
    f: (p, x) => (x <= p[0] ? 0 : p[1] * Math.pow(x - p[0], p[2]) * Math.exp(-(x - p[0]) / p[3])),
  },
];

export function getModel(id: number): FitModel {
  return FIT_MODELS.find((m) => m.id === id) ?? FIT_MODELS[0];
}

export interface FitResult {
  model: FitModel;
  params: number[];
  rSquared: number;
  sse: number;
  evaluate: (x: number) => number;
}

function linReg(x: number[], y: number[]): { a: number; b: number } {
  const n = x.length;
  const mx = x.reduce((s, v) => s + v, 0) / n;
  const my = y.reduce((s, v) => s + v, 0) / n;
  let sxy = 0;
  let sxx = 0;
  for (let i = 0; i < n; i++) {
    sxy += (x[i] - mx) * (y[i] - my);
    sxx += (x[i] - mx) ** 2;
  }
  const b = sxx > 0 ? sxy / sxx : 0;
  return { a: my - b * mx, b };
}

/** Solve A·x = b with Gaussian elimination and partial pivoting. */
function solve(A: number[][], b: number[]): number[] {
  const n = b.length;
  const M = A.map((row, i) => [...row, b[i]]);
  for (let c = 0; c < n; c++) {
    let piv = c;
    for (let r = c + 1; r < n; r++) if (Math.abs(M[r][c]) > Math.abs(M[piv][c])) piv = r;
    [M[c], M[piv]] = [M[piv], M[c]];
    const d = M[c][c];
    if (Math.abs(d) < 1e-300) continue;
    for (let r = 0; r < n; r++) {
      if (r === c) continue;
      const f = M[r][c] / d;
      for (let k = c; k <= n; k++) M[r][k] -= f * M[c][k];
    }
  }
  return M.map((row, i) => (Math.abs(row[i]) < 1e-300 ? 0 : row[n] / row[i]));
}

/** Least squares polynomial; x is standardised internally for numerical stability. */
function fitPoly(x: number[], y: number[], deg: number): number[] {
  const n = x.length;
  const d = Math.min(deg, n - 1);
  const mx = x.reduce((s, v) => s + v, 0) / n;
  const sx = Math.sqrt(x.reduce((s, v) => s + (v - mx) ** 2, 0) / n) || 1;
  const t = x.map((v) => (v - mx) / sx);
  const A: number[][] = [];
  const b: number[] = [];
  for (let i = 0; i <= d; i++) {
    A.push([]);
    for (let j = 0; j <= d; j++) A[i].push(t.reduce((s, v) => s + Math.pow(v, i + j), 0));
    b.push(t.reduce((s, v, k) => s + Math.pow(v, i) * y[k], 0));
  }
  const q = solve(A, b);
  // Expand q(t) with t = (x - mx)/sx into coefficients of x.
  const coef = new Array<number>(deg + 1).fill(0);
  for (let i = 0; i <= d; i++) {
    // (x - mx)^i / sx^i
    for (let k = 0; k <= i; k++) {
      coef[k] += (q[i] * binom(i, k) * Math.pow(-mx, i - k)) / Math.pow(sx, i);
    }
  }
  return coef;
}

function binom(n: number, k: number): number {
  let r = 1;
  for (let i = 1; i <= k; i++) r = (r * (n - k + i)) / i;
  return r;
}

/** Nelder-Mead simplex minimisation. */
function nelderMead(fn: (p: number[]) => number, start: number[], maxIter = 4000): number[] {
  const n = start.length;
  let simplex = [start.slice()];
  for (let i = 0; i < n; i++) {
    const p = start.slice();
    p[i] = p[i] !== 0 ? p[i] * 1.1 : 0.01;
    simplex.push(p);
  }
  let values = simplex.map(fn);
  for (let it = 0; it < maxIter; it++) {
    const order = values.map((_, i) => i).sort((a, b) => values[a] - values[b]);
    simplex = order.map((i) => simplex[i]);
    values = order.map((i) => values[i]);
    if (Math.abs(values[n] - values[0]) <= 1e-12 * (Math.abs(values[0]) + 1e-12)) break;
    const centroid = new Array<number>(n).fill(0);
    for (let i = 0; i < n; i++) for (let j = 0; j < n; j++) centroid[j] += simplex[i][j] / n;
    const at = (t: number) => centroid.map((c, j) => c + t * (simplex[n][j] - c));
    const xr = at(-1);
    const fr = fn(xr);
    if (fr < values[0]) {
      const xe = at(-2);
      const fe = fn(xe);
      if (fe < fr) {
        simplex[n] = xe;
        values[n] = fe;
      } else {
        simplex[n] = xr;
        values[n] = fr;
      }
    } else if (fr < values[n - 1]) {
      simplex[n] = xr;
      values[n] = fr;
    } else {
      const xc = fr < values[n] ? at(-0.5) : at(0.5);
      const fc = fn(xc);
      if (fc < Math.min(fr, values[n])) {
        simplex[n] = xc;
        values[n] = fc;
      } else {
        for (let i = 1; i <= n; i++) {
          simplex[i] = simplex[i].map((v, j) => simplex[0][j] + 0.5 * (v - simplex[0][j]));
          values[i] = fn(simplex[i]);
        }
      }
    }
  }
  let best = 0;
  for (let i = 1; i <= n; i++) if (values[i] < values[best]) best = i;
  return simplex[best];
}

function initialParams(id: number, x: number[], y: number[]): number[] {
  const n = x.length;
  const xmin = Math.min(...x);
  const xmax = Math.max(...x);
  const ymin = Math.min(...y);
  const ymax = Math.max(...y);
  const xr = xmax - xmin || 1;
  const xmean = x.reduce((s, v) => s + v, 0) / n;
  const ymean = y.reduce((s, v) => s + v, 0) / n;
  const iFirst = x.indexOf(xmin);
  const iLast = x.indexOf(xmax);
  const xAtYmax = x[y.indexOf(ymax)];
  const positive = y.every((v) => v > 0);
  switch (id) {
    case 4: {
      if (!positive) return [ymean, 0];
      const r = linReg(x, y.map(Math.log));
      return [Math.exp(r.a), r.b];
    }
    case 5: {
      if (!positive || x.some((v) => v <= 0)) return [ymean, 1];
      const r = linReg(x.map(Math.log), y.map(Math.log));
      return [Math.exp(r.a), r.b];
    }
    case 6: {
      if (x.some((v) => v <= 0)) return [ymean, 1];
      const r = linReg(x.map(Math.log), y);
      const a = r.b || 1;
      return [a, Math.exp(r.a / a)];
    }
    case 9: {
      const c = xmin - 0.1 * xr;
      const r = linReg(
        x.map((v) => Math.log(v - c)),
        y,
      );
      return [r.a, r.b, c];
    }
    case 7:
      return [y[iFirst], 1, xmean, y[iLast]];
    case 10:
    case 14:
      return [xmin - 0.1 * xr, 1, ymean, xmax + 0.1 * xr];
    case 11: {
      const decreasing = y[iLast] < y[iFirst];
      const c = decreasing ? ymin : ymax;
      return [y[iFirst] - c || 1, (decreasing ? 1 : -1) * (3 / xr), c];
    }
    case 12:
      return [ymin, ymax, xAtYmax, 0.39 * xr];
    case 21:
      return [ymax, xAtYmax, 0.39 * xr];
    case 13:
      return [ymax - ymin, 3 / xr, ymin];
    case 22:
      return [ymax, 3 / xr];
    case 23:
      return [ymax * 1.5, 1 / (Math.abs(xmean) || 1), 1.5];
    case 8:
      return [xmin - 0.1 * xr, ymax, 2, xr / 4];
    default:
      return [ymean, 0];
  }
}

export function fitCurve(x: number[], y: number[], methodId: number): FitResult {
  const model = getModel(methodId);
  let params: number[];
  if (x.length === 0) {
    params = new Array<number>(model.nParams).fill(0);
  } else if (model.poly !== undefined) {
    params = fitPoly(x, y, model.poly);
  } else if (model.regression === 'exp' && y.every((v) => v > 0)) {
    const r = linReg(x, y.map(Math.log));
    params = [Math.exp(r.a), r.b];
  } else if (model.regression === 'power' && y.every((v) => v > 0) && x.every((v) => v > 0)) {
    const r = linReg(x.map(Math.log), y.map(Math.log));
    params = [Math.exp(r.a), r.b];
  } else {
    const yScale = Math.max(...y.map(Math.abs)) || 1;
    const sse = (p: number[]) => {
      let s = 0;
      for (let i = 0; i < x.length; i++) {
        const v = model.f(p, x[i]);
        if (!Number.isFinite(v)) return Number.MAX_VALUE;
        s += ((v - y[i]) / yScale) ** 2;
      }
      return s;
    };
    params = initialParams(model.id, x, y);
    for (let restart = 0; restart < 4; restart++) params = nelderMead(sse, params);
  }
  const evaluate = (v: number) => model.f(params, v);
  const ymean = y.reduce((s, v) => s + v, 0) / (y.length || 1);
  let sse = 0;
  let sst = 0;
  for (let i = 0; i < x.length; i++) {
    sse += (evaluate(x[i]) - y[i]) ** 2;
    sst += (y[i] - ymean) ** 2;
  }
  return { model, params, sse, rSquared: sst > 0 ? 1 - sse / sst : 1, evaluate };
}
