import { useMemo, useState } from 'react';
import { fitCurve, FIT_MODELS } from '../core/curvefit';
import type { Experiment, Marker } from '../core/types';
import { deleteMarker, saveMarker } from '../storage/db';
import { CurveChart } from './Charts';

interface Props {
  markers: Marker[];
  experiments: Experiment[];
}

export function MarkersPage({ markers, experiments }: Props) {
  const [selectedId, setSelectedId] = useState<string | null>(markers[0]?.id ?? null);
  const selected = markers.find((m) => m.id === selectedId) ?? null;

  return (
    <div className="page">
      <div className="page-header">
        <h2>Markers</h2>
        <span className="small muted">New markers are defined from the reference lanes of a gel, in step 3.3 of the assistant.</span>
      </div>
      <div className="compare">
        <div className="card" style={{ padding: 0 }}>
          <table className="grid">
            <thead>
              <tr>
                <th>Name</th>
                <th>Bands</th>
                <th>Used</th>
              </tr>
            </thead>
            <tbody>
              {markers.map((m) => (
                <tr
                  key={m.id}
                  onClick={() => setSelectedId(m.id)}
                  style={{ cursor: 'pointer', background: m.id === selectedId ? 'var(--accent-soft)' : undefined }}
                >
                  <td>
                    {m.name} {m.builtin && <span className="badge">built-in</span>}
                  </td>
                  <td>{m.mw.length}</td>
                  <td>{experiments.filter((e) => e.markerId === m.id).length}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {selected && <MarkerDetail key={selected.id} marker={selected} usedBy={experiments.filter((e) => e.markerId === selected.id).length} />}
      </div>
    </div>
  );
}

function MarkerDetail({ marker, usedBy }: { marker: Marker; usedBy: number }) {
  const [draft, setDraft] = useState({ name: marker.name, mw: marker.mw.map(String), fitMethod: marker.fitMethod });
  const [error, setError] = useState<string | null>(null);
  const mw = draft.mw.map(Number);
  const valid = mw.every((v) => Number.isFinite(v) && v > 0);
  const fit = useMemo(() => (valid ? fitCurve(marker.refPositions, mw, draft.fitMethod) : null), [valid, marker, draft]); // eslint-disable-line react-hooks/exhaustive-deps
  const readOnly = !!marker.builtin;
  const dirty = draft.name !== marker.name || draft.fitMethod !== marker.fitMethod || draft.mw.some((v, i) => Number(v) !== marker.mw[i]);

  const save = async () => {
    setError(null);
    if (!draft.name.trim()) return setError('The name cannot be empty.');
    if (!valid) return setError('Molecular weights must be positive numbers.');
    for (let i = 0; i < mw.length - 1; i++) if (mw[i] < mw[i + 1]) return setError('Molecular weights must decrease from top to bottom.');
    await saveMarker({ ...marker, name: draft.name.trim(), mw, fitMethod: draft.fitMethod });
  };

  return (
    <div className="card stack">
      <h3>{marker.name}</h3>
      {readOnly && <p className="small muted">Built-in markers from GelJ cannot be modified.</p>}
      {usedBy > 0 && !readOnly && (
        <p className="small muted">
          Used by {usedBy} experiment(s). Changes apply the next time those experiments are re-analysed.
        </p>
      )}
      <label className="field">
        <span>Name</span>
        <input type="text" value={draft.name} disabled={readOnly} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />
      </label>
      <label className="field">
        <span>Migration model</span>
        <select value={draft.fitMethod} disabled={readOnly} onChange={(e) => setDraft({ ...draft, fitMethod: Number(e.target.value) })}>
          {FIT_MODELS.map((m) => (
            <option key={m.id} value={m.id} disabled={m.nParams > mw.length}>
              {m.name}
            </option>
          ))}
        </select>
      </label>
      <CurveChart x={marker.refPositions} y={mw} fit={fit} />
      {fit && (
        <span className="small muted">
          {fit.model.formula} — R² = {fit.rSquared.toFixed(4)}
        </span>
      )}
      <div className="table-wrap">
        <table className="grid small">
          <thead>
            <tr>
              <th>Band</th>
              <th>Reference position (px)</th>
              <th>Molecular weight</th>
            </tr>
          </thead>
          <tbody>
            {marker.refPositions.map((p, i) => (
              <tr key={i}>
                <td>{i + 1}</td>
                <td>{p}</td>
                <td>
                  <input
                    type="number"
                    step="any"
                    value={draft.mw[i]}
                    disabled={readOnly}
                    aria-label={`Molecular weight of band ${i + 1}`}
                    onChange={(e) => setDraft({ ...draft, mw: draft.mw.map((v, j) => (j === i ? e.target.value : v)) })}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <span className="small muted">
        Reference lane: {marker.refWidth}×{marker.refHeight}px
      </span>
      {error && <div className="alert error small">{error}</div>}
      {!readOnly && (
        <div className="row">
          <button className="primary" onClick={save} disabled={!dirty}>
            Save
          </button>
          <span className="spacer" />
          <button
            className="danger"
            disabled={usedBy > 0}
            title={usedBy > 0 ? 'The marker is used by some experiments' : undefined}
            onClick={async () => {
              if (window.confirm(`Delete the marker "${marker.name}"?`)) await deleteMarker(marker.id);
            }}
          >
            Delete
          </button>
        </div>
      )}
    </div>
  );
}
