import { useEffect, useState } from 'react';
import { dataUrlBytes, sha256Hex } from '../core/report';

export interface ReportImage {
  label: string;
  /** Data URL of the image as stored by GelJ Web. */
  url: string;
  width: number;
  height: number;
}

export interface Checksum {
  bytes: number;
  sha256: string | null;
}

/** SHA-256 of every image (keyed by data URL); null until computed. */
export function useChecksums(images: ReportImage[]): Map<string, Checksum> | null {
  const [sums, setSums] = useState<{ key: string; map: Map<string, Checksum> } | null>(null);
  const key = images.map((i) => i.url.length + ':' + i.url.slice(-24)).join('|');
  useEffect(() => {
    let alive = true;
    (async () => {
      const map = new Map<string, Checksum>();
      for (const img of images) {
        if (map.has(img.url)) continue;
        const bytes = dataUrlBytes(img.url);
        map.set(img.url, { bytes: bytes.length, sha256: await sha256Hex(bytes) });
      }
      if (alive) setSums({ key, map });
    })();
    return () => {
      alive = false;
    };
    // `key` identifies the list of images.
  }, [key]); // eslint-disable-line react-hooks/exhaustive-deps
  return sums && sums.key === key ? sums.map : null;
}
