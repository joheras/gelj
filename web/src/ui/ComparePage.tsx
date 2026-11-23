import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import { bandMatrixCsv, bandMatrixNexus, matchBands, type BandMatching } from '../core/bandmatch';
import { Bootstrap, bootstrapData } from '../core/bootstrap';
import { cluster, copheneticCorrelation, leafOrder, LINKAGES, toNewick, type ClusterNode } from '../core/cluster';
import { rotate90 } from '../core/image';
import { distanceMatrix, isBandBased, isCoefficient, mwTolerance, SIMILARITY_METHODS } from '../core/similarity';
import type { Comparison, Experiment, Lab, Lane, Linkage, Marker, NormalisedLane, OutputMode, SimilarityMethod } from '../core/types';
import { uid } from '../core/uid';
import { deleteComparison, listComparisons, saveComparison } from '../storage/db';
import { cutTree, similarityToHeight } from '../core/treecut';
import { CLUSTER_COLOURS } from './clusterColours';
import { Dendrogram, type DendroCut, type DendroLeaf } from './Dendrogram';
import { svgToPngBlob, svgToString } from './svgexport';
import { ComparisonReport } from './ComparisonReport';
import { dataURLToGray, downloadBlob, downloadText, grayToPng, safeFilename } from './imageio';

type Field = Comparison['fields'][number];

const FIELDS: [Field, string][] = [
  ['lane', 'Lane'],
  ['experiment', 'Experiment'],
  ['lab', 'Laboratory'],
  ['strain', 'Strain number'],
  ['genus', 'Genus'],
  ['species', 'Species'],
  ['country', 'Country'],
  ['mlst', 'MLST'],
  ['comments', 'Comments'],
];

const OUTPUTS: [OutputMode, string][] = [
  ['dendrogram+images+bands', 'Dendrogram + images + bands'],
  ['dendrogram+images', 'Dendrogram + images'],
  ['dendrogram+bands', 'Dendrogram + bands'],
  ['dendrogram', 'Dendrogram'],
];

interface Entry {
  key: string;
  exp: Experiment;
  lab?: Lab;
  lane: Lane;
  norm: NormalisedLane;
}

interface Result {
  title: string;
  entries: Entry[];
  dist: number[][];
  tree: ClusterNode;
  coefficient: boolean;
  cophenetic: number;
  tolerance: number;
  leaves: DendroLeaf[];
  headers: string[];
  names: string[];
  /** Tolerance as entered (% of the molecular-weight range). */
  tolerancePercent: number;
  output: OutputMode;
  similarity: SimilarityMethod;
  linkage: Linkage;
  matching: BandMatching;
}

const BOOTSTRAP_BATCH = 5;

function fieldValue(e: Entry, f: Field): string {
  if (f === 'lane') return e.lane.name;
  if (f === 'experiment') return e.exp.name;
  if (f === 'lab') return e.lab?.name ?? '';
  return e.lane.meta[f];
}

const rotatedCache = new Map<string, string>();
const ROTATED_CACHE_SIZE = 2000;

async function rotatedLaneImage(n: NormalisedLane): Promise<string> {
  const hit = rotatedCache.get(n.image);
  if (hit) return hit;
  const url = await grayToPng(rotate90(await dataURLToGray(n.image), true));
  rotatedCache.set(n.image, url);
  // Bounded, oldest first: entries of deleted or re-analysed experiments are never requested again.
  if (rotatedCache.size > ROTATED_CACHE_SIZE) rotatedCache.delete(rotatedCache.keys().next().value!);
  return url;
}

