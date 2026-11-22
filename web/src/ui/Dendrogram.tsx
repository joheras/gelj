import { forwardRef } from 'react';
import type { ClusterNode } from '../core/cluster';
import { leafOrder } from '../core/cluster';
import type { OutputMode } from '../core/types';
import { CLUSTER_COLOURS } from './clusterColours';

export interface DendroLeaf {
  /** Label columns. */
  labels: string[];
  /** Lane image rotated so that the top of the lane is on the left (data URL). */
  image?: string;
  /** Band positions as fractions of the lane length. */
  positions: number[];
}

/** Clusters defined by a cut-off line. */
export interface DendroCut {
  /** Merge height (distance) of the cut. */
  height: number;
  /** Cluster number of every leaf (by leaf index). */
  groups: number[];
  /** Size of every cluster (index = number - 1). */
  sizes: number[];
  /** Text of the cut-off level (e.g. "80%"). */
  label: string;
}

const INK = '#1c2430';

interface Props {
  tree: ClusterNode;
  leaves: DendroLeaf[];
  headers: string[];
  coefficient: boolean;
  output: OutputMode;
  title: string;
  showValues: boolean;
  cut?: DendroCut | null;
  /** Bootstrap support (0..100) of the internal nodes. */
  support?: Map<ClusterNode, number> | null;
}

const FONT = 'system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif';

function textWidth(s: string, size = 12) {
  return s.length * size * 0.58;
}

