import { Fragment, useMemo, useState } from 'react';
import { formatScore, hitsCsv, identify, summaryCsv, verdict, type Hit, type Identification, type IdentifySettings, type LibraryEntry } from '../core/identify';
import { isBandBased, isCoefficient, SIMILARITY_METHODS } from '../core/similarity';
import type { Experiment, Lab, Lane, NormalisedLane, SimilarityMethod } from '../core/types';
import { deleteLibraryEntries, saveLibraryEntries } from '../storage/db';
import { downloadText, safeFilename } from './imageio';

/** A sample lane of a complete experiment (marker lanes are never identified nor kept in the library). */
interface LaneEntry {
  key: string;
  exp: Experiment;
  lab?: Lab;
  lane: Lane;
  norm: NormalisedLane;
  name: string;
}

interface Ref {
  entry: LibraryEntry;
  lane: LaneEntry;
}

interface Run {
  settings: IdentifySettings;
  queries: { lane: LaneEntry; result: Identification<Ref> }[];
}

type TypeSource = 'custom' | 'genus+species' | 'species' | 'mlst' | 'strain';

const TYPE_SOURCES: [TypeSource, string][] = [
  ['custom', 'Type name below'],
  ['genus+species', 'Genus and species of each lane'],
  ['species', 'Species of each lane'],
  ['mlst', 'MLST of each lane'],
  ['strain', 'Strain number of each lane'],
];

function typeFromMeta(l: Lane, source: TypeSource, custom: string): string {
  switch (source) {
    case 'custom':
      return custom.trim();
    case 'genus+species':
      return [l.meta.genus, l.meta.species].map((s) => s.trim()).filter(Boolean).join(' ');
    default:
      return l.meta[source].trim();
  }
}

function runIdentification(queries: LaneEntry[], refs: Ref[], settings: IdentifySettings): Run {
  return {
    settings,
    queries: queries.map((lane) => ({
      lane,
      // A library lane is never its own match.
      result: identify(
        { mw: lane.norm.mw, profile: lane.norm.profile },
        refs.filter((r) => r.lane.key !== lane.key).map((r) => ({ item: r, type: r.entry.type, lane: { mw: r.lane.norm.mw, profile: r.lane.norm.profile } })),
        settings,
      ),
    })),
  };
}

const DEFAULT_SETTINGS: IdentifySettings = { similarity: 'Dice', tolerance: 1, cutoff: null };

interface Props {
  experiments: Experiment[];
  labs: Lab[];
  library: LibraryEntry[];
  /** Lanes (`${experimentId}/${laneId}`) to identify on opening the page. */
  initialQueries?: string[];
  notify: (msg: string) => void;
}

