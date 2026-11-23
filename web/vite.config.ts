import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';
import { pwa } from './pwa/plugin.js';

// Commit and date of the build, shown in the analysis reports. GITHUB_SHA is set by GitHub Actions.
const env = (globalThis as { process?: { env: Record<string, string | undefined> } }).process?.env ?? {};

// `base: './'` makes the build work under any GitHub Pages path (https://<user>.github.io/<repo>/).
export default defineConfig({
  base: './',
  plugins: [react(), pwa()],
  define: {
    __GELJ_COMMIT__: JSON.stringify(env.GITHUB_SHA ?? env.GELJ_COMMIT ?? ''),
    __GELJ_BUILD_DATE__: JSON.stringify(new Date().toISOString()),
  },
  test: {
    include: ['src/**/*.test.ts'],
  },
});