export const Dendrogram = forwardRef<SVGSVGElement, Props>(function Dendrogram(
  { tree, leaves, headers, coefficient, output, title, showValues, cut, support },
  ref,
) {
  const withImages = output === 'dendrogram+images+bands' || output === 'dendrogram+images';
  const withBands = output === 'dendrogram+images+bands' || output === 'dendrogram+bands';
  const rowH = withImages ? 30 : 22;
  const top = cut || support ? 98 : 84;
  const left = 16;
  const treeW = 300;
  const stripW = 220;
  const gap = 14;
  const order = leafOrder(tree);
  const n = order.length;

  const maxH = Math.max(1e-9, tree.height);
  let lo = 0;
  if (coefficient) {
    const minSim = 100 * (1 - maxH);
    lo = Math.max(0, Math.min(90, Math.floor(minSim / 10) * 10));
  }
  const xOf = (h: number) =>
    coefficient ? left + (treeW * (100 * (1 - h) - lo)) / (100 - lo) : left + treeW * (1 - h / maxH);

  const rowY = new Map<number, number>();
  order.forEach((leaf, i) => rowY.set(leaf, top + i * rowH + rowH / 2));

  // Colour of a node: its cluster's colour when every leaf below it belongs to one multi-lane cluster.
  const multi = (g: number) => !!cut && cut.sizes[g - 1] >= 2;
  const groupOf = (node: ClusterNode): number => {
    if (!cut) return 0;
    if (!node.left || !node.right) return multi(cut.groups[node.index]) ? cut.groups[node.index] : 0;
    const a = groupOf(node.left);
    return a !== 0 && a === groupOf(node.right) ? a : 0;
  };
  const colourOf = (g: number) => (g ? CLUSTER_COLOURS[(g - 1) % CLUSTER_COLOURS.length] : INK);

  const lines = new Map<string, string[]>();
  const addLine = (colour: string, d: string) => lines.set(colour, [...(lines.get(colour) ?? []), d]);
  const values: { x: number; y: number; v: string; s?: number }[] = [];
  const walk = (node: ClusterNode): { x: number; y: number } => {
    if (!node.left || !node.right) return { x: left + treeW, y: rowY.get(node.index)! };
    const a = walk(node.left);
    const b = walk(node.right);
    const x = Math.min(xOf(node.height), a.x, b.x);
    addLine(colourOf(groupOf(node)), `M${x},${a.y}V${b.y}M${x},${a.y}H${a.x}M${x},${b.y}H${b.x}`);
    const y = (a.y + b.y) / 2;
    values.push({
      x,
      y,
      v: coefficient ? (100 * (1 - node.height)).toFixed(1) : node.height.toPrecision(3),
      s: node === tree ? undefined : support?.get(node),
    });
    return { x, y };
  };
  const root = walk(tree);
  addLine(INK, `M${left - 8},${root.y}H${root.x}`);
  const cutX = cut ? Math.max(left, Math.min(left + treeW, xOf(cut.height))) : 0;
  const clusterCount = cut ? cut.sizes.filter((n) => n >= 2).length : 0;
  const singletons = cut ? cut.sizes.length - clusterCount : 0;

  // Axis ticks.
  const ticks: number[] = [];
  if (coefficient) {
    const step = 100 - lo <= 20 ? 2 : 100 - lo <= 50 ? 5 : 10;
    for (let v = lo; v <= 100 + 1e-9; v += step) ticks.push(v);
  } else {
    for (let i = 0; i <= 5; i++) ticks.push((maxH * i) / 5);
  }

  let x = left + treeW + gap;
  const imgX = withImages ? x : -1;
  if (withImages) x += stripW + gap;
  const bandX = withBands ? x : -1;
  if (withBands) x += stripW + gap;
  const clusterX = cut ? x : -1;
  if (cut) x += Math.max(textWidth('Cluster', 11), textWidth(String(cut.sizes.length)) + 16) + gap;
  const colX: number[] = [];
  headers.forEach((h, c) => {
    colX.push(x);
    const w = Math.min(220, Math.max(textWidth(h, 11), ...leaves.map((l) => textWidth(l.labels[c] ?? ''))));
    x += w + gap;
  });
  const width = Math.max(x + 8, textWidth(title, 14) + 40);
  const height = top + n * rowH + 16;

  return (
    <svg
      ref={ref}
      xmlns="http://www.w3.org/2000/svg"
      xmlnsXlink="http://www.w3.org/1999/xlink"
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      fontFamily={FONT}
      style={{ display: 'block' }}
    >
      <rect width={width} height={height} fill="#ffffff" />
      <text x={left} y={18} fontSize={14} fontWeight={600} fill="#111">
        {title}
      </text>
      {(cut || support) && (
        <text x={left} y={36} fontSize={11} fill="#555">
          {[
            cut && `Cut-off ${cut.label}: ${clusterCount} cluster${clusterCount === 1 ? '' : 's'}, ${singletons} unclustered lane${singletons === 1 ? '' : 's'}`,
            support && 'Bootstrap support (%) in blue',
          ]
            .filter(Boolean)
            .join(' · ')}
        </text>
      )}
      {/* axis */}
      <g fontSize={10} fill="#444" stroke="none">
        <line x1={left} x2={left + treeW} y1={top - 14} y2={top - 14} stroke="#888" />
        {ticks.map((t) => {
          const tx = coefficient ? left + (treeW * (t - lo)) / (100 - lo) : left + treeW * (1 - t / maxH);
          return (
            <g key={t}>
              <line x1={tx} x2={tx} y1={top - 18} y2={top - 10} stroke="#888" />
              <text x={tx} y={top - 22} textAnchor="middle">
                {coefficient ? t : t.toPrecision(2)}
              </text>
            </g>
          );
        })}
        <text x={left} y={top - 44} fontSize={10} fill="#666">
          {coefficient ? 'Similarity (%)' : 'Distance'}
        </text>
      </g>
      {[...lines].map(([colour, d]) => (
        <path key={colour} d={d.join('')} stroke={colour} strokeWidth={colour === INK ? 1.3 : 1.8} fill="none" />
      ))}
      {cut && (
        <g>
          <line x1={cutX} x2={cutX} y1={top - 14} y2={top + n * rowH} stroke="#d62728" strokeWidth={1} strokeDasharray="4 3" />
          <text x={cutX} y={top - 32} fontSize={10} fill="#d62728" textAnchor="middle">
            {cut.label}
          </text>
        </g>
      )}
      {showValues &&
        values.map((v, i) => (
          <text key={i} x={v.x + 3} y={v.y - 3} fontSize={9} fill="#c0392b">
            {v.v}
          </text>
        ))}
      {support &&
        values
          .filter((v) => v.s !== undefined)
          .map((v, i) => (
            <text key={i} x={v.x - 3} y={v.y + 10} fontSize={9} fill="#1f5fbf" textAnchor="end">
              {Math.round(v.s!)}
            </text>
          ))}
      {/* headers */}
      <g fontSize={11} fontWeight={600} fill="#444">
        {withImages && (
          <text x={imgX} y={top - 8}>
            Normalised lanes
          </text>
        )}
        {withBands && (
          <text x={bandX} y={top - 8}>
            Bands
          </text>
        )}
        {cut && (
          <text x={clusterX} y={top - 8}>
            Cluster
          </text>
        )}
        {headers.map((h, c) => (
          <text key={c} x={colX[c]} y={top - 8}>
            {h}
          </text>
        ))}
      </g>
      {order.map((leaf, i) => {
        const y = top + i * rowH;
        const l = leaves[leaf];
        return (
          <g key={leaf}>
            {withImages && l.image && (
              <image href={l.image} x={imgX} y={y + 2} width={stripW} height={rowH - 4} preserveAspectRatio="none" />
            )}
            {withBands && (
              <g>
                <rect x={bandX} y={y + 2} width={stripW} height={rowH - 4} fill="#f2f4f7" stroke="#d0d5dd" strokeWidth={0.5} />
                {l.positions.map((p, k) => (
                  <line
                    key={k}
                    x1={bandX + p * stripW}
                    x2={bandX + p * stripW}
                    y1={y + 4}
                    y2={y + rowH - 4}
                    stroke="#1c2430"
                    strokeWidth={1.6}
                  />
                ))}
              </g>
            )}
            {cut && (
              <g>
                <rect x={clusterX} y={y + rowH / 2 - 5} width={10} height={10} fill={colourOf(multi(cut.groups[leaf]) ? cut.groups[leaf] : 0)} opacity={multi(cut.groups[leaf]) ? 1 : 0.25} />
                <text x={clusterX + 14} y={y + rowH / 2 + 4} fontSize={12} fill="#111">
                  {cut.groups[leaf]}
                </text>
              </g>
            )}
            {l.labels.map((t, c) => (
              <text key={c} x={colX[c]} y={y + rowH / 2 + 4} fontSize={12} fill="#111">
                {t.length > 34 ? t.slice(0, 33) + '…' : t}
              </text>
            ))}
            <line x1={left + treeW} x2={left + treeW + 6} y1={y + rowH / 2} y2={y + rowH / 2} stroke={colourOf(groupOf({ index: leaf, height: 0, size: 1 }))} strokeWidth={1.3} />
          </g>
        );
      })}
    </svg>
  );
});