export function LibraryPage({ experiments, labs, library, initialQueries, notify }: Props) {
  const lanes = useMemo(() => {
    const out: LaneEntry[] = [];
    for (const exp of experiments) {
      if (exp.status !== 'complete') continue;
      for (const lane of exp.lanes) {
        const norm = exp.normalised.find((n) => n.laneId === lane.id);
        if (norm && !lane.isReference)
          out.push({ key: `${exp.id}/${lane.id}`, exp, lane, norm, lab: labs.find((l) => l.id === exp.labId), name: `${exp.name} · ${lane.name}` });
      }
    }
    return out;
  }, [experiments, labs]);
  const laneByKey = useMemo(() => new Map(lanes.map((l) => [l.key, l])), [lanes]);
  const libByKey = useMemo(() => new Map(library.map((r) => [r.id, r])), [library]);
  const refs = useMemo(
    () => library.flatMap((entry) => (laneByKey.has(entry.id) ? [{ entry, lane: laneByKey.get(entry.id)! }] : [])),
    [library, laneByKey],
  );
  const types = useMemo(() => [...new Set(library.map((r) => r.type))].sort((a, b) => a.localeCompare(b)), [library]);

  const [selected, setSelected] = useState<Set<string>>(() => new Set((initialQueries ?? []).filter((k) => laneByKey.has(k))));
  const [filter, setFilter] = useState('');
  const [hideLibrary, setHideLibrary] = useState(false);
  const [typeSource, setTypeSource] = useState<TypeSource>('custom');
  const [typeName, setTypeName] = useState('');
  const [similarity, setSimilarity] = useState<SimilarityMethod>(DEFAULT_SETTINGS.similarity);
  const [tolerance, setTolerance] = useState(DEFAULT_SETTINGS.tolerance);
  const [cutOn, setCutOn] = useState(true);
  const [cutoff, setCutoff] = useState(80);
  const [top, setTop] = useState(5);
  const [run, setRun] = useState<Run | null>(() => {
    const qs = (initialQueries ?? []).flatMap((k) => (laneByKey.has(k) ? [laneByKey.get(k)!] : []));
    return qs.length && refs.length ? runIdentification(qs, refs, { ...DEFAULT_SETTINGS, cutoff: 80 }) : null;
  });
  const [current, setCurrent] = useState(0);
  const [tab, setTab] = useState<'library' | 'identify'>(run ? 'identify' : 'library');
  const [typeFilter, setTypeFilter] = useState('');
  const [libFilter, setLibFilter] = useState('');

  const matchesFilter = (l: LaneEntry, f: string) => {
    if (!f.trim()) return true;
    const hay = [l.exp.name, l.lane.name, l.lab?.name, l.lab?.country, libByKey.get(l.key)?.type, ...Object.values(l.lane.meta)].join(' ').toLowerCase();
    return f
      .toLowerCase()
      .split(/\s+/)
      .every((t) => hay.includes(t));
  };
  const visible = lanes.filter((l) => (!hideLibrary || !libByKey.has(l.key)) && matchesFilter(l, filter));
  const byExp = new Map<string, LaneEntry[]>();
  for (const l of visible) byExp.set(l.exp.id, [...(byExp.get(l.exp.id) ?? []), l]);
  const toggle = (keys: string[], on: boolean) =>
    setSelected((s) => {
      const n = new Set(s);
      keys.forEach((k) => (on ? n.add(k) : n.delete(k)));
      return n;
    });
  const chosen = lanes.filter((l) => selected.has(l.key));

  const addToLibrary = async () => {
    const now = Date.now();
    const entries: LibraryEntry[] = [];
    let skipped = 0;
    for (const l of chosen) {
      const type = typeFromMeta(l.lane, typeSource, typeName);
      if (!type) {
        skipped++;
        continue;
      }
      const old = libByKey.get(l.key);
      entries.push({ id: l.key, type, comments: old?.comments ?? '', createdAt: old?.createdAt ?? now });
    }
    if (entries.length) await saveLibraryEntries(entries);
    notify(
      `${entries.length} lane${entries.length === 1 ? '' : 's'} added to the library` +
        (skipped ? ` · ${skipped} skipped (no ${typeSource === 'custom' ? 'type name' : 'value in that field'})` : '') +
        '.',
    );
    if (entries.length) setTab('library');
  };

  const identifySelected = () => {
    setRun(runIdentification(chosen, refs, { similarity, tolerance, cutoff: cutOn ? cutoff : null }));
    setCurrent(0);
    setTab('identify');
  };

  const renameType = async (from: string) => {
    const to = window.prompt(`Rename the type "${from}" (every entry of this type) to`, from)?.trim();
    if (!to || to === from) return;
    await saveLibraryEntries(library.filter((r) => r.type === from).map((r) => ({ ...r, type: to })));
    if (typeFilter === from) setTypeFilter(to);
  };

  const editEntry = async (r: LibraryEntry) => {
    const to = window.prompt('Type of this reference', r.type)?.trim();
    if (!to || to === r.type) return;
    await saveLibraryEntries([{ ...r, type: to }]);
  };

  const missing = library.filter((r) => !laneByKey.has(r.id));
  const shownLibrary = library.filter((r) => {
    if (typeFilter && r.type !== typeFilter) return false;
    const l = laneByKey.get(r.id);
    return l ? matchesFilter(l, libFilter) : !libFilter.trim();
  });

  return (
    <div className="page">
      <div className="page-header">
        <h2>Reference library</h2>
        <span className="small muted">
          {library.length} reference lanes · {types.length} types
        </span>
      </div>
      <div className="compare">
        <div className="stack">
          <div className="card stack">
            <h3>1. Lanes</h3>
            <input type="search" placeholder="Filter by name, strain, species, type, laboratory…" value={filter} onChange={(e) => setFilter(e.target.value)} />
            <div className="row small">
              <button onClick={() => toggle(visible.map((l) => l.key), true)}>Select visible</button>
              <button onClick={() => setSelected(new Set())}>Clear</button>
              <label className="check">
                <input type="checkbox" checked={hideLibrary} onChange={(e) => setHideLibrary(e.target.checked)} />
                Hide library lanes
              </label>
              <span className="spacer" />
              <span className="muted">{selected.size} selected</span>
            </div>
            {lanes.length === 0 ? (
              <div className="empty small">Complete the analysis of an experiment to build the library and identify its lanes.</div>
            ) : (
              <div className="lane-picker">
                <table className="grid small">
                  <tbody>
                    {[...byExp.entries()].map(([id, list]) => (
                      <Fragment key={id}>
                        <tr className="exp-row">
                          <td style={{ width: 28 }}>
                            <input
                              type="checkbox"
                              checked={list.every((l) => selected.has(l.key))}
                              aria-label={`Select every lane of ${list[0].exp.name}`}
                              onChange={(e) => toggle(list.map((x) => x.key), e.target.checked)}
                            />
                          </td>
                          <td colSpan={3}>
                            {list[0].exp.name}
                            {list[0].lab && <span className="muted" style={{ fontWeight: 400 }}> · {list[0].lab.name}</span>}
                          </td>
                        </tr>
                        {list.map((l) => (
                          <tr key={l.key}>
                            <td>
                              <input type="checkbox" checked={selected.has(l.key)} aria-label={`Select ${l.lane.name}`} onChange={(ev) => toggle([l.key], ev.target.checked)} />
                            </td>
                            <td>
                              {l.lane.name} {libByKey.has(l.key) && <span className="badge ok">{libByKey.get(l.key)!.type}</span>}
                            </td>
                            <td className="muted">{[l.lane.meta.strain, l.lane.meta.genus, l.lane.meta.species].filter(Boolean).join(' · ')}</td>
                            <td className="muted" style={{ textAlign: 'right' }}>
                              {l.norm.mw.length}b
                            </td>
                          </tr>
                        ))}
                      </Fragment>
                    ))}
                  </tbody>
                </table>
                {visible.length === 0 && <div className="empty small">No lanes match the filter.</div>}
              </div>
            )}
          </div>
          <div className="card stack">
            <h3>2. Add to the library</h3>
            <label className="field">
              <span>Type of each reference</span>
              <select value={typeSource} onChange={(e) => setTypeSource(e.target.value as TypeSource)}>
                {TYPE_SOURCES.map(([k, l]) => (
                  <option key={k} value={k}>
                    {l}
                  </option>
                ))}
              </select>
            </label>
            {typeSource === 'custom' && (
              <label className="field">
                <span>Type name (pulsotype, ribotype, species…)</span>
                <input type="text" list="library-types" value={typeName} placeholder="e.g. PT-A" onChange={(e) => setTypeName(e.target.value)} />
                <datalist id="library-types">
                  {types.map((t) => (
                    <option key={t} value={t} />
                  ))}
                </datalist>
              </label>
            )}
            <div className="row">
              <button onClick={addToLibrary} disabled={chosen.length === 0 || (typeSource === 'custom' && !typeName.trim())}>
                {chosen.length ? `Add ${chosen.length} lane${chosen.length === 1 ? '' : 's'} to the library` : 'Add lanes to the library'}
              </button>
            </div>
          </div>
          <div className="card stack">
            <h3>3. Identify</h3>
            <label className="field">
              <span>Similarity measure</span>
              <select value={similarity} onChange={(e) => setSimilarity(e.target.value as SimilarityMethod)}>
                <optgroup label="Band-based">
                  {SIMILARITY_METHODS.filter(isBandBased).map((m) => (
                    <option key={m}>{m}</option>
                  ))}
                </optgroup>
                <optgroup label="Densitometric curve">
                  {SIMILARITY_METHODS.filter((m) => !isBandBased(m)).map((m) => (
                    <option key={m}>{m}</option>
                  ))}
                </optgroup>
              </select>
            </label>
            {isBandBased(similarity) && (
              <label className="field">
                <span>Tolerance (% of the molecular-weight range)</span>
                <input type="number" min={0} step={0.1} value={tolerance} onChange={(e) => setTolerance(Math.max(0, Number(e.target.value)))} />
              </label>
            )}
            <div className="row small">
              <label className="check">
                <input type="checkbox" checked={cutOn} onChange={(e) => setCutOn(e.target.checked)} />
                {isCoefficient(similarity) ? 'Identify only above' : 'Identify only below distance'}
              </label>
              <input
                type="number"
                aria-label="Cut-off"
                min={0}
                max={isCoefficient(similarity) ? 100 : undefined}
                step={isCoefficient(similarity) ? 1 : 0.01}
                value={cutoff}
                disabled={!cutOn}
                style={{ width: 70 }}
                onChange={(e) => setCutoff(Math.max(0, Number(e.target.value)))}
              />
              {isCoefficient(similarity) && <span>%</span>}
            </div>
            <label className="field">
              <span>Matches shown per lane</span>
              <input type="number" min={1} max={50} value={top} onChange={(e) => setTop(Math.max(1, Math.min(50, Math.round(Number(e.target.value)) || 1)))} />
            </label>
            {refs.length === 0 && <div className="alert warn small">Add reference lanes to the library first.</div>}
            {new Set([...chosen, ...refs.map((r) => r.lane)].map((l) => l.exp.markerId)).size > 1 && (
              <div className="alert warn small">
                These lanes were normalised with different markers. Band-based measures compare molecular weights; curve-based measures are
                less reliable across markers.
              </div>
            )}
            <div className="row">
              <button className="primary" onClick={identifySelected} disabled={chosen.length === 0 || refs.length === 0}>
                {chosen.length ? `Identify ${chosen.length} lane${chosen.length === 1 ? '' : 's'}` : 'Identify lanes'}
              </button>
            </div>
          </div>
        </div>
        <div className="card" style={{ minWidth: 0 }}>
          <div className="result-tabs">
            <button className={tab === 'library' ? 'active' : ''} onClick={() => setTab('library')}>
              Library ({library.length})
            </button>
            <button className={tab === 'identify' ? 'active' : ''} onClick={() => setTab('identify')}>
              Identification{run ? ` (${run.queries.length})` : ''}
            </button>
          </div>
          {tab === 'library' ? (
            library.length === 0 ? (
              <div className="empty">
                The library is empty. Select lanes of known type on the left, give the type (pulsotype, ribotype, species…) and add them to the
                library; then identify other lanes against it.
              </div>
            ) : (
              <div className="stack">
                <div className="row small">
                  <select value={typeFilter} aria-label="Filter by type" onChange={(e) => setTypeFilter(e.target.value)}>
                    <option value="">All types ({types.length})</option>
                    {types.map((t) => (
                      <option key={t} value={t}>
                        {t} ({library.filter((r) => r.type === t).length})
                      </option>
                    ))}
                  </select>
                  {typeFilter && <button onClick={() => renameType(typeFilter)}>Rename type…</button>}
                  <input type="search" placeholder="Filter…" value={libFilter} onChange={(e) => setLibFilter(e.target.value)} style={{ flex: 1, minWidth: 140 }} />
                  {missing.length > 0 && (
                    <button
                      className="danger"
                      title="Entries whose experiment or lane was deleted"
                      onClick={async () => {
                        if (window.confirm(`Remove ${missing.length} entries whose lane no longer exists?`)) await deleteLibraryEntries(missing.map((r) => r.id));
                      }}
                    >
                      Remove {missing.length} missing
                    </button>
                  )}
                </div>
                <div className="table-wrap" style={{ maxHeight: '70vh' }}>
                  <table className="grid small">
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Lane</th>
                        <th>Strain · species</th>
                        <th>Laboratory</th>
                        <th>Bands</th>
                        <th />
                      </tr>
                    </thead>
                    <tbody>
                      {shownLibrary.map((r) => {
                        const l = laneByKey.get(r.id);
                        return (
                          <tr key={r.id}>
                            <td>
                              <button className="link" title="Change the type of this reference" onClick={() => editEntry(r)}>
                                {r.type}
                              </button>
                            </td>
                            <td>{l ? l.name : <span className="badge warn">missing lane</span>}</td>
                            <td className="muted">{l && [l.lane.meta.strain, l.lane.meta.genus, l.lane.meta.species].filter(Boolean).join(' · ')}</td>
                            <td className="muted">{l?.lab?.name}</td>
                            <td>{l?.norm.mw.length}</td>
                            <td style={{ textAlign: 'right' }}>
                              <button className="danger" onClick={() => deleteLibraryEntries([r.id])}>
                                Remove
                              </button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )
          ) : !run ? (
            <div className="empty">Select lanes on the left and press Identify to rank the closest reference lanes.</div>
          ) : (
            <Results run={run} top={top} current={Math.min(current, run.queries.length - 1)} onSelect={setCurrent} />
          )}
        </div>
      </div>
    </div>
  );
}

function Results({ run, top, current, onSelect }: { run: Run; top: number; current: number; onSelect: (i: number) => void }) {
  const { settings } = run;
  const coefficient = isCoefficient(settings.similarity);
  const bandBased = isBandBased(settings.similarity);
  const q = run.queries[current];
  const base = safeFilename(`identification-${settings.similarity}`);
  const queries = run.queries.map((x) => ({ name: x.lane.name, lane: { mw: x.lane.norm.mw, profile: x.lane.norm.profile }, result: x.result }));
  const refName = (r: Ref) => r.lane.name;
  const cutText =
    settings.cutoff === null ? 'no cut-off' : coefficient ? `cut-off ${settings.cutoff}%` : `cut-off distance ${settings.cutoff}`;
  const hits = q.result.hits.slice(0, top);
  return (
    <div className="stack">
      <div className="row small">
        <span className="muted">
          {settings.similarity}
          {bandBased && ` · tolerance ${settings.tolerance}% (±${q.result.tolerance.toPrecision(3)})`} · {cutText}
        </span>
        <span className="spacer" />
        <button onClick={() => downloadText(`${base}-summary.csv`, summaryCsv(queries, refName), 'text/csv')}>Summary CSV</button>
        <button onClick={() => downloadText(`${base}-matches.csv`, hitsCsv(queries, refName, top), 'text/csv')} title={`The ${top} closest references of every lane`}>
          Matches CSV
        </button>
      </div>
      {run.queries.length > 1 && (
        <div className="table-wrap" style={{ maxHeight: '40vh' }}>
          <table className="grid small">
            <thead>
              <tr>
                <th>Lane</th>
                <th>Identified as</th>
                <th>Best match</th>
                <th>{coefficient ? 'Similarity' : 'Distance'}</th>
                {bandBased && <th>Bands</th>}
                <th>Next other type</th>
              </tr>
            </thead>
            <tbody>
              {run.queries.map((x, i) => {
                const v = verdict(x.result);
                return (
                  <tr key={x.lane.key} onClick={() => onSelect(i)} style={{ cursor: 'pointer', background: i === current ? 'var(--accent-soft)' : undefined }}>
                    <td>{x.lane.name}</td>
                    <td>{v.type ? <span className="badge ok">{v.type}</span> : <span className="badge warn">unidentified</span>}</td>
                    <td className="muted">{v.best ? `${v.best.type} · ${v.best.item.lane.name}` : '—'}</td>
                    <td>{v.best ? formatScore(v.best) : ''}</td>
                    {bandBased && <td>{v.best && `${v.best.bands.pairs.length}/${x.lane.norm.mw.length}`}</td>}
                    <td className="muted">{v.runnerUp ? `${v.runnerUp.type} (${formatScore(v.runnerUp)})` : '—'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      <h3 style={{ margin: '8px 0 0' }}>
        {q.lane.name}{' '}
        {(() => {
          const v = verdict(q.result);
          return v.type ? <span className="badge ok">{v.type}</span> : <span className="badge warn">unidentified</span>;
        })()}
      </h3>
      {hits.length === 0 ? (
        <div className="empty small">The library has no other lanes to compare with.</div>
      ) : (
        <>
          <div className="table-wrap">
            <table className="grid small">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Type</th>
                  <th>Reference lane</th>
                  <th>{coefficient ? 'Similarity' : 'Distance'}</th>
                  {bandBased && (
                    <>
                      <th title="Matched bands (bands of the lane, bands of the reference)">Matched bands</th>
                      <th>Unmatched in lane</th>
                      <th>Unmatched in reference</th>
                    </>
                  )}
                </tr>
              </thead>
              <tbody>
                {hits.map((h, k) => (
                  <tr key={h.item.entry.id} style={{ opacity: h.passes ? 1 : 0.6 }}>
                    <td>{k + 1}</td>
                    <td>
                      <b>{h.type}</b>
                    </td>
                    <td>{h.item.lane.name}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {h.similarity !== null && (
                        <span
                          style={{ display: 'inline-block', width: 60, height: 8, marginRight: 6, background: 'var(--surface-2)', borderRadius: 4, overflow: 'hidden', verticalAlign: 'middle' }}
                        >
                          <span style={{ display: 'block', height: '100%', width: `${h.similarity}%`, background: h.passes ? 'var(--ok)' : 'var(--muted)' }} />
                        </span>
                      )}
                      {formatScore(h)}
                      {!h.passes && <span className="muted"> · below cut-off</span>}
                    </td>
                    {bandBased && (
                      <>
                        <td>
                          {h.bands.pairs.length} <span className="muted">(lane {q.lane.norm.mw.length}, ref. {h.lane.mw.length})</span>
                        </td>
                        <td className="muted">{h.bands.queryOnly.map((i) => Number(q.lane.norm.mw[i].toPrecision(4))).join(', ')}</td>
                        <td className="muted">{h.bands.refOnly.map((i) => Number(h.lane.mw[i].toPrecision(4))).join(', ')}</td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <SideBySide query={q.lane} hits={hits.slice(0, 6)} bandBased={bandBased} />
        </>
      )}
    </div>
  );
}

/** The query lane next to its closest references; with band-based measures, matched bands are green and unmatched ones red. */
function SideBySide({ query, hits, bandBased }: { query: LaneEntry; hits: Hit<Ref>[]; bandBased: boolean }) {
  const H = 320;
  const matchedInQuery = new Set(hits[0]?.bands.pairs.map(([i]) => i));
  const column = (key: string, n: NormalisedLane, label: string, sub: string, matched: (i: number) => boolean | null) => {
    const w = Math.max(24, (n.width / n.height) * H);
    return (
      <div key={key} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
        <div style={{ position: 'relative', width: w, height: H }}>
          <img src={n.image} alt={label} style={{ width: w, height: H, display: 'block' }} />
          {n.positions.map((p, i) => {
            const m = matched(i);
            return (
              <div
                key={i}
                title={`${Number(n.mw[i].toPrecision(4))}${m === null ? '' : m ? ' · matched' : ' · unmatched'}`}
                style={{ position: 'absolute', left: -4, width: 6, top: (p / n.height) * H - 1, height: 2, background: m === null ? '#2f7dff' : m ? '#4ac26b' : '#f2716b' }}
              />
            );
          })}
        </div>
        <span style={{ color: '#ddd', fontSize: 11, maxWidth: w + 40, textAlign: 'center' }}>{label}</span>
        <span style={{ color: '#9aa', fontSize: 11 }}>{sub}</span>
      </div>
    );
  };
  return (
    <div className="stack" style={{ gap: 6 }}>
      <span className="small muted">
        Lane and closest references{bandBased && ' — green: band matched (lane vs. best match; each reference vs. the lane), red: unmatched'}
      </span>
      <div className="montage">
        <div style={{ display: 'flex', gap: 10 }}>
          {column('query', query.norm, query.lane.name, 'lane', (i) => (bandBased && hits.length ? matchedInQuery.has(i) : null))}
          <div style={{ width: 1, background: '#555' }} />
          {hits.map((h) => {
            const used = new Set(h.bands.pairs.map(([, j]) => j));
            return column(h.item.entry.id, h.item.lane.norm, h.type, formatScore(h), (j) => (bandBased ? used.has(j) : null));
          })}
        </div>
      </div>
    </div>
  );
}
