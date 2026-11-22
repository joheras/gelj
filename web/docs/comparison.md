# GelJ Web compared with other gel-analysis tools

This note compares GelJ Web with the original GelJ desktop application and with other tools for DNA
fingerprint and gel image analysis. It then lists the gaps and ranks the improvements by value and
effort. Every improvement has to work in a client-side, static-site app with no server. The
improvements marked **✅ implemented** were added together with this document.

Sources: the GelJ source in `src/` and the GelJ paper (Heras et al., *BMC Bioinformatics* 16:270, 2015),
the PyElph paper (Pavel & Vasile, *BMC Bioinformatics* 13:9, 2012), the GelGenie paper (Aquilina et al.,
*Nature Communications* 16, 2025), the GelClust paper (Khakabimamaghani et al., *Comput. Methods Programs
Biomed.* 2013), and the public documentation of GelCompar II/BioNumerics, GelAnalyzer and ImageJ/Fiji.
The commercial tools were not tested hands-on. Their cells describe documented features, and **?** marks
features we could not confirm.

## Tools

| Tool | Type | Focus |
| --- | --- | --- |
| **GelJ Web** (this app) | Free, open source, runs in the browser | Fingerprint analysis: normalisation, band matching, clustering |
| **GelJ desktop** | Free, open source (Java/ImageJ) | The same workflow, plus users, studies and population-genetics exports |
| **GelCompar II / BioNumerics** (Applied Maths, bioMérieux) | Commercial desktop software | The reference standard for fingerprint typing (PFGE, rep-PCR, AFLP) and strain databases |
| **PyElph** | Free, open source (Python) | From a gel image to a phylogenetic tree |
| **GelClust** | Free (Java) | Clustering of gel lanes |
| **GelAnalyzer** | Freeware desktop software (Windows) | Lane and band detection, MW estimation, densitometry |
| **ImageJ / Fiji** (*Analyze ▸ Gels*, *Subtract Background*) | Free, open source | General image analysis: lane profiles, peak areas |
| **GelGenie** | Free, open source (Python, QuPath extension) | Deep-learning (U-Net) band segmentation and quantification |

## Feature table

Legend: ✔ available · ◐ partial or manual · ✘ not available · ? not confirmed. "now ✔" marks a
feature added in this change.

| Feature | GelJ Web (before → now) | GelJ desktop | GelCompar II / BioNumerics | PyElph | GelClust | GelAnalyzer | ImageJ / Fiji | GelGenie |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Crop / invert / rotate / flip | ✔ | ✔ | ✔ | ✔ | ◐ | ✔ | ✔ | ◐ |
| Contrast, brightness, gamma | ◐ → **now ✔** (gamma) | ✔ | ✔ | ◐ | ? | ✔ | ✔ | ✘ |
| Background subtraction (rolling ball) | ✘ → **now ✔** | ✔ (ImageJ *Subtract Background*) | ✔ | ✘ | ? | ✔ | ✔ | ✘ (implicit in segmentation) |
| Noise filters (median…) | ✘ → **now ✔** (3×3 median) | ✔ (ImageJ filters) | ✔ | ✘ | ? | ◐ | ✔ | ✘ |
| Automatic lane detection + manual editing | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ◐ (manual rectangles) | ✔ (segmentation) |
| Curved or distorted lanes (smile correction) | ✔ (spline warping between markers) | ✔ | ✔ | ✘ | ✘ | ◐ | ✘ | ◐ |
| Automatic band detection (threshold) + manual picking | ✔ (global and per-lane threshold) | ✔ | ✔ | ✔ | ✔ | ✔ | ◐ (manual peaks) | ✔ (AI) |
| MW markers library + migration models | ✔ | ✔ | ✔ | ✔ | ◐ | ✔ | ✘ | ✘ |
| Normalisation across gels (reference lanes) | ✔ | ✔ | ✔ | ✘ | ✘ | ✘ | ✘ | ✘ |
| Band quantification (area / volume) | ✘ | ✘ | ✔ | ✘ | ✘ | ✔ | ✔ | ✔ |
| Band-based coefficients (Dice, Jaccard…) with position tolerance | ✔ | ✔ | ✔ | ✔ (Dice-type) | ✔ | ? | ✘ | ✘ |
| Optimisation of the position tolerance / lane shift | ✘ | ✘ | ✔ | ✘ | ✘ | ✘ | ✘ | ✘ |
| Curve-based coefficients (Pearson, cosine…) | ✔ | ✔ | ✔ | ✘ | ? | ✘ | ✘ | ✘ |
| Band classes / band-matching table | ✘ → **now ✔** | ◐ (in XSLT exports) | ✔ | ✔ | ? | ? | ✘ | ✘ |
| Binary (0/1) matrix export | ✘ → **now ✔** (CSV, NEXUS) | ✔ (population-genetics XSLT exports) | ✔ | ◐ | ? | ? | ✘ | ✘ |
| Hierarchical clustering (UPGMA, Ward, single, complete…) | ✔ | ✔ | ✔ | ✔ | ✔ | ? | ✘ | ✘ |
| Neighbour joining / other tree methods | ✘ | ✘ | ✔ | ✔ | ? | ✘ | ✘ | ✘ |
| Cophenetic correlation | ✔ | ✔ | ✔ | ✘ | ? | ✘ | ✘ | ✘ |
| Cut-off line, cluster colouring and cluster list | ✘ → **now ✔** | ✘ | ✔ | ✘ | ? | ✘ | ✘ | ✘ |
| Bootstrap support of branches | ✘ → **now ✔** | ✘ | ✔ | ✘ | ✘ | ✘ | ✘ | ✘ |
| Dendrogram with lane images, band diagram and metadata | ✔ | ✔ | ✔ | ◐ | ◐ | ✘ | ✘ | ✘ |
| Tree exports (Newick, SVG/PNG) | ✔ (now with bootstrap labels in Newick) | ◐ | ✔ | ✔ | ? | ✘ | ✘ | ✘ |
| Strain metadata and searchable database | ✔ (IndexedDB, JSON backup) | ✔ (SQL database) | ✔ | ✘ | ✘ | ✘ | ✘ | ✘ |
| Multi-lab / multi-user | ◐ (labs only) | ✔ | ✔ | ✘ | ✘ | ✘ | ✘ | ✘ |
| Install-free, cross-platform | ✔ | ✘ (Java) | ✘ | ✘ (Python) | ✘ (Java) | ✘ (Windows) | ✘ | ✘ |
| Cost | Free | Free | Licence | Free | Free | Free | Free | Free |

