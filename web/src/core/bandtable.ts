// Import of band tables produced by other software (GelCompar II / BioNumerics band-matching or band
// exports, GelJ desktop or GelJ Web band CSVs, spreadsheets): delimited-text parsing, layout and column
// guessing, and conversion into lanes with molecular weights. Pure functions; the UI turns the result
// into "band-table" experiments (lanes without a gel image).
import { createImage } from './image';
import type { GrayImage, LaneMeta } from './types';

/**
 * - `long`: one row per band (lane label repeated, or left empty on the lane's following rows).
 * - `wide`: one row per lane; bands in several columns and/or as a list in one cell ("1200; 850; 400").
 * - `matrix`: band-matching / character table: one row per lane, one column per band class headed by
 *   its molecular weight (or position); a cell is a presence mark (1, +, an intensity > 0) or empty/0.
 */
export type BandTableLayout = 'long' | 'wide' | 'matrix';

/** What band values (or, in a matrix, band-class headers) are. */
export type BandValues = 'mw' | 'position';

export type MetaField = keyof LaneMeta | 'lab';

export const META_FIELDS: [MetaField, string][] = [
  ['strain', 'Strain number'],
  ['genus', 'Genus'],
  ['species', 'Species'],
  ['country', 'Country'],
  ['mlst', 'MLST'],
  ['comments', 'Comments'],
  ['lab', 'Laboratory'],
];

export interface ParsedTable {
  delimiter: string;
  /** Every non-empty row, cells trimmed. */
  rows: string[][];
}

export interface BandTableMapping {
  layout: BandTableLayout;
  /** Index of the header row in `ParsedTable.rows`; rows above it are ignored. */
  headerRow: number;
  /** Column of the lane label (key / sample / isolate). */
  lane: number;
  meta: Partial<Record<MetaField, number>>;
  /** Optional column marking marker/reference lanes (yes/true/1/x). */
  reference?: number;
  /** Band columns: one (long), one or more (wide) or every band-class column (matrix). */
  bands: number[];
  values: BandValues;
  /** Factor applied to every band value (e.g. 1000 for kb → bp). */
  scale: number;
}

export interface ImportedLane {
  name: string;
  meta: LaneMeta;
  lab: string;
  isReference: boolean;
  /** Band values (molecular weights or positions, after scaling), sorted from the top of the gel. */
  values: number[];
}

export interface ImportIssue {
  level: 'error' | 'warning';
  message: string;
}

export interface BandTableResult {
  lanes: ImportedLane[];
  issues: ImportIssue[];
}

const DELIMITERS = ['\t', ';', ','];

/** Splits one line, honouring double quotes ("a, b" and "" escapes). */
function splitLine(line: string, d: string): string[] {
  const out: string[] = [];
  let cur = '';
  let quoted = false;
  for (let i = 0; i < line.length; i++) {
    const c = line[i];
    if (quoted) {
      if (c === '"' && line[i + 1] === '"') {
        cur += '"';
        i++;
      } else if (c === '"') quoted = false;
      else cur += c;
    } else if (c === '"' && cur.trim() === '') {
      quoted = true;
      cur = '';
    } else if (c === d) {
      out.push(cur.trim());
      cur = '';
    } else cur += c;
  }
  out.push(cur.trim());
  return out;
}

/** Splits text into lines, keeping newlines that are inside quotes. */
function splitRecords(text: string): string[] {
  const out: string[] = [];
  let cur = '';
  let quoted = false;
  for (let i = 0; i < text.length; i++) {
    const c = text[i];
    if (c === '"') quoted = !quoted;
    if (!quoted && (c === '\n' || c === '\r')) {
      if (c === '\r' && text[i + 1] === '\n') i++;
      out.push(cur);
      cur = '';
    } else cur += c;
  }
  out.push(cur);
  return out;
}

/** Guesses the delimiter: the candidate splitting the first lines into the most, and most consistent, columns. */
export function detectDelimiter(text: string): string {
  const lines = splitRecords(text)
    .filter((l) => l.trim())
    .slice(0, 20);
  let best = ',';
  let bestScore = -1;
  for (const d of DELIMITERS) {
    const freq = new Map<number, number>();
    for (const l of lines) {
      const c = splitLine(l, d).length;
      freq.set(c, (freq.get(c) ?? 0) + 1);
    }
    let mode = 0;
    let consistent = 0;
    for (const [c, k] of freq) if (k > consistent || (k === consistent && c > mode)) [mode, consistent] = [c, k];
    if (mode < 2) continue;
    // Lines with the most frequent column count, weighted by that count.
    const score = consistent * 100 + mode;
    if (score > bestScore) {
      bestScore = score;
      best = d;
    }
  }
  return best;
}

