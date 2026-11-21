// Browser image I/O: decode files to greyscale, encode greyscale to PNG data URLs.
import UTIF from 'utif';
import { createImage, fromRGBA, toRGBA } from '../core/image';
import type { GrayImage } from '../core/types';

function canvasFor(w: number, h: number): HTMLCanvasElement {
  const c = document.createElement('canvas');
  c.width = w;
  c.height = h;
  return c;
}

export function grayToCanvas(img: GrayImage): HTMLCanvasElement {
  const c = canvasFor(img.width, img.height);
  c.getContext('2d')!.putImageData(new ImageData(toRGBA(img), img.width, img.height), 0, 0);
  return c;
}

export function grayToDataURL(img: GrayImage): string {
  return grayToCanvas(img).toDataURL('image/png');
}

function loadHtmlImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const el = new Image();
    el.onload = () => resolve(el);
    el.onerror = () => reject(new Error('Could not decode image'));
    el.src = src;
  });
}

function drawableToGray(el: CanvasImageSource, w: number, h: number): GrayImage {
  const c = canvasFor(w, h);
  const ctx = c.getContext('2d', { willReadFrequently: true })!;
  ctx.drawImage(el, 0, 0);
  return fromRGBA(ctx.getImageData(0, 0, w, h).data, w, h);
}

export async function dataURLToGray(url: string): Promise<GrayImage> {
  const el = await loadHtmlImage(url);
  return drawableToGray(el, el.naturalWidth, el.naturalHeight);
}

function decodeTiff(buf: ArrayBuffer): GrayImage {
  const ifds = UTIF.decode(buf);
  if (ifds.length === 0) throw new Error('Empty TIFF file');
  const ifd = ifds[0];
  UTIF.decodeImage(buf, ifd);
  const w = ifd.width;
  const h = ifd.height;
  const bps = ifd.t258 ? ifd.t258[0] : 1;
  const samples = ifd.t258 ? ifd.t258.length : 1;
  const interp = ifd.t262 ? ifd.t262[0] : 1;
  if (bps === 16 && samples === 1 && (interp === 0 || interp === 1)) {
    // 16-bit greyscale (common for gel scanners): scale the data range to 8 bits.
    const data = ifd.data as Uint8Array;
    const px = new Uint16Array(w * h);
    let min = 65535;
    let max = 0;
    for (let i = 0; i < px.length; i++) {
      const v = data[2 * i] | (data[2 * i + 1] << 8);
      px[i] = v;
      if (v < min) min = v;
      if (v > max) max = v;
    }
    const out = createImage(w, h);
    const scale = max > min ? 255 / (max - min) : 1;
    for (let i = 0; i < px.length; i++) {
      const v = (px[i] - min) * scale;
      out.data[i] = interp === 0 ? 255 - v : v;
    }
    return out;
  }
  return fromRGBA(new Uint8ClampedArray(UTIF.toRGBA8(ifd)), w, h);
}

export async function fileToGray(file: File): Promise<GrayImage> {
  const name = file.name.toLowerCase();
  if (name.endsWith('.tif') || name.endsWith('.tiff') || file.type === 'image/tiff') {
    return decodeTiff(await file.arrayBuffer());
  }
  const bitmap = await createImageBitmap(file);
  try {
    return drawableToGray(bitmap, bitmap.width, bitmap.height);
  } finally {
    bitmap.close();
  }
}

export function downloadText(filename: string, text: string, type = 'application/json') {
  downloadBlob(filename, new Blob([text], { type }));
}

export function downloadBlob(filename: string, blob: Blob) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function safeFilename(s: string): string {
  return s.replace(/[^\w.-]+/g, '_').replace(/^_+|_+$/g, '') || 'gelj';
}
