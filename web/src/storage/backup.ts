// Validation of imported backups: malformed records are rejected before anything is written, and
// fields missing in backups made by older versions get their default values.
import { LINKAGES } from '../core/cluster';
import type { LibraryEntry } from '../core/identify';
import { SIMILARITY_METHODS } from '../core/similarity';
import type { Comparison, Experiment, Lab, Lane, Marker, NormalisedLane } from '../core/types';
import { EMPTY_META } from '../core/types';

export interface BackupContents {
  experiments: Experiment[];
  markers: Marker[];
  comparisons: Comparison[];
  labs: Lab[];
  library: LibraryEntry[];
}

type Obj = Record<string, unknown>;

class InvalidRecord extends Error {}

const isObj = (v: unknown): v is Obj => typeof v === 'object' && v !== null && !Array.isArray(v);
const isNum = (v: unknown): v is number => typeof v === 'number' && Number.isFinite(v);
const isNumArray = (v: unknown): v is number[] => Array.isArray(v) && v.every(isNum);
const str = (v: unknown, fallback = ''): string => (typeof v === 'string' ? v : fallback);

function need(cond: unknown, what: string): asserts cond {
  if (!cond) throw new InvalidRecord(what);
}

function list(v: unknown, what: string): unknown[] {
  if (v === undefined) return [];
  need(Array.isArray(v), `"${what}" is not a list`);
  return v;
}

function lane(v: unknown): Lane {
  need(isObj(v) && typeof v.id === 'string', 'a lane has no id');
  const r = v.rect;
  need(isObj(r) && isNum(r.x) && isNum(r.y) && isNum(r.w) && isNum(r.h), `lane ${v.id} has no valid rectangle`);
  need(v.bands === undefined || isNumArray(v.bands), `lane ${v.id} has invalid bands`);
  const meta = isObj(v.meta) ? v.meta : {};
  return {
    id: v.id,
    name: str(v.name, 'Lane'),
    rect: { x: r.x, y: r.y, w: r.w, h: r.h },
    isReference: v.isReference === true,
    threshold: isNum(v.threshold) ? v.threshold : 128,
    bands: ((v.bands as number[] | undefined) ?? []).slice().sort((a, b) => a - b),
    meta: Object.fromEntries(Object.keys(EMPTY_META).map((k) => [k, str(meta[k])])) as unknown as Lane['meta'],
  };
}

function normalised(v: unknown): NormalisedLane {
  need(isObj(v) && typeof v.laneId === 'string' && typeof v.image === 'string', 'a normalised lane is incomplete');
  need(isNumArray(v.positions) && Array.isArray(v.mw) && v.mw.length === v.positions.length, `normalised lane ${v.laneId} has invalid bands`);
  return {
    laneId: v.laneId,
    width: isNum(v.width) ? v.width : 1,
    height: isNum(v.height) ? v.height : 1,
    image: v.image,
    profile: isNumArray(v.profile) ? v.profile : [],
    positions: v.positions,
    // JSON turns NaN into null.
    mw: v.mw.map((x) => (isNum(x) ? x : NaN)),
  };
}

function experiment(v: unknown): Experiment {
  need(isObj(v) && typeof v.id === 'string', 'an experiment has no id');
  need(typeof v.image === 'string' && v.image.startsWith('data:image/'), `experiment ${v.id} has no image`);
  const lanes = list(v.lanes, 'lanes').map(lane);
  const norm = list(v.normalised, 'normalised').map(normalised);
  const status = v.status === 'complete' && norm.length > 0 ? 'complete' : 'draft';
  return {
    id: v.id,
    name: str(v.name, 'Untitled experiment'),
    comments: str(v.comments),
    createdAt: isNum(v.createdAt) ? v.createdAt : Date.now(),
    updatedAt: isNum(v.updatedAt) ? v.updatedAt : Date.now(),
    status,
    step: isNum(v.step) ? v.step : 0,
    originalImage: typeof v.originalImage === 'string' ? v.originalImage : v.image,
    image: v.image,
    width: isNum(v.width) ? v.width : 0,
    height: isNum(v.height) ? v.height : 0,
    darkBackground: v.darkBackground !== false,
    markerId: typeof v.markerId === 'string' ? v.markerId : null,
    referenceMw: isNumArray(v.referenceMw) ? v.referenceMw : [],
    lanes,
    normalised: norm,
    bandColour: str(v.bandColour, '#2f7dff'),
    labId: typeof v.labId === 'string' ? v.labId : null,
    ...(bandTableSource(v.bandTable) ?? {}),
  };
}

