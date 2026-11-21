// Analysis assistant, following GelJ's four-step workflow:
// 1 pre-processing, 2 lane detection, 3 normalisation (markers), 4 band detection.
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { fitCurve, FIT_MODELS } from '../core/curvefit';
import { detectBands, detectLanes, laneProfile, makeLanes, suggestThreshold } from '../core/detect';
import {
  brightnessContrast,
  crop,
  enhanceContrast,
  flipHorizontal,
  flipVertical,
  invert,
  rotate90,
} from '../core/image';
import { NormalisationError, normaliseLanes, validateReferences } from '../core/normalise';
import type { GrayImage, Lane, Marker, Rect } from '../core/types';
import { EMPTY_META } from '../core/types';
import { uid } from '../core/uid';
import { listMarkers, saveExperiment, saveMarker } from '../storage/db';
import { CurveChart, ProfileChart } from './Charts';
import { encodeNormalised, experimentFromWork, type WorkState } from './experimentio';
import { GelCanvas, type Point, type PointerKind } from './GelCanvas';

const STEPS = [
  { id: 'crop', group: 'Step 1. Image preprocessing', label: '1.1 Crop' },
  { id: 'invert', group: 'Step 1. Image preprocessing', label: '1.2 Invert' },
  { id: 'contrast', group: 'Step 1. Image preprocessing', label: '1.3 Adjust contrast' },
  { id: 'other', group: 'Step 1. Image preprocessing', label: '1.4 Other operations' },
  { id: 'background', group: 'Step 2. Lane detection', label: '2.1 Background' },
  { id: 'lanes', group: 'Step 2. Lane detection', label: '2.2 Lane edition' },
  { id: 'references', group: 'Step 2. Lane detection', label: '2.3 Reference lanes' },
  { id: 'markerThreshold', group: 'Step 3. Normalisation', label: '3.1 Markers threshold' },
  { id: 'markerBands', group: 'Step 3. Normalisation', label: '3.2 Markers picking' },
  { id: 'markerModel', group: 'Step 3. Normalisation', label: '3.3 Migration model' },
  { id: 'bandThreshold', group: 'Step 4. Band detection', label: '4.1 Global threshold' },
  { id: 'laneThreshold', group: 'Step 4. Band detection', label: '4.2 Lane threshold' },
  { id: 'bandPicking', group: 'Step 4. Band detection', label: '4.3 Band picking' },
  { id: 'details', group: 'Step 5. Finish', label: '5.1 Lane details' },
] as const;

type StepId = (typeof STEPS)[number]['id'];

export const STEP_COUNT = STEPS.length;

const BAND_COLOURS: [string, string][] = [
  ['Blue', '#2f7dff'],
  ['Red', '#ff3b30'],
  ['Green', '#34c759'],
  ['Magenta', '#ff2dd6'],
  ['Cyan', '#32d2f5'],
  ['Yellow', '#ffd60a'],
  ['Orange', '#ff9500'],
];

const REF_COLOUR = '#ff9f1a';
const LANE_COLOUR = '#4c93ff';
const SELECT_COLOUR = '#ffffff';

type Drag =
  | { kind: 'crop'; start: Point }
  | { kind: 'newLane'; start: Point }
  | { kind: 'moveLane'; id: string; start: Point; orig: Rect }
  | { kind: 'resize'; id: string; edge: 'l' | 'r' | 't' | 'b'; start: Point; orig: Rect }
  | { kind: 'band'; laneId: string; index: number };

interface Selection {
  laneId: string | null;
  band: number | null;
}

function sortLanes(lanes: Lane[]): Lane[] {
  return lanes.slice().sort((a, b) => a.rect.x - b.rect.x);
}

function normRect(a: Point, b: Point): Rect {
  return { x: Math.min(a.x, b.x), y: Math.min(a.y, b.y), w: Math.abs(a.x - b.x), h: Math.abs(a.y - b.y) };
}

function clampRect(r: Rect, img: GrayImage): Rect {
  const x = Math.max(0, Math.min(img.width - 2, Math.round(r.x)));
  const y = Math.max(0, Math.min(img.height - 2, Math.round(r.y)));
  const w = Math.max(2, Math.min(img.width - x, Math.round(r.w)));
  const h = Math.max(2, Math.min(img.height - y, Math.round(r.h)));
  return { x, y, w, h };
}

function isTyping(e: KeyboardEvent): boolean {
  const t = e.target as HTMLElement | null;
  return !!t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.tagName === 'SELECT' || t.isContentEditable);
}

interface Props {
  initial: WorkState;
  startStep: number;
  onClose: (savedId?: string) => void;
}

