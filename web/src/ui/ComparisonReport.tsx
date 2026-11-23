import { useMemo, useState, type ReactNode } from 'react';
import type { BandMatching } from '../core/bandmatch';
import { leafOrder, type ClusterNode } from '../core/cluster';
import { chunk } from '../core/report';
import { isBandBased } from '../core/similarity';
import { labLabel, type Experiment, type Lab, type Lane, type Linkage, type Marker, type NormalisedLane, type OutputMode, type SimilarityMethod } from '../core/types';
import { BUILD } from './buildInfo';
import { Dendrogram, type DendroCut, type DendroLeaf } from './Dendrogram';
import { ChecksumTable, KeyValues, ReportSection, ReportShell } from './Report';
import { useChecksums, type ReportImage } from './useChecksums';

/** What the comparison page computed (structurally its `Result`). */
export interface ComparisonResultData {
  title: string;
  entries: { key: string; exp: Experiment; lab?: Lab; lane: Lane; norm: NormalisedLane }[];
  dist: number[][];
  tree: ClusterNode;
  coefficient: boolean;
  cophenetic: number;
  /** Absolute band-matching tolerance (molecular-weight units). */
  tolerance: number;
  leaves: DendroLeaf[];
  headers: string[];
  names: string[];
  similarity: SimilarityMethod;
  linkage: Linkage;
  matching: BandMatching;
}

interface Props {
  result: ComparisonResultData;
  /** Tolerance as entered (% of the molecular-weight range). */
  tolerancePercent: number;
  output: OutputMode;
  cut: DendroCut | null;
  support: Map<ClusterNode, number> | null;
  /** Bootstrap replicates behind `support`. */
  replicates: number | null;
  markers: Marker[];
  /** Further settings to list (e.g. how the tolerance was optimised). */
  extraSettings?: [string, ReactNode][];
  onClose: () => void;
}

const OUTPUT_LABELS: Record<OutputMode, string> = {
  'dendrogram+images+bands': 'Dendrogram, lane images and bands',
  'dendrogram+images': 'Dendrogram and lane images',
  'dendrogram+bands': 'Dendrogram and bands',
  dendrogram: 'Dendrogram',
};

/** Columns per printed block of the similarity matrix and of the band-matching matrix. */
const MATRIX_BLOCK = 22;
const BAND_BLOCK = 40;