export function parseDelimited(text: string, delimiter?: string): ParsedTable {
  const clean = text.replace(/^\uFEFF/, '');
  const d = delimiter ?? detectDelimiter(clean);
  const rows = splitRecords(clean)
    .map((l) => splitLine(l, d))
    .filter((r) => r.some((c) => c !== ''));
  // Drop trailing empty columns (spreadsheets often export them).
  let width = 0;
  for (const r of rows) for (let i = r.length - 1; i >= 0; i--) if (r[i] !== '') { width = Math.max(width, i + 1); break; }
  return { delimiter: d, rows: rows.map((r) => Array.from({ length: width }, (_, i) => r[i] ?? '')) };
}

/**
 * Parses a number written as `1200`, `1.2e3`, `1,5` (decimal comma), `850 bp` or `1.5kb`.
 * Units are dropped: `kb`/`bp` are only accepted so that cells like "850 bp" parse; scaling is the
 * mapping's job. Returns NaN for anything else.
 */
export function parseNumber(s: string): number {
  let t = s.trim().replace(/\s*(bp|kb|kbp|mb|da|kda|%)$/i, '');
  if (t === '') return NaN;
  if (/^[-+]?\d+,\d+$/.test(t)) t = t.replace(',', '.');
  else if (/^[-+]?\d{1,3}(,\d{3})+(\.\d+)?$/.test(t)) t = t.replace(/,/g, '');
  if (!/^[-+]?(\d+\.?\d*|\.\d+)(e[-+]?\d+)?$/i.test(t)) return NaN;
  return Number(t);
}

/** Splits a cell holding several bands ("1200; 850; 400", "1200|850", "1200 850"). */
export function parseNumberList(s: string): number[] {
  const t = s.trim();
  if (t === '') return [];
  const single = parseNumber(t);
  if (!Number.isNaN(single)) return [single];
  return t
    .split(/[;|/]+|\s+|,\s+/)
    .filter(Boolean)
    .map(parseNumber);
}

const SYNONYMS: Record<'lane' | 'reference' | 'band' | 'position' | MetaField, RegExp> = {
  lane: /^(key|lane|lane ?(name|label|id)|name|entry|entry ?key|sample|sample ?(name|id)|isolate|isolate ?(name|id)|label|track|id)$/,
  strain: /^(strain|strain ?(number|no\.?|nr|id|name)|isolate ?number)$/,
  genus: /^genus$/,
  species: /^(species|organism)$/,
  country: /^(country|country ?of ?origin|origin)$/,
  mlst: /^(mlst|st|sequence ?type|mlst ?type)$/,
  comments: /^(comments?|notes?|remarks?|description)$/,
  lab: /^(lab|laboratory|institution|institute|centre|center|laboratory ?name)$/,
  reference: /^(reference|ref|marker|is ?reference|is ?marker|standard)$/,
  band: /^(mw|m\.w\.|molecular ?weights?|molecular_weights?|size|sizes|fragment ?size|band ?size|bp|kb|metrics?|bands|band ?sizes|band ?mw|mol\.? ?weight)( ?\((bp|kb)\))?$/,
  position: /^(position|pos|rf|relative ?position|position ?\(%\)|band ?position)$/,
};

const norm = (h: string) => h.toLowerCase().replace(/[_\s]+/g, ' ').trim();

/** Header of a band column in a "wide" table: "Band 1", "B2", "MW 3", "Band_4 (bp)"... */
const NUMBERED_BAND = /^(band|b|mw|size|fragment|peak)\s*#?\s*\d+( ?\((bp|kb)\))?$/;

function findColumn(headers: string[], re: RegExp, taken: Set<number>, last = false): number | undefined {
  const matches = headers.flatMap((h, k) => (!taken.has(k) && re.test(norm(h)) ? [k] : []));
  return last ? matches[matches.length - 1] : matches[0];
}

/**
 * Value of a band-class column header: a plain number ("2600", "45.3") or a label ending with a number in
 * brackets, as in GelJ desktop's population-genetics exports ("Locus1(2600.5)") or "B3 [1250 bp]".
 * NaN when the header is not a band class.
 */
