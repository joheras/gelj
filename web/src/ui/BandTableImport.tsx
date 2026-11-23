import { Fragment, useMemo, useRef, useState } from 'react';
import {
  guessMapping,
  META_FIELDS,
  numericHeaderColumns,
  parseDelimited,
  readBandTable,
  type BandTableLayout,
  type BandTableMapping,
  type MetaField,
} from '../core/bandtable';
import type { Lab } from '../core/types';
import { saveExperiment, saveLab } from '../storage/db';
import { experimentsFromBandTable } from './bandtableio';
import { LabSelect } from './Labs';

const SAMPLES: [string, string][] = [
  ['band-matching-table.txt', 'Band-matching table (binary, tab-delimited)'],
  ['bands-one-row-per-band.csv', 'One row per band (GelJ bands CSV)'],
  ['bands-one-row-per-lane.csv', 'One row per lane (kb, semicolons)'],
];

const LAYOUTS: [BandTableLayout, string][] = [
  ['long', 'One row per band'],
  ['wide', 'One row per lane, band columns or a list'],
  ['matrix', 'Band-matching / character table (0/1)'],
];

const DELIMS: [string, string][] = [
  ['', 'Automatic'],
  ['\t', 'Tab'],
  [';', 'Semicolon'],
  [',', 'Comma'],
];

const SCALES: [number, string][] = [
  [1, 'As in the file'],
  [1000, 'kb → bp (× 1000)'],
  [0.001, 'bp → kb (÷ 1000)'],
];

interface Props {
  labs: Lab[];
  defaultLabId: string | null;
  onDone: (msg: string) => void;
  onCancel: () => void;
}

interface Loaded {
  file: string;
  text: string;
}

