import { useState } from 'react';
import type { Experiment, Lane, Marker } from '../core/types';
import { saveExperiment } from '../storage/db';
import { downloadText, safeFilename } from './imageio';
import { LaneMetaEditor } from './Wizard';

interface Props {
  experiment: Experiment;
  markers: Marker[];
  onBack: () => void;
  onEdit: () => void;
  notify: (msg: string) => void;
}

export function ExperimentView({ experiment, markers, onBack, onEdit, notify }: Props) {
  const [lanes, setLanes] = useState<Lane[]>(experiment.lanes);
  const [name, setName] = useState(experiment.name);
  const [comments, setComments] = useState(experiment.comments);
  const dirty = lanes !== experiment.lanes || name !== experiment.name || comments !== experiment.comments;
  const marker = markers.find((m) => m.id === experiment.markerId);
  const H = 360;

  const save = async () => {
    await saveExperiment({ ...experiment, name: name.trim() || experiment.name, comments, lanes });
    notify('Experiment saved.');
  };

  const exportBands = () => {
    const q = (s: string) => `"${s.replace(/"/g, '""')}"`;
    const rows = [['lane', 'reference', 'strain', 'genus', 'species', 'country', 'mlst', 'band', 'position', 'molecular_weight'].join(',')];
    for (const l of lanes) {
      const n = experiment.normalised.find((x) => x.laneId === l.id);
      n?.mw.forEach((mw, i) =>
        rows.push(
          [q(l.name), l.isReference, q(l.meta.strain), q(l.meta.genus), q(l.meta.species), q(l.meta.country), q(l.meta.mlst), i + 1, n.positions[i].toFixed(2), mw.toFixed(3)].join(','),
        ),
      );
    }
    downloadText(`${safeFilename(experiment.name)}-bands.csv`, rows.join('\n'), 'text/csv');
  };

  return (
    <div className="page">
      <div className="page-header">
        <div className="row">
          <button onClick={onBack}>‹ Experiments</button>
          <h2>{experiment.name}</h2>
          <span className="badge ok">Complete</span>
        </div>
        <div className="row">
          <button onClick={onEdit}>Edit analysis</button>
          <button onClick={exportBands}>Bands CSV</button>
          <button className="primary" onClick={save} disabled={!dirty}>
            Save changes
          </button>
        </div>
      </div>
      <div className="stack" style={{ gap: 16 }}>
        <div className="card stack">
          <div className="row" style={{ alignItems: 'flex-start' }}>
            <label className="field" style={{ flex: 1, minWidth: 220 }}>
              <span>Name</span>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
            </label>
            <label className="field" style={{ flex: 2, minWidth: 260 }}>
              <span>Comments</span>
              <input type="text" value={comments} onChange={(e) => setComments(e.target.value)} />
            </label>
          </div>
          <div className="small muted">
            Marker: <b>{marker?.name ?? 'unknown'}</b> · {experiment.lanes.length} lanes · {experiment.darkBackground ? 'dark' : 'light'}{' '}
            background · created {new Date(experiment.createdAt).toLocaleDateString()}
          </div>
        </div>
        <div className="card stack">
          <h3>Normalised gel</h3>
          <div className="montage">
            <div style={{ display: 'flex', gap: 6 }}>
              {lanes.map((l) => {
                const n = experiment.normalised.find((x) => x.laneId === l.id);
                if (!n) return null;
                const w = Math.max(24, (n.width / n.height) * H);
                return (
                  <div key={l.id} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                    <div style={{ position: 'relative', width: w, height: H }}>
                      <img src={n.image} alt={l.name} style={{ width: w, height: H, display: 'block' }} />
                      {n.positions.map((p, i) => (
                        <div
                          key={i}
                          title={`${n.mw[i].toPrecision(4)}`}
                          style={{
                            position: 'absolute',
                            left: -3,
                            width: 4,
                            top: (p / n.height) * H - 1,
                            height: 2,
                            background: l.isReference ? 'var(--ref)' : experiment.bandColour,
                          }}
                        />
                      ))}
                    </div>
                    <span style={{ color: '#ddd', fontSize: 11, maxWidth: w + 20, textAlign: 'center' }}>{l.name}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
        <div className="card stack">
          <h3>Bands</h3>
          <div className="table-wrap">
            <table className="grid small">
              <thead>
                <tr>
                  <th>Lane</th>
                  <th>Bands</th>
                  <th>Molecular weights</th>
                </tr>
              </thead>
              <tbody>
                {lanes.map((l) => {
                  const n = experiment.normalised.find((x) => x.laneId === l.id);
                  return (
                    <tr key={l.id}>
                      <td>
                        {l.name} {l.isReference && <span className="badge ref">ref</span>}
                      </td>
                      <td>{n?.mw.length ?? 0}</td>
                      <td className="muted">{n?.mw.map((v) => v.toPrecision(4)).join(', ')}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
        <div className="card stack">
          <h3>Lane metadata</h3>
          <LaneMetaEditor lanes={lanes} onChange={setLanes} />
        </div>
      </div>
    </div>
  );
}
