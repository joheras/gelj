// Pure helpers of the printable analysis reports.
import type { FitResult } from './curvefit';
import type { PreprocessStep } from './types';

/** Bytes encoded in a base64 data URL (e.g. the PNG of an image stored by GelJ Web). */
export function dataUrlBytes(url: string): Uint8Array {
  const comma = url.indexOf(',');
  if (!url.startsWith('data:') || comma < 0) throw new Error('Not a data URL');
  const header = url.slice(5, comma);
  const body = url.slice(comma + 1);
  if (!header.endsWith(';base64')) return new TextEncoder().encode(decodeURIComponent(body));
  const bin = atob(body);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

/** Lower-case hexadecimal SHA-256 of `bytes`; null where Web Crypto is unavailable (non-secure contexts). */
export async function sha256Hex(bytes: Uint8Array): Promise<string | null> {
  const subtle = globalThis.crypto?.subtle;
  if (!subtle) return null;
  const digest = await subtle.digest('SHA-256', bytes as Uint8Array<ArrayBuffer>);
  return Array.from(new Uint8Array(digest), (b) => b.toString(16).padStart(2, '0')).join('');
}

/** SHA-256 of the file stored in a data URL. */
export function sha256OfDataUrl(url: string): Promise<string | null> {
  return sha256Hex(dataUrlBytes(url));
}

const fmt = (v: number | string | boolean) => (typeof v === 'number' ? String(Number(v.toPrecision(6))) : String(v));

/** Human-readable description of a recorded preprocessing operation. */
export function describeStep(s: PreprocessStep): string {
  const p = s.params ?? {};
  switch (s.op) {
    case 'crop':
      return `Crop to the rectangle x = ${fmt(p.x)}, y = ${fmt(p.y)}, width ${fmt(p.w)}, height ${fmt(p.h)} px`;
    case 'invert':
      return 'Invert';
    case 'autoContrast':
      return `Automatic brightness/contrast (${fmt(p.saturated ?? 0.35)}% saturated pixels)`;
    case 'brightnessContrast':
      return `Brightness ${fmt(p.brightness ?? 0)}, contrast ${fmt(p.contrast ?? 0)}`;
    case 'flipHorizontal':
      return 'Flip horizontally';
    case 'flipVertical':
      return 'Flip vertically';
    case 'rotateLeft':
      return 'Rotate 90° left';
    case 'rotateRight':
      return 'Rotate 90° right';
    case 'subtractBackground':
      return `Subtract background (rolling ball, radius ${fmt(p.radius)} px, ${p.lightBackground ? 'light' : 'dark'} background)`;
    case 'gamma':
      return `Gamma ${fmt(p.gamma)}`;
    case 'median':
      return `Median filter (${fmt(p.size ?? 3)}×${fmt(p.size ?? 3)})`;
    default: {
      const args = Object.entries(p).map(([k, v]) => `${k} = ${fmt(v)}`);
      return args.length ? `${s.op} (${args.join(', ')})` : s.op;
    }
  }
}

/** A number with 6 significant digits, in exponential notation when very large or small. */
export function formatNumber(v: number): string {
  if (!Number.isFinite(v)) return String(v);
  const a = Math.abs(v);
  return a !== 0 && (a >= 1e6 || a < 1e-3) ? v.toExponential(5) : String(Number(v.toPrecision(6)));
}

/** Parameters of a fitted model as "a = …, b = …". */
export function fitParameters(fit: FitResult): string {
  return fit.params.map((v, i) => `${'abcdefghi'[i]} = ${formatNumber(v)}`).join(', ');
}

/** Local date and time as "2026-11-23 09:41 (UTC+01:00)": unambiguous in any locale. */
export function formatDateTime(d: Date, seconds = false): string {
  const p = (n: number) => String(Math.abs(n)).padStart(2, '0');
  const off = -d.getTimezoneOffset();
  const zone = `UTC${off >= 0 ? '+' : '-'}${p(Math.trunc(off / 60))}:${p(off % 60)}`;
  const time = `${p(d.getHours())}:${p(d.getMinutes())}${seconds ? `:${p(d.getSeconds())}` : ''}`;
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${time} (${zone})`;
}

/** Splits `items` in consecutive blocks of at most `size` elements (to print wide matrices on several pages). */
export function chunk<T>(items: T[], size: number): T[][] {
  const out: T[][] = [];
  for (let i = 0; i < items.length; i += Math.max(1, size)) out.push(items.slice(i, i + Math.max(1, size)));
  return out;
}
