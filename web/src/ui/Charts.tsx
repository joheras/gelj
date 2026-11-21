import type { FitResult } from '../core/curvefit';

const W = 320;

/** Lane intensity profile with the detection threshold and the bands. */
export function ProfileChart({
  profile,
  threshold,
  bands,
  colour,
}: {
  profile: number[];
  threshold?: number;
  bands: number[];
  colour: string;
}) {
  const H = 130;
  const n = Math.max(1, profile.length - 1);
  const x = (i: number) => (i / n) * W;
  const y = (v: number) => H - (v / 255) * (H - 8) - 4;
  const path = profile.map((v, i) => `${i ? 'L' : 'M'}${x(i).toFixed(1)},${y(v).toFixed(1)}`).join('');
  return (
    <svg className="chart" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Lane intensity profile">
      {bands.map((b, i) => (
        <line key={i} x1={x(b)} x2={x(b)} y1={0} y2={H} stroke={colour} strokeWidth={1.5} opacity={0.7} />
      ))}
      <path d={path} fill="none" stroke="currentColor" strokeWidth={1.2} />
      {threshold !== undefined && (
        <>
          <line x1={0} x2={W} y1={y(threshold)} y2={y(threshold)} stroke="#d33" strokeDasharray="4 3" />
          <text x={W - 4} y={y(threshold) - 3} textAnchor="end" fontSize={10} fill="#d33">
            threshold {threshold}
          </text>
        </>
      )}
      <text x={4} y={12} fontSize={10} fill="currentColor" opacity={0.6}>
        top
      </text>
      <text x={W - 4} y={H - 4} textAnchor="end" fontSize={10} fill="currentColor" opacity={0.6}>
        bottom
      </text>
    </svg>
  );
}

/** Marker calibration: band position vs molecular weight with the fitted model. */
export function CurveChart({ x, y, fit }: { x: number[]; y: number[]; fit: FitResult | null }) {
  const H = 200;
  const pad = 34;
  if (x.length === 0) return null;
  const xmin = Math.min(...x);
  const xmax = Math.max(...x);
  const xr = xmax - xmin || 1;
  const x0 = xmin - xr * 0.05;
  const x1 = xmax + xr * 0.05;
  const samples = fit ? Array.from({ length: 80 }, (_, i) => x0 + ((x1 - x0) * i) / 79) : [];
  const fy = samples.map((v) => fit!.evaluate(v)).filter(Number.isFinite);
  const ys = [...y, ...fy];
  const ymin = Math.min(...ys);
  const ymax = Math.max(...ys);
  const yr = ymax - ymin || 1;
  const sx = (v: number) => pad + ((v - x0) / (x1 - x0)) * (W - pad - 8);
  const sy = (v: number) => H - pad + 10 - ((v - ymin) / yr) * (H - pad - 4);
  const path = fit
    ? samples
        .map((v) => [v, fit.evaluate(v)])
        .filter(([, v]) => Number.isFinite(v))
        .map(([a, b], i) => `${i ? 'L' : 'M'}${sx(a).toFixed(1)},${Math.max(-50, Math.min(H + 50, sy(b))).toFixed(1)}`)
        .join('')
    : '';
  return (
    <svg className="chart" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Migration model">
      <line x1={pad} x2={W - 8} y1={H - pad + 10} y2={H - pad + 10} stroke="currentColor" opacity={0.3} />
      <line x1={pad} x2={pad} y1={6} y2={H - pad + 10} stroke="currentColor" opacity={0.3} />
      <text x={(W + pad) / 2} y={H - 4} textAnchor="middle" fontSize={10} fill="currentColor" opacity={0.7}>
        band position (px)
      </text>
      <text x={10} y={H / 2} fontSize={10} fill="currentColor" opacity={0.7} transform={`rotate(-90 10 ${H / 2})`} textAnchor="middle">
        molecular weight
      </text>
      <text x={pad + 2} y={14} fontSize={9} fill="currentColor" opacity={0.6}>
        {ymax.toPrecision(4)}
      </text>
      <text x={pad + 2} y={H - pad + 6} fontSize={9} fill="currentColor" opacity={0.6}>
        {ymin.toPrecision(4)}
      </text>
      <path d={path} fill="none" stroke="var(--accent)" strokeWidth={1.5} />
      {x.map((v, i) => (
        <circle key={i} cx={sx(v)} cy={sy(y[i])} r={3} fill="var(--ref)" />
      ))}
    </svg>
  );
}