export function classHeaderValue(h: string): number {
  const v = parseNumber(h);
  if (!Number.isNaN(v)) return v;
  const m = /[([]\s*([^()[\]]+?)\s*[)\]]\s*$/.exec(h);
  return m ? parseNumber(m[1]) : NaN;
}

/** Columns whose header is a band class (see `classHeaderValue`). */
export function numericHeaderColumns(headers: string[]): number[] {
  return headers.flatMap((h, i) => (classHeaderValue(h) > 0 ? [i] : []));
}

/** Index of the first row that looks like a header (at least two non-empty cells, not all numeric). */
export function guessHeaderRow(t: ParsedTable): number {
  const i = t.rows.findIndex((r) => r.filter((c) => c !== '').length >= 2 && r.some((c) => c !== '' && Number.isNaN(parseNumber(c))));
  return i >= 0 ? i : 0;
}

/** Proposes a layout and column mapping from the headers and values. */
export function guessMapping(t: ParsedTable, headerRow = guessHeaderRow(t)): BandTableMapping {
  const headers = t.rows[headerRow] ?? [];
  const body = t.rows.slice(headerRow + 1);
  const taken = new Set<number>();
  const take = (re: RegExp, last = false) => {
    const i = findColumn(headers, re, taken, last);
    if (i !== undefined) taken.add(i);
    return i;
  };
  const numericCols = numericHeaderColumns(headers);
  numericCols.forEach((i) => taken.add(i));
  let lane = take(SYNONYMS.lane);
  const meta: BandTableMapping['meta'] = {};
  for (const [f] of META_FIELDS) {
    // GelJ's band CSVs have the laboratory's country first and the lane's country later.
    const i = take(SYNONYMS[f], f === 'country');
    if (i !== undefined) meta[f] = i;
  }
  const reference = take(SYNONYMS.reference);
  if (lane === undefined) {
    // Fall back on the strain column, else on the first free column.
    lane = meta.strain ?? headers.findIndex((_, i) => !taken.has(i));
    if (lane < 0) lane = 0;
  }

  let layout: BandTableLayout;
  let bands: number[] = [];
  let values: BandValues = 'mw';
  if (numericCols.length >= 2) {
    layout = 'matrix';
    bands = numericCols;
    // Band-class headers between 0 and 100 with decimals are probably positions (% of the gel).
    const hv = numericCols.map((i) => classHeaderValue(headers[i]));
    if (hv.every((v) => v <= 100) && hv.some((v) => !Number.isInteger(v))) values = 'position';
  } else {
    const numbered = headers.flatMap((h, i) => (!taken.has(i) && NUMBERED_BAND.test(norm(h)) ? [i] : []));
    if (numbered.length >= 2) {
      layout = 'wide';
      bands = numbered;
    } else {
      let band = take(SYNONYMS.band);
      if (band === undefined) {
        band = take(SYNONYMS.position);
        if (band !== undefined) values = 'position';
      }
      if (band === undefined) {
        // First free column whose values are mostly numeric.
        band = headers.findIndex((_, i) => !taken.has(i) && body.filter((r) => !Number.isNaN(parseNumber(r[i] ?? ''))).length > body.length / 2);
        if (band < 0) band = headers.findIndex((_, i) => !taken.has(i));
      }
      bands = band >= 0 ? [band] : [];
      // One row per band when lane labels repeat (or are left empty) and band cells hold single values.
      const labels = body.map((r) => r[lane] ?? '');
      const repeats = new Set(labels.filter(Boolean)).size < labels.length;
      const lists = band >= 0 && body.some((r) => parseNumberList(r[band] ?? '').length > 1);
      layout = repeats && !lists ? 'long' : 'wide';
    }
  }
  // Values in kilobases ("Size (kb)", "Band 1 (kb)") are converted to base pairs.
  const scale = values === 'mw' && layout !== 'matrix' && bands.some((i) => /\bkb\b/i.test(headers[i] ?? '')) ? 1000 : 1;
  return { layout, headerRow, lane, meta, reference, bands, values, scale };
}

const TRUE = /^(1|y|yes|true|x|\+|ref|reference|marker)$/i;