function bandTableSource(v: unknown): Pick<Experiment, 'bandTable'> | null {
  if (!isObj(v)) return null;
  const layout = v.layout === 'long' || v.layout === 'wide' || v.layout === 'matrix' ? v.layout : 'long';
  return { bandTable: { file: str(v.file), layout, values: v.values === 'position' ? 'position' : 'mw' } };
}

function marker(v: unknown): Marker {
  need(isObj(v) && typeof v.id === 'string', 'a marker has no id');
  need(
    isNumArray(v.mw) && isNumArray(v.refPositions) && v.mw.length === v.refPositions.length && v.mw.length >= 2,
    `marker ${v.id} needs matching molecular weights and positions`,
  );
  need(isNum(v.refWidth) && isNum(v.refHeight), `marker ${v.id} has no reference size`);
  return {
    id: v.id,
    name: str(v.name, v.id),
    mw: v.mw,
    refWidth: v.refWidth,
    refHeight: v.refHeight,
    refPositions: v.refPositions,
    fitMethod: isNum(v.fitMethod) ? v.fitMethod : 0,
    ...(v.builtin === true ? { builtin: true } : {}),
  };
}

function comparison(v: unknown): Comparison {
  need(isObj(v) && typeof v.id === 'string', 'a comparison has no id');
  need(Array.isArray(v.laneRefs) && v.laneRefs.every((r) => typeof r === 'string'), `comparison ${v.id} has no lanes`);
  need(SIMILARITY_METHODS.includes(v.similarity as never), `comparison ${v.id} has an unknown similarity`);
  need(LINKAGES.includes(v.linkage as never), `comparison ${v.id} has an unknown linkage`);
  return {
    id: v.id,
    name: str(v.name, 'Comparison'),
    createdAt: isNum(v.createdAt) ? v.createdAt : Date.now(),
    laneRefs: v.laneRefs as string[],
    similarity: v.similarity as Comparison['similarity'],
    linkage: v.linkage as Comparison['linkage'],
    tolerance: isNum(v.tolerance) ? v.tolerance : 1,
    ...(isNum(v.optimisation) && v.optimisation > 0 ? { optimisation: v.optimisation } : {}),
    output: (['dendrogram+images+bands', 'dendrogram+images', 'dendrogram+bands', 'dendrogram'] as const).includes(v.output as never)
      ? (v.output as Comparison['output'])
      : 'dendrogram+images+bands',
    fields: Array.isArray(v.fields) ? (v.fields.filter((f) => typeof f === 'string') as Comparison['fields']) : ['lane'],
    comments: str(v.comments),
  };
}

function lab(v: unknown): Lab {
  need(isObj(v) && typeof v.id === 'string' && typeof v.name === 'string', 'a laboratory has no id or name');
  return { id: v.id, name: v.name, city: str(v.city), country: str(v.country) };
}

function libraryEntry(v: unknown): LibraryEntry {
  need(isObj(v) && typeof v.id === 'string' && /^[^/]+\/[^/]+$/.test(v.id), 'a library entry has no lane reference');
  need(typeof v.type === 'string' && v.type.trim() !== '', `library entry ${v.id} has no type`);
  return { id: v.id, type: v.type.trim(), comments: str(v.comments), createdAt: isNum(v.createdAt) ? v.createdAt : Date.now() };
}

/** Checks a parsed backup file; throws an Error naming the first invalid record. */
export function parseBackup(data: unknown): BackupContents {
  if (!isObj(data) || data.format !== 'gelj-web') throw new Error('This file is not a GelJ Web export.');
  const parse = <T>(key: string, fn: (v: unknown) => T): T[] =>
    list(data[key], key).map((v, i) => {
      try {
        return fn(v);
      } catch (e) {
        if (e instanceof InvalidRecord) throw new Error(`Invalid ${key} record #${i + 1}: ${e.message}.`);
        throw e;
      }
    });
  return {
    experiments: parse('experiments', experiment),
    markers: parse('markers', marker).filter((m) => !m.builtin),
    comparisons: parse('comparisons', comparison),
    labs: parse('labs', lab),
    // Absent in backups made before the reference library existed.
    library: parse('library', libraryEntry),
  };
}
