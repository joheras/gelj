// Local persistence in IndexedDB. Everything stays in the user's browser.
import { openDB, type DBSchema, type IDBPDatabase } from 'idb';
import { BUILTIN_MARKERS } from '../core/markers';
import type { Comparison, Experiment, Marker } from '../core/types';

interface GelJDB extends DBSchema {
  experiments: { key: string; value: Experiment };
  markers: { key: string; value: Marker };
  comparisons: { key: string; value: Comparison };
}

let dbPromise: Promise<IDBPDatabase<GelJDB>> | null = null;

function db() {
  if (!dbPromise) {
    dbPromise = openDB<GelJDB>('gelj', 1, {
      upgrade(d) {
        d.createObjectStore('experiments', { keyPath: 'id' });
        d.createObjectStore('markers', { keyPath: 'id' });
        d.createObjectStore('comparisons', { keyPath: 'id' });
      },
    });
  }
  return dbPromise;
}

const listeners = new Set<() => void>();

/** Subscribe to any change in the database. */
export function onChange(fn: () => void): () => void {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

function changed() {
  listeners.forEach((fn) => fn());
}

export async function listExperiments(): Promise<Experiment[]> {
  const all = await (await db()).getAll('experiments');
  return all.sort((a, b) => b.updatedAt - a.updatedAt);
}

export async function getExperiment(id: string): Promise<Experiment | undefined> {
  return (await db()).get('experiments', id);
}

export async function saveExperiment(e: Experiment): Promise<void> {
  await (await db()).put('experiments', { ...e, updatedAt: Date.now() });
  changed();
}

export async function deleteExperiment(id: string): Promise<void> {
  await (await db()).delete('experiments', id);
  changed();
}

export async function listMarkers(): Promise<Marker[]> {
  const custom = await (await db()).getAll('markers');
  return [...BUILTIN_MARKERS, ...custom.sort((a, b) => a.name.localeCompare(b.name))];
}

export async function saveMarker(m: Marker): Promise<void> {
  if (m.builtin) return;
  await (await db()).put('markers', m);
  changed();
}

export async function deleteMarker(id: string): Promise<void> {
  await (await db()).delete('markers', id);
  changed();
}

export async function listComparisons(): Promise<Comparison[]> {
  const all = await (await db()).getAll('comparisons');
  return all.sort((a, b) => b.createdAt - a.createdAt);
}

export async function saveComparison(c: Comparison): Promise<void> {
  await (await db()).put('comparisons', c);
  changed();
}

export async function deleteComparison(id: string): Promise<void> {
  await (await db()).delete('comparisons', id);
  changed();
}

/** Portable backup of the whole local database. */
export interface Backup {
  format: 'gelj-web';
  version: 1;
  exportedAt: string;
  experiments: Experiment[];
  markers: Marker[];
  comparisons: Comparison[];
}

export async function exportBackup(experimentIds?: string[]): Promise<Backup> {
  const d = await db();
  let experiments = await d.getAll('experiments');
  if (experimentIds) experiments = experiments.filter((e) => experimentIds.includes(e.id));
  const usedMarkers = new Set(experiments.map((e) => e.markerId));
  let markers = await d.getAll('markers');
  if (experimentIds) markers = markers.filter((m) => usedMarkers.has(m.id));
  const comparisons = experimentIds ? [] : await d.getAll('comparisons');
  return { format: 'gelj-web', version: 1, exportedAt: new Date().toISOString(), experiments, markers, comparisons };
}

export async function importBackup(data: unknown): Promise<{ experiments: number; markers: number; comparisons: number }> {
  const b = data as Partial<Backup>;
  if (!b || b.format !== 'gelj-web') throw new Error('This file is not a GelJ Web export.');
  const d = await db();
  const tx = d.transaction(['experiments', 'markers', 'comparisons'], 'readwrite');
  for (const e of b.experiments ?? []) await tx.objectStore('experiments').put(e);
  for (const m of b.markers ?? []) if (!m.builtin) await tx.objectStore('markers').put(m);
  for (const c of b.comparisons ?? []) await tx.objectStore('comparisons').put(c);
  await tx.done;
  changed();
  return {
    experiments: b.experiments?.length ?? 0,
    markers: b.markers?.length ?? 0,
    comparisons: b.comparisons?.length ?? 0,
  };
}
