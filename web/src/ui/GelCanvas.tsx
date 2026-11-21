import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import type { GrayImage } from '../core/types';
import { grayToCanvas } from './imageio';

export interface Point {
  x: number;
  y: number;
}

export type PointerKind = 'down' | 'move' | 'up' | 'double';

interface Props {
  image: GrayImage;
  /** Draw overlays; the context is already transformed to image coordinates. */
  draw?: (ctx: CanvasRenderingContext2D, scale: number) => void;
  onPointer?: (kind: PointerKind, p: Point, e: React.PointerEvent | React.MouseEvent, scale: number) => void;
  cursor?: string;
  zoom: number;
}

/** Scrollable canvas that shows a greyscale image scaled to fit, plus interactive overlays. */
export function GelCanvas({ image, draw, onPointer, cursor, zoom }: Props) {
  const hostRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [hostSize, setHostSize] = useState({ w: 800, h: 600 });
  const bitmap = useMemo(() => grayToCanvas(image), [image]);

  useLayoutEffect(() => {
    const el = hostRef.current;
    if (!el) return;
    const ro = new ResizeObserver(() => setHostSize({ w: el.clientWidth, h: el.clientHeight }));
    ro.observe(el);
    setHostSize({ w: el.clientWidth, h: el.clientHeight });
    return () => ro.disconnect();
  }, []);

  const fit = Math.min((hostSize.w - 24) / image.width, (hostSize.h - 24) / image.height);
  const scale = Math.max(0.05, (fit > 0 ? fit : 1) * zoom);
  const cssW = Math.round(image.width * scale);
  const cssH = Math.round(image.height * scale);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const dpr = window.devicePixelRatio || 1;
    canvas.width = Math.round(cssW * dpr);
    canvas.height = Math.round(cssH * dpr);
    const ctx = canvas.getContext('2d')!;
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.imageSmoothingEnabled = scale < 1;
    ctx.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
    ctx.setTransform(scale * dpr, 0, 0, scale * dpr, 0, 0);
    draw?.(ctx, scale);
  });

  const toImage = (e: React.PointerEvent | React.MouseEvent): Point => {
    const r = canvasRef.current!.getBoundingClientRect();
    return { x: (e.clientX - r.left) / scale, y: (e.clientY - r.top) / scale };
  };

  return (
    <div className="canvas-host" ref={hostRef}>
      <canvas
        ref={canvasRef}
        style={{ width: cssW, height: cssH, cursor }}
        onPointerDown={(e) => {
          (e.target as HTMLCanvasElement).setPointerCapture(e.pointerId);
          onPointer?.('down', toImage(e), e, scale);
        }}
        onPointerMove={(e) => onPointer?.('move', toImage(e), e, scale)}
        onPointerUp={(e) => onPointer?.('up', toImage(e), e, scale)}
        onDoubleClick={(e) => onPointer?.('double', toImage(e), e, scale)}
      />
    </div>
  );
}
