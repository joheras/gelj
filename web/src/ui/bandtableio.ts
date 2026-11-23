import { bandPositions, BAND_LANE_HEIGHT, BAND_LANE_WIDTH, montage, renderBandLane, type ImportedLane } from '../core/bandtable';
import type { BandTableSource, Experiment, Lab, Lane, NormalisedLane } from '../core/types';
import { uid } from '../core/uid';
import { grayToPng } from './imageio';

export interface BandTableImportOptions {
  name: string;
  source: BandTableSource;
  /** Laboratory of lanes without a laboratory column (or with an empty one). */
  labId: string | null;
  labs: Lab[];
}

/**
 * Builds complete experiments from imported lanes: one per laboratory named in the table (labs are matched
 * by name, and created when missing). Lane images are drawn from the bands; profiles are left empty so that
 * curve-based measures are not computed on made-up densitometry.
 */
export async function experimentsFromBandTable(lanes: ImportedLane[], o: BandTableImportOptions): Promise<{ experiments: Experiment[]; newLabs: Lab[] }> {
  const newLabs: Lab[] = [];
  const labFor = (name: string): string | null => {
    const n = name.trim();
    if (!n) return o.labId;
    const found = [...o.labs, ...newLabs].find((l) => l.name.trim().toLowerCase() === n.toLowerCase());
    if (found) return found.id;
    const lab: Lab = { id: uid(), name: n, city: '', country: '' };
    newLabs.push(lab);
    return lab.id;
  };
  const groups = new Map<string | null, ImportedLane[]>();
  for (const l of lanes) {
    const id = labFor(l.lab);
    groups.set(id, [...(groups.get(id) ?? []), l]);
  }
  // A common scale for the whole file, so that the virtual gels of every laboratory line up.
  const allPositions = bandPositions(
    lanes.map((l) => l.values),
    o.source.values,
  );
  const positionOf = new Map(lanes.map((l, i) => [l, allPositions[i]]));

  const experiments: Experiment[] = [];
  const now = Date.now();
  for (const [labId, list] of groups) {
    const labName = [...o.labs, ...newLabs].find((l) => l.id === labId)?.name;
    const images = list.map((l) => renderBandLane(positionOf.get(l)!));
    const [pngs, thumb] = await Promise.all([Promise.all(images.map(grayToPng)), grayToPng(montage(images))]);
    const laneRecords: Lane[] = [];
    const normalised: NormalisedLane[] = [];
    list.forEach((l, i) => {
      const id = uid();
      const positions = positionOf.get(l)!.map((p) => Math.round(p * 100) / 100);
      laneRecords.push({
        id,
        name: l.name,
        rect: { x: 6 + i * (BAND_LANE_WIDTH + 6), y: 0, w: BAND_LANE_WIDTH, h: BAND_LANE_HEIGHT },
        isReference: l.isReference,
        threshold: 0,
        bands: positions,
        meta: l.meta,
      });
      normalised.push({
        laneId: id,
        width: BAND_LANE_WIDTH,
        height: BAND_LANE_HEIGHT,
        image: pngs[i],
        profile: [],
        positions,
        mw: l.values,
      });
    });
    experiments.push({
      id: uid(),
      name: groups.size > 1 && labName ? `${o.name} — ${labName}` : o.name,
      comments: `Imported from ${o.source.file}`,
      createdAt: now,
      updatedAt: now,
      status: 'complete',
      step: 0,
      originalImage: thumb,
      image: thumb,
      width: images.length * (BAND_LANE_WIDTH + 6) + 6,
      height: BAND_LANE_HEIGHT,
      darkBackground: true,
      markerId: null,
      referenceMw: [],
      lanes: laneRecords,
      normalised,
      bandColour: '#2f7dff',
      labId,
      bandTable: o.source,
    });
  }
  return { experiments, newLabs };
}

/** Whether a normalised lane has a densitometric curve (gel lanes) or only band values (imported tables). */
export function hasProfile(n: NormalisedLane): boolean {
  return n.profile.length > 0;
}
