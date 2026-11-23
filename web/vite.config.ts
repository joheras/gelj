import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';
import { pwa } from './pwa/plugin.js';

// `base: './'` makes the build work under any GitHub Pages path (https://<user>.github.io/<repo>/).
export default defineConfig({
  base: './',
  plugins: [react(), pwa()],
  test: {
    include: ['src/**/*.test.ts'],
  },
});