export function BandTableImport({ labs, defaultLabId, onDone, onCancel }: Props) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const [delimiter, setDelimiter] = useState('');
  const [mapping, setMapping] = useState<BandTableMapping | null>(null);
  const [name, setName] = useState('');
  const [labId, setLabId] = useState<string | null>(defaultLabId);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const table = useMemo(() => (loaded ? parseDelimited(loaded.text, delimiter || undefined) : null), [loaded, delimiter]);
  const result = useMemo(() => (table && mapping ? readBandTable(table, mapping) : null), [table, mapping]);
  const headers = table && mapping ? (table.rows[mapping.headerRow] ?? []) : [];
  const errors = result?.issues.filter((i) => i.level === 'error') ?? [];
  const warnings = result?.issues.filter((i) => i.level === 'warning') ?? [];

  const load = (file: string, text: string) => {
    setError(null);
    const t = parseDelimited(text);
    if (t.rows.length < 2) {
      setError(`${file} has no data rows.`);
      return;
    }
    setLoaded({ file, text });
    setDelimiter('');
    setMapping(guessMapping(t));
    setName(file.replace(/\.[^.]+$/, ''));
  };

  const openFile = async (f: File) => {
    try {
      load(f.name, await f.text());
    } catch (e) {
      setError(`Could not read ${f.name}: ${(e as Error).message}`);
    }
  };

  const openSample = async (file: string) => {
    try {
      const res = await fetch(`${import.meta.env.BASE_URL}samples/${file}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      load(file, await res.text());
    } catch (e) {
      setError(`Could not load the sample: ${(e as Error).message}`);
    }
  };

  const setDelim = (d: string) => {
    setDelimiter(d);
    if (loaded) setMapping(guessMapping(parseDelimited(loaded.text, d || undefined)));
  };

  const update = (patch: Partial<BandTableMapping>) => setMapping((m) => (m ? { ...m, ...patch } : m));

  const setLayout = (layout: BandTableLayout) => {
    if (!table || !mapping) return;
    const h = table.rows[mapping.headerRow] ?? [];
    let bands = mapping.bands.filter((b) => b !== mapping.lane);
    if (layout === 'matrix') bands = numericHeaderColumns(h);
    else if (layout === 'long') bands = bands.slice(0, 1);
    update({ layout, bands });
  };

  const setHeaderRow = (row: number) => {
    if (!table) return;
    setMapping(guessMapping(table, row));
  };

  const importNow = async () => {
    if (!result || !mapping || !loaded || errors.length) return;
    setSaving(true);
    setError(null);
    try {
      const { experiments, newLabs } = await experimentsFromBandTable(result.lanes, {
        name: name.trim() || loaded.file,
        source: { file: loaded.file, layout: mapping.layout, values: mapping.values },
        labId,
        labs,
      });
      for (const l of newLabs) await saveLab(l);
      for (const e of experiments) await saveExperiment(e);
      const lanes = experiments.reduce((s, e) => s + e.lanes.length, 0);
      onDone(
        `Imported ${lanes} lanes into ${experiments.length} experiment${experiments.length > 1 ? 's' : ''}` +
          (newLabs.length ? ` and created ${newLabs.length} laborator${newLabs.length > 1 ? 'ies' : 'y'}.` : '.'),
      );
    } catch (e) {
      setError(`Import failed: ${(e as Error).message}`);
      setSaving(false);
    }
  };

  const colOptions = headers.map((h, i) => (
    <option key={i} value={i}>
      {i + 1}: {h || '(no header)'}
    </option>
  ));

  const colSelect = (value: number | undefined, onChange: (v: number | undefined) => void, label: string, optional = true) => (
    <label className="field" style={{ minWidth: 150, flex: 1 }}>
      <span>{label}</span>
      <select value={value ?? ''} onChange={(e) => onChange(e.target.value === '' ? undefined : Number(e.target.value))}>
        {optional && <option value="">—</option>}
        {colOptions}
      </select>
    </label>
  );

  const hasLabColumn = mapping?.meta.lab !== undefined;
  const preview = result?.lanes ?? [];

  return (
    <div className="stack" style={{ gap: 16 }}>
      <div className="card stack">
        <div className="row">
          <h3 style={{ margin: 0 }}>Import a band table</h3>
          <span className="spacer" />
          <button onClick={onCancel}>Cancel</button>
        </div>
        <p className="small muted" style={{ margin: 0 }}>
          Bring band data from GelCompar II / BioNumerics (band-matching or band exports), GelJ or a spreadsheet as CSV, TSV or text. Imported
          lanes have no gel image: they are compared with band-based coefficients (Dice, Jaccard…) and drawn from their bands. See the{' '}
          <a href="https://github.com/joheras/gelj/blob/main/web/docs/migration-from-gelcompar.md" target="_blank" rel="noreferrer">
            migration guide
          </a>
          .
        </p>
        <div className="row">
          <button className="primary" onClick={() => fileRef.current?.click()}>
            Choose a file…
          </button>
          <span className="small muted">or try a sample:</span>
          {SAMPLES.map(([f, label]) => (
            <button key={f} className="link small" onClick={() => openSample(f)} title={f}>
              {label}
            </button>
          ))}
          <input
            ref={fileRef}
            type="file"
            hidden
            accept=".csv,.tsv,.txt,.tab,text/csv,text/plain,text/tab-separated-values"
            onChange={(e) => {
              const f = e.target.files?.[0];
              e.target.value = '';
              if (f) openFile(f);
            }}
          />
        </div>
        {error && <div className="alert error small">{error}</div>}
      </div>

      {loaded && table && mapping && (
        <>
          <div className="card stack">
            <h3>1. Layout and columns</h3>
            <div className="small muted">
              <b>{loaded.file}</b> · {table.rows.length - mapping.headerRow - 1} data rows · {headers.length} columns
            </div>
            <div className="row" style={{ alignItems: 'flex-end' }}>
              <label className="field" style={{ minWidth: 150, flex: 1 }}>
                <span>Delimiter</span>
                <select value={delimiter} onChange={(e) => setDelim(e.target.value)}>
                  {DELIMS.map(([d, l]) => (
                    <option key={l} value={d}>
                      {l}
                      {d === '' ? ` (${DELIMS.find(([x]) => x === table.delimiter)?.[1] ?? table.delimiter})` : ''}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field" style={{ minWidth: 150, flex: 1 }}>
                <span>Header row</span>
                <select value={mapping.headerRow} onChange={(e) => setHeaderRow(Number(e.target.value))}>
                  {table.rows.slice(0, Math.min(10, table.rows.length - 1)).map((r, i) => (
                    <option key={i} value={i}>
                      Row {i + 1}: {r.filter(Boolean).slice(0, 3).join(', ').slice(0, 40)}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field" style={{ minWidth: 220, flex: 2 }}>
                <span>Layout</span>
                <select value={mapping.layout} onChange={(e) => setLayout(e.target.value as BandTableLayout)}>
                  {LAYOUTS.map(([k, l]) => (
                    <option key={k} value={k}>
                      {l}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            {mapping.headerRow > 0 && <div className="small muted">Rows above the header row are ignored.</div>}
            <div className="row" style={{ alignItems: 'flex-end' }}>
              {colSelect(mapping.lane, (v) => update({ lane: v ?? 0, bands: mapping.bands.filter((b) => b !== v) }), 'Lane label (key) *', false)}
              {colSelect(mapping.reference, (v) => update({ reference: v }), 'Marker lane flag')}
              {META_FIELDS.map(([f, label]) => (
                <Fragment key={f}>{colSelect(mapping.meta[f], (v) => update({ meta: { ...mapping.meta, [f]: v } as Record<MetaField, number> }), label)}</Fragment>
              ))}
            </div>
            {mapping.layout === 'long' ? (
              <div className="row" style={{ alignItems: 'flex-end' }}>
                {colSelect(mapping.bands[0], (v) => update({ bands: v === undefined ? [] : [v] }), 'Band column *', false)}
                <BandValueSettings mapping={mapping} update={update} />
              </div>
            ) : (
              <div className="stack">
                <span className="small muted">
                  {mapping.layout === 'matrix'
                    ? 'Band-class columns (headed by the molecular weight or position of the class; cells: 1/+/x or an intensity > 0 = present, 0/empty = absent) *'
                    : 'Band columns (each cell holds one band, or a list separated by ; | / or spaces) *'}
                </span>
                <div className="row small" style={{ maxHeight: 140, overflow: 'auto' }}>
                  {headers.map((h, i) =>
                    i === mapping.lane ? null : (
                      <label className="check" key={i}>
                        <input
                          type="checkbox"
                          checked={mapping.bands.includes(i)}
                          onChange={(e) =>
                            update({ bands: e.target.checked ? [...mapping.bands, i].sort((a, b) => a - b) : mapping.bands.filter((b) => b !== i) })
                          }
                        />
                        {h || `column ${i + 1}`}
                      </label>
                    ),
                  )}
                </div>
                <div className="row" style={{ alignItems: 'flex-end' }}>
                  <BandValueSettings mapping={mapping} update={update} />
                </div>
              </div>
            )}
          </div>

          <div className="card stack">
            <h3>2. Preview and validation</h3>
            {errors.length > 0 && (
              <div className="alert error small">
                {errors.slice(0, 8).map((i, k) => (
                  <div key={k}>{i.message}</div>
                ))}
                {errors.length > 8 && <div>… and {errors.length - 8} more errors.</div>}
              </div>
            )}
            {warnings.length > 0 && (
              <div className="alert warn small">
                {warnings.slice(0, 8).map((i, k) => (
                  <div key={k}>{i.message}</div>
                ))}
                {warnings.length > 8 && <div>… and {warnings.length - 8} more warnings.</div>}
              </div>
            )}
            {mapping.values === 'position' && (
              <div className="alert warn small">
                Positions (% of a normalised gel) are only comparable with lanes normalised with the same reference system. Compare these lanes
                with each other, not with gel lanes, which use molecular weights.
              </div>
            )}
            <div className="small muted">
              {preview.length} lanes · {preview.reduce((s, l) => s + l.values.length, 0)} bands
              {preview.length > 50 && ' · first 50 shown'}
            </div>
            <div className="table-wrap" style={{ maxHeight: 360 }}>
              <table className="grid small">
                <thead>
                  <tr>
                    <th>Lane</th>
                    {META_FIELDS.filter(([f]) => mapping.meta[f] !== undefined).map(([f, l]) => (
                      <th key={f}>{l}</th>
                    ))}
                    <th>Bands</th>
                    <th>{mapping.values === 'mw' ? `Molecular weights${mapping.scale !== 1 ? ' (scaled)' : ''}` : 'Positions (%)'}</th>
                  </tr>
                </thead>
                <tbody>
                  {preview.slice(0, 50).map((l, i) => (
                    <tr key={i}>
                      <td>
                        {l.name} {l.isReference && <span className="badge ref">ref</span>}
                      </td>
                      {META_FIELDS.filter(([f]) => mapping.meta[f] !== undefined).map(([f]) => (
                        <td key={f}>{f === 'lab' ? l.lab : l.meta[f]}</td>
                      ))}
                      <td>{l.values.length}</td>
                      <td className="muted">{l.values.map((v) => Number(v.toPrecision(5))).join(', ')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card stack">
            <h3>3. Save</h3>
            <div className="row" style={{ alignItems: 'flex-start' }}>
              <label className="field" style={{ flex: 1, minWidth: 220 }}>
                <span>Experiment name</span>
                <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
              </label>
              <div className="field" style={{ flex: 1, minWidth: 240 }}>
                <span className="small muted">{hasLabColumn ? 'Laboratory of lanes without one' : 'Laboratory'}</span>
                <LabSelect labs={labs} value={labId} onChange={setLabId} />
              </div>
            </div>
            {hasLabColumn && (
              <div className="small muted">
                One experiment is created per laboratory of the <i>Laboratory</i> column; laboratories that do not exist yet are created.
              </div>
            )}
            <div className="row">
              <button className="primary" onClick={importNow} disabled={saving || errors.length > 0 || preview.length === 0}>
                {saving ? 'Importing…' : `Import ${preview.length} lanes`}
              </button>
              {errors.length > 0 && <span className="small muted">Fix the errors above (or the column mapping) to import.</span>}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

function BandValueSettings({ mapping, update }: { mapping: BandTableMapping; update: (p: Partial<BandTableMapping>) => void }) {
  return (
    <>
      <label className="field" style={{ minWidth: 200, flex: 1 }}>
        <span>{mapping.layout === 'matrix' ? 'Column headers are' : 'Band values are'}</span>
        <select value={mapping.values} onChange={(e) => update({ values: e.target.value as BandTableMapping['values'], scale: 1 })}>
          <option value="mw">Molecular weights (bp, kb…)</option>
          <option value="position">Positions (% of the normalised gel)</option>
        </select>
      </label>
      {mapping.values === 'mw' && (
        <label className="field" style={{ minWidth: 200, flex: 1 }}>
          <span>Units</span>
          <select value={mapping.scale} onChange={(e) => update({ scale: Number(e.target.value) })}>
            {SCALES.map(([v, l]) => (
              <option key={v} value={v}>
                {l}
              </option>
            ))}
          </select>
        </label>
      )}
    </>
  );
}
