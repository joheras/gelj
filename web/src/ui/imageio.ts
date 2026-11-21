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

/** PNG data URLs of images already encoded or decoded (images are never mutated once created). */
const encoded = new WeakMap<GrayImage, string>();

/** Synchronous RGB PNG (via canvas); prefer `grayToPng` where an async call is possible. */
export function grayToDataURL(img: GrayImage): string {
  let url = encoded.get(img);
  if (!url) {
    url = grayToCanvas(img).toDataURL('image/png');
    encoded.set(img, url);
  }
  return url;
}

const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();

function pngChunk(type: string, data: Uint8Array): Uint8Array {
  const out = new Uint8Array(12 + data.length);
  const view = new DataView(out.buffer);
  view.setUint32(0, data.length);
  for (let i = 0; i < 4; i++) out[4 + i] = type.charCodeAt(i);
  out.set(data, 8);
  let crc = 0xffffffff;
  for (let i = 4; i < 8 + data.length; i++) crc = CRC_TABLE[(crc ^ out[i]) & 255] ^ (crc >>> 8);
  view.setUint32(8 + data.length, (crc ^ 0xffffffff) >>> 0);
  return out;
}

function bytesToBase64(bytes: Uint8Array): string {
  let bin = '';
  for (let i = 0; i < bytes.length; i += 0x8000) bin += String.fromCharCode(...bytes.subarray(i, i + 0x8000));
  return btoa(bin);
}

/**
 * 8-bit greyscale PNG data URL. About half the size of the RGB PNG produced by a canvas, which matters
 * because images are stored as data URLs in IndexedDB and in backups. Cached like `grayToDataURL`.
 */
export async function grayToPng(img: GrayImage): Promise<string> {
  const hit = encoded.get(img);
  if (hit) return hit;
  if (typeof CompressionStream === 'undefined') return grayToDataURL(img);
  const { width: w, height: h, data } = img;
  // Each row: filter type 1 (Sub), then the difference with the pixel on the left.
  const raw = new Uint8Array((w + 1) * h);
  for (let y = 0, o = 0; y < h; y++) {
    raw[o++] = 1;
    let left = 0;
    for (let x = 0, i = y * w; x < w; x++, i++) {
      raw[o++] = (data[i] - left) & 255;
      left = data[i];
    }
  }
  const zlib = new Uint8Array(await new Response(new Blob([raw]).stream().pipeThrough(new CompressionStream('deflate'))).arrayBuffer());
  const ihdr = new Uint8Array(13);
  new DataView(ihdr.buffer).setUint32(0, w);
  new DataView(ihdr.buffer).setUint32(4, h);
  ihdr.set([8, 0, 0, 0, 0], 8); // 8 bits, greyscale, deflate, adaptive filtering, no interlace
  const parts = [new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]), pngChunk('IHDR', ihdr), pngChunk('IDAT', zlib), pngChunk('IEND', new Uint8Array(0))];
  const png = new Uint8Array(parts.reduce((n, p) => n + p.length, 0));
  let o = 0;
  for (const p of parts) {
    png.set(p, o);
    o += p.length;
  }
  const url = `data:image/png;base64,${bytesToBase64(png)}`;
  encoded.set(img, url);
  return url;
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
  const img = drawableToGray(el, el.naturalWidth, el.naturalHeight);
  encoded.set(img, url);
  return img;
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