export function ComparePage({ experiments, labs, markers = [] }: { experiments: Experiment[]; labs: Lab[]; markers?: Marker[] }) {
  const complete = useMemo(() => experiments.filter((e) => e.status === 'complete'), [experiments]);
  const entries = useMemo(() => {
    const out: Entry[] = [];
    for (const exp of complete) {
      for (const lane of exp.lanes) {
        const norm = exp.normalised.find((n) => n.laneId === lane.id);
        if (norm) out.push({ key: `${exp.id}/${lane.id}`, exp, lane, norm, lab: labs.find((l) => l.id === exp.labId) });
      }
    }
    return out;
  }, [complete, labs]);

  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [filter, setFilter] = useState('');
  const [showRefs, setShowRefs] = useState(false);
  const [similarity, setSimilarity] = useState<SimilarityMethod>('Dice');
  const [linkage, setLinkage] = useState<Linkage>('UPGMA');
  const [tolerance, setTolerance] = useState(1);
  const [output, setOutput] = useState<OutputMode>('dendrogram+images+bands');
  const [fields, setFields] = useState<Field[]>(['lane', 'experiment', 'strain']);
  const [showValues, setShowValues] = useState(false);
  const [result, setResult] = useState<Result | null>(null);
  const [tab, setTab] = useState<'dendrogram' | 'matrix' | 'bands'>('dendrogram');
  const [cutOn, setCutOn] = useState(false);
  const [cutoff, setCutoff] = useState(80);
  const [replicates, setReplicates] = useState(100);
  const [support, setSupport] = useState<Map<ClusterNode, number> | null>(null);
  const [bootProgress, setBootProgress] = useState<number | null>(null);
  const [bootReplicates, setBootReplicates] = useState<number | null>(null);
  const [report, setReport] = useState(false);
  const bootToken = useRef(0);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState<Comparison[]>([]);
  const svgRef = useRef<SVGSVGElement>(null);

  useEffect(() => {
    listComparisons().then(setSaved);
  }, []);

  const visible = entries.filter((e) => {
    if (!showRefs && e.lane.isReference) return false;
    if (!filter.trim()) return true;
    const hay = [e.exp.name, e.lane.name, e.lab?.name, e.lab?.city, e.lab?.country, ...Object.values(e.lane.meta)].join(' ').toLowerCase();
    return filter
      .toLowerCase()
      .split(/\s+/)
      .every((t) => hay.includes(t));
  });

  const byExp = new Map<string, Entry[]>();
  for (const e of visible) byExp.set(e.exp.id, [...(byExp.get(e.exp.id) ?? []), e]);

  const toggle = (keys: string[], on: boolean) =>
    setSelected((s) => {
      const n = new Set(s);
      keys.forEach((k) => (on ? n.add(k) : n.delete(k)));
      return n;
    });

  const markersUsed = new Set(entries.filter((e) => selected.has(e.key)).map((e) => e.exp.markerId));

  const run = async (opts?: Partial<Comparison>) => {
    setError(null);
    const sim = opts?.similarity ?? similarity;
    const link = opts?.linkage ?? linkage;
    const tol = opts?.tolerance ?? tolerance;
    const out = opts?.output ?? output;
    const flds = opts?.fields ?? fields;
    const cut = opts ? opts.cutoff : cutOn ? cutoff : null;
    const keys = opts?.laneRefs ? new Set(opts.laneRefs) : selected;
    const chosen = entries.filter((e) => keys.has(e.key));
    if (chosen.length < 2) {
      setError('Select at least two lanes.');
      return;
    }
    setRunning(true);
    try {
      const lanes = chosen.map((e) => ({ mw: e.norm.mw, profile: e.norm.profile }));
      const tolAbs = mwTolerance(lanes, tol);
      const dist = distanceMatrix(sim, lanes, tolAbs);
      const tree = cluster(dist, link);
      const withImages = out === 'dendrogram+images+bands' || out === 'dendrogram+images';
      const images = withImages ? await Promise.all(chosen.map((e) => rotatedLaneImage(e.norm))) : [];
      const leaves: DendroLeaf[] = chosen.map((e, i) => ({
        labels: flds.map((f) => fieldValue(e, f)),
        image: images[i],
        positions: e.norm.positions.map((p) => p / e.norm.height),
      }));
      const headers = flds.map((f) => FIELDS.find(([k]) => k === f)?.[1] ?? f);
      const tolText = isBandBased(sim) ? ` (tol. ${tol}%)` : '';
      bootToken.current++;
      setSupport(null);
      setBootProgress(null);
      if (cut !== undefined && cut !== null) {
        setCutOn(true);
        setCutoff(cut);
      } else if (opts) {
        setCutOn(false);
      }
      if (!opts && result && isCoefficient(sim) !== result.coefficient) {
        // The cut-off is a % similarity for coefficients and a distance otherwise.
        setCutoff(isCoefficient(sim) ? 80 : Number((tree.height / 2).toPrecision(2)));
      }
      setResult({
        title: `${sim}${tolText} — ${link}`,
        entries: chosen,
        dist,
        tree,
        coefficient: isCoefficient(sim),
        cophenetic: copheneticCorrelation(dist, tree),
        tolerance: tolAbs,
        leaves,
        headers,
        names: chosen.map((e) => `${e.exp.name} ${e.lane.name}`),
        tolerancePercent: tol,
        output: out,
        similarity: sim,
        linkage: link,
        matching: matchBands(lanes.map((l) => l.mw), tolAbs),
      });
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setRunning(false);
    }
  };

  const load = (c: Comparison) => {
    setSimilarity(c.similarity);
    setLinkage(c.linkage);
    setTolerance(c.tolerance);
    setOutput(c.output);
    setFields(c.fields);
    setSelected(new Set(c.laneRefs));
    run(c);
  };

  const save = async () => {
    const name = window.prompt('Name of the comparison', result?.title ?? 'Comparison');
    if (!name) return;
    const c: Comparison = {
      id: uid(),
      name,
      createdAt: Date.now(),
      laneRefs: result ? result.entries.map((e) => e.key) : [...selected],
      similarity,
      linkage,
      tolerance,
      output,
      fields,
      comments: '',
      cutoff: cutOn ? cutoff : null,
    };
    await saveComparison(c);
    setSaved(await listComparisons());
  };

  const base = safeFilename(result?.title ?? 'comparison');

  const cut: DendroCut | null = useMemo(() => {
    if (!result || !cutOn || !Number.isFinite(cutoff)) return null;
    const height = result.coefficient ? similarityToHeight(cutoff) : cutoff;
    const c = cutTree(result.tree, height);
    return { height, groups: c.groups, sizes: c.sizes, label: result.coefficient ? `${cutoff}%` : `${cutoff}` };
  }, [result, cutOn, cutoff]);

  const runBootstrap = () => {
    if (!result) return;
    const token = ++bootToken.current;
    const bs = new Bootstrap(
      result.tree,
      result.similarity,
      result.linkage,
      bootstrapData(result.similarity, result.matching.matrix, result.entries.map((e) => e.norm.profile)),
    );
    const total = Math.max(10, Math.min(1000, replicates));
    setSupport(null);
    setBootProgress(0);
    const step = () => {
      if (token !== bootToken.current) return;
      bs.run(Math.min(BOOTSTRAP_BATCH, total - bs.replicates));
      if (bs.replicates >= total) {
        setSupport(bs.support());
        setBootReplicates(bs.replicates);
        setBootProgress(null);
      } else {
        setBootProgress(bs.replicates / total);
        setTimeout(step, 0);
      }
    };
    setTimeout(step, 0);
  };

  const exportClustersCsv = () => {
    if (!result || !cut) return;
    const q = (s: string) => `"${s.replace(/"/g, '""')}"`;
    const rows = [
      [q('Lane'), q('Cluster'), q('Cluster size'), ...result.headers.map(q)].join(','),
      ...leafOrder(result.tree).map((i) =>
        [q(result.names[i]), cut.groups[i], cut.sizes[cut.groups[i] - 1], ...result.leaves[i].labels.map(q)].join(','),
      ),
    ];
    downloadText(`${base}-clusters.csv`, rows.join('\n'), 'text/csv');
  };

  const exportBandsCsv = () => {
    if (!result) return;
    const order = leafOrder(result.tree);
    const extra = result.headers.map((header, c) => ({ header, values: order.map((i) => result.leaves[i].labels[c]) }));
    if (cut) extra.unshift({ header: 'Cluster', values: order.map((i) => String(cut.groups[i])) });
    const matching = { classes: result.matching.classes, matrix: order.map((i) => result.matching.matrix[i]) };
    downloadText(`${base}-bands.csv`, bandMatrixCsv(matching, order.map((i) => result.names[i]), extra), 'text/csv');
  };

  const exportMatrixCsv = () => {
    if (!result) return;
    const order = leafOrder(result.tree);
    const q = (s: string) => `"${s.replace(/"/g, '""')}"`;
    const fmt = (v: number) => (result.coefficient ? (100 * (1 - v)).toFixed(2) : v.toPrecision(6));
    const rows = [
      ['', ...order.map((i) => q(result.names[i]))].join(','),
      ...order.map((i) => [q(result.names[i]), ...order.map((j) => fmt(result.dist[i][j]))].join(',')),
    ];
    downloadText(`${base}-matrix.csv`, rows.join('\n'), 'text/csv');
  };

  if (report && result) {
    return (
      <ComparisonReport
        result={result}
        tolerancePercent={result.tolerancePercent}
        output={result.output}
        cut={cut}
        support={support}
        replicates={bootReplicates}
        markers={markers}
        onClose={() => setReport(false)}
      />
    );
  }

  if (complete.length === 0) {
    return (
      <div className="page">
        <h2>Compare lanes</h2>
        <div className="card empty">Complete the analysis of at least one experiment to compare its lanes.</div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Compare lanes</h2>
        {saved.length > 0 && (
          <div className="row">
            <span className="small muted">Saved comparisons:</span>
            <select
              value=""
              onChange={(e) => {
                const c = saved.find((s) => s.id === e.target.value);
                if (c) load(c);
              }}
            >
              <option value="">Open…</option>
              {saved.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>
      <div className="compare">
        <div className="stack">
          <div className="card stack">
            <h3>1. Lanes</h3>
            <div className="row">
              <input type="search" placeholder="Filter by name, strain, species, laboratory…" value={filter} onChange={(e) => setFilter(e.target.value)} style={{ flex: 1 }} />
            </div>
            <div className="row small">
              <button onClick={() => toggle(visible.map((e) => e.key), true)}>Select visible</button>
              <button onClick={() => setSelected(new Set())}>Clear</button>
              <label className="check">
                <input type="checkbox" checked={showRefs} onChange={(e) => setShowRefs(e.target.checked)} />
                Show marker lanes
              </label>
              <span className="spacer" />
              <span className="muted">{selected.size} selected</span>
            </div>
            <div className="lane-picker">
              <table className="grid small">
                <tbody>
                  {[...byExp.entries()].map(([id, list]) => {
                    const allOn = list.every((e) => selected.has(e.key));
                    return (
                      <Fragment key={id}>
                        <tr className="exp-row">
                          <td style={{ width: 28 }}>
                            <input
                              type="checkbox"
                              checked={allOn}
                              aria-label={`Select every lane of ${list[0].exp.name}`}
                              onChange={(e) => toggle(list.map((x) => x.key), e.target.checked)}
                            />
                          </td>
                          <td colSpan={3}>
                            {list[0].exp.name}
                            {list[0].lab && <span className="muted" style={{ fontWeight: 400 }}> · {list[0].lab.name}</span>}
                          </td>
                        </tr>
                        {list.map((e) => (
                          <tr key={e.key}>
                            <td>
                              <input
                                type="checkbox"
                                checked={selected.has(e.key)}
                                aria-label={`Select ${e.lane.name}`}
                                onChange={(ev) => toggle([e.key], ev.target.checked)}
                              />
                            </td>
                            <td>
                              {e.lane.name} {e.lane.isReference && <span className="badge ref">ref</span>}
                            </td>
                            <td className="muted">{[e.lane.meta.strain, e.lane.meta.genus, e.lane.meta.species].filter(Boolean).join(' · ')}</td>
                            <td className="muted" style={{ textAlign: 'right' }}>
                              {e.norm.mw.length}b
                            </td>
                          </tr>
                        ))}
                      </Fragment>
                    );
                  })}
                </tbody>
              </table>
              {visible.length === 0 && <div className="empty small">No lanes match the filter.</div>}
            </div>
          </div>
          <div className="card stack">
            <h3>2. Settings</h3>
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
            <label className="field">
              <span>Linkage</span>
              <select value={linkage} onChange={(e) => setLinkage(e.target.value as Linkage)}>
                {LINKAGES.map((m) => (
                  <option key={m}>{m}</option>
                ))}
              </select>
            </label>
            {isBandBased(similarity) && (
              <label className="field">
                <span>Tolerance (% of the molecular-weight range)</span>
                <input type="number" min={0} step={0.1} value={tolerance} onChange={(e) => setTolerance(Math.max(0, Number(e.target.value)))} />
              </label>
            )}
            <label className="field">
              <span>Output</span>
              <select value={output} onChange={(e) => setOutput(e.target.value as OutputMode)}>
                {OUTPUTS.map(([k, l]) => (
                  <option key={k} value={k}>
                    {l}
                  </option>
                ))}
              </select>
            </label>
            <div className="field">
              <span className="small muted">Labels</span>
              <div className="row small">
                {FIELDS.map(([k, l]) => (
                  <label className="check" key={k}>
                    <input
                      type="checkbox"
                      checked={fields.includes(k)}
                      onChange={(e) => setFields((f) => (e.target.checked ? FIELDS.map(([x]) => x).filter((x) => f.includes(x) || x === k) : f.filter((x) => x !== k)))}
                    />
                    {l}
                  </label>
                ))}
              </div>
            </div>
            {markersUsed.size > 1 && (
              <div className="alert warn small">
                The selected lanes were normalised with different markers. Band-based measures compare molecular weights; curve-based measures
                are less reliable across markers.
              </div>
            )}
            {error && <div className="alert error small">{error}</div>}
            <div className="row">
              <button className="primary" onClick={() => run()} disabled={running || selected.size < 2}>
                {running ? 'Computing…' : 'Compare'}
              </button>
              <button onClick={save} disabled={selected.size < 2}>
                Save comparison
              </button>
            </div>
          </div>
          {saved.length > 0 && (
            <div className="card stack">
              <h3>Saved comparisons</h3>
              {saved.map((c) => (
                <div className="row small" key={c.id}>
                  <button className="link" onClick={() => load(c)}>
                    {c.name}
                  </button>
                  <span className="muted">
                    {c.laneRefs.length} lanes · {c.similarity} · {c.linkage}
                  </span>
                  <span className="spacer" />
                  <button
                    className="danger"
                    onClick={async () => {
                      if (!window.confirm(`Delete the comparison "${c.name}"?`)) return;
                      await deleteComparison(c.id);
                      setSaved(await listComparisons());
                    }}
                  >
                    Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="card" style={{ minWidth: 0 }}>
          {!result ? (
            <div className="empty">Select lanes and press Compare to build the dendrogram.</div>
          ) : (
            <>
              <div className="row" style={{ marginBottom: 8 }}>
                <span className="small muted">
                  {result.entries.length} lanes · cophenetic correlation {result.cophenetic.toFixed(3)}
                  {isBandBased(similarity) && ` · band tolerance ±${result.tolerance.toPrecision(3)}`}
                </span>
                <span className="spacer" />
                <button onClick={() => setReport(true)} title="Printable report of the comparison (print or save as PDF)">
                  Report
                </button>
                <label className="check small">
                  <input type="checkbox" checked={showValues} onChange={(e) => setShowValues(e.target.checked)} />
                  Node values
                </label>
              </div>
              <div className="row small" style={{ marginBottom: 8 }}>
                <label className="check">
                  <input type="checkbox" checked={cutOn} onChange={(e) => setCutOn(e.target.checked)} />
                  {result.coefficient ? 'Cut-off at' : 'Cut-off at distance'}
                </label>
                <input
                  type="number"
                  aria-label="Cut-off"
                  min={0}
                  max={result.coefficient ? 100 : undefined}
                  step={result.coefficient ? 1 : 0.01}
                  value={cutoff}
                  disabled={!cutOn}
                  style={{ width: 70 }}
                  onChange={(e) => setCutoff(Math.max(0, Number(e.target.value)))}
                />
                {result.coefficient && <span>%</span>}
                {cut && (
                  <span className="muted">
                    {cut.sizes.filter((k) => k >= 2).length} clusters · {cut.sizes.filter((k) => k < 2).length} unclustered
                  </span>
                )}
                <span className="spacer" />
                <span>Bootstrap</span>
                <input
                  type="number"
                  aria-label="Bootstrap replicates"
                  min={10}
                  max={1000}
                  step={10}
                  value={replicates}
                  style={{ width: 70 }}
                  onChange={(e) => setReplicates(Number(e.target.value))}
                />
                <span>replicates</span>
                {bootProgress !== null ? (
                  <>
                    <span className="muted">{Math.round(bootProgress * 100)}%</span>
                    <button
                      onClick={() => {
                        bootToken.current++;
                        setBootProgress(null);
                      }}
                    >
                      Cancel
                    </button>
                  </>
                ) : (
                  <button onClick={runBootstrap} title="Resample band classes (band-based measures) or profile points (curve-based measures)">
                    {support ? 'Re-run' : 'Run'}
                  </button>
                )}
                {support && (
                  <button className="link" onClick={() => setSupport(null)}>
                    Hide
                  </button>
                )}
              </div>
              <div className="result-tabs">
                <button className={tab === 'dendrogram' ? 'active' : ''} onClick={() => setTab('dendrogram')}>
                  Dendrogram
                </button>
                <button className={tab === 'matrix' ? 'active' : ''} onClick={() => setTab('matrix')}>
                  {result.coefficient ? 'Similarity matrix' : 'Distance matrix'}
                </button>
                <button className={tab === 'bands' ? 'active' : ''} onClick={() => setTab('bands')}>
                  Band matching
                </button>
                <span className="spacer" />
                {tab === 'dendrogram' ? (
                  <>
                    <button onClick={() => svgRef.current && downloadText(`${base}.svg`, svgToString(svgRef.current), 'image/svg+xml')}>SVG</button>
                    <button onClick={async () => svgRef.current && downloadBlob(`${base}.png`, await svgToPngBlob(svgRef.current))}>PNG</button>
                    <button onClick={() => downloadText(`${base}.nwk`, toNewick(result.tree, result.names, support ?? undefined), 'text/plain')}>
                      Newick
                    </button>
                    {cut && <button onClick={exportClustersCsv}>Clusters CSV</button>}
                  </>
                ) : tab === 'matrix' ? (
                  <button onClick={exportMatrixCsv}>CSV</button>
                ) : (
                  <>
                    <button onClick={exportBandsCsv}>CSV</button>
                    <button onClick={() => downloadText(`${base}-bands.nex`, bandMatrixNexus(result.matching, result.names), 'text/plain')} title="Binary matrix for PAUP*, SplitsTree, MrBayes…">
                      NEXUS
                    </button>
                  </>
                )}
              </div>
              {tab === 'dendrogram' ? (
                <div className="dendro-wrap">
                  <Dendrogram
                    ref={svgRef}
                    tree={result.tree}
                    leaves={result.leaves}
                    headers={result.headers}
                    coefficient={result.coefficient}
                    output={output}
                    title={result.title}
                    showValues={showValues}
                    cut={cut}
                    support={support}
                  />
                </div>
              ) : tab === 'matrix' ? (
                <Matrix result={result} />
              ) : (
                <BandTable result={result} cut={cut} />
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function Matrix({ result }: { result: Result }) {
  const order = leafOrder(result.tree);
  // A loop, not Math.max(...flat): spreading n² values overflows the call stack for a few hundred lanes.
  let max = 1e-9;
  for (const row of result.dist) for (const v of row) if (v > max) max = v;
  const cellStyle = (d: number) => {
    const t = result.coefficient ? 1 - d : 1 - d / max; // 1 = similar
    const lightness = Math.round(97 - t * 50);
    return { background: `hsl(210 80% ${lightness}%)`, color: lightness < 62 ? '#fff' : '#111' };
  };
  return (
    <div className="table-wrap" style={{ maxHeight: '70vh' }}>
      <table className="matrix" style={{ borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th />
            {order.map((i) => (
              <th key={i} className="small" style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)', padding: 4, fontWeight: 500 }}>
                {result.names[i]}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {order.map((i) => (
            <tr key={i}>
              <th className="small" style={{ textAlign: 'right', paddingRight: 6, fontWeight: 500, whiteSpace: 'nowrap' }}>
                {result.names[i]}
              </th>
              {order.map((j) => (
                <td key={j} className="cell" style={cellStyle(result.dist[i][j])}>
                  {result.coefficient ? (100 * (1 - result.dist[i][j])).toFixed(1) : result.dist[i][j].toPrecision(3)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/** Band-matching table: one row per lane (dendrogram order), one column per band class. */
function BandTable({ result, cut }: { result: Result; cut: DendroCut | null }) {
  const order = leafOrder(result.tree);
  const { classes, matrix } = result.matching;
  const counts = classes.map((_, k) => matrix.reduce((s, row) => s + row[k], 0));
  const polymorphic = counts.filter((c) => c > 0 && c < matrix.length).length;
  return (
    <div className="stack">
      <span className="small muted">
        {classes.length} band classes ({polymorphic} polymorphic) · tolerance ±{result.tolerance.toPrecision(3)} · columns headed by the mean
        molecular weight of each class
      </span>
      <div className="table-wrap" style={{ maxHeight: '70vh' }}>
        <table className="matrix" style={{ borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th />
              {cut && <th className="small">Cl.</th>}
              {classes.map((v, k) => (
                <th key={k} className="small" style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)', padding: 4, fontWeight: 500 }}>
                  {Number(v.toPrecision(4))}
                </th>
              ))}
              <th className="small">Bands</th>
            </tr>
          </thead>
          <tbody>
            {order.map((i) => {
              const g = cut?.groups[i] ?? 0;
              const colour = cut && cut.sizes[g - 1] >= 2 ? CLUSTER_COLOURS[(g - 1) % CLUSTER_COLOURS.length] : undefined;
              return (
                <tr key={i}>
                  <th className="small" style={{ textAlign: 'right', paddingRight: 6, fontWeight: 500, whiteSpace: 'nowrap' }}>
                    {result.names[i]}
                  </th>
                  {cut && (
                    <td className="small" style={{ color: colour, fontWeight: 600, textAlign: 'center' }}>
                      {g}
                    </td>
                  )}
                  {matrix[i].map((v, k) => (
                    <td key={k} className="cell" style={{ background: v ? (colour ?? '#1c2430') : '#f2f4f7', color: '#fff', minWidth: 14 }} title={`${result.names[i]} · ${Number(classes[k].toPrecision(4))}: ${v ? 'present' : 'absent'}`}>
                      {v ? '1' : ''}
                    </td>
                  ))}
                  <td className="small muted" style={{ textAlign: 'right' }}>
                    {matrix[i].reduce((a, b) => a + b, 0)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
