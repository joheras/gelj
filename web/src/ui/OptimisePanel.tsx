// "Optimise tolerance": scans band-matching tolerances and optimisations (as GelCompar II / BioNumerics'
// tolerance and optimisation analysis) in a worker and lets the user apply the best setting.
import { useEffect, useRef, useState } from 'react';
import { bestPoint, gridValues, type ScanCriterion, type ScanOptions, type ScanPoint } from '../core/optimise';
import type { ComparableLane } from '../core/similarity';
import type { Linkage, SimilarityMethod } from '../core/types';
import type { ScanMessage, ScanRequest } from './optimise.worker';

export interface GroupOption {
  key: string;
  label: string;
  /** Value of the field for every lane (same order as `lanes`). */
  values: string[];
}

const MAX_POINTS = 400;
const COPH_COLOUR = '#1f6feb';
const ARI_COLOUR = '#e8891a';

function Range({ label, value, onChange }: { label: string; value: [number, number, number]; onChange: (v: [number, number, number]) => void }) {
  const set = (k: number, v: number) => onChange(value.map((x, i) => (i === k ? Math.max(0, v) : x)) as [number, number, number]);
  return (
    <div className="row small">
      <span style={{ width: 92 }}>{label}</span>
      from
      <input type="number" aria-label={`${label} from`} min={0} step={0.1} value={value[0]} style={{ width: 62 }} onChange={(e) => set(0, Number(e.target.value))} />
      to
      <input type="number" aria-label={`${label} to`} min={0} step={0.1} value={value[1]} style={{ width: 62 }} onChange={(e) => set(1, Number(e.target.value))} />
      step
      <input type="number" aria-label={`${label} step`} min={0} step={0.05} value={value[2]} style={{ width: 62 }} onChange={(e) => set(2, Number(e.target.value))} />
      <span className="muted">%</span>
    </div>
  );
}