export function Wizard({ initial, startStep, onClose }: Props) {
  const [work, setWork] = useState<WorkState>(initial);
  const [stepIndex, setStepIndex] = useState(startStep);
  const [maxStep, setMaxStep] = useState(Math.max(startStep, initial.lanes.length ? 6 : 0));
  const [imageUndo, setImageUndo] = useState<GrayImage[]>([]);
  const [past, setPast] = useState<Lane[][]>([]);
  const [future, setFuture] = useState<Lane[][]>([]);
  const [sel, setSel] = useState<Selection>({ laneId: null, band: null });
  const [addMode, setAddMode] = useState(false);
  const [cropRect, setCropRect] = useState<Rect | null>(null);
  const [zoom, setZoom] = useState(1);
  const [bc, setBc] = useState({ brightness: 0, contrast: 0 });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [peakTolerance, setPeakTolerance] = useState(2);
  const [markers, setMarkers] = useState<Marker[]>([]);
  const [markerMode, setMarkerMode] = useState<'existing' | 'new'>('existing');
  const [chosenMarker, setChosenMarker] = useState<string | null>(initial.markerId);
  const [markerPick, setMarkerPick] = useState<boolean[]>([]);
  const [newMarker, setNewMarker] = useState({ name: '', mw: [] as string[], fitMethod: 2 });
  const drag = useRef<Drag | null>(null);
  const dragSnapshot = useRef<Lane[] | null>(null);

  const step: StepId = STEPS[stepIndex].id;
  const { image, lanes, darkBackground } = work;

  useEffect(() => {
    listMarkers().then(setMarkers);
  }, []);

  // ---- Lane history ----------------------------------------------------------------
  const pushHistory = (before: Lane[]) => {
    setPast((p) => [...p.slice(-99), before]);
    setFuture([]);
  };

  const commitLanes = (next: Lane[]) => {
    pushHistory(work.lanes);
    setWork((w) => ({ ...w, lanes: next }));
  };

  const undo = () => {
    if (!past.length) return;
    const prev = past[past.length - 1];
    setPast(past.slice(0, -1));
    setFuture([work.lanes, ...future]);
    setWork((w) => ({ ...w, lanes: prev }));
  };

  const redo = () => {
    if (!future.length) return;
    const next = future[0];
    setFuture(future.slice(1));
    setPast([...past, work.lanes]);
    setWork((w) => ({ ...w, lanes: next }));
  };

  // ---- Derived data -----------------------------------------------------------------
  const profiles = useMemo(() => {
    const m = new Map<string, number[]>();
    for (const l of lanes) m.set(l.id, laneProfile(image, l.rect, darkBackground));
    return m;
  }, [image, lanes, darkBackground]);

  const refs = useMemo(() => sortLanes(lanes.filter((l) => l.isReference)), [lanes]);
  const samples = useMemo(() => sortLanes(lanes.filter((l) => !l.isReference)), [lanes]);
  const selectedLane = lanes.find((l) => l.id === sel.laneId) ?? null;
  const markerBandCount = refs[0]?.bands.length ?? 0;

  const displayImage = useMemo(() => {
    if (step === 'contrast' && (bc.brightness !== 0 || bc.contrast !== 0)) {
      return brightnessContrast(image, bc.brightness, bc.contrast);
    }
    return image;
  }, [step, image, bc]);

  const detectFor = useCallback(
    (ls: Lane[], which: (l: Lane) => boolean): Lane[] =>
      ls.map((l) =>
        which(l) ? { ...l, bands: detectBands(laneProfile(image, l.rect, darkBackground), l.threshold, peakTolerance) } : l,
      ),
    [image, darkBackground, peakTolerance],
  );

  // ---- Image operations (step 1) ------------------------------------------------------
  const applyImageOp = (op: (img: GrayImage) => GrayImage, geometric: boolean) => {
    if (geometric && lanes.length > 0) {
      if (!window.confirm('Changing the geometry of the image removes the detected lanes and bands. Continue?')) return;
    }
    setImageUndo((u) => [...u.slice(-19), image]);
    setWork((w) => ({ ...w, image: op(w.image), lanes: geometric ? [] : w.lanes }));
    if (geometric) {
      setPast([]);
      setFuture([]);
      setSel({ laneId: null, band: null });
    }
  };

  const undoImage = () => {
    const prev = imageUndo[imageUndo.length - 1];
    if (!prev) return;
    setImageUndo((u) => u.slice(0, -1));
    setWork((w) => ({
      ...w,
      image: prev,
      lanes: prev.width === w.image.width && prev.height === w.image.height ? w.lanes : [],
    }));
  };

  // ---- Navigation ---------------------------------------------------------------------
  const enterStep = (index: number, w: WorkState): WorkState => {
    const id = STEPS[index].id;
    let next = w;
    if (id === 'lanes' && w.lanes.length === 0) {
      next = { ...next, lanes: makeLanes(detectLanes(w.image, w.darkBackground)) };
      setPast([]);
      setFuture([]);
    }
    if (id === 'markerThreshold') {
      const rs = next.lanes.filter((l) => l.isReference);
      if (rs.every((l) => l.bands.length === 0)) {
        const t = suggestThreshold(rs.map((l) => laneProfile(w.image, l.rect, w.darkBackground)));
        next = { ...next, lanes: detectFor(next.lanes.map((l) => (l.isReference ? { ...l, threshold: t } : l)), (l) => l.isReference) };
      }
    }
    if (id === 'bandThreshold') {
      const ss = next.lanes.filter((l) => !l.isReference);
      if (ss.every((l) => l.bands.length === 0)) {
        const t = suggestThreshold(ss.map((l) => laneProfile(w.image, l.rect, w.darkBackground)));
        next = { ...next, lanes: detectFor(next.lanes.map((l) => (!l.isReference ? { ...l, threshold: t } : l)), (l) => !l.isReference) };
      }
    }
    if (id === 'markerModel') {
      const k = next.lanes.find((l) => l.isReference)?.bands.length ?? 0;
      setNewMarker((m) => ({
        ...m,
        mw: Array.from({ length: k }, (_, i) => m.mw[i] ?? (next.referenceMw[i] !== undefined ? String(next.referenceMw[i]) : '')),
        fitMethod: k >= 5 ? 2 : k >= 3 ? 1 : 0,
      }));
    }
    return next;
  };

  /** Validates the current step before moving forward. Returns an error message or null. */
  const leaveStep = (): string | null | Promise<string | null> => {
    switch (step) {
      case 'lanes':
        if (lanes.length === 0) return 'Add at least one lane.';
        setWork((w) => ({ ...w, lanes: renumber(sortLanes(w.lanes)) }));
        return null;
      case 'references':
        return refs.length === 0 ? 'Select at least one reference (marker) lane.' : null;
      case 'markerBands':
        try {
          validateReferences(lanes);
        } catch (e) {
          return (e as Error).message;
        }
        return null;
      case 'markerModel':
        return chooseMarker();
      default:
        return null;
    }
  };

  const chooseMarker = async (): Promise<string | null> => {
    const k = markerBandCount;
    if (markerMode === 'existing') {
      const m = markers.find((x) => x.id === chosenMarker);
      if (!m) return 'Select a marker or define a new one.';
      const picked = m.mw.filter((_, i) => markerPick[i]);
      if (picked.length !== k) return `Select exactly ${k} bands of the marker (one for each reference band).`;
      setWork((w) => ({ ...w, markerId: m.id, referenceMw: picked }));
      return null;
    }
    const name = newMarker.name.trim();
    if (!name) return 'Give the new marker a name.';
    if (markers.some((m) => m.name === name)) return `There is already a marker called ${name}.`;
    const mw = newMarker.mw.map(Number);
    if (mw.length !== k || mw.some((v) => !Number.isFinite(v) || v <= 0)) return 'Enter a positive molecular weight for every band.';
    for (let i = 0; i < mw.length - 1; i++) if (mw[i] < mw[i + 1]) return 'Molecular weights must be sorted from top (largest) to bottom.';
    const ref = refs[0];
    const marker: Marker = {
      id: uid(),
      name,
      mw,
      refWidth: ref.rect.w,
      refHeight: ref.rect.h,
      refPositions: ref.bands.slice(),
      fitMethod: newMarker.fitMethod,
    };
    await saveMarker(marker);
    setMarkers(await listMarkers());
    setChosenMarker(marker.id);
    setMarkerMode('existing');
    setMarkerPick(mw.map(() => true));
    setWork((w) => ({ ...w, markerId: marker.id, referenceMw: mw }));
    return null;
  };

  const goTo = async (index: number) => {
    if (index === stepIndex) return;
    setError(null);
    if (index > stepIndex) {
      // Steps after the current one were validated when they were first completed.
      const pending = leaveStep();
      const err = pending instanceof Promise ? await pending : pending;
      if (err) {
        setError(err);
        return;
      }
    }
    setWork((w) => enterStep(index, w));
    setSel({ laneId: null, band: null });
    setAddMode(false);
    setCropRect(null);
    setBc({ brightness: 0, contrast: 0 });
    setStepIndex(index);
    setMaxStep((m) => Math.max(m, index));
  };

  const saveDraft = async () => {
    setBusy(true);
    try {
      await saveExperiment(experimentFromWork(work, 'draft', stepIndex));
      onClose(work.id);
    } finally {
      setBusy(false);
    }
  };

  const finish = async () => {
    setError(null);
    const marker = markers.find((m) => m.id === work.markerId);
    if (!marker) {
      setError('Choose a marker in step 3.3.');
      return;
    }
    setBusy(true);
    try {
      const ordered = sortLanes(work.lanes);
      const data = normaliseLanes(work.image, work.darkBackground, ordered, marker, work.referenceMw);
      const exp = experimentFromWork({ ...work, lanes: ordered }, 'complete', stepIndex, encodeNormalised(data));
      await saveExperiment(exp);
      onClose(exp.id);
    } catch (e) {
      setError(e instanceof NormalisationError ? e.message : `Normalisation failed: ${(e as Error).message}`);
    } finally {
      setBusy(false);
    }
  };

  // ---- Editing helpers ------------------------------------------------------------------
  const bandEditable = (l: Lane) => (step === 'markerBands' ? l.isReference : step === 'bandPicking' ? !l.isReference : false);

  const deleteSelected = () => {
    if (step === 'lanes' && sel.laneId) {
      commitLanes(lanes.filter((l) => l.id !== sel.laneId));
      setSel({ laneId: null, band: null });
    } else if (sel.laneId && sel.band !== null && selectedLane && bandEditable(selectedLane)) {
      commitLanes(lanes.map((l) => (l.id === sel.laneId ? { ...l, bands: l.bands.filter((_, i) => i !== sel.band) } : l)));
      setSel({ laneId: sel.laneId, band: null });
    }
  };

  const toggleReference = (id: string | null) => {
    if (!id) return;
    commitLanes(lanes.map((l) => (l.id === id ? { ...l, isReference: !l.isReference, bands: [] } : l)));
  };

  const setThreshold = (ids: Set<string>, t: number) => {
    setWork((w) => ({
      ...w,
      lanes: w.lanes.map((l) =>
        ids.has(l.id)
          ? { ...l, threshold: t, bands: detectBands(laneProfile(w.image, l.rect, w.darkBackground), t, peakTolerance) }
          : l,
      ),
    }));
  };

  // ---- Keyboard shortcuts ---------------------------------------------------------------------
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (isTyping(e)) return;
      const k = e.key.toLowerCase();
      if ((e.ctrlKey || e.metaKey) && k === 'z') {
        e.preventDefault();
        if (step === 'crop' || step === 'contrast' || step === 'other' || step === 'invert') undoImage();
        else undo();
      } else if (((e.ctrlKey || e.metaKey) && k === 'y') || (k === 'r' && !e.ctrlKey && !e.metaKey)) {
        e.preventDefault();
        redo();
      } else if (k === 'a' && (step === 'lanes' || step === 'markerBands' || step === 'bandPicking')) {
        setAddMode((m) => !m);
      } else if (k === 'delete' || k === 'backspace') {
        e.preventDefault();
        deleteSelected();
      } else if (k === 'f' && step === 'references') {
        toggleReference(sel.laneId);
      } else if (k === 'escape') {
        setAddMode(false);
        setSel({ laneId: null, band: null });
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  });

  // ---- Pointer interaction ----------------------------------------------------------------------
  const hitLane = (p: Point) =>
    lanes.find((l) => p.x >= l.rect.x && p.x <= l.rect.x + l.rect.w && p.y >= l.rect.y && p.y <= l.rect.y + l.rect.h);

  const onPointer = (kind: PointerKind, p: Point, _e: unknown, scale: number) => {
    const tol = 6 / scale;
    if (step === 'crop') {
      if (kind === 'down') {
        drag.current = { kind: 'crop', start: p };
        setCropRect({ x: p.x, y: p.y, w: 0, h: 0 });
      } else if (kind === 'move' && drag.current?.kind === 'crop') {
        setCropRect(normRect(drag.current.start, p));
      } else if (kind === 'up') drag.current = null;
      return;
    }

    if (step === 'lanes') {
      if (kind === 'down') {
        dragSnapshot.current = lanes;
        if (addMode) {
          drag.current = { kind: 'newLane', start: p };
          return;
        }
        const current = selectedLane;
        if (current) {
          const r = current.rect;
          const inY = p.y >= r.y - tol && p.y <= r.y + r.h + tol;
          const inX = p.x >= r.x - tol && p.x <= r.x + r.w + tol;
          let edge: 'l' | 'r' | 't' | 'b' | null = null;
          if (inY && Math.abs(p.x - r.x) < tol) edge = 'l';
          else if (inY && Math.abs(p.x - (r.x + r.w)) < tol) edge = 'r';
          else if (inX && Math.abs(p.y - r.y) < tol) edge = 't';
          else if (inX && Math.abs(p.y - (r.y + r.h)) < tol) edge = 'b';
          if (edge) {
            drag.current = { kind: 'resize', id: current.id, edge, start: p, orig: r };
            return;
          }
        }
        const hit = hitLane(p);
        setSel({ laneId: hit?.id ?? null, band: null });
        if (hit) drag.current = { kind: 'moveLane', id: hit.id, start: p, orig: hit.rect };
      } else if (kind === 'move' && drag.current) {
        const d = drag.current;
        if (d.kind === 'moveLane') {
          const r = clampRect({ ...d.orig, x: d.orig.x + p.x - d.start.x, y: d.orig.y + p.y - d.start.y }, image);
          setWork((w) => ({ ...w, lanes: w.lanes.map((l) => (l.id === d.id ? { ...l, rect: r } : l)) }));
        } else if (d.kind === 'resize') {
          const o = d.orig;
          const r =
            d.edge === 'l'
              ? { ...o, x: Math.min(p.x, o.x + o.w - 2), w: o.x + o.w - Math.min(p.x, o.x + o.w - 2) }
              : d.edge === 'r'
                ? { ...o, w: Math.max(2, p.x - o.x) }
                : d.edge === 't'
                  ? { ...o, y: Math.min(p.y, o.y + o.h - 2), h: o.y + o.h - Math.min(p.y, o.y + o.h - 2) }
                  : { ...o, h: Math.max(2, p.y - o.y) };
          setWork((w) => ({ ...w, lanes: w.lanes.map((l) => (l.id === d.id ? { ...l, rect: clampRect(r, image) } : l)) }));
        } else if (d.kind === 'newLane') {
          setCropRect(normRect(d.start, p));
        }
      } else if (kind === 'up' && drag.current) {
        const d = drag.current;
        drag.current = null;
        if (d.kind === 'newLane') {
          const r = normRect(d.start, p);
          setCropRect(null);
          if (r.w > 4 && r.h > 4) {
            const lane: Lane = {
              id: uid(),
              name: `Lane ${lanes.length + 1}`,
              rect: clampRect(r, image),
              isReference: false,
              threshold: 128,
              bands: [],
              meta: { ...EMPTY_META },
            };
            commitLanes([...lanes, lane]);
            setSel({ laneId: lane.id, band: null });
            setAddMode(false);
          }
        } else if (dragSnapshot.current && dragSnapshot.current !== lanes) {
          pushHistory(dragSnapshot.current);
        }
        dragSnapshot.current = null;
      }
      return;
    }

    if (step === 'references') {
      if (kind === 'down') setSel({ laneId: hitLane(p)?.id ?? null, band: null });
      if (kind === 'double') toggleReference(hitLane(p)?.id ?? null);
      return;
    }

    // Band steps.
    if (kind === 'down') {
      const lane = hitLane(p);
      if (!lane) {
        setSel({ laneId: null, band: null });
        return;
      }
      const rel = p.y - lane.rect.y;
      const editable = bandEditable(lane);
      const idx = lane.bands.findIndex((b) => Math.abs(b - rel) < Math.max(3, tol));
      if (editable && idx >= 0) {
        dragSnapshot.current = lanes;
        drag.current = { kind: 'band', laneId: lane.id, index: idx };
        setSel({ laneId: lane.id, band: idx });
      } else if (editable && addMode) {
        addBand(lane, rel);
      } else {
        setSel({ laneId: lane.id, band: null });
      }
    } else if (kind === 'double') {
      const lane = hitLane(p);
      if (lane && bandEditable(lane) && !lane.bands.some((b) => Math.abs(b - (p.y - lane.rect.y)) < 3)) {
        addBand(lane, p.y - lane.rect.y);
      }
    } else if (kind === 'move' && drag.current?.kind === 'band') {
      const d = drag.current;
      setWork((w) => ({
        ...w,
        lanes: w.lanes.map((l) => {
          if (l.id !== d.laneId) return l;
          const bands = l.bands.slice();
          bands[d.index] = Math.max(0, Math.min(l.rect.h - 1, Math.round(p.y - l.rect.y)));
          return { ...l, bands };
        }),
      }));
    } else if (kind === 'up' && drag.current?.kind === 'band') {
      const d = drag.current;
      drag.current = null;
      // Keep bands sorted and the selection on the moved band.
      const lane = lanes.find((l) => l.id === d.laneId);
      if (lane) {
        const value = lane.bands[d.index];
        const sorted = lane.bands.slice().sort((a, b) => a - b);
        setSel({ laneId: d.laneId, band: sorted.indexOf(value) });
        setWork((w) => ({ ...w, lanes: w.lanes.map((l) => (l.id === d.laneId ? { ...l, bands: sorted } : l)) }));
      }
      if (dragSnapshot.current && dragSnapshot.current !== lanes) pushHistory(dragSnapshot.current);
      dragSnapshot.current = null;
    }
  };

  const addBand = (lane: Lane, rel: number) => {
    const y = Math.max(0, Math.min(lane.rect.h - 1, Math.round(rel)));
    const bands = [...lane.bands, y].sort((a, b) => a - b);
    commitLanes(lanes.map((l) => (l.id === lane.id ? { ...l, bands } : l)));
    setSel({ laneId: lane.id, band: bands.indexOf(y) });
  };

  // ---- Drawing ----------------------------------------------------------------------------------
  const draw = (ctx: CanvasRenderingContext2D, scale: number) => {
    const px = 1 / scale;
    if (cropRect && (step === 'crop' || step === 'lanes')) {
      ctx.save();
      ctx.strokeStyle = '#ffd60a';
      ctx.lineWidth = 1.5 * px;
      ctx.setLineDash([6 * px, 4 * px]);
      ctx.strokeRect(cropRect.x, cropRect.y, cropRect.w, cropRect.h);
      ctx.restore();
    }
    if (step === 'crop' || step === 'invert' || step === 'contrast' || step === 'other' || step === 'background') return;

    const bandSteps = !(step === 'lanes' || step === 'references');
    sortLanes(lanes).forEach((l, i) => {
      const selected = l.id === sel.laneId;
      const r = l.rect;
      ctx.strokeStyle = selected ? SELECT_COLOUR : l.isReference ? REF_COLOUR : LANE_COLOUR;
      ctx.lineWidth = (selected ? 2.5 : bandSteps ? 1 : 1.5) * px;
      ctx.globalAlpha = bandSteps && !selected ? 0.6 : 1;
      ctx.strokeRect(r.x, r.y, r.w, r.h);
      ctx.globalAlpha = 1;
      ctx.font = `600 ${12 * px}px system-ui, sans-serif`;
      ctx.textAlign = 'center';
      ctx.fillStyle = l.isReference ? REF_COLOUR : LANE_COLOUR;
      ctx.fillText(l.isReference ? `R` : String(i + 1), r.x + r.w / 2, Math.max(12 * px, r.y - 4 * px));
      if (step === 'lanes' && selected) {
        ctx.fillStyle = SELECT_COLOUR;
        const hs = 6 * px;
        for (const [hx, hy] of [
          [r.x, r.y + r.h / 2],
          [r.x + r.w, r.y + r.h / 2],
          [r.x + r.w / 2, r.y],
          [r.x + r.w / 2, r.y + r.h],
        ]) {
          ctx.fillRect(hx - hs / 2, hy - hs / 2, hs, hs);
        }
      }
      if (bandSteps) {
        const markerStep = step === 'markerThreshold' || step === 'markerBands' || step === 'markerModel';
        const dim = markerStep ? !l.isReference : l.isReference;
        l.bands.forEach((b, bi) => {
          const isSel = selected && sel.band === bi;
          ctx.fillStyle = isSel ? SELECT_COLOUR : l.isReference ? REF_COLOUR : work.bandColour;
          ctx.globalAlpha = dim ? 0.35 : 1;
          const h = Math.max(2, (isSel ? 3 : 2) * px);
          ctx.fillRect(r.x + 1 * px, r.y + b - h / 2, r.w - 2 * px, h);
        });
        ctx.globalAlpha = 1;
      }
    });
  };

  const cursor =
    step === 'crop' || addMode ? 'crosshair' : step === 'lanes' ? 'pointer' : step === 'references' ? 'pointer' : 'default';

  // ---- Side panels ------------------------------------------------------------------------------
  const markerOptions = markers.filter((m) => m.mw.length >= markerBandCount);
  const chosen = markers.find((m) => m.id === chosenMarker) ?? null;

  useEffect(() => {
    if (!chosen) return;
    setMarkerPick((p) =>
      p.length === chosen.mw.length && p.filter(Boolean).length > 0
        ? p
        : chosen.mw.map((_, i) => (chosen.mw.length === markerBandCount ? true : i < markerBandCount)),
    );
  }, [chosen, markerBandCount]);

  const newMarkerFit = useMemo(() => {
    if (step !== 'markerModel' || markerMode !== 'new' || !refs[0]) return null;
    const mw = newMarker.mw.map(Number);
    if (mw.length < 2 || mw.some((v) => !Number.isFinite(v) || v <= 0)) return null;
    return fitCurve(refs[0].bands, mw, newMarker.fitMethod);
  }, [step, markerMode, newMarker, refs]);

  const existingFit = useMemo(
    () => (step === 'markerModel' && chosen ? fitCurve(chosen.refPositions, chosen.mw, chosen.fitMethod) : null),
    [step, chosen],
  );

  const bandColourPicker = (
    <label className="field">
      <span>Band colour</span>
      <select value={work.bandColour} onChange={(e) => setWork((w) => ({ ...w, bandColour: e.target.value }))}>
        {BAND_COLOURS.map(([n, c]) => (
          <option key={c} value={c}>
            {n}
          </option>
        ))}
      </select>
    </label>
  );

  const profilePanel = (lane: Lane | null, showThreshold: boolean) =>
    lane ? (
      <div className="stack">
        <div className="small muted">
          Profile of <b>{lane.name}</b> {lane.isReference && <span className="badge ref">reference</span>} — {lane.bands.length} bands
        </div>
        <ProfileChart
          profile={profiles.get(lane.id) ?? []}
          threshold={showThreshold ? lane.threshold : undefined}
          bands={lane.bands}
          colour={lane.isReference ? REF_COLOUR : work.bandColour}
        />
      </div>
    ) : (
      <p className="small muted">Click a lane to see its intensity profile.</p>
    );

  const thresholdSliders = (ls: Lane[]) => (
    <div className="stack">
      {ls.map((l) => (
        <div className="slider-row" key={l.id}>
          <button className="link small" style={{ justifyContent: 'flex-start' }} onClick={() => setSel({ laneId: l.id, band: null })}>
            {l.name}
          </button>
          <input
            type="range"
            min={1}
            max={255}
            value={l.threshold}
            aria-label={`Threshold of ${l.name}`}
            onChange={(e) => setThreshold(new Set([l.id]), Number(e.target.value))}
            onPointerDown={() => setSel({ laneId: l.id, band: null })}
          />
          <span className="value small">{l.bands.length}b</span>
        </div>
      ))}
    </div>
  );

  const editTools = (what: 'band' | 'lane') => (
    <div className="row">
      <button className={`toggle ${addMode ? 'on' : ''}`} onClick={() => setAddMode((m) => !m)}>
        Add {what} <span className="kbd">a</span>
      </button>
      <button onClick={deleteSelected} disabled={!sel.laneId || (what === 'band' && sel.band === null)}>
        Delete {what} <span className="kbd">Del</span>
      </button>
      <button onClick={undo} disabled={!past.length}>
        Undo <span className="kbd">Ctrl+Z</span>
      </button>
      <button onClick={redo} disabled={!future.length}>
        Redo <span className="kbd">R</span>
      </button>
    </div>
  );

  let panel: React.ReactNode = null;
  switch (step) {
    case 'crop':
      panel = (
        <>
          <p className="instructions">
            Drag on the image to select the region to keep, then press <b>Crop</b>. Cropping away labels and empty borders improves lane
            detection.
          </p>
          <div className="row">
            <button className="primary" disabled={!cropRect || cropRect.w < 4 || cropRect.h < 4} onClick={() => {
              applyImageOp((img) => crop(img, cropRect!), true);
              setCropRect(null);
            }}>
              Crop
            </button>
            <button onClick={undoImage} disabled={!imageUndo.length}>
              Undo
            </button>
          </div>
        </>
      );
      break;
    case 'invert':
      panel = (
        <>
          <p className="instructions">Invert the colours of the image if needed (for instance, to work with a negative).</p>
          <div className="row">
            <button onClick={() => applyImageOp(invert, false)}>Invert</button>
            <button onClick={undoImage} disabled={!imageUndo.length}>
              Undo
            </button>
          </div>
        </>
      );
      break;
    case 'contrast':
      panel = (
        <>
          <p className="instructions">
            Automatically stretch the brightness and contrast (0.35% saturated pixels), or adjust them manually and press <b>Apply</b>.
          </p>
          <div className="row">
            <button onClick={() => applyImageOp((img) => enhanceContrast(img, 0.35), false)}>Auto adjust brightness/contrast</button>
          </div>
          <div className="stack">
            <div className="slider-row">
              <span className="small">Brightness</span>
              <input type="range" min={-100} max={100} value={bc.brightness} onChange={(e) => setBc({ ...bc, brightness: Number(e.target.value) })} />
              <span className="value small">{bc.brightness}</span>
            </div>
            <div className="slider-row">
              <span className="small">Contrast</span>
              <input type="range" min={-100} max={95} value={bc.contrast} onChange={(e) => setBc({ ...bc, contrast: Number(e.target.value) })} />
              <span className="value small">{bc.contrast}</span>
            </div>
            <div className="row">
              <button
                className="primary"
                disabled={bc.brightness === 0 && bc.contrast === 0}
                onClick={() => {
                  applyImageOp((img) => brightnessContrast(img, bc.brightness, bc.contrast), false);
                  setBc({ brightness: 0, contrast: 0 });
                }}
              >
                Apply
              </button>
              <button onClick={() => setBc({ brightness: 0, contrast: 0 })}>Reset</button>
              <button onClick={undoImage} disabled={!imageUndo.length}>
                Undo
              </button>
            </div>
          </div>
        </>
      );
      break;
    case 'other':
      panel = (
        <>
          <p className="instructions">Flip or rotate the image so that lanes are vertical and samples migrate from top to bottom.</p>
          <div className="row">
            <button onClick={() => applyImageOp(flipHorizontal, true)}>Flip horizontally</button>
            <button onClick={() => applyImageOp(flipVertical, true)}>Flip vertically</button>
            <button onClick={() => applyImageOp((i) => rotate90(i, true), true)}>Rotate 90° left</button>
            <button onClick={() => applyImageOp((i) => rotate90(i, false), true)}>Rotate 90° right</button>
            <button onClick={undoImage} disabled={!imageUndo.length}>
              Undo
            </button>
          </div>
          <button className="link small" style={{ alignSelf: 'flex-start' }} onClick={() => {
            if (window.confirm('Discard every change made to the image?')) applyImageOp(() => work.original, true);
          }}>
            Revert to the original image
          </button>
        </>
      );
      break;
    case 'background':
      panel = (
        <>
          <p className="instructions">Indicate whether the background of the image is dark (bright bands) or light (dark bands).</p>
          <div className="stack">
            <label className="check">
              <input
                type="radio"
                checked={darkBackground}
                onChange={() => setWork((w) => ({ ...w, darkBackground: true, lanes: w.darkBackground ? w.lanes : [] }))}
              />
              Dark background
            </label>
            <label className="check">
              <input
                type="radio"
                checked={!darkBackground}
                onChange={() => setWork((w) => ({ ...w, darkBackground: false, lanes: w.darkBackground ? [] : w.lanes }))}
              />
              Light background
            </label>
          </div>
          {lanes.length > 0 && <p className="small muted">Lanes are already defined; changing the background detects them again.</p>}
        </>
      );
      break;
    case 'lanes':
      panel = (
        <>
          <p className="instructions">
            The lanes have been detected automatically. Click a lane to select it, drag it to move it and drag its edges to resize it.
            To add a lane, press <span className="kbd">a</span> and drag a rectangle on the image.
          </p>
          {editTools('lane')}
          <div className="row">
            <button
              onClick={() => {
                if (lanes.length && !window.confirm('Replace the current lanes with automatically detected ones?')) return;
                commitLanes(makeLanes(detectLanes(image, darkBackground)));
              }}
            >
              Detect lanes again
            </button>
            <span className="muted small">{lanes.length} lanes</span>
          </div>
          {selectedLane && (
            <div className="stack">
              <label className="field">
                <span>Name of the selected lane</span>
                <input
                  type="text"
                  value={selectedLane.name}
                  onChange={(e) => setWork((w) => ({ ...w, lanes: w.lanes.map((l) => (l.id === selectedLane.id ? { ...l, name: e.target.value } : l)) }))}
                />
              </label>
              <button
                onClick={() => {
                  const r = selectedLane.rect;
                  commitLanes(lanes.map((l) => ({ ...l, rect: clampRect({ ...l.rect, y: r.y, h: r.h }, image) })));
                }}
              >
                Use this lane&apos;s height for every lane
              </button>
            </div>
          )}
        </>
      );
      break;
    case 'references':
      panel = (
        <>
          <p className="instructions">
            Fix the reference lanes, i.e. the lanes that contain the molecular-weight marker. At least one is required. Select a lane and
            press <span className="kbd">f</span> (or double-click it).
          </p>
          <div className="row">
            <button className="primary" disabled={!selectedLane} onClick={() => toggleReference(sel.laneId)}>
              {selectedLane?.isReference ? 'Unset reference lane' : 'Fix reference lane'} <span className="kbd">f</span>
            </button>
          </div>
          <div className="stack small">
            {sortLanes(lanes).map((l) => (
              <label className="check" key={l.id}>
                <input type="checkbox" checked={l.isReference} onChange={() => toggleReference(l.id)} />
                {l.name}
              </label>
            ))}
          </div>
        </>
      );
      break;
    case 'markerThreshold':
      panel = (
        <>
          <p className="instructions">
            The bands of each marker (reference lane) are detected automatically. Move the threshold of each marker until its bands are
            found; you can fine-tune them in the next step.
          </p>
          {thresholdSliders(refs)}
          {profilePanel(selectedLane?.isReference ? selectedLane : refs[0] ?? null, true)}
          <details>
            <summary className="small muted">Advanced</summary>
            <div className="slider-row" style={{ marginTop: 8 }}>
              <span className="small">Peak tolerance</span>
              <input type="range" min={0} max={20} step={0.5} value={peakTolerance} onChange={(e) => setPeakTolerance(Number(e.target.value))} />
              <span className="value small">{peakTolerance}</span>
            </div>
            <button className="small" style={{ marginTop: 6 }} onClick={() => setWork((w) => ({ ...w, lanes: detectFor(w.lanes, (l) => l.isReference) }))}>
              Detect marker bands again
            </button>
          </details>
        </>
      );
      break;
    case 'markerBands': {
      const counts = refs.map((r) => r.bands.length);
      const same = counts.every((c) => c === counts[0]);
      panel = (
        <>
          <p className="instructions">
            Pick the bands of the markers manually. Click a band to select it and drag it to move it; double-click (or press{' '}
            <span className="kbd">a</span> and click) to add one. All the markers must have the same number of bands.
          </p>
          {editTools('band')}
          <div className={`alert ${same && counts[0] >= 2 ? '' : 'warn'} small`}>
            {refs.map((r) => `${r.name}: ${r.bands.length}`).join(' · ')}
            {!same && ' — the counts differ'}
          </div>
          {profilePanel(selectedLane?.isReference ? selectedLane : refs[0] ?? null, false)}
        </>
      );
      break;
    }
    case 'markerModel':
      panel = (
        <>
          <p className="instructions">
            Choose the molecular-weight marker used in the reference lanes, or define a new one by entering the weight of every band from
            top to bottom. The migration model converts band positions into molecular weights.
          </p>
          <div className="row">
            <label className="check">
              <input type="radio" checked={markerMode === 'existing'} onChange={() => setMarkerMode('existing')} />
              Use available marker
            </label>
            <label className="check">
              <input type="radio" checked={markerMode === 'new'} onChange={() => setMarkerMode('new')} />
              New marker
            </label>
          </div>
          {markerMode === 'existing' ? (
            <div className="stack">
              <select
                value={chosenMarker ?? ''}
                onChange={(e) => {
                  setChosenMarker(e.target.value || null);
                  setMarkerPick([]);
                }}
              >
                <option value="">— Select a marker —</option>
                {markerOptions.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.name} ({m.mw.length} bands)
                  </option>
                ))}
              </select>
              {markerOptions.length < markers.length && (
                <span className="small muted">Markers with fewer than {markerBandCount} bands are hidden.</span>
              )}
              {chosen && (
                <>
                  <span className="small">
                    Tick the {markerBandCount} marker bands visible in the reference lanes ({markerPick.filter(Boolean).length} selected):
                  </span>
                  <div className="row small">
                    {chosen.mw.map((mw, i) => (
                      <label className="check" key={i} style={{ minWidth: 70 }}>
                        <input
                          type="checkbox"
                          checked={!!markerPick[i]}
                          onChange={(e) => setMarkerPick((p) => chosen.mw.map((_, j) => (j === i ? e.target.checked : !!p[j])))}
                        />
                        {mw}
                      </label>
                    ))}
                  </div>
                  <CurveChart x={chosen.refPositions} y={chosen.mw} fit={existingFit} />
                  <span className="small muted">
                    Model: {existingFit?.model.name} — R² = {existingFit?.rSquared.toFixed(4)}
                  </span>
                </>
              )}
            </div>
          ) : (
            <div className="stack">
              <label className="field">
                <span>Marker name</span>
                <input type="text" value={newMarker.name} onChange={(e) => setNewMarker({ ...newMarker, name: e.target.value })} />
              </label>
              <div className="mw-inputs small">
                {newMarker.mw.map((v, i) => (
                  <MwInput
                    key={i}
                    index={i}
                    value={v}
                    onChange={(val) => setNewMarker((m) => ({ ...m, mw: m.mw.map((x, j) => (j === i ? val : x)) }))}
                  />
                ))}
              </div>
              <label className="field">
                <span>Migration model</span>
                <select value={newMarker.fitMethod} onChange={(e) => setNewMarker({ ...newMarker, fitMethod: Number(e.target.value) })}>
                  {FIT_MODELS.map((m) => (
                    <option key={m.id} value={m.id} disabled={m.nParams > markerBandCount}>
                      {m.name}
                    </option>
                  ))}
                </select>
              </label>
              {newMarkerFit && refs[0] && (
                <>
                  <CurveChart x={refs[0].bands} y={newMarker.mw.map(Number)} fit={newMarkerFit} />
                  <span className="small muted">
                    {newMarkerFit.model.formula} — R² = {newMarkerFit.rSquared.toFixed(4)}
                  </span>
                </>
              )}
            </div>
          )}
        </>
      );
      break;
    case 'bandThreshold': {
      const avg = samples.length ? Math.round(samples.reduce((s, l) => s + l.threshold, 0) / samples.length) : 128;
      panel = (
        <>
          <p className="instructions">
            Fix a global threshold to automatically locate the bands of every non-marker lane. Bands are local maxima of the lane profile
            above the threshold.
          </p>
          <div className="slider-row">
            <span className="small">Threshold</span>
            <input
              type="range"
              min={1}
              max={255}
              value={avg}
              onChange={(e) => setThreshold(new Set(samples.map((l) => l.id)), Number(e.target.value))}
            />
            <span className="value small">{avg}</span>
          </div>
          {bandColourPicker}
          {profilePanel(selectedLane && !selectedLane.isReference ? selectedLane : samples[0] ?? null, true)}
          <details>
            <summary className="small muted">Advanced</summary>
            <div className="slider-row" style={{ marginTop: 8 }}>
              <span className="small">Peak tolerance</span>
              <input type="range" min={0} max={20} step={0.5} value={peakTolerance} onChange={(e) => setPeakTolerance(Number(e.target.value))} />
              <span className="value small">{peakTolerance}</span>
            </div>
            <button className="small" style={{ marginTop: 6 }} onClick={() => setWork((w) => ({ ...w, lanes: detectFor(w.lanes, (l) => !l.isReference) }))}>
              Detect bands again
            </button>
          </details>
        </>
      );
      break;
    }
    case 'laneThreshold':
      panel = (
        <>
          <p className="instructions">Fine-tune the threshold of each lane.</p>
          {thresholdSliders(samples)}
          {profilePanel(selectedLane && !selectedLane.isReference ? selectedLane : samples[0] ?? null, true)}
        </>
      );
      break;
    case 'bandPicking':
      panel = (
        <>
          <p className="instructions">
            Pick the bands of the non-marker lanes manually. Click a band to select it and drag it to move it; double-click (or press{' '}
            <span className="kbd">a</span> and click) to add one.
          </p>
          {editTools('band')}
          {bandColourPicker}
          {profilePanel(selectedLane, false)}
        </>
      );
      break;
    case 'details':
      panel = (
        <>
          <label className="field">
            <span>Experiment name</span>
            <input type="text" value={work.name} onChange={(e) => setWork((w) => ({ ...w, name: e.target.value }))} />
          </label>
          <label className="field">
            <span>Comments</span>
            <textarea rows={2} value={work.comments} onChange={(e) => setWork((w) => ({ ...w, comments: e.target.value }))} />
          </label>
          <p className="small muted">
            Fill in the lane names and metadata in the table below the gel; they are used to label dendrograms and can be edited later.
          </p>
        </>
      );
      break;
  }

  const last = stepIndex === STEPS.length - 1;
  let lastGroup = '';

  return (
    <div className="wizard">
      <div className="stage">
        <div className="stage-toolbar">
          <b>{work.name || 'Untitled experiment'}</b>
          <span className="muted small">
            {image.width}×{image.height}px
          </span>
          <span className="spacer" />
          <button onClick={() => setZoom((z) => Math.max(0.25, z / 1.25))} aria-label="Zoom out">
            −
          </button>
          <button onClick={() => setZoom(1)}>Fit</button>
          <button onClick={() => setZoom((z) => Math.min(8, z * 1.25))} aria-label="Zoom in">
            +
          </button>
          <span className="muted small" style={{ width: 44, textAlign: 'right' }}>
            {Math.round(zoom * 100)}%
          </span>
        </div>
        <GelCanvas image={displayImage} draw={draw} onPointer={onPointer} cursor={cursor} zoom={zoom} />
        {step === 'details' && (
          <div className="stage-bottom">
            <LaneMetaEditor lanes={sortLanes(lanes)} onChange={(next) => setWork((w) => ({ ...w, lanes: next }))} />
          </div>
        )}
      </div>
      <aside className="side">
        <div className="side-scroll">
          <ul className="steps">
            {STEPS.map((s, i) => {
              const header = s.group !== lastGroup ? s.group : null;
              lastGroup = s.group;
              const expanded = s.group === STEPS[stepIndex].group;
              const reachable = i <= maxStep || i === stepIndex + 1;
              return (
                <li key={s.id}>
                  {header && (
                    <button
                      className="group"
                      disabled={expanded || !reachable}
                      onClick={() => goTo(i)}
                      title={expanded ? undefined : `Go to ${s.label}`}
                    >
                      {header}
                    </button>
                  )}
                  {expanded && (
                    <button className={i === stepIndex ? 'current' : i <= maxStep ? 'done' : ''} disabled={!reachable} onClick={() => goTo(i)}>
                      {s.label}
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
          <h3 style={{ margin: 0 }}>{STEPS[stepIndex].label}</h3>
          {panel}
          {error && <div className="alert error small">{error}</div>}
        </div>
        <div className="side-footer">
          <button onClick={() => goTo(stepIndex - 1)} disabled={stepIndex === 0 || busy}>
            ‹ Back
          </button>
          <button
            onClick={() => {
              if (window.confirm('Close the assistant? Unsaved changes will be lost (use "Save draft" to keep them).')) onClose();
            }}
            disabled={busy}
          >
            Cancel
          </button>
          <button onClick={saveDraft} disabled={busy} title="Save the unfinished analysis to resume it later">
            Save draft
          </button>
          <span className="spacer" />
          {last ? (
            <button className="primary" onClick={finish} disabled={busy}>
              {busy ? 'Normalising…' : 'Finish'}
            </button>
          ) : (
            <button className="primary" onClick={() => goTo(stepIndex + 1)} disabled={busy}>
              Next ›
            </button>
          )}
        </div>
      </aside>
    </div>
  );
}

function renumber(lanes: Lane[]): Lane[] {
  return lanes.map((l, i) => (/^Lane \d+$/.test(l.name) ? { ...l, name: `Lane ${i + 1}` } : l));
}

function MwInput({ index, value, onChange }: { index: number; value: string; onChange: (v: string) => void }) {
  return (
    <>
      <span>Band {index + 1}</span>
      <input type="number" min={0} step="any" value={value} onChange={(e) => onChange(e.target.value)} />
    </>
  );
}

const META_FIELDS: [keyof Lane['meta'], string][] = [
  ['strain', 'Strain number'],
  ['genus', 'Genus'],
  ['species', 'Species'],
  ['country', 'Country'],
  ['mlst', 'MLST'],
  ['comments', 'Comments'],
];

export function LaneMetaEditor({ lanes, onChange }: { lanes: Lane[]; onChange: (lanes: Lane[]) => void }) {
  const update = (id: string, patch: Partial<Lane>) => onChange(lanes.map((l) => (l.id === id ? { ...l, ...patch } : l)));
  return (
    <div className="table-wrap">
      <table className="grid small">
        <thead>
          <tr>
            <th>Lane</th>
            {META_FIELDS.map(([, label]) => (
              <th key={label}>{label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {lanes.map((l) => (
            <tr key={l.id}>
              <td style={{ minWidth: 130 }}>
                <div className="row" style={{ flexWrap: 'nowrap', gap: 4 }}>
                  <input type="text" value={l.name} onChange={(e) => update(l.id, { name: e.target.value })} aria-label="Lane name" />
                  {l.isReference && <span className="badge ref">ref</span>}
                </div>
              </td>
              {META_FIELDS.map(([key, label]) => (
                <td key={key} style={{ minWidth: 100 }}>
                  <input
                    type="text"
                    value={l.meta[key]}
                    aria-label={label}
                    onChange={(e) => update(l.id, { meta: { ...l.meta, [key]: e.target.value } })}
                  />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
