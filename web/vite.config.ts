import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

// `base: './'` makes the build work under any GitHub Pages path (https://<user>.github.io/<repo>/).
export default defineConfig({
  base: './',
  plugins: [react()],
  test: {
    include: ['src/**/*.test.ts'],
  },
});
