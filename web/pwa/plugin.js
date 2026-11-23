import { createHash } from 'node:crypto';
import { readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { join, relative, sep } from 'node:path';

const SW_FILE = 'sw.js';
const TEMPLATE = new URL('./sw.js', import.meta.url);

/** @param {string} dir @returns {string[]} */
function listFiles(dir) {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? listFiles(path) : [path];
  });
}

/**
 * Writes dist/sw.js after the build: the template in pwa/sw.js with the list of every file of the build
 * (precached for offline use) and a version derived from their contents, so any change to the app yields
 * a new service worker and therefore an update prompt. Build only: the dev server never sees a service worker.
 * Plain JavaScript (typed in plugin.d.ts) so that it needs no Node.js type definitions.
 * @returns {import('vite').Plugin}
 */
export function pwa() {
  /** @type {import('vite').ResolvedConfig} */
  let config;
  return {
    name: 'gelj-pwa',
    apply: 'build',
    configResolved(c) {
      config = c;
    },
    closeBundle() {
      const outDir = config.build.outDir;
      const files = listFiles(outDir)
        .map((f) => relative(outDir, f).split(sep).join('/'))
        .filter((f) => f !== SW_FILE && !f.endsWith('.map'))
        .sort();
      const hash = createHash('sha256');
      for (const f of files) hash.update(f).update(readFileSync(join(outDir, f)));
      const version = hash.digest('hex').slice(0, 12);
      const sw = readFileSync(TEMPLATE, 'utf8')
        .replace('__VERSION__', version)
        .replace('__PRECACHE__', JSON.stringify(files, null, 2));
      writeFileSync(join(outDir, SW_FILE), sw);
      config.logger.info(`gelj-pwa: ${SW_FILE} (version ${version}, ${files.length} files precached)`);
    },
  };
}
