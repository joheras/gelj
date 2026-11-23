// Band matching across lanes: band classes and the binary (presence/absence) matrix used by
// population-genetics and phylogenetics software.

export interface BandMatching {
  /** Mean molecular weight of each band class, from largest to smallest. */
  classes: number[];
  /** matrix[lane][class] = 1 when the lane has a band in that class. */
  matrix: number[][];
}

/**
 * Groups the bands of every lane into band classes. Bands are pooled and sorted by decreasing molecular
 * weight; a band joins the current class when it lies within `tolerance` of the class's first (largest)
 * band and its lane has no band in the class yet, otherwise it starts a new class. Every pair of bands in
 * a class therefore matches with the same tolerance used by the band-based coefficients.
 */
export function matchBands(lanes: number[][], tolerance: number): BandMatching {
  const pooled = lanes.flatMap((mw, lane) => mw.map((v) => ({ v, lane }))).sort((a, b) => b.v - a.v);
  const groups: { first: number; values: number[]; lanes: Set<number> }[] = [];
  for (const b of pooled) {
    const g = groups[groups.length - 1];
    if (g && g.first - b.v <= tolerance && !g.lanes.has(b.lane)) {
      g.values.push(b.v);
      g.lanes.add(b.lane);
    } else {
      groups.push({ first: b.v, values: [b.v], lanes: new Set([b.lane]) });
    }
  }
  return {
    classes: groups.map((g) => g.values.reduce((a, b) => a + b, 0) / g.values.length),
    matrix: lanes.map((_, lane) => groups.map((g) => (g.lanes.has(lane) ? 1 : 0))),
  };
}

function formatMw(v: number): string {
  return Number(v.toPrecision(4)).toString();
}

/** CSV with one row per lane and one column per band class (headed by its mean molecular weight). */
export function bandMatrixCsv(m: BandMatching, names: string[], extra: { header: string; values: string[] }[] = []): string {
  const q = (s: string) => `"${s.replace(/"/g, '""')}"`;
  const head = [q('Lane'), ...extra.map((e) => q(e.header)), ...m.classes.map(formatMw)].join(',');
  const rows = m.matrix.map((row, i) => [q(names[i]), ...extra.map((e) => q(e.values[i] ?? '')), ...row].join(','));
  return [head, ...rows].join('\n');
}

/** NEXUS data block with standard 0/1 characters (readable by PAUP*, SplitsTree, MrBayes, Mesquite…). */
export function bandMatrixNexus(m: BandMatching, names: string[], note = ''): string {
  const q = (s: string) => `'${s.replace(/'/g, "''")}'`;
  const labels = m.classes.map((v) => q(`${formatMw(v)}`)).join(' ');
  return [
    '#NEXUS',
    `[Band-matching table exported by GelJ Web${note ? ` (${note.replace(/[[\]]/g, '')})` : ''}: 1 = band present, 0 = absent]`,
    'BEGIN TAXA;',
    `  DIMENSIONS NTAX=${names.length};`,
    `  TAXLABELS ${names.map(q).join(' ')};`,
    'END;',
    'BEGIN CHARACTERS;',
    `  DIMENSIONS NCHAR=${m.classes.length};`,
    '  FORMAT DATATYPE=STANDARD SYMBOLS="01" MISSING=? GAP=-;',
    `  CHARLABELS ${labels};`,
    '  MATRIX',
    ...m.matrix.map((row, i) => `    ${q(names[i])} ${row.join('')}`),
    '  ;',
    'END;',
    '',
  ].join('\n');
}