export function OptimisePanel({
  lanes,
  method,
  linkage,
  cutoff: initialCutoff,
  groupOptions,
  onApply,
  onClose,
}: {
  lanes: ComparableLane[];
  method: SimilarityMethod;
  linkage: Linkage;
  cutoff: number;
  groupOptions: GroupOption[];
  onApply: (tolerance: number, optimisation: number) => void;
  onClose: () => void;
}) {
  const [tol, setTol] = useState<[number, number, number]>([0.2, 3, 0.2]);
  const [opt, setOpt] = useState<[number, number, number]>([0, 2, 0.5]);
  const [groupKey, setGroupKey] = useState('');
  const [cutoff, setCutoff] = useState(initialCutoff);
  const [criterion, setCriterion] = useState<ScanCriterion>('cophenetic');
  const [points, setPoints] = useState<ScanPoint[]>([]);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [chosen, setChosen] = useState<ScanPoint | null>(null);
  const worker = useRef<Worker | null>(null);

  useEffect(() => () => worker.current?.terminate(), []);

  const tolerances = gridValues(tol[0], tol[1], tol[2], 60);
  const optimisations = gridValues(opt[0], opt[1], opt[2], 20);
  const size = tolerances.length * optimisations.length;
  const group = groupOptions.find((g) => g.key === groupKey);
  const known = group ? group.values.filter((v) => v.trim()).length : 0;
  const effective: ScanCriterion = group && known >= 2 ? criterion : 'cophenetic';

  const cancel = () => {
    worker.current?.terminate();
    worker.current = null;
    setProgress(null);
  };

  const start = () => {
    cancel();
    setError(null);
    setPoints([]);
    setChosen(null);
    if (size > MAX_POINTS) {
      setError(`The grid has ${size} settings; use larger steps (at most ${MAX_POINTS}).`);
      return;
    }
    const options: ScanOptions = { method, linkage, tolerances, optimisations, cutoff, groups: group?.values };
    const w = new Worker(new URL('./optimise.worker.ts', import.meta.url), { type: 'module' });
    worker.current = w;
    setProgress(0);
    const found: ScanPoint[] = [];
    w.onmessage = (e: MessageEvent<ScanMessage>) => {
      const m = e.data;
      if (m.type === 'point') {
        found.push(m.point);
        setPoints([...found]);
        setProgress(m.done / m.total);
      } else {
        if (m.type === 'error') setError(m.message);
        cancel();
      }
    };
    w.onerror = (e) => {
      setError(e.message || 'The analysis failed.');
      cancel();
    };
    w.postMessage({ lanes: lanes.map((l) => ({ mw: l.mw, profile: [] })), options } satisfies ScanRequest);
  };

  const best = bestPoint(points, effective);
  const selected = chosen ?? best;
  const running = progress !== null;
  const hasAri = points.some((p) => p.ari !== null);

  return (
    <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && !running && onClose()}>
      <div className="modal" role="dialog" aria-label="Optimise tolerance" style={{ maxWidth: 780, maxHeight: '92vh', overflow: 'auto' }}>
        <h3>Optimise tolerance</h3>
        <p className="small muted" style={{ margin: 0 }}>
          Builds the {method} / {linkage} dendrogram of the {lanes.length} selected lanes for every position tolerance and optimisation
          (maximum shift between two whole lanes), both in % of the molecular-weight range, and scores each setting.
        </p>
        <Range label="Tolerance" value={tol} onChange={setTol} />
        <Range label="Optimisation" value={opt} onChange={setOpt} />
        <div className="row small">
          <span style={{ width: 92 }}>Known groups</span>
          <select value={groupKey} onChange={(e) => setGroupKey(e.target.value)}>
            <option value="">None (cophenetic correlation only)</option>
            {groupOptions.map((g) => (
              <option key={g.key} value={g.key}>
                {g.label}
              </option>
            ))}
          </select>
          {group && (
            <>
              <span>clusters at</span>
              <input type="number" aria-label="Cut-off for the clusters" min={0} max={100} step={1} value={cutoff} style={{ width: 58 }} onChange={(e) => setCutoff(Math.min(100, Math.max(0, Number(e.target.value))))} />
              <span>%</span>
            </>
          )}
        </div>
        {group && (
          <div className="row small">
            <span style={{ width: 92 }}>Best by</span>
            <label className="check">
              <input type="radio" name="criterion" checked={criterion === 'ari'} onChange={() => setCriterion('ari')} />
              agreement with the {group.label} groups (adjusted Rand index)
            </label>
            <label className="check">
              <input type="radio" name="criterion" checked={criterion === 'cophenetic'} onChange={() => setCriterion('cophenetic')} />
              cophenetic correlation
            </label>
            {known < 2 && <span className="muted">fewer than two selected lanes have a value for {group.label}</span>}
          </div>
        )}
        {error && <div className="alert error small">{error}</div>}
        <div className="row small">
          {running ? (
            <>
              <button onClick={cancel}>Cancel</button>
              <span className="muted">
                {Math.round((progress ?? 0) * 100)}% of {size} settings
              </span>
            </>
          ) : (
            <button className="primary" onClick={start} disabled={size === 0}>
              {points.length ? 'Run again' : 'Run'} ({size} settings)
            </button>
          )}
          <span className="spacer" />
          <button onClick={onClose} disabled={running}>
            Close
          </button>
        </div>
        {points.length > 0 && (
          <>
            {optimisations.length > 1 && (
              <Heatmap points={points} tolerances={tolerances} optimisations={optimisations} criterion={effective} best={best} selected={selected} onSelect={setChosen} />
            )}
            <ScoreChart
              points={points.filter((p) => p.optimisation === (selected?.optimisation ?? optimisations[0]))}
              tolerances={tolerances}
              showAri={hasAri}
              selected={selected}
              onSelect={setChosen}
            />
            {selected && (
              <div className="row small">
                <span>
                  {chosen && chosen !== best ? 'Selected' : 'Best'}: tolerance <b>{selected.tolerance}%</b>, optimisation <b>{selected.optimisation}%</b> ·
                  cophenetic {selected.cophenetic?.toFixed(3) ?? '—'}
                  {selected.ari !== null && ` · adjusted Rand ${selected.ari.toFixed(3)}`}
                  {selected.clusters !== null && group && ` · ${selected.clusters} clusters at ${cutoff}%`}
                </span>
                <span className="spacer" />
                {chosen && chosen !== best && (
                  <button className="link" onClick={() => setChosen(null)}>
                    Back to best
                  </button>
                )}
                <button className="primary" disabled={running} onClick={() => onApply(selected.tolerance, selected.optimisation)}>
                  Apply and compare
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

function fmt(v: number | null) {
  return v === null ? '—' : v.toFixed(3);
}

function tip(p: ScanPoint) {
  return `Tolerance ${p.tolerance}% · optimisation ${p.optimisation}%\nCophenetic ${fmt(p.cophenetic)}${p.ari !== null ? `\nAdjusted Rand ${fmt(p.ari)}` : ''}${p.clusters !== null ? `\n${p.clusters} clusters` : ''}`;
}

/** Score of the criterion for every tolerance (columns) and optimisation (rows); one blue, darker = better. */
function Heatmap({
  points,
  tolerances,
  optimisations,
  criterion,
  best,
  selected,
  onSelect,
}: {
  points: ScanPoint[];
  tolerances: number[];
  optimisations: number[];
  criterion: ScanCriterion;
  best: ScanPoint | null;
  selected: ScanPoint | null;
  onSelect: (p: ScanPoint) => void;
}) {
  const scores = points.map((p) => p[criterion]).filter((v): v is number => v !== null);
  const lo = Math.min(...scores);
  const hi = Math.max(...scores);
  const at = (t: number, o: number) => points.find((p) => p.tolerance === t && p.optimisation === o);
  return (
    <div className="stack" style={{ gap: 4 }}>
      <span className="small muted">
        {criterion === 'ari' ? 'Adjusted Rand index' : 'Cophenetic correlation'} ({fmt(lo)}–{fmt(hi)}; darker is better, outlined = best). Click a
        cell to select it.
      </span>
      <div className="table-wrap">
        <table className="matrix" style={{ borderCollapse: 'separate', borderSpacing: 2 }}>
          <thead>
            <tr>
              <th className="small" style={{ textAlign: 'right', fontWeight: 500 }}>
                opt. \ tol.
              </th>
              {tolerances.map((t) => (
                <th key={t} className="small" style={{ fontWeight: 500, padding: '0 2px' }}>
                  {t}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {optimisations.map((o) => (
              <tr key={o}>
                <th className="small" style={{ textAlign: 'right', fontWeight: 500, paddingRight: 4 }}>
                  {o}%
                </th>
                {tolerances.map((t) => {
                  const p = at(t, o);
                  const v = p?.[criterion] ?? null;
                  const k = v === null ? null : hi > lo ? (v - lo) / (hi - lo) : 1;
                  const light = k === null ? 0 : Math.round(95 - k * 55);
                  return (
                    <td
                      key={t}
                      title={p ? tip(p) : undefined}
                      onClick={() => p && onSelect(p)}
                      style={{
                        width: 18,
                        height: 18,
                        minWidth: 14,
                        padding: 0,
                        cursor: p ? 'pointer' : undefined,
                        borderRadius: 3,
                        background: k === null ? 'var(--surface-2)' : `hsl(215 75% ${light}%)`,
                        outline: p && p === selected ? '2px solid var(--text)' : p && p === best ? '2px dashed var(--text)' : undefined,
                      }}
                    />
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/** Scores against the tolerance, for one optimisation. */
function ScoreChart({
  points,
  tolerances,
  showAri,
  selected,
  onSelect,
}: {
  points: ScanPoint[];
  tolerances: number[];
  showAri: boolean;
  selected: ScanPoint | null;
  onSelect: (p: ScanPoint) => void;
}) {
  const W = 700;
  const H = 220;
  const L = 40;
  const R = 12;
  const T = 12;
  const B = 34;
  const [hover, setHover] = useState<ScanPoint | null>(null);
  const t0 = tolerances[0];
  const t1 = tolerances[tolerances.length - 1];
  const values = points.flatMap((p) => [p.cophenetic, showAri ? p.ari : null]).filter((v): v is number => v !== null);
  const yMin = Math.min(0, Math.floor(Math.min(...values, 1) * 10) / 10);
  const x = (t: number) => (t1 > t0 ? L + ((t - t0) / (t1 - t0)) * (W - L - R) : (W + L - R) / 2);
  const y = (v: number) => T + ((1 - v) / (1 - yMin)) * (H - T - B);
  const path = (key: 'cophenetic' | 'ari') => {
    let d = '';
    let pen = false;
    for (const p of points) {
      const v = p[key];
      if (v === null) {
        pen = false;
        continue;
      }
      d += `${pen ? 'L' : 'M'}${x(p.tolerance).toFixed(1)},${y(v).toFixed(1)}`;
      pen = true;
    }
    return d;
  };
  const ticks = [];
  for (let v = 1; v >= yMin - 1e-9; v -= 0.25) ticks.push(Number(v.toFixed(2)));
  const xticks = tolerances.length <= 12 ? tolerances : tolerances.filter((_, i) => i % Math.ceil(tolerances.length / 10) === 0);
  const opt = points[0]?.optimisation ?? 0;
  const hp = hover ?? selected;
  return (
    <div className="stack" style={{ gap: 4 }}>
      <div className="row small">
        <span className="muted">Scores against the tolerance (optimisation {opt}%). Click a point to select it.</span>
        <span className="spacer" />
        <span className="row" style={{ gap: 4 }}>
          <svg width="18" height="8" aria-hidden>
            <line x1="0" x2="18" y1="4" y2="4" stroke={COPH_COLOUR} strokeWidth="2" />
          </svg>
          cophenetic correlation
        </span>
        {showAri && (
          <span className="row" style={{ gap: 4 }}>
            <svg width="18" height="8" aria-hidden>
              <line x1="0" x2="18" y1="4" y2="4" stroke={ARI_COLOUR} strokeWidth="2" strokeDasharray="5 3" />
            </svg>
            adjusted Rand index
          </span>
        )}
      </div>
      <svg className="chart" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Quality scores against the tolerance" onMouseLeave={() => setHover(null)}>
        {ticks.map((v) => (
          <g key={v}>
            <line x1={L} x2={W - R} y1={y(v)} y2={y(v)} stroke="currentColor" opacity={0.12} />
            <text x={L - 6} y={y(v) + 3} fontSize={10} textAnchor="end" fill="currentColor" opacity={0.65}>
              {v}
            </text>
          </g>
        ))}
        {xticks.map((t) => (
          <text key={t} x={x(t)} y={H - B + 14} fontSize={10} textAnchor="middle" fill="currentColor" opacity={0.65}>
            {t}
          </text>
        ))}
        <text x={(W + L - R) / 2} y={H - 4} fontSize={11} textAnchor="middle" fill="currentColor" opacity={0.75}>
          Tolerance (% of the molecular-weight range)
        </text>
        {hp && <line x1={x(hp.tolerance)} x2={x(hp.tolerance)} y1={T} y2={H - B} stroke="currentColor" opacity={0.35} strokeDasharray="3 3" />}
        <path d={path('cophenetic')} fill="none" stroke={COPH_COLOUR} strokeWidth={2} />
        {showAri && <path d={path('ari')} fill="none" stroke={ARI_COLOUR} strokeWidth={2} strokeDasharray="5 3" />}
        {points.map((p) => (
          <g key={p.tolerance}>
            {p.cophenetic !== null && <circle cx={x(p.tolerance)} cy={y(p.cophenetic)} r={p === selected ? 5 : 3} fill={COPH_COLOUR} stroke="var(--surface)" strokeWidth={2} />}
            {showAri && p.ari !== null && <circle cx={x(p.tolerance)} cy={y(p.ari)} r={p === selected ? 5 : 3} fill={ARI_COLOUR} stroke="var(--surface)" strokeWidth={2} />}
            <rect
              x={x(p.tolerance) - Math.max(6, (W - L - R) / Math.max(1, tolerances.length) / 2)}
              width={Math.max(12, (W - L - R) / Math.max(1, tolerances.length))}
              y={T}
              height={H - T - B}
              fill="transparent"
              style={{ cursor: 'pointer' }}
              onMouseEnter={() => setHover(p)}
              onClick={() => onSelect(p)}
            >
              <title>{tip(p)}</title>
            </rect>
          </g>
        ))}
      </svg>
    </div>
  );
}