## Gaps

Before this change, GelJ Web had three groups of gaps:

1. **Missing parity with GelJ desktop in pre-processing.** The desktop *Other operations* step offers
   ImageJ's *Subtract Background*, *Gamma* and filters. Uneven illumination and smears are the most
   common problem of real gel photographs. Without background subtraction, global thresholds pick up
   background in the brighter parts of the gel and miss bands in the darker parts.
2. **Typing outputs that GelCompar II users expect.** For PFGE, rep-PCR or AFLP typing, the clustering
   result is acted on through a similarity **cut-off** that defines types (for example ≥ 80 % for PFGE
   pulsotypes), together with **branch reliability**: cophenetic correlation, which GelJ Web had, and
   bootstrap, which it lacked.
3. **Interoperability.** Population-genetics and phylogenetics software (PAUP*, SplitsTree, MrBayes,
   PAST, GenAlEx, FAMD…) works on a **binary band-matching matrix**. GelJ desktop produced it with XSLT
   exports that were left out of the web port. PyElph and GelCompar II both produce one.

Smaller or more costly gaps: band quantification (GelAnalyzer, ImageJ, GelGenie), optimisation of the
position tolerance and lane shift (GelCompar II), neighbour-joining trees (PyElph, BioNumerics), and
AI-based band segmentation (GelGenie).

## Prioritised improvements

Scores: value V (1–5) is how much it helps typical users; effort E (1–5) is the implementation cost in a
static, client-side app.

| # | Improvement | V | E | Status |
| --- | --- | --- | --- | --- |
| 1 | **Background subtraction (rolling ball), gamma and median filter** in step 1.4: parity with GelJ desktop, and better band detection on real gels | 5 | 2 | ✅ implemented |
| 2 | **Band-matching table and binary 0/1 matrix** (band classes with the comparison tolerance; CSV and NEXUS export) | 5 | 2 | ✅ implemented |
| 3 | **Dendrogram cut-off**: dashed line, cluster colours, cluster column, cluster CSV; saved with the comparison | 5 | 2 | ✅ implemented |
| 4 | **Bootstrap support** of branches (band classes or profile points resampled; Newick with support labels) | 4 | 3 | ✅ implemented |
| 5 | Tolerance optimisation: scan the tolerance and suggest the one that maximises the cophenetic correlation or the separation of known types (as in GelCompar II) | 4 | 2 | proposed |
| 6 | Band quantification: band area and relative intensity from the lane profile, exported with the bands, optionally weighting band matching | 3 | 3 | proposed |
| 7 | Neighbour-joining trees and a minimum spanning tree | 3 | 3 | proposed |
| 8 | Printable report: one page with the settings, dendrogram, clusters and matrices | 3 | 2 | proposed |
| 9 | Run clustering and bootstrap in a Web Worker, for comparisons of hundreds of lanes | 3 | 2 | proposed |
| 10 | Editing of band classes (merge or split classes by hand) before export | 3 | 3 | proposed |
| 11 | AI band segmentation (GelGenie-like U-Net in ONNX Runtime Web) as an alternative detector | 3 | 5 | proposed |

### What was implemented

- **Background subtraction** (`src/core/filters.ts`): a rolling ball. This is a grey-scale opening with
  a spherical structuring element, computed on a block-minimum-shrunk copy of the image and enlarged
  again by bilinear interpolation, as in ImageJ. It has a *light background* option and handles both dark
  and light gels. The shrink factor is applied earlier than in ImageJ, so the ball never exceeds 8 px on
  the shrunk image and a 2000×1500 gel is processed in about a second. **Gamma** (with a live preview)
  and a **3×3 median filter** complete step 1.4.
- **Band matching** (`src/core/bandmatch.ts`): the bands of the compared lanes are pooled and grouped
  into band classes with the comparison tolerance. A class never holds two bands of the same lane, and
  any two bands in a class match under the same rule as the band-based coefficients. The *Band matching*
  tab on the Compare page shows the table in dendrogram order, coloured by cluster, and exports it as
  CSV (with the label columns and the cluster) or as a NEXUS standard-data matrix.
- **Cut-off and clusters** (`src/core/treecut.ts`): the tree is cut at a % similarity, or at a distance
  for distance measures. The dendrogram shows the cut-off line, colours the branches of every
  multi-lane cluster and adds a *Cluster* column. The clusters can be exported as CSV, and the cut-off
  is stored with saved comparisons.
- **Bootstrap** (`src/core/bootstrap.ts`): replicates resample the band classes with replacement for
  band-based measures (coefficients are computed from the weighted presence/absence counts) or the
  points of the densitometric profiles for curve-based measures. Each replicate rebuilds the tree with
  the same linkage. The support of each branch is the percentage of replicates that contain the same
  group of lanes. It runs in small batches, so the page stays responsive and the run can be
  cancelled. The values are shown in blue on the dendrogram and written as internal-node labels in the
  Newick export. The random generator is seeded, so results are reproducible.
