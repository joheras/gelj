// Shared data model for GelJ Web.

/** 8-bit greyscale image (values 0..255), row-major. */
export interface GrayImage {
  width: number;
  height: number;
  data: Uint8ClampedArray;
}

export interface Rect {
  x: number;
  y: number;
  w: number;
  h: number;
}

export interface LaneMeta {
  genus: string;
  species: string;
  country: string;
  mlst: string;
  strain: string;
  comments: string;
}

export const EMPTY_META: LaneMeta = {
  genus: '',
  species: '',
  country: '',
  mlst: '',
  strain: '',
  comments: '',
};

export interface Lane {
  id: string;
  name: string;
  rect: Rect;
  isReference: boolean;
  /** Intensity threshold used by automatic band detection (0..255). */
  threshold: number;
  /** Band positions, in pixels from the top of the lane rectangle. Sorted ascending. */
  bands: number[];
  meta: LaneMeta;
}

/** Fit models, numbered as ImageJ's CurveFitter so legacy marker files keep working. */
export type FitMethod = number;

export interface Marker {
  id: string;
  name: string;
  /** Molecular weights of the marker bands, from top (largest) to bottom. */
  mw: number[];
  /** Size of the reference lane image the marker was defined on. */
  refWidth: number;
  refHeight: number;
  /** Position (px from lane top) of each band in the reference lane. */
  refPositions: number[];
  fitMethod: FitMethod;
  builtin?: boolean;
}

export interface NormalisedLane {
  laneId: string;
  width: number;
  height: number;
  /** Greyscale PNG data URL of the warped lane. */
  image: string;
  /** Row-averaged intensity profile (bands = high values). Length = height. */
  profile: number[];
  /** Band positions in the normalised lane (px from top). */
  positions: number[];
  /** Molecular weight of each band. */
  mw: number[];
}

/** Laboratory where a gel was run (as in GelJ, where each user belonged to a lab). */
export interface Lab {
  id: string;
  name: string;
  city: string;
  country: string;
}

export function labLabel(lab: Lab | undefined | null): string {
  if (!lab) return '';
  const place = [lab.city, lab.country].filter(Boolean).join(', ');
  return place ? `${lab.name} (${place})` : lab.name;
}

export type ExperimentStatus = 'draft' | 'complete';

export interface Experiment {
  id: string;
  name: string;
  comments: string;
  createdAt: number;
  updatedAt: number;
  status: ExperimentStatus;
  /** Wizard step to resume a draft at. */
  step: number;
  /** Image as loaded (greyscale PNG data URL). */
  originalImage: string;
  /** Image after pre-processing (greyscale PNG data URL). */
  image: string;
  width: number;
  height: number;
  darkBackground: boolean;
  markerId: string | null;
  /** MW assigned to each band of the reference lanes (same count for every reference lane). */
  referenceMw: number[];
  lanes: Lane[];
  normalised: NormalisedLane[];
  bandColour: string;
  /** Laboratory of the experiment (absent in data created before labs existed). */
  labId?: string | null;
  /** Set for experiments imported from a band table: their lane images are drawn from the bands, and their profiles are empty. */
  bandTable?: BandTableSource;
}

/** Provenance of an experiment imported from a band table (GelCompar II / BioNumerics, spreadsheets…). */
export interface BandTableSource {
  file: string;
  layout: 'long' | 'wide' | 'matrix';
  /** Band values are molecular weights, or positions in % of a normalised gel (comparable only within one reference system). */
  values: 'mw' | 'position';
}

export type SimilarityMethod =
  | 'Dice'
  | 'Jaccard'
  | 'Band difference'
  | "Jeffrey's X"
  | 'Ochiai'
  | 'Pearson correlation'
  | 'Cosine coefficient'
  | 'Euclidean distance'
  | 'Manhattan distance';

export type Linkage = 'UPGMA' | 'UPGMC' | 'Single linkage' | 'Complete linkage' | 'Mean linkage' | 'Ward';

export type OutputMode = 'dendrogram+images+bands' | 'dendrogram+images' | 'dendrogram+bands' | 'dendrogram';

export interface Comparison {
  id: string;
  name: string;
  createdAt: number;
  /** References to lanes as `${experimentId}/${laneId}`. */
  laneRefs: string[];
  similarity: SimilarityMethod;
  linkage: Linkage;
  /** Tolerance as a percentage of the molecular-weight range. */
  tolerance: number;
  output: OutputMode;
  fields: (keyof LaneMeta | 'experiment' | 'lane' | 'lab')[];
  comments: string;
  /** Dendrogram cut-off defining clusters (% similarity, or distance for distance measures); absent or null = none. */
  cutoff?: number | null;
}
