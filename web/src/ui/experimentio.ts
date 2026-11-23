import type { NormalisedLaneData } from '../core/normalise';
import type { Experiment, GrayImage, Lane, NormalisedLane, PreprocessStep } from '../core/types';
import { uid } from '../core/uid';
import { dataURLToGray, grayToPng } from './imageio';

/** In-memory state of an analysis being edited in the wizard. */
export interface WorkState {
  id: string;
  name: string;
  comments: string;
  createdAt: number;
  original: GrayImage;
  image: GrayImage;
  darkBackground: boolean;
  lanes: Lane[];
  markerId: string | null;
  referenceMw: number[];
  bandColour: string;
  labId: string | null;
  /** Operations that turned `original` into `image`; null when unknown (experiments saved before they were recorded). */
  preprocessing: PreprocessStep[] | null;
}

export function newWork(image: GrayImage, name: string, labId: string | null = null): WorkState {
  return {
    id: uid(),
    name,
    comments: '',
    createdAt: Date.now(),
    original: image,
    image,
    darkBackground: true,
    lanes: [],
    markerId: null,
    referenceMw: [],
    bandColour: '#2f7dff',
    labId,
    preprocessing: [],
  };
}

export async function workFromExperiment(e: Experiment): Promise<WorkState> {
  const [original, image] = await Promise.all([dataURLToGray(e.originalImage), dataURLToGray(e.image)]);
  return {
    id: e.id,
    name: e.name,
    comments: e.comments,
    createdAt: e.createdAt,
    original,
    image,
    darkBackground: e.darkBackground,
    lanes: e.lanes,
    markerId: e.markerId,
    referenceMw: e.referenceMw,
    bandColour: e.bandColour,
    labId: e.labId ?? null,
    preprocessing: e.preprocessing ?? null,
  };
}

export async function experimentFromWork(
  w: WorkState,
  status: Experiment['status'],
  step: number,
  normalised: NormalisedLane[] = [],
): Promise<Experiment> {
  const [originalImage, image] = await Promise.all([grayToPng(w.original), grayToPng(w.image)]);
  return {
    id: w.id,
    name: w.name.trim() || 'Untitled experiment',
    comments: w.comments,
    createdAt: w.createdAt,
    updatedAt: Date.now(),
    status,
    step,
    originalImage,
    image,
    width: w.image.width,
    height: w.image.height,
    darkBackground: w.darkBackground,
    markerId: w.markerId,
    referenceMw: w.referenceMw,
    lanes: w.lanes,
    normalised,
    bandColour: w.bandColour,
    labId: w.labId,
    ...(w.preprocessing ? { preprocessing: w.preprocessing } : {}),
  };
}

export async function encodeNormalised(data: NormalisedLaneData[]): Promise<NormalisedLane[]> {
  const images = await Promise.all(data.map((d) => grayToPng(d.image)));
  return data.map((d, i) => ({
    laneId: d.laneId,
    width: d.image.width,
    height: d.image.height,
    image: images[i],
    profile: d.profile.map((v) => Math.round(v * 100) / 100),
    positions: d.positions,
    mw: d.mw,
  }));
}