function isPresent(cell: string): boolean | null {
  const c = cell.trim();
  if (c === '' || c === '-' || c === '.') return false;
  if (c === '?') return null;
  if (/^(\+|x|yes|y|present)$/i.test(c)) return true;
  const v = parseNumber(c);
  if (Number.isNaN(v)) return null;
  return v > 0;
}

/** Applies a mapping: groups rows into lanes and validates every band value. */
export function readBandTable(t: ParsedTable, m: BandTableMapping): BandTableResult {
  const issues: ImportIssue[] = [];
  const headers = t.rows[m.headerRow] ?? [];
  const body = t.rows.slice(m.headerRow + 1);
  const rowNo = (i: number) => m.headerRow + i + 2; // 1-based line number in the file (ignoring blank lines)
  const lanes: ImportedLane[] = [];
  const byName = new Map<string, ImportedLane>();
  const conflicts = new Set<string>();
  let skipped = 0;
  let unknownCells = 0;

  if (m.bands.length === 0) {
    issues.push({ level: 'error', message: 'Choose the column(s) that hold the bands.' });
    return { lanes, issues };
  }
  if (m.bands.includes(m.lane)) issues.push({ level: 'error', message: 'The lane column cannot also be a band column.' });

  const metaOf = (r: string[]) => {
    const meta = { genus: '', species: '', country: '', mlst: '', strain: '', comments: '' } as LaneMeta;
    for (const [f, i] of Object.entries(m.meta) as [MetaField, number | undefined][]) if (i !== undefined && f !== 'lab') meta[f] = r[i] ?? '';
    return meta;
  };
  const cell = (r: string[], i: number | undefined) => (i === undefined ? '' : (r[i] ?? ''));

  const classValues = m.layout === 'matrix' ? m.bands.map((i) => classHeaderValue(headers[i] ?? '') * m.scale) : [];
  if (m.layout === 'matrix') {
    const bad = m.bands.filter((_, k) => !(classValues[k] > 0)).map((i) => `"${headers[i] ?? ''}"`);
    if (bad.length) issues.push({ level: 'error', message: `Band-class columns must be headed by a positive number (e.g. 1250 or Locus3(1250)): ${bad.join(', ')}.` });
  }

  let current: ImportedLane | null = null;
  body.forEach((r, i) => {
    let name = cell(r, m.lane);
    if (name === '' && m.layout === 'long' && current) name = current.name;
    if (name === '') {
      // A row with bands but no lane label.
      if (m.bands.some((b) => (r[b] ?? '') !== '')) {
        skipped++;
        issues.push({ level: 'warning', message: `Line ${rowNo(i)}: no lane label; row skipped.` });
      }
      return;
    }
    let lane = byName.get(name);
    if (lane && m.layout !== 'long') {
      issues.push({ level: 'warning', message: `Line ${rowNo(i)}: lane "${name}" appears more than once; its bands were merged.` });
    }
    if (!lane) {
      lane = { name, meta: metaOf(r), lab: cell(r, m.meta.lab), isReference: TRUE.test(cell(r, m.reference)), values: [] };
      byName.set(name, lane);
      lanes.push(lane);
    } else {
      // Fill metadata left empty on the lane's first row; flag contradictions.
      const meta = metaOf(r);
      for (const k of Object.keys(meta) as (keyof LaneMeta)[]) {
        if (!meta[k]) continue;
        if (!lane.meta[k]) lane.meta[k] = meta[k];
        else if (lane.meta[k] !== meta[k] && !conflicts.has(`${name}/${k}`)) {
          conflicts.add(`${name}/${k}`);
          issues.push({ level: 'warning', message: `Lane "${name}": several values for ${k} ("${lane.meta[k]}", "${meta[k]}"); the first one is kept.` });
        }
      }
      if (!lane.lab) lane.lab = cell(r, m.meta.lab);
      if (TRUE.test(cell(r, m.reference))) lane.isReference = true;
    }
    current = lane;

    if (m.layout === 'matrix') {
      m.bands.forEach((col, k) => {
        const p = isPresent(r[col] ?? '');
        if (p === null) unknownCells++;
        else if (p && classValues[k] > 0) lane.values.push(classValues[k]);
      });
      return;
    }
    for (const col of m.bands) {
      const raw = r[col] ?? '';
      if (raw === '') continue;
      const vals = parseNumberList(raw);
      vals.forEach((v) => {
        if (!Number.isFinite(v)) issues.push({ level: 'error', message: `Line ${rowNo(i)}: "${raw}" is not a number.` });
        else if (v <= 0 && m.values === 'mw') issues.push({ level: 'error', message: `Line ${rowNo(i)}: molecular weight ${raw} must be positive.` });
        else if (v < 0) issues.push({ level: 'error', message: `Line ${rowNo(i)}: position ${raw} must not be negative.` });
        else lane.values.push(v * m.scale);
      });
    }
  });

  if (unknownCells) issues.push({ level: 'warning', message: `${unknownCells} cells were neither a presence mark nor empty/0 (e.g. "?"); they were read as absent.` });
  for (const l of lanes) {
    // Top of the gel first: decreasing molecular weight, increasing position.
    l.values.sort((a, b) => (m.values === 'mw' ? b - a : a - b));
    if (l.values.length === 0) issues.push({ level: 'warning', message: `Lane "${l.name}" has no bands.` });
    const dup = l.values.some((v, k) => k > 0 && v === l.values[k - 1]);
    if (dup) issues.push({ level: 'warning', message: `Lane "${l.name}" has repeated band values (doublets are kept).` });
  }
  if (m.values === 'position') {
    const max = Math.max(0, ...lanes.flatMap((l) => l.values));
    if (max > 100) issues.push({ level: 'error', message: `Positions must be percentages of the gel (0–100); found ${max}.` });
  }
  if (lanes.length === 0) issues.push({ level: 'error', message: 'No lanes were found with this mapping.' });
  if (skipped > 5) {
    // Keep the list readable: collapse the per-line warnings.
    const rest = issues.filter((x) => !x.message.endsWith('no lane label; row skipped.'));
    rest.push({ level: 'warning', message: `${skipped} rows had bands but no lane label and were skipped.` });
    return { lanes, issues: rest };
  }
  return { lanes, issues };
}

