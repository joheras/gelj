import { version } from '../../package.json';

/** Software identification printed in the analysis reports. */
export const BUILD = {
  name: 'GelJ Web',
  version,
  /** Short git commit, or '' when the build does not know it (local builds). */
  commit: __GELJ_COMMIT__.slice(0, 7),
  date: __GELJ_BUILD_DATE__,
  url: 'https://joheras.github.io/gelj/',
  source: 'https://github.com/joheras/gelj',
};

export function buildLabel(): string {
  const parts = [`${BUILD.name} ${BUILD.version}`];
  parts.push(BUILD.commit ? `commit ${BUILD.commit}` : 'local build');
  return parts.join(', ');
}