/** Printable report of a comparison: settings, lanes, dendrogram, clusters and matrices. */
export function ComparisonReport({ result: r, tolerancePercent, output, cut, support, replicates, markers, extraSettings = [], onClose }: Props) {
  const [generated] = useState(() => new Date());
  const order = useMemo(() => leafOrder(r.tree), [r.tree]);
  const bandBased = isBandBased(r.similarity);
  const markerNames = [...new Set(r.entries.map((e) => markers.find((m) => m.id === e.exp.markerId)?.name ?? 'unknown marker'))];
  const experiments = [...new Map(r.entries.map((e) => [e.exp.id, e])).values()];

  const images: ReportImage[] = useMemo(
    () => r.entries.map((e) => ({ label: `${e.exp.name} · ${e.lane.name}`, url: e.norm.image, width: e.norm.width, height: e.norm.height })),
    [r.entries],
  );
  const sums = useChecksums(images);

  const clusters = useMemo(() => {
    if (!cut) return [];
    return cut.sizes
      .map((size, g) => ({ id: g + 1, size, members: order.filter((i) => cut.groups[i] === g + 1) }))
      .filter((c) => c.size >= 2);
  }, [cut, order]);
  const singletons = cut ? order.filter((i) => cut.sizes[cut.groups[i] - 1] < 2) : [];

  const cutText = cut ? (r.coefficient ? `${cut.label} similarity` : `distance ${cut.label}`) : 'None';
  const toleranceText = bandBased ? `${tolerancePercent}% of the molecular-weight range (±${Number(r.tolerance.toPrecision(4))})` : 'Not applicable (curve-based measure)';
  const methods = [
    `Lanes were compared with ${BUILD.name} ${BUILD.version}${BUILD.commit ? ` (commit ${BUILD.commit})` : ''}.`,
    bandBased
      ? `Similarity between the ${r.entries.length} lanes was computed with the ${r.similarity} coefficient on band molecular weights, with a position tolerance of ${tolerancePercent}% of the molecular-weight range.`
      : `${r.coefficient ? 'Similarity' : 'Distance'} between the ${r.entries.length} lanes was computed from the densitometric curves with the ${r.similarity} measure.`,
    `The dendrogram was built with the ${r.linkage} method (cophenetic correlation ${r.cophenetic.toFixed(3)}).`,
    cut ? `Clusters were defined at a cut-off of ${cutText} (${clusters.length} clusters of two or more lanes, ${singletons.length} unclustered lanes).` : '',
    support && replicates ? `Branch support was estimated with ${replicates} bootstrap replicates.` : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <ReportShell kind="Comparison report" title={r.title} generated={generated} ready={sums !== null} onClose={onClose}>
      <ReportSection title="1. Settings">
        <KeyValues
          rows={[
            ['Similarity measure', `${r.similarity} (${bandBased ? 'band-based' : 'densitometric curve'}${r.coefficient ? ', similarity coefficient' : ', distance'})`],
            ['Band-matching tolerance', toleranceText],
            ...extraSettings,
            ['Linkage', r.linkage],
            ['Cut-off', cutText],
            ['Bootstrap', support && replicates ? `${replicates} replicates` : 'Not run'],
            ['Cophenetic correlation', r.cophenetic.toFixed(4)],
            ['Lanes', `${r.entries.length} from ${experiments.length} experiment${experiments.length === 1 ? '' : 's'}`],
            ['Markers', markerNames.join(', ')],
            ['Output', OUTPUT_LABELS[output]],
          ]}
        />
      </ReportSection>

      <ReportSection title="2. Lanes included">
        <table className="report-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Lane</th>
              <th>Experiment</th>
              <th>Laboratory</th>
              <th>Strain</th>
              <th>Genus / species</th>
              <th>Country</th>
              <th>MLST</th>
              <th>Bands</th>
              {cut && <th>Cluster</th>}
            </tr>
          </thead>
          <tbody>
            {order.map((i, k) => {
              const e = r.entries[i];
              return (
                <tr key={e.key}>
                  <td className="num">{k + 1}</td>
                  <td>
                    {e.lane.name}
                    {e.lane.isReference ? ' (ref.)' : ''}
                  </td>
                  <td>{e.exp.name}</td>
                  <td>{e.lab ? labLabel(e.lab) : '—'}</td>
                  <td>{e.lane.meta.strain}</td>
                  <td>
                    <i>{[e.lane.meta.genus, e.lane.meta.species].filter(Boolean).join(' ')}</i>
                  </td>
                  <td>{e.lane.meta.country}</td>
                  <td>{e.lane.meta.mlst}</td>
                  <td className="num">{e.norm.mw.length}</td>
                  {cut && <td className="num">{cut.sizes[cut.groups[i] - 1] >= 2 ? cut.groups[i] : '–'}</td>}
                </tr>
              );
            })}
          </tbody>
        </table>
        <p className="report-note">In dendrogram order; # numbers the lanes in the matrices below.</p>
      </ReportSection>

      <ReportSection title="3. Dendrogram" pageBreak>
        <div className="report-dendrogram">
          <Dendrogram
            tree={r.tree}
            leaves={r.leaves}
            headers={r.headers}
            coefficient={r.coefficient}
            output={output}
            title={r.title}
            showValues={false}
            cut={cut}
            support={support}
          />
        </div>
        <p className="report-note">
          {r.coefficient ? 'Scale: % similarity.' : `Scale: ${r.similarity.toLowerCase()}.`}
          {cut ? ' The dashed line marks the cut-off; cluster numbers are given next to the lanes.' : ''}
          {support ? ' Numbers at the branches: bootstrap support (%).' : ''}
        </p>
      </ReportSection>

      <ReportSection title="4. Clusters">
        {!cut ? (
          <p>No cut-off was applied.</p>
        ) : (
          <>
            <p>
              Cut-off at {cutText}: {clusters.length} cluster{clusters.length === 1 ? '' : 's'} of two or more lanes and {singletons.length} unclustered
              lane{singletons.length === 1 ? '' : 's'}.
            </p>
            <table className="report-table">
              <thead>
                <tr>
                  <th>Cluster</th>
                  <th>Lanes</th>
                  <th>Members</th>
                </tr>
              </thead>
              <tbody>
                {clusters.map((c) => (
                  <tr key={c.id}>
                    <td className="num">{c.id}</td>
                    <td className="num">{c.size}</td>
                    <td>{c.members.map((i) => r.names[i]).join('; ')}</td>
                  </tr>
                ))}
                {singletons.length > 0 && (
                  <tr>
                    <td>Unclustered</td>
                    <td className="num">{singletons.length}</td>
                    <td>{singletons.map((i) => r.names[i]).join('; ')}</td>
                  </tr>
                )}
              </tbody>
            </table>
          </>
        )}
      </ReportSection>

      <ReportSection title={`5. ${r.coefficient ? 'Similarity' : 'Distance'} matrix`}>
        <p className="report-note">
          {r.coefficient ? '% similarity' : r.similarity} between every pair of lanes, numbered as in section 2
          {order.length > MATRIX_BLOCK ? `; printed in blocks of ${MATRIX_BLOCK} columns` : ''}. Darker cells are more similar.
        </p>
        <SimilarityMatrix result={r} order={order} />
      </ReportSection>

      <ReportSection title="6. Band-matching matrix">
        <p className="report-note">
          {r.matching.classes.length} band classes (tolerance ±{Number(r.tolerance.toPrecision(4))}), headed by their mean molecular weight; ■ =
          band present; Σ = bands of the lane. Lanes numbered as in section 2.
          {!bandBased ? ' The similarity measure is curve-based: this matrix is given for reference and was not used in the clustering.' : ''}
        </p>
        <BandMatrix result={r} order={order} />
      </ReportSection>

      <ReportSection title="7. Image checksums">
        <ChecksumTable images={images} sums={sums} />
        <p className="report-note">Normalised lane images compared (as stored in their experiments).</p>
      </ReportSection>

      <ReportSection title="8. Suggested methods text">
        <p className="report-methods">{methods}</p>
      </ReportSection>
    </ReportShell>
  );
}