/** Height (px) of the synthetic lanes drawn for imported band tables. */
export const BAND_LANE_HEIGHT = 400;
export const BAND_LANE_WIDTH = 24;
const MARGIN = 0.05;

/**
 * Vertical position (px from the top of a synthetic lane) of every band: positions in % of the gel are
 * scaled; molecular weights are placed on a log scale spanning the range of the whole import, so that
 * the virtual gel looks like a normalised gel (large fragments on top).
 */
export function bandPositions(lanes: number[][], values: BandValues, height = BAND_LANE_HEIGHT): number[][] {
  if (values === 'position') return lanes.map((l) => l.map((v) => (Math.min(100, Math.max(0, v)) / 100) * height));
  const all = lanes.flat().filter((v) => v > 0);
  const hi = Math.log(Math.max(...all, 1));
  const lo = Math.log(Math.min(...all, Math.exp(hi)));
  const span = hi - lo;
  return lanes.map((l) =>
    l.map((v) => {
      const t = span > 0 ? (hi - Math.log(v)) / span : 0.5;
      return height * (MARGIN + (1 - 2 * MARGIN) * t);
    }),
  );
}

/** Draws a synthetic lane (bright bands on a dark background) from band positions. */
export function renderBandLane(positions: number[], width = BAND_LANE_WIDTH, height = BAND_LANE_HEIGHT): GrayImage {
  const img = createImage(width, height, 14);
  const sigma = 1.6;
  for (const p of positions) {
    for (let y = Math.max(0, Math.floor(p - 5)); y <= Math.min(height - 1, Math.ceil(p + 5)); y++) {
      const a = 210 * Math.exp(-((y - p) ** 2) / (2 * sigma * sigma));
      for (let x = 2; x < width - 2; x++) img.data[y * width + x] = Math.min(255, img.data[y * width + x] + a);
    }
  }
  return img;
}

/** Side-by-side montage of lanes with a gap, used as the thumbnail of an imported experiment. */
export function montage(lanes: GrayImage[], gap = 6): GrayImage {
  const h = Math.max(1, ...lanes.map((l) => l.height));
  const w = Math.max(1, lanes.reduce((s, l) => s + l.width + gap, gap));
  const img = createImage(w, h, 0);
  let x0 = gap;
  for (const l of lanes) {
    for (let y = 0; y < l.height; y++) for (let x = 0; x < l.width; x++) img.data[y * w + x0 + x] = l.data[y * l.width + x];
    x0 += l.width + gap;
  }
  return img;
}
