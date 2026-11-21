import type { NormalisedLaneData } from '../core/normalise';
import type { Experiment, GrayImage, Lane, NormalisedLane } from '../core/types';
import { uid } from '../core/uid';
import { dataURLToGray, grayToDataURL } from './imageio';

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
}

export function newWork(image: GrayImage, name: string): WorkState {
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
  };
}

export function experimentFromWork(
  w: WorkState,
  status: Experiment['status'],
  step: number,
  normalised: NormalisedLane[] = [],
): Experiment {
  return {
    id: w.id,
    name: w.name.trim() || 'Untitled experiment',
    comments: w.comments,
    createdAt: w.createdAt,
    updatedAt: Date.now(),
    status,
    step,
    originalImage: grayToDataURL(w.original),
    image: grayToDataURL(w.image),
    width: w.image.width,
    height: w.image.height,
    darkBackground: w.darkBackground,
    markerId: w.markerId,
    referenceMw: w.referenceMw,
    lanes: w.lanes,
    normalised,
    bandColour: w.bandColour,
  };
}

export function encodeNormalised(data: NormalisedLaneData[]): NormalisedLane[] {
  return data.map((d) => ({
    laneId: d.laneId,
    width: d.image.width,
    height: d.image.height,
    image: grayToDataURL(d.image),
    profile: d.profile.map((v) => Math.round(v * 100) / 100),
    positions: d.positions,
    mw: d.mw,
  }));
}
