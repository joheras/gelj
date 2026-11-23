# GelJ Web

Browser version of **GelJ**, a tool for analysing DNA fingerprint gel images. It runs entirely in the
user's browser (no server): images and results are kept in the browser's IndexedDB and can be exported
as JSON backups. It is designed to be hosted as a static site on GitHub Pages.

## Features

- **Assistant** with GelJ's four steps:
  1. Pre-processing: crop, invert, automatic/manual brightness and contrast, flip and rotate, background
     subtraction (rolling ball, dark or light background), gamma and 3×3 median filter.
  2. Lane detection from the minima of the horizontal intensity profile, with manual editing
     (add, move, resize, delete) and selection of reference (marker) lanes.
  3. Normalisation: marker band detection (per-marker threshold) and manual picking, choice of an existing
     marker or definition of a new one, and migration model (ImageJ curve-fitter models). Marker
     positions are interpolated across the gel and every lane is warped onto the marker reference with a
     cubic spline.
  4. Band detection (global and per-lane threshold) and manual band picking.
- Drafts can be saved and resumed; completed experiments can be re-edited.
- **Comparison** of lanes from any experiments: Dice, Jaccard, band difference, Jeffrey's X and Ochiai
  (with a tolerance) or Pearson, cosine, Euclidean and Manhattan on densitometric curves; UPGMA, UPGMC,
  single, complete, mean and Ward linkage. Output: dendrogram (+ normalised lane images + band diagram +
  metadata), similarity matrix, cophenetic correlation. Exports: SVG, PNG, Newick, CSV.
  - **Cut-off**: cut the dendrogram at a similarity (or distance) level to define clusters (e.g. PFGE
    pulsotypes); clusters are coloured, listed in a *Cluster* column, exported as CSV and saved with the
    comparison.
  - **Bootstrap** support of the branches (band classes or profile points resampled with replacement),
    shown on the dendrogram and written in the Newick export.
  - **Band matching**: band classes built with the comparison tolerance and the binary presence/absence
    matrix, exported as CSV or NEXUS for population-genetics and phylogenetics software.
- **Markers** shipped with GelJ plus user-defined markers.
- **Laboratories** (name, city, country) assigned to experiments, as in GelJ where each user belonged to a
  lab. Your own lab is preselected for new experiments; labs can label dendrograms, filter lanes and are
  included in the bands CSV and in exports.
- Opens TIFF (8/16-bit), PNG, JPEG, GIF, BMP and WebP images.
- **Installable and offline**: after the first visit the app works with no network connection and can be
  installed as a desktop/mobile app (*Install app* in the top bar on Chrome/Edge, or the browser's install /
  *Add to Home Screen* menu). New versions are announced with a *Reload* prompt instead of being switched
  silently. The Help page shows the storage used and lets you request persistent storage so the browser
  does not evict the data.

## Development

Requires Node.js ≥ 20.

```bash
npm install
npm run dev      # development server
npm test         # unit tests of the algorithms (src/core)
npm run build    # static build in dist/
```

## Project layout

- `src/core/` — algorithms ported from the Java version, independent of the UI:
  image operations, peak finding (ImageJ `MaximumFinder`), lane/band detection, curve fitting,
  spline, normalisation, similarity measures and clustering, background subtraction and filters,
  band matching, dendrogram cut-off and bootstrap.
- `src/storage/` — IndexedDB persistence and JSON import/export.
- `src/ui/` — React interface.
- `src/pwa.ts`, `pwa/` — offline/installable app: `pwa/sw.js` is the service-worker template and
  `pwa/plugin.js` a Vite plugin that, after `vite build`, writes `dist/sw.js` with the list of all built
  files to precache and a version hash of their contents (no service worker in `npm run dev`).
  `public/manifest.webmanifest` is the web app manifest; `scripts/icons.sh` regenerates `public/icons/` from
  `public/favicon.svg` (ImageMagick).

## Deployment on GitHub Pages

The workflow `.github/workflows/deploy-web.yml` (at the repository root) builds this folder and publishes
`dist/` on every push to `main`/`master` that touches `web/`. In the repository settings, set
**Pages → Build and deployment → Source** to **GitHub Actions**. The build uses relative paths
(`base: './'`), so it works under any `https://<user>.github.io/<repo>/` URL; the manifest's `start_url`
and `scope` and the service worker's scope are relative too. To try the offline build locally, serve
`dist/` (e.g. copy it to `<dir>/gelj/` and run `python3 -m http.server --directory <dir>`), open
`http://localhost:8000/gelj/` once and stop the server: the app keeps working. Service workers need
HTTPS or `localhost`.

## Differences from the desktop version

- Lanes are rectangles (the desktop version used 6-point polygons that were rectangles in practice).
- Hierarchical clustering uses the Lance–Williams update formulas on the distance matrix; "Mean linkage"
  is the mean of all pairwise distances in the merged cluster, as in Weka. Results can differ slightly from
  the modified Weka build used by GelJ, mainly for UPGMC and Ward.
- The band-matching tolerance is a percentage of the molecular-weight range of all compared lanes (GelJ
  used the range of the first lane), and bands are paired one-to-one with an optimal (symmetric) matching.
- Euclidean and Manhattan distances are computed on profiles scaled to [0, 1] and averaged per point.
- Where the migration model gives an undefined or non-positive molecular weight (e.g. logarithmic models
  below the last marker band), the weight is interpolated log-linearly between the marker bands; GelJ kept
  the model value.
- Users, studies and the population-genetics exports (XSLT) of the desktop version are not included; the
  band-matching matrix (CSV/NEXUS) covers the binary data those exports were built on.
- Background subtraction shrinks the image earlier than ImageJ for large radii (the ball never exceeds
  8 px on the shrunk image), so results can differ by a few grey levels; ImageJ's options (sliding
  paraboloid, smoothing, separate background image) are not offered.
- Cut-off clusters and bootstrap support are not available in the desktop version.

See [docs/comparison.md](docs/comparison.md) for a comparison with GelJ desktop, GelCompar II/BioNumerics,
PyElph, GelClust, GelAnalyzer, ImageJ/Fiji and GelGenie, and the list of proposed improvements.