function SimilarityMatrix({ result: r, order }: { result: ComparisonResultData; order: number[] }) {
  let max = 1e-9;
  for (const row of r.dist) for (const v of row) if (v > max) max = v;
  const pos = new Map(order.map((i, k) => [i, k + 1]));
  const value = (d: number) => (r.coefficient ? (100 * (1 - d)).toFixed(1) : d.toPrecision(3));
  const shade = (d: number) => {
    const t = r.coefficient ? 1 - d : 1 - d / max; // 1 = similar
    const l = Math.round(100 - t * 45);
    return { background: `hsl(0 0% ${l}%)`, color: l < 70 ? '#fff' : '#000' };
  };
  return (
    <>
      {chunk(order, MATRIX_BLOCK).map((cols, b) => (
        <table key={b} className="report-matrix">
          <thead>
            <tr>
              <th className="name">Lane</th>
              {cols.map((j) => (
                <th key={j}>{pos.get(j)}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {order.map((i) => (
              <tr key={i}>
                <th className="name">
                  {pos.get(i)}. {r.names[i]}
                </th>
                {cols.map((j) => (
                  <td key={j} style={shade(r.dist[i][j])}>
                    {value(r.dist[i][j])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      ))}
    </>
  );
}

function BandMatrix({ result: r, order }: { result: ComparisonResultData; order: number[] }) {
  const { classes, matrix } = r.matching;
  const pos = new Map(order.map((i, k) => [i, k + 1]));
  if (classes.length === 0) return <p>No bands.</p>;
  const ks = classes.map((_, k) => k);
  return (
    <>
      {chunk(ks, BAND_BLOCK).map((cols, b) => (
        <table key={b} className="report-matrix report-bandmatrix">
          <thead>
            <tr>
              <th className="name">Lane</th>
              {cols.map((k) => (
                <th key={k} className="vertical">
                  <span>{Number(classes[k].toPrecision(4))}</span>
                </th>
              ))}
              <th>Σ</th>
            </tr>
          </thead>
          <tbody>
            {order.map((i) => (
              <tr key={i}>
                <th className="name">
                  {pos.get(i)}. {r.names[i]}
                </th>
                {cols.map((k) => (
                  <td key={k}>{matrix[i][k] ? '■' : ''}</td>
                ))}
                <td className="num">{matrix[i].reduce((s, v) => s + v, 0)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ))}
    </>
  );
}
