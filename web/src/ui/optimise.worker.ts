// Runs the tolerance/optimisation scan off the main thread, posting every point as it is computed.
import { evaluatePoint, prepareScan, scanGrid, type ScanOptions, type ScanPoint } from '../core/optimise';
import type { ComparableLane } from '../core/similarity';

export interface ScanRequest {
  lanes: ComparableLane[];
  options: ScanOptions;
}

export type ScanMessage =
  | { type: 'point'; point: ScanPoint; done: number; total: number }
  | { type: 'done' }
  | { type: 'error'; message: string };

self.onmessage = (e: MessageEvent<ScanRequest>) => {
  // `self` is typed as a Window by the DOM library; in a worker postMessage takes the message only.
  const post = (m: ScanMessage) => (self as unknown as Worker).postMessage(m);
  try {
    const ctx = prepareScan(e.data.lanes, e.data.options);
    const grid = scanGrid(e.data.options);
    grid.forEach(([t, o], k) => post({ type: 'point', point: evaluatePoint(ctx, t, o), done: k + 1, total: grid.length }));
    post({ type: 'done' });
  } catch (err) {
    post({ type: 'error', message: (err as Error).message });
  }
};
