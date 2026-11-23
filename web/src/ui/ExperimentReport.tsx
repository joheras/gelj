import { useMemo, useState, type ReactNode } from 'react';
import { migrationModel } from '../core/normalise';
import { describeStep, fitParameters, formatDateTime, formatNumber } from '../core/report';
import { labLabel, type Experiment, type Lab, type Lane, type Marker, type NormalisedLane } from '../core/types';
import { BUILD } from './buildInfo';
import { CurveChart } from './Charts';
import { ChecksumTable, KeyValues, ReportSection, ReportShell } from './Report';
import { useChecksums, type ReportImage } from './useChecksums';

interface Props {
  experiment: Experiment;
  marker?: Marker;
  lab?: Lab;
  /** True when the experiment page has unsaved changes (they are included in the report). */
  unsaved?: boolean;
  onClose: () => void;
}

const META: [keyof Lane['meta'], string][] = [
  ['strain', 'Strain'],
  ['genus', 'Genus'],
  ['species', 'Species'],
  ['country', 'Country'],
  ['mlst', 'MLST'],
  ['comments', 'Comments'],
];

const mwText = (v: number) => (Number.isFinite(v) ? String(Number(v.toPrecision(4))) : '–');

/** Printable report of an analysed gel: images, preprocessing, lanes, normalisation and bands. */
export function ExperimentReport({ experiment: e, marker, lab, unsaved, onClose }: Props) {
  const [generated] = useState(() => new Date());
  const lanes = e.lanes;
  const refs = lanes.filter((l) => l.isReference);
  const normOf = (l: Lane) => e.normalised.find((n) => n.laneId === l.id);
  const fit = useMemo(() => {
    try {
      return marker ? migrationModel(marker) : null;
    } catch {
      return null;
    }
  }, [marker]);

  const images: ReportImage[] = useMemo(() => {
    // The size of the original image is not stored: it is read when the image loads (NaN until then).
    const same = e.originalImage === e.image;
    const out: ReportImage[] = [
      { label: 'Original image', url: e.originalImage, width: same ? e.width : NaN, height: same ? e.height : NaN },
      { label: 'Processed image', url: e.image, width: e.width, height: e.height },
    ];
    for (const l of e.lanes) {
      const n = e.normalised.find((x) => x.laneId === l.id);
      if (n) out.push({ label: `Normalised lane ${l.name}`, url: n.image, width: n.width, height: n.height });
    }
    return out;
  }, [e]);
  const sums = useChecksums(images);
  const [originalSize, setOriginalSize] = useState<{ width: number; height: number } | null>(null);
  const shownImages = images.map((img, i) => (i === 0 && originalSize ? { ...img, ...originalSize } : img));

  const totalBands = lanes.filter((l) => !l.isReference).reduce((s, l) => s + (normOf(l)?.mw.length ?? 0), 0);
  const steps = e.preprocessing;
  const sameImage = e.originalImage === e.image;

  const methods = [
    `The gel image was analysed with ${BUILD.name} ${BUILD.version}${BUILD.commit ? ` (commit ${BUILD.commit})` : ''}.`,
    steps && steps.length > 0 ? `It was preprocessed as follows: ${steps.map(describeStep).map(lower).join('; ')}.` : '',
    `${lanes.length} lanes were defined (${refs.length} reference lane${refs.length === 1 ? '' : 's'}) on a ${e.darkBackground ? 'dark' : 'light'} background.`,
    marker
      ? `Lanes were normalised with the ${marker.name} marker (${e.referenceMw.length} bands used) by interpolating the marker band positions across the gel and warping every lane with a cubic spline; molecular weights were computed with the ${fit?.model.name ?? 'fitted'} migration model${fit ? ` (${fit.model.formula}; R² = ${fit.rSquared.toFixed(4)})` : ''}.`
      : '',
    `${totalBands} bands were detected in the ${lanes.length - refs.length} sample lanes.`,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <ReportShell
      kind="Gel analysis report"
      title={e.name}
      generated={generated}
      ready={sums !== null}
      note={unsaved ? 'The report includes changes that are not saved yet.' : undefined}
      onClose={onClose}
    >
      <ReportSection title="1. Experiment">
        <KeyValues
          rows={[
            ['Experiment', e.name],
            ['Laboratory', lab ? labLabel(lab) : 'Not recorded'],
            ['Created', formatDateTime(new Date(e.createdAt))],
            ['Last modified', formatDateTime(new Date(e.updatedAt))],
            ['Status', e.status === 'complete' ? 'Analysis complete' : 'Draft'],
            ['Background', e.darkBackground ? 'Dark (bright bands)' : 'Light (dark bands)'],
            ['Lanes', `${lanes.length} (${refs.length} reference, ${lanes.length - refs.length} samples)`],
            ['Comments', e.comments || '—'],
          ]}
        />
      </ReportSection>

      <ReportSection title="2. Gel image">
        <div className="report-figures">
          <figure>
            <img
              src={e.originalImage}
              alt="Original gel image"
              onLoad={(ev) => setOriginalSize({ width: ev.currentTarget.naturalWidth, height: ev.currentTarget.naturalHeight })}
            />
            <figcaption>
              Original image{originalSize ? ` (${originalSize.width}×${originalSize.height} px)` : ''}
            </figcaption>
          </figure>
          <figure>
            <img src={e.image} alt="Processed gel image" />
            <figcaption>
              Processed image ({e.width}×{e.height} px)
            </figcaption>
          </figure>
        </div>
      </ReportSection>

      <ReportSection title="3. Preprocessing">
        {steps === undefined ? (
          <p className="report-note">
            Not recorded: this experiment was analysed before GelJ Web recorded the preprocessing operations
            {sameImage ? ' (the processed image is identical to the original)' : ''}.
          </p>
        ) : steps.length === 0 ? (
          <p>None: the original image was analysed as loaded.</p>
        ) : (
          <ol className="report-steps">
            {steps.map((s, i) => (
              <li key={i}>{describeStep(s)}</li>
            ))}
          </ol>
        )}
      </ReportSection>

      <ReportSection title="4. Lanes">
        <table className="report-table">
          <thead>
            <tr>
              <th>Lane</th>
              <th>Type</th>
              {META.map(([k, label]) => (
                <th key={k}>{label}</th>
              ))}
              <th>Bands</th>
              <th title="Detection threshold (0–255)">Thr.</th>
              <th>Position x, y, w×h (px)</th>
            </tr>
          </thead>
          <tbody>
            {lanes.map((l) => (
              <tr key={l.id}>
                <td>{l.name}</td>
                <td>{l.isReference ? 'Reference' : 'Sample'}</td>
                {META.map(([k]) => (
                  <td key={k}>{l.meta[k]}</td>
                ))}
                <td className="num">{normOf(l)?.mw.length ?? l.bands.length}</td>
                <td className="num">{l.threshold}</td>
                <td className="num nowrap">
                  {Math.round(l.rect.x)}, {Math.round(l.rect.y)}, {Math.round(l.rect.w)}×{Math.round(l.rect.h)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </ReportSection>

      <ReportSection title="5. Marker and migration model">
        <KeyValues
          rows={[
            ['Reference lanes', refs.map((l) => l.name).join(', ') || '—'],
            ['Marker', marker ? `${marker.name}${marker.builtin ? ' (built-in)' : ''}` : 'Not found (the marker was deleted)'],
            ['Reference bands (MW)', e.referenceMw.map(mwText).join(', ') || '—'],
            ...(fit
              ? ([
                  ['Migration model', fit.model.name],
                  ['Formula', <span className="mono">{fit.model.formula}</span>],
                  ['Parameters', <span className="mono">{fitParameters(fit)}</span>],
                  ['Goodness of fit', `R² = ${fit.rSquared.toFixed(5)}, SSE = ${formatNumber(fit.sse)}`],
                ] as [string, ReactNode][])
              : []),
          ]}
        />
        {marker && fit && (
          <div className="report-two-col">
            <figure className="report-chart">
              <CurveChart x={marker.refPositions} y={marker.mw} fit={fit} />
              <figcaption>Calibration: marker band position (x, px in the {marker.refHeight}-px reference lane) vs molecular weight (y).</figcaption>
            </figure>
            <table className="report-table compact">
              <thead>
                <tr>
                  <th>Band</th>
                  <th>Position</th>
                  <th>MW</th>
                  <th>Model</th>
                  <th>Error</th>
                </tr>
              </thead>
              <tbody>
                {marker.mw.map((mw, i) => {
                  const m = fit.evaluate(marker.refPositions[i]);
                  return (
                    <tr key={i}>
                      <td className="num">{i + 1}</td>
                      <td className="num">{marker.refPositions[i]}</td>
                      <td className="num">{mwText(mw)}</td>
                      <td className="num">{mwText(m)}</td>
                      <td className="num">{Number.isFinite(m) ? `${(((m - mw) / mw) * 100).toFixed(1)}%` : '–'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </ReportSection>

      <ReportSection title="6. Normalised gel" pageBreak>
        <NormalisedGel lanes={lanes} normalised={e.normalised} marker={marker} />
        <p className="report-note">
          Lanes warped onto the marker reference lane. Ticks mark the detected bands (thick ticks: reference lanes); the scale on the left gives
          the marker molecular weights.
        </p>
      </ReportSection>

      <ReportSection title="7. Bands">
        <div className="report-bands">
          {lanes.map((l) => {
            const n = normOf(l);
            return (
              <table key={l.id} className="report-table compact">
                <thead>
                  <tr>
                    <th colSpan={3}>
                      {l.name}
                      {l.isReference ? ' (reference)' : ''}
                    </th>
                  </tr>
                  <tr>
                    <th>Band</th>
                    <th>Position (px)</th>
                    <th>MW</th>
                  </tr>
                </thead>
                <tbody>
                  {!n || n.mw.length === 0 ? (
                    <tr>
                      <td colSpan={3}>No bands</td>
                    </tr>
                  ) : (
                    n.mw.map((mw, i) => (
                      <tr key={i}>
                        <td className="num">{i + 1}</td>
                        <td className="num">{n.positions[i].toFixed(1)}</td>
                        <td className="num">{mwText(mw)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            );
          })}
        </div>
        <p className="report-note">Positions are measured from the top of the normalised lane ({e.normalised[0]?.height ?? '–'} px long).</p>
      </ReportSection>

      <ReportSection title="8. Image checksums">
        <ChecksumTable images={shownImages} sums={sums} />
      </ReportSection>

      <ReportSection title="9. Suggested methods text">
        <p className="report-methods">{methods}</p>
      </ReportSection>
    </ReportShell>
  );
}

function lower(s: string): string {
  return s.charAt(0).toLowerCase() + s.slice(1);
}

/** Normalised lanes side by side with their bands and a molecular-weight scale (vector, so it scales to the page). */
function NormalisedGel({ lanes, normalised, marker }: { lanes: Lane[]; normalised: NormalisedLane[]; marker?: Marker }) {
  const items = lanes.map((l) => ({ lane: l, n: normalised.find((x) => x.laneId === l.id) })).filter((x): x is { lane: Lane; n: NormalisedLane } => !!x.n);
  if (items.length === 0) return <p className="report-note">No normalised lanes.</p>;
  const H = 420;
  const gap = 10;
  const axis = marker ? 58 : 6;
  const labelH = 70;
  const widths = items.map(({ n }) => Math.max(18, Math.min(60, (n.width / n.height) * H)));
  let x = axis;
  const xs = widths.map((w) => {
    const v = x;
    x += w + gap;
    return v;
  });
  const W = x + 40; // room for the last rotated label
  const refH = items[0].n.height;
  return (
    <svg className="report-gel" viewBox={`0 0 ${W} ${H + labelH}`} width={W} height={H + labelH} role="img" aria-label="Normalised gel with bands">
      {marker && (
        <g fontSize={9} fill="#222">
          <line x1={axis - 8} x2={axis - 8} y1={0} y2={H} stroke="#222" strokeWidth={0.8} />
          {marker.refPositions.map((p, i) => {
            const y = (p / marker.refHeight) * H;
            return (
              <g key={i}>
                <line x1={axis - 12} x2={axis - 8} y1={y} y2={y} stroke="#222" strokeWidth={0.8} />
                <text x={axis - 14} y={y + 3} textAnchor="end">
                  {mwText(marker.mw[i])}
                </text>
              </g>
            );
          })}
        </g>
      )}
      {items.map(({ lane, n }, i) => (
        <g key={lane.id}>
          <image href={n.image} x={xs[i]} y={0} width={widths[i]} height={H} preserveAspectRatio="none" />
          <rect x={xs[i]} y={0} width={widths[i]} height={H} fill="none" stroke="#666" strokeWidth={0.5} />
          {n.positions.map((p, k) => {
            const y = (p / (n.height || refH)) * H;
            return <line key={k} x1={xs[i] + widths[i]} x2={xs[i] + widths[i] + (lane.isReference ? 6 : 4)} y1={y} y2={y} stroke="#000" strokeWidth={lane.isReference ? 2 : 1.2} />;
          })}
          <text x={xs[i] + widths[i] / 2} y={H + 10} fontSize={10} fill="#111" transform={`rotate(45 ${xs[i] + widths[i] / 2} ${H + 10})`}>
            {lane.name.length > 14 ? lane.name.slice(0, 13) + '…' : lane.name}
            {lane.isReference ? ' (R)' : ''}
          </text>
        </g>
      ))}
    </svg>
  );
}
