// Image filters of GelJ's "Other operations" step (ImageJ's Subtract Background, Gamma and Median).
import { createImage } from './image';
import type { GrayImage } from './types';

/**
 * Shrink factor applied before rolling the ball. ImageJ uses 1/2/4/8 for radii up to 10/30/100/more;
 * we shrink earlier (the ball never exceeds 8 px on the shrunk image) to keep large gels interactive.
 */
export function shrinkFactor(radius: number): number {
  return radius <= 4 ? 1 : radius <= 16 ? 2 : radius <= 64 ? 4 : 8;
}

/** Block minimum (ImageJ shrinks the image this way before rolling the ball). */
function shrink(src: Float32Array, w: number, h: number, f: number): { data: Float32Array; w: number; h: number } {
  if (f === 1) return { data: src, w, h };
  const sw = Math.ceil(w / f);
  const sh = Math.ceil(h / f);
  const out = new Float32Array(sw * sh);
  for (let sy = 0; sy < sh; sy++) {
    for (let sx = 0; sx < sw; sx++) {
      let m = Infinity;
      for (let y = sy * f; y < Math.min(h, (sy + 1) * f); y++) {
        for (let x = sx * f; x < Math.min(w, (sx + 1) * f); x++) m = Math.min(m, src[y * w + x]);
      }
      out[sy * sw + sx] = m;
    }
  }
  return { data: out, w: sw, h: sh };
}

/**
 * Grey-scale opening with a ball-shaped structuring element of radius `r` (pixels and intensity in the
 * same units): the surface reached by the top of a ball rolled under the intensity landscape.
 */
function rollBall(src: Float32Array, w: number, h: number, r: number): Float32Array {
  const ri = Math.max(1, Math.floor(r));
  // Ball heights, one row of the structuring element per dy.
  const rows: { dy: number; hw: number; z: Float32Array }[] = [];
  for (let dy = -ri; dy <= ri; dy++) {
    const hw = Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
    const z = new Float32Array(2 * hw + 1);
    for (let dx = -hw; dx <= hw; dx++) z[dx + hw] = Math.sqrt(Math.max(0, r * r - dx * dx - dy * dy));
    rows.push({ dy, hw, z });
  }
  const pass = (input: Float32Array, erode: boolean): Float32Array => {
    const out = new Float32Array(w * h).fill(erode ? Infinity : -Infinity);
    for (let y = 0; y < h; y++) {
      const o = y * w;
      for (const { dy, hw, z } of rows) {
        const yy = y + dy;
        if (yy < 0 || yy >= h) continue;
        const ii = yy * w;
        for (let x = 0; x < w; x++) {
          const lo = Math.max(-hw, -x);
          const hi = Math.min(hw, w - 1 - x);
          let v = out[o + x];
          if (erode) {
            for (let dx = lo; dx <= hi; dx++) {
              const p = input[ii + x + dx] - z[dx + hw];
              if (p < v) v = p;
            }
          } else {
            for (let dx = lo; dx <= hi; dx++) {
              const p = input[ii + x + dx] + z[dx + hw];
              if (p > v) v = p;
            }
          }
          out[o + x] = v;
        }
      }
    }
    return out;
  };
  return pass(pass(src, true), false);
}

/** Bilinear enlargement of a shrunk background to w x h. */
function enlarge(small: Float32Array, sw: number, sh: number, w: number, h: number, f: number): Float32Array {
  if (f === 1) return small;
  const out = new Float32Array(w * h);
  for (let y = 0; y < h; y++) {
    const fy = Math.min(sh - 1, Math.max(0, (y + 0.5) / f - 0.5));
    const y0 = Math.floor(fy);
    const y1 = Math.min(sh - 1, y0 + 1);
    const dy = fy - y0;
    for (let x = 0; x < w; x++) {
      const fx = Math.min(sw - 1, Math.max(0, (x + 0.5) / f - 0.5));
      const x0 = Math.floor(fx);
      const x1 = Math.min(sw - 1, x0 + 1);
      const dx = fx - x0;
      const a = small[y0 * sw + x0];
      const b = small[y0 * sw + x1];
      const c = small[y1 * sw + x0];
      const d = small[y1 * sw + x1];
      out[y * w + x] = a * (1 - dx) * (1 - dy) + b * dx * (1 - dy) + c * (1 - dx) * dy + d * dx * dy;
    }
  }
  return out;
}

/**
 * Rolling-ball background (Sternberg, as ImageJ's "Subtract Background"). For a light background the
 * image is processed inverted. Returns the background in the same orientation as the image.
 */
export function rollingBallBackground(img: GrayImage, radius: number, lightBackground: boolean): GrayImage {
  const { width: w, height: h } = img;
  const src = new Float32Array(w * h);
  for (let i = 0; i < src.length; i++) src[i] = lightBackground ? 255 - img.data[i] : img.data[i];
  const f = shrinkFactor(radius);
  const small = shrink(src, w, h, f);
  const bg = enlarge(rollBall(small.data, small.w, small.h, radius / f), small.w, small.h, w, h, f);
  const out = createImage(w, h);
  for (let i = 0; i < bg.length; i++) {
    // The interpolated background must never exceed the image (ImageJ enforces the same).
    const v = Math.min(bg[i], src[i]);
    out.data[i] = lightBackground ? 255 - v : v;
  }
  return out;
}

/** ImageJ "Subtract Background" with the rolling-ball algorithm. */
export function subtractBackground(img: GrayImage, radius: number, lightBackground: boolean): GrayImage {
  const bg = rollingBallBackground(img, radius, lightBackground);
  const out = createImage(img.width, img.height);
  for (let i = 0; i < out.data.length; i++) {
    out.data[i] = lightBackground ? 255 - (bg.data[i] - img.data[i]) : img.data[i] - bg.data[i];
  }
  return out;
}

/** ImageJ "Gamma": v' = 255 * (v/255)^gamma. */
export function gamma(img: GrayImage, g: number): GrayImage {
  const lut = new Uint8ClampedArray(256);
  for (let v = 0; v < 256; v++) lut[v] = Math.round(255 * Math.pow(v / 255, g));
  const out = createImage(img.width, img.height);
  for (let i = 0; i < out.data.length; i++) out.data[i] = lut[img.data[i]];
  return out;
}

/** Median filter over a (2r+1)x(2r+1) square, edges replicated. Removes speckle noise and dust. */
export function median(img: GrayImage, r = 1): GrayImage {
  const { width: w, height: h } = img;
  const out = createImage(w, h);
  const win: number[] = [];
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      win.length = 0;
      for (let dy = -r; dy <= r; dy++) {
        const yy = Math.min(h - 1, Math.max(0, y + dy));
        for (let dx = -r; dx <= r; dx++) {
          const xx = Math.min(w - 1, Math.max(0, x + dx));
          win.push(img.data[yy * w + xx]);
        }
      }
      win.sort((a, b) => a - b);
      out.data[y * w + x] = win[win.length >> 1];
    }
  }
  return out;
}
