// Greyscale image operations (port of the ImageJ operations GelJ relies on).
import type { GrayImage, Rect } from './types';

export function createImage(width: number, height: number, fill = 0): GrayImage {
  const data = new Uint8ClampedArray(width * height);
  if (fill) data.fill(fill);
  return { width, height, data };
}

export function cloneImage(img: GrayImage): GrayImage {
  return { width: img.width, height: img.height, data: new Uint8ClampedArray(img.data) };
}

/** RGBA -> 8-bit using ImageJ's default unweighted conversion (r+g+b)/3. */
export function fromRGBA(rgba: Uint8ClampedArray, width: number, height: number): GrayImage {
  const out = createImage(width, height);
  for (let i = 0, j = 0; i < out.data.length; i++, j += 4) {
    out.data[i] = (rgba[j] + rgba[j + 1] + rgba[j + 2]) / 3;
  }
  return out;
}

export function toRGBA(img: GrayImage): Uint8ClampedArray<ArrayBuffer> {
  const out = new Uint8ClampedArray(img.width * img.height * 4);
  for (let i = 0, j = 0; i < img.data.length; i++, j += 4) {
    const v = img.data[i];
    out[j] = v;
    out[j + 1] = v;
    out[j + 2] = v;
    out[j + 3] = 255;
  }
  return out;
}

export function invert(img: GrayImage): GrayImage {
  const out = cloneImage(img);
  for (let i = 0; i < out.data.length; i++) out.data[i] = 255 - out.data[i];
  return out;
}

export function crop(img: GrayImage, r: Rect): GrayImage {
  const x0 = Math.max(0, Math.round(r.x));
  const y0 = Math.max(0, Math.round(r.y));
  const x1 = Math.min(img.width, Math.round(r.x + r.w));
  const y1 = Math.min(img.height, Math.round(r.y + r.h));
  const w = Math.max(1, x1 - x0);
  const h = Math.max(1, y1 - y0);
  const out = createImage(w, h);
  for (let y = 0; y < h; y++) {
    const src = (y0 + y) * img.width + x0;
    out.data.set(img.data.subarray(src, src + w), y * w);
  }
  return out;
}

export function flipHorizontal(img: GrayImage): GrayImage {
  const out = createImage(img.width, img.height);
  for (let y = 0; y < img.height; y++) {
    for (let x = 0; x < img.width; x++) {
      out.data[y * img.width + x] = img.data[y * img.width + (img.width - 1 - x)];
    }
  }
  return out;
}

export function flipVertical(img: GrayImage): GrayImage {
  const out = createImage(img.width, img.height);
  for (let y = 0; y < img.height; y++) {
    const src = (img.height - 1 - y) * img.width;
    out.data.set(img.data.subarray(src, src + img.width), y * img.width);
  }
  return out;
}

/** Rotate 90 degrees; `left` = counter-clockwise. */
export function rotate90(img: GrayImage, left: boolean): GrayImage {
  const w = img.height;
  const h = img.width;
  const out = createImage(w, h);
  for (let y = 0; y < img.height; y++) {
    for (let x = 0; x < img.width; x++) {
      const v = img.data[y * img.width + x];
      const nx = left ? y : img.height - 1 - y;
      const ny = left ? img.width - 1 - x : x;
      out.data[ny * w + nx] = v;
    }
  }
  return out;
}

export function histogram(img: GrayImage): number[] {
  const h = new Array<number>(256).fill(0);
  for (let i = 0; i < img.data.length; i++) h[img.data[i]]++;
  return h;
}

/** Linear map of [min,max] onto [0,255]. */
export function applyDisplayRange(img: GrayImage, min: number, max: number): GrayImage {
  const out = createImage(img.width, img.height);
  const scale = max > min ? 255 / (max - min) : 1;
  for (let i = 0; i < img.data.length; i++) {
    out.data[i] = (img.data[i] - min) * scale;
  }
  return out;
}

/**
 * ImageJ "Enhance Contrast" (saturated=0.35): stretch the histogram so that
 * the given percentage of pixels saturate (split between both ends).
 */
