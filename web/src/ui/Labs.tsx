import { useState } from 'react';
import type { Experiment, Lab } from '../core/types';
import { uid } from '../core/uid';
import { deleteLab, saveLab, setDefaultLabId } from '../storage/db';

const EMPTY = { name: '', city: '', country: '' };

/** Name / city / country form, used to create or edit a lab. */
function LabForm({
  initial,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial: { name: string; city: string; country: string };
  submitLabel: string;
  onSubmit: (v: { name: string; city: string; country: string }) => Promise<void> | void;
  onCancel?: () => void;
}) {
  const [v, setV] = useState(initial);
  const [error, setError] = useState<string | null>(null);
  const submit = async () => {
    if (!v.name.trim()) {
      setError('The laboratory needs a name.');
      return;
    }
    setError(null);
    await onSubmit({ name: v.name.trim(), city: v.city.trim(), country: v.country.trim() });
  };
  return (
    <div className="stack">
      <label className="field">
        <span>Laboratory name</span>
        <input type="text" value={v.name} autoFocus onChange={(e) => setV({ ...v, name: e.target.value })} />
      </label>
      <div className="row" style={{ flexWrap: 'nowrap' }}>
        <label className="field" style={{ flex: 1 }}>
          <span>City</span>
          <input type="text" value={v.city} onChange={(e) => setV({ ...v, city: e.target.value })} />
        </label>
        <label className="field" style={{ flex: 1 }}>
          <span>Country</span>
          <input type="text" value={v.country} onChange={(e) => setV({ ...v, country: e.target.value })} />
        </label>
      </div>
      {error && <div className="alert error small">{error}</div>}
      <div className="row">
        <button className="primary" onClick={submit}>
          {submitLabel}
        </button>
        {onCancel && <button onClick={onCancel}>Cancel</button>}
      </div>
    </div>
  );
}

/** Lab selector with inline creation of a new lab. */
export function LabSelect({ labs, value, onChange }: { labs: Lab[]; value: string | null; onChange: (id: string | null) => void }) {
  const [creating, setCreating] = useState(false);
  if (creating) {
    return (
      <div className="card stack" style={{ padding: 12 }}>
        <b className="small">New laboratory</b>
        <LabForm
          initial={EMPTY}
          submitLabel="Add laboratory"
          onCancel={() => setCreating(false)}
          onSubmit={async (v) => {
            const lab: Lab = { id: uid(), ...v };
            await saveLab(lab);
            onChange(lab.id);
            setCreating(false);
          }}
        />
      </div>
    );
  }
  return (
    <div className="row" style={{ flexWrap: 'nowrap' }}>
      <select value={value ?? ''} onChange={(e) => onChange(e.target.value || null)} style={{ flex: 1, minWidth: 0 }} aria-label="Laboratory">
        <option value="">— No laboratory —</option>
        {labs.map((l) => (
          <option key={l.id} value={l.id}>
            {l.name}
            {l.city || l.country ? ` — ${[l.city, l.country].filter(Boolean).join(', ')}` : ''}
          </option>
        ))}
      </select>
      <button onClick={() => setCreating(true)}>New…</button>
    </div>
  );
}

export function LabsPage({ labs, experiments, defaultLabId }: { labs: Lab[]; experiments: Experiment[]; defaultLabId: string | null }) {
  const [editing, setEditing] = useState<string | 'new' | null>(labs.length ? null : 'new');
  const used = (id: string) => experiments.filter((e) => e.labId === id).length;

  return (
    <div className="page" style={{ maxWidth: 900 }}>
      <div className="page-header">
        <h2>Laboratories</h2>
        <button className="primary" onClick={() => setEditing('new')} disabled={editing === 'new'}>
          New laboratory
        </button>
      </div>
      <p className="muted small">
        Record the laboratory where each gel was run. Your own laboratory is preselected for new experiments, and laboratories can be used to
        label and filter lanes when comparing gels from different centres.
      </p>
      <div className="stack" style={{ gap: 12 }}>
        {editing === 'new' && (
          <div className="card">
            <LabForm
              initial={EMPTY}
              submitLabel="Add laboratory"
              onCancel={labs.length ? () => setEditing(null) : undefined}
              onSubmit={async (v) => {
                const lab: Lab = { id: uid(), ...v };
                await saveLab(lab);
                if (!defaultLabId) setDefaultLabId(lab.id);
                setEditing(null);
              }}
            />
          </div>
        )}
        {labs.length > 0 && (
          <div className="card" style={{ padding: 0 }}>
            <table className="grid">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>City</th>
                  <th>Country</th>
                  <th>Experiments</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {labs.map((l) =>
                  editing === l.id ? (
                    <tr key={l.id}>
                      <td colSpan={5}>
                        <LabForm
                          initial={l}
                          submitLabel="Save"
                          onCancel={() => setEditing(null)}
                          onSubmit={async (v) => {
                            await saveLab({ ...l, ...v });
                            setEditing(null);
                          }}
                        />
                      </td>
                    </tr>
                  ) : (
                    <tr key={l.id}>
                      <td>
                        {l.name} {l.id === defaultLabId && <span className="badge ok">my lab</span>}
                      </td>
                      <td>{l.city}</td>
                      <td>{l.country}</td>
                      <td>{used(l.id)}</td>
                      <td>
                        <div className="row" style={{ justifyContent: 'flex-end' }}>
                          {l.id !== defaultLabId && (
                            <button className="small" onClick={() => setDefaultLabId(l.id)} title="Preselect this laboratory for new experiments">
                              Set as my lab
                            </button>
                          )}
                          <button className="small" onClick={() => setEditing(l.id)}>
                            Edit
                          </button>
                          <button
                            className="small danger"
                            onClick={async () => {
                              const n = used(l.id);
                              const msg = n
                                ? `Delete "${l.name}"? ${n} experiment(s) will no longer have a laboratory.`
                                : `Delete "${l.name}"?`;
                              if (window.confirm(msg)) await deleteLab(l.id);
                            }}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ),
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
