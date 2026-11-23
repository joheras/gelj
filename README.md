# GelJ

**GelJ** is a tool for analysing DNA fingerprint gel images — PFGE, RAPD, rep-PCR, AFLP and similar
typing methods. It detects lanes and bands, normalises gels with molecular-weight markers, and compares
lanes with similarity coefficients and hierarchical clustering to build dendrograms.

This repository contains two versions:

| | **GelJ Web** ([`web/`](web/)) | **GelJ desktop** ([`src/`](src/)) |
| --- | --- | --- |
| Runs in | Any modern browser, no installation | Java desktop application (Swing + ImageJ) |
| Data | Stays in the browser (IndexedDB), JSON backups | Local Java DB (Derby) database |
| Status | Actively developed | Original version, kept for reference |

## GelJ Web

**Open it at <https://joheras.github.io/gelj/>.** Everything runs in your browser: images and results
are never uploaded. The app can be installed and works offline after the first visit.

### What it does

- **Analysis assistant** (as in GelJ desktop):
  1. Preprocessing: crop, invert, contrast, flip/rotate, rolling-ball background subtraction, gamma and
     median filter.
  2. Lane detection with manual editing, and selection of the marker (reference) lanes.
  3. Normalisation with molecular-weight markers (built-in or user-defined) and a migration model;
     every lane is warped onto the marker reference to correct smiles and distortions.
  4. Band detection with global or per-lane thresholds, and manual band picking.
- **Comparison** of lanes from any experiments:
  - band-based coefficients (Dice, Jaccard, Ochiai, Jeffrey's X, band difference) with position
    tolerance and optimisation, or curve-based measures (Pearson, cosine, Euclidean, Manhattan);
  - UPGMA, UPGMC, single, complete, mean or Ward linkage;
  - dendrogram with lane images, band diagram and metadata, cut-off clusters, bootstrap support,
    cophenetic correlation and similarity matrix;
  - automatic tolerance/optimisation analysis;
  - band-matching (presence/absence) matrix for population-genetics software;
  - exports to SVG, PNG, Newick, CSV and NEXUS.
- **Reference library**: store lanes of known type (pulsotype, ribotype, species…) and identify new
  lanes against it.
- **Band-table import** from GelCompar II / BioNumerics, GelJ desktop or spreadsheets, with a
  [migration guide](web/docs/migration-from-gelcompar.md) for users of those discontinued programs.
- **Printable reports** (print or save as PDF) of experiments and comparisons, with all the parameters,
  image checksums and a suggested methods paragraph.
- **Laboratories**, lane metadata (strain, species, country, MLST…) and export/import of all the data.
- Opens TIFF (8/16-bit), PNG, JPEG, GIF, BMP and WebP images.

### Development

The web app is a static site built with Vite, React and TypeScript. It needs Node.js 20 or later.

```bash
cd web
npm install
npm run dev      # development server
npm test         # unit tests
npm run build    # static build in web/dist
```

Every push to `main` that changes `web/` is tested, built and published to GitHub Pages by
[`.github/workflows/deploy-web.yml`](.github/workflows/deploy-web.yml). See [`web/README.md`](web/README.md)
for the project layout and the differences from the desktop version.

### Documentation

- [Comparison with other gel analysis tools](web/docs/comparison.md)
- [Market comparison of open and commercial software](web/docs/market-comparison.md)
- [Migrating from GelCompar II / BioNumerics](web/docs/migration-from-gelcompar.md)
- [Code audit](web/docs/audit.md)

## GelJ desktop

The original Java application, built on [ImageJ](https://imagej.net/). The analysis assistant starts
in `src/db/Interface/Main.java` (`ij/workspace/GelJ/Assistant4`); the entry point is
`db.Interface.SelectUser`. Besides the analysis, it manages users, labs and studies, and exports data
to population-genetics formats through the XSLT files in [`xslts/`](xslts/).

It is a NetBeans (Ant) project targeting Java 7. The library paths in `nbproject/project.properties`
point to the original development machine: to build it, point them to the JARs in
[`dependencies/`](dependencies/) and make the NetBeans *AbsoluteLayout* and *Java DB (Derby)* libraries
available, then build with NetBeans or `ant jar`. Built-in molecular-weight markers are in
[`standardfiles/`](standardfiles/).

## Citation

If you use GelJ in your research, please cite:

> Heras J, Domínguez C, Mata E, Pascual V, Lozano C, Torres C, Zarazaga M. GelJ – a tool for analyzing
> DNA fingerprint gel images. *BMC Bioinformatics* 16:270 (2015).