export function enhanceContrast(img: GrayImage, saturated = 0.35): GrayImage {
  const hist = histogram(img);
  const total = img.data.length;
  const threshold = Math.floor((total * saturated) / 200);
  let lo = 0;
  let count = 0;
  for (; lo < 255; lo++) {
    count += hist[lo];
    if (count > threshold) break;
  }
  let hi = 255;
  count = 0;
  for (; hi > 0; hi--) {
    count += hist[hi];
    if (count > threshold) break;
  }
  if (hi <= lo) return cloneImage(img);
  return applyDisplayRange(img, lo, hi);
}

/**
 * Brightness/contrast as in ImageJ's B&C dialog, both in [-100, 100]. 0/0 is identity.
 */
export function brightnessContrast(img: GrayImage, brightness: number, contrast: number): GrayImage {
  const centre = 127.5 - (brightness / 100) * 127.5;
  const slope = contrast >= 0 ? 1 / (1 - Math.min(contrast, 99) / 100) : 1 + contrast / 100;
  const half = 127.5 / slope;
  return applyDisplayRange(img, centre - half, centre + half);
}

/** Bilinear resize. */
export function resize(img: GrayImage, w: number, h: number): GrayImage {
  const out = createImage(w, h);
  const sx = img.width / w;
  const sy = img.height / h;
  for (let y = 0; y < h; y++) {
    const fy = Math.min(img.height - 1, Math.max(0, (y + 0.5) * sy - 0.5));
    const y0 = Math.floor(fy);
    const y1 = Math.min(img.height - 1, y0 + 1);
    const dy = fy - y0;
    for (let x = 0; x < w; x++) {
      const fx = Math.min(img.width - 1, Math.max(0, (x + 0.5) * sx - 0.5));
      const x0 = Math.floor(fx);
      const x1 = Math.min(img.width - 1, x0 + 1);
      const dx = fx - x0;
      const a = img.data[y0 * img.width + x0];
      const b = img.data[y0 * img.width + x1];
      const c = img.data[y1 * img.width + x0];
      const d = img.data[y1 * img.width + x1];
      out.data[y * w + x] = a * (1 - dx) * (1 - dy) + b * dx * (1 - dy) + c * (1 - dx) * dy + d * dx * dy;
    }
  }
  return out;
}

/** Mean intensity of each column inside `r` (horizontal profile). */
export function columnProfile(img: GrayImage, r: Rect, signal: (v: number) => number = (v) => v): number[] {
  const x0 = Math.max(0, Math.floor(r.x));
  const x1 = Math.min(img.width, Math.floor(r.x + r.w));
  const y0 = Math.max(0, Math.floor(r.y));
  const y1 = Math.min(img.height, Math.floor(r.y + r.h));
  const out: number[] = [];
  for (let x = x0; x < x1; x++) {
    let s = 0;
    for (let y = y0; y < y1; y++) s += signal(img.data[y * img.width + x]);
    out.push(y1 > y0 ? s / (y1 - y0) : 0);
  }
  return out;
}

/** Mean intensity of each row inside `r` (vertical profile, as used for lanes). */
export function rowProfile(img: GrayImage, r: Rect, signal: (v: number) => number = (v) => v): number[] {
  const x0 = Math.max(0, Math.floor(r.x));
  const x1 = Math.min(img.width, Math.floor(r.x + r.w));
  const y0 = Math.max(0, Math.floor(r.y));
  const y1 = Math.min(img.height, Math.floor(r.y + r.h));
  const out: number[] = [];
  for (let y = y0; y < y1; y++) {
    let s = 0;
    for (let x = x0; x < x1; x++) s += signal(img.data[y * img.width + x]);
    out.push(x1 > x0 ? s / (x1 - x0) : 0);
  }
  return out;
}

/** Signal transform so that bands always have high values. */
export function signalFn(darkBackground: boolean): (v: number) => number {
  return darkBackground ? (v) => v : (v) => 255 - v;
}
