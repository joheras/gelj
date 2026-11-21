import { useRef, useState } from 'react';
import { BUILTIN_MARKERS } from '../core/markers';
import { SAMPLE_PROFILES, syntheticGel } from '../core/synthetic';
import type { Experiment, Marker } from '../core/types';
import { deleteExperiment, exportBackup, importBackup } from '../storage/db';
import { downloadText, fileToGray, safeFilename } from './imageio';
import { newWork, type WorkState } from './experimentio';

interface Props {
  experiments: Experiment[];
  markers: Marker[];
  onNew: (work: WorkState) => void;
  onOpen: (e: Experiment) => void;
  notify: (msg: string) => void;
}

export function ExperimentsPage({ experiments, markers, onNew, onOpen, notify }: Props) {
  const fileRef = useRef<HTMLInputElement>(null);
  const importRef = useRef<HTMLInputElement>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const openImage = async (file: File) => {
    setError(null);
    setLoading(true);
    try {
      const img = await fileToGray(file);
      onNew(newWork(img, file.name.replace(/\.[^.]+$/, '')));
    } catch (e) {
      setError(`Could not open ${file.name}: ${(e as Error).message}`);
    } finally {
      setLoading(false);
    }
  };

  const sample = () => {
    const gel = syntheticGel(BUILTIN_MARKERS[0], SAMPLE_PROFILES, { seed: Date.now() % 1000 });
    onNew(newWork(gel.image, 'Sample gel'));
  };

  const importFile = async (file: File) => {
    setError(null);
    try {
      const res = await importBackup(JSON.parse(await file.text()));
      notify(`Imported ${res.experiments} experiments, ${res.markers} markers and ${res.comparisons} comparisons.`);
    } catch (e) {
      setError(`Import failed: ${(e as Error).message}`);
    }
  };

  const exportAll = async () => {
    const data = await exportBackup();
    downloadText(`gelj-backup-${new Date().toISOString().slice(0, 10)}.json`, JSON.stringify(data));
  };

  const exportOne = async (e: Experiment) => {
    const data = await exportBackup([e.id]);
    downloadText(`${safeFilename(e.name)}.gelj.json`, JSON.stringify(data));
  };

  const markerName = (id: string | null) => markers.find((m) => m.id === id)?.name ?? '—';

  return (
    <div className="page">
      <div className="page-header">
        <h2>Experiments</h2>
        <div className="row">
          <button className="primary" onClick={() => fileRef.current?.click()} disabled={loading}>
            {loading ? 'Opening…' : 'New experiment from image'}
          </button>
          <button onClick={sample}>Try a sample gel</button>
          <button onClick={() => importRef.current?.click()}>Import</button>
          <button onClick={exportAll} disabled={experiments.length === 0}>
            Export all
          </button>
        </div>
        <input
          ref={fileRef}
          type="file"
          hidden
          accept="image/*,.tif,.tiff"
          onChange={(e) => {
            const f = e.target.files?.[0];
            e.target.value = '';
            if (f) openImage(f);
          }}
        />
        <input
          ref={importRef}
          type="file"
          hidden
          accept=".json,application/json"
          onChange={(e) => {
            const f = e.target.files?.[0];
            e.target.value = '';
            if (f) importFile(f);
          }}
        />
      </div>
      {error && <div className="alert error" style={{ marginBottom: 12 }}>{error}</div>}
      {experiments.length === 0 ? (
        <div
          className="card empty"
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault();
            const f = e.dataTransfer.files[0];
            if (f) openImage(f);
          }}
        >
          <h3>No experiments yet</h3>
          <p>
            Open a gel image (TIFF, PNG, JPEG…) or drop it here to start the analysis assistant, or try the sample gel to see how it works.
          </p>
          <p className="small">Your images and results are stored only in this browser. Use Export to keep a backup.</p>
        </div>
      ) : (
        <div className="exp-grid">
          {experiments.map((e) => (
            <div className="card exp-card" key={e.id}>
              <div className="thumb" onClick={() => onOpen(e)} title="Open">
                <img src={e.image} alt="" />
              </div>
              <div className="body">
                <div className="row">
                  <span className="title" title={e.name}>
                    {e.name}
                  </span>
                  <span className="spacer" />
                  {e.status === 'complete' ? <span className="badge ok">Complete</span> : <span className="badge warn">Draft</span>}
                </div>
                <div className="small muted">
                  {e.lanes.length} lanes · {markerName(e.markerId)}
                  <br />
                  Updated {new Date(e.updatedAt).toLocaleString()}
                </div>
                <div className="row">
                  <button className="primary" onClick={() => onOpen(e)}>
                    {e.status === 'complete' ? 'Open' : 'Resume'}
                  </button>
                  <button onClick={() => exportOne(e)}>Export</button>
                  <span className="spacer" />
                  <button
                    className="danger"
                    onClick={async () => {
                      if (window.confirm(`Delete "${e.name}"? This cannot be undone.`)) await deleteExperiment(e.id);
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
