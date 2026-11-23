# Market comparison: gel image analysis software and GelJ Web

This note surveys the software on the market for analysing 1D gel electrophoresis images, both
free/open-source and commercial, and places GelJ Web among them. It covers licences, prices, platforms,
maintenance status and features, then sets out GelJ Web's position and a prioritised list of
opportunities.

It complements [comparison.md](comparison.md), which compares GelJ Web feature by feature with GelJ
desktop, GelCompar II/BioNumerics, PyElph, GelClust, GelAnalyzer, ImageJ/Fiji and GelGenie and lists the
improvements already implemented. That document's details are not repeated here.

**Method.** All facts were checked on 23 November 2026 against vendor pages, documentation, repositories,
publications and reseller listings (see [Sources](#sources)). No commercial product was tested hands-on:
the cells describe documented features. **?** marks a fact we could not confirm, and "not public" means
the vendor publishes no price. Reseller prices are list prices on the date checked and are indicative
only. Some vendor sites refused automated access (bio-rad.com returned HTTP 403 and applied-maths.com
served an invalid TLS certificate), so for those products we relied on vendor PDFs, user guides hosted by
universities, reseller listings and secondary sources, and say so.

## Executive summary

- **The market has two halves.** Most gel software is *quantification* software bundled with an imager
  (Bio-Rad Image Lab, LI-COR Image Studio, Thermo Fisher iBright, Syngene GeneTools, Azure AzureSpot,
  Analytik Jena VisionWorks, Vilber Bio-1D, Cytiva ImageQuant TL, TotalLab Phoretix 1D). It detects lanes
  and bands, sizes them against a ladder and measures intensities, mainly for Western blots and protein
  gels. Only a handful of tools do **DNA fingerprint typing**: band matching across gels, similarity
  coefficients, clustering and dendrograms. That is GelJ Web's segment.
- **The reference commercial product for fingerprinting has left the market.** Applied Maths
  (bioMérieux) discontinued **GelCompar II** (maintenance and support stopped) and announced in
  October 2021 that **BioNumerics** would be phased out by the end of 2024. The US Department of Veterans
  Affairs technology register now lists BioNumerics as "de-supported and decommissioned by the vendor";
  its last release was 8.1.1 (6 October 2022). The reason given is the move of epidemiological typing to
  whole-genome sequencing.
- **What remains commercially** for fingerprinting is TotalLab **CLIQS 1D Pro** (Windows, Dice/Jaccard,
  UPGMA, a reference library, 21 CFR Part 11 through the AuditSafe add-on; price on request), which
  TotalLab now markets explicitly as a BioNumerics/GelCompar II replacement, and the *similarity score*
  dendrogram of Cytiva **ImageQuant TL 10** (about USD 4,600, aimed at protein purification rather than
  strain typing).
- **The free fingerprinting tools are mostly unmaintained**: GelJ desktop (last update April 2021),
  PyElph (2015), GelClust (download no longer found). The one new entrant is **Gel56** (October–November
  2026), a browser-based tool with Dice/Jaccard and UPGMA, still "under development" and without a
  licence file. **GelGenie** (2025) brings deep-learning band segmentation but targets quantification,
  not typing.
- **GelJ Web is the only free, maintained, install-free tool with a complete fingerprint-typing pipeline**:
  marker normalisation with spline warping, nine similarity measures, six linkage methods, cut-off
  clusters, bootstrap, cophenetic correlation, band-matching matrix (CSV/NEXUS) and data that never leave
  the browser. Its main gaps against the market are band **quantification**, a **reference library /
  identification** search, **report generation**, **shared multi-user databases** and **regulatory
  compliance** (audit trail, electronic signatures).
- **Best opportunities** (details in [Opportunities](#opportunities-for-gelj-web)): become the natural
  destination for orphaned GelCompar II/BioNumerics users (migration guide, band-table import, reference
  library search, tolerance optimisation, printable report); make the app installable and usable offline;
  add relative band quantification; improve inter-lab exchange without a server.

## Products at a glance

Target use: **FP** = DNA fingerprint typing (band matching and clustering); **Q** = quantification of
bands (protein gels, Western blots, DNA amounts); **QC** = quality control of DNA fragment sizes.

### Commercial and vendor software

| Product (vendor) | Target | Licence and price | Platforms | Lock-in | Status (last release) |
| --- | --- | --- | --- | --- | --- |
| **GelCompar II** (Applied Maths, bioMérieux) | FP | Commercial, modular; price not public | Windows | None (any scanner) | **Discontinued**; existing licences keep working, no maintenance or support |
| **BioNumerics** (Applied Maths, bioMérieux) | FP + sequences, WGS, MALDI | Commercial, modular; price not public | Windows | None | **Phased out** (announced Oct 2021, end of 2024); last release 8.1.1 (Oct 2022) |
| **CLIQS 1D Pro** (TotalLab) | FP | Commercial, per-computer licence; price not public; free trial | Windows (Vista–10 listed) | None (TIFF only) | Sold; "in active development since 1996" |
| **Phoretix 1D** (TotalLab) | Q | Commercial, per-computer licence incl. updates and support; no free edition; 7-day trial; price not public | Windows 11, macOS (Intel and Apple silicon), Linux, ARM | None | Sold; launched January 2023, replaces CLIQS, TotalLab Quant, TL100/TL120 |
| **Image Lab** (Bio-Rad) | Q | Standard edition free; Security Edition (21 CFR Part 11) paid, Windows only | Windows, macOS | Optimised for Bio-Rad imagers; opens other images | Sold; 6.1 (? newer version not confirmed) |
| **Quantity One** (Bio-Rad) | Q | Commercial | Windows, macOS (?) | Bio-Rad imagers | Licences listed as discontinued by Bio-Rad |
| **Image Studio** (LI-COR) | Q (Western) | Free (registration); separate 21 CFR Part 11-ready edition (7.x) | Windows 10/11, macOS | LI-COR imagers; imports TIFF | Sold; 6.3 |
| **Empiria Studio** (LI-COR) | Q (Western, also gels) | Paid: USD 323 per user (1 yr of updates, lifetime use), 5–25-user packs | Windows 10/11, macOS | LI-COR imagers | Sold |
| **Image Studio Lite** (LI-COR) | Q | Was free | Windows, macOS | — | **Discontinued** (2021); last 5.0 (2015) |
| **iBright Analysis Software** (Thermo Fisher) | Q | Free desktop download (registration); secure (Part 11) desktop version; cloud on Thermo Fisher Connect | Windows, macOS, cloud | iBright imagers | Sold; desktop 5.3.0 |
| **GeneTools** (Syngene, Synoptics) | Q | Free with every Syngene system (unlimited licences, free updates) | Windows (?) | Syngene imagers | Maintained; 4.3.18 (2025) |
| **AzureSpot Q / AzureSpot Pro** (Azure Biosystems) | Q | Commercial, access key, quote; free trial | Q: Windows 10+; Pro: Windows, macOS | None stated | Q sold; Pro discontinued (last 1.5.1105) |
| **ImageQuant TL 10** (Cytiva) | Q, protein similarity | Commercial, USD 4,605 (US list); GxP edition for 21 CFR Part 11 | Windows 10, macOS 10.15+ | None stated | Sold; 10.2 |
| **VisionWorks** (Analytik Jena / UVP) | Q | Bundled with UVP imagers | Windows (?); app on UVP Gel Compact | UVP imagers | **Unclear**: no imaging products on Analytik Jena's global site; still listed by distributors |
| **Bio-1D** (Vilber) | Q | Bundled with Vilber systems; also EUR 1,730 per licence (reseller) | Windows (?) | Vilber imagers | Sold ("CFR21 Part 11 ready") |
| **LabImage 1D** (Kapelan) | Q | Commercial; GBP 1,116–3,571 (reseller, by edition); Part 11 module | Windows (?) | None | Sold (? current version not confirmed) |
| **Kodak / Carestream MI** | Q | Commercial | Windows | Kodak/Carestream imagers | **Discontinued**: Carestream stopped producing Gel Logic and Image Station (2012) |
| **Cleaver Scientific** gel doc software | Q | Bundled | ? | Cleaver imagers | ? (cleaverscientific.com now redirects to the distributor Thistle Scientific) |

### Free and open-source software

| Product | Target | Licence and price | Platforms | Status (last release) |
| --- | --- | --- | --- | --- |
| **GelJ Web** (this app) | FP | Free, open source; no installation | Any modern browser (desktop) | Maintained (November 2026) |
| **GelJ desktop** | FP | Free, GPLv3 | Java (Windows, macOS, Linux) | Last update April 2021 (SourceForge) |
| **PyElph** | FP | Free, open source (? licence not stated on SourceForge) | Python | Last release 1.4 (2012), project updated 2015 |
| **GelClust** | FP | Free | Windows (C#) | SourceForge project returns 404; availability not confirmed |
| **Gel56** | FP (exploratory) + sizing | Free; no licence file in the repository | Browser (static site, offline-capable) | New: 42 commits, October–November 2026, "under development" |
| **GelAnalyzer** | Q | Freeware, closed source; *Plus* features by one-off licence key (price not public) | Windows, macOS (Apple silicon), Debian, Java | Maintained; 26.1.1 (2026) |
| **ImageJ / Fiji** (*Analyze ▸ Gels*) | Q | Free, open source | Java (all) | Maintained |
| **GelGenie** | Q (AI segmentation) | Free, Apache-2.0 | QuPath extension (all), Python | v1.1.0 (July 2025) |
| **GelInsight** | QC | Free, open source | MATLAB source; Windows app | Published January 2026 (*PLOS ONE*) |
| **GelApp** (A\*STAR) | Sizing | Free mobile app | iOS | 2015; no recent update confirmed |

## Feature matrix

Legend: ✔ available · ◐ partial or manual · ✘ not available · ? not confirmed · — not applicable.

### Fingerprint typing tools

| Feature | GelJ Web | GelJ desktop | GelCompar II / BioNumerics | CLIQS 1D Pro | ImageQuant TL 10 | PyElph | GelClust | Gel56 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Preprocessing (crop, rotate, invert, contrast) | ✔ | ✔ | ✔ | ? | ✔ | ✔ | ◐ | ✔ |
| Background subtraction | ✔ (rolling ball) | ✔ | ✔ | ✔ | ✔ | ✘ | ? | ✔ (flat field) |
| Automatic lane detection | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✘ (manual lanes) |
| Automatic band detection + editing | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| MW calibration (ladder, fitted models) | ✔ (ImageJ models + spline) | ✔ | ✔ | ✔ (curve fit) | ✔ | ✔ | ◐ | ✔ (log-linear, PCHIP) |
| Normalisation across gels (reference lanes) | ✔ | ✔ | ✔ | ✔ (Rf calibration) | ? | ✘ | ✘ | ✘ |
| Band matching with tolerance | ✔ | ✔ | ✔ | ✔ | ? | ✔ | ✔ | ✔ |
| Tolerance / shift optimisation | ✘ | ✘ | ✔ | ✘ | ? | ✘ | ✘ | ✘ |
| Band-based coefficients | ✔ Dice, Jaccard, band difference, Jeffrey's X, Ochiai | ✔ | ✔ (many) | ✔ Dice, Jaccard | ? | ✔ | ✔ | ✔ fuzzy Dice, fuzzy Jaccard |
| Curve-based coefficients (Pearson…) | ✔ Pearson, cosine, Euclidean, Manhattan | ✔ | ✔ | ✘ | ◐ (similarity score) | ✘ | ? | ✘ |
| Clustering methods | ✔ UPGMA, UPGMC, single, complete, mean, Ward | ✔ | ✔ (+ NJ, MST) | ◐ UPGMA | ◐ dendrogram (method ?) | ✔ UPGMA, NJ | ✔ | ◐ UPGMA |
| Cut-off clusters | ✔ | ✘ | ✔ | ✔ (cluster tables) | ? | ✘ | ? | ✘ |
| Bootstrap / cophenetic correlation | ✔ / ✔ | ✘ / ✔ | ✔ / ✔ | ? / ? | ? / ? | ✘ / ✘ | ? | ✘ / ✘ |
| Dendrogram with lane images and metadata | ✔ | ✔ | ✔ | ✔ | ◐ | ◐ | ◐ | ◐ (PCoA, heatmap) |
| Band quantification | ✘ | ✘ | ✔ | ✔ | ✔ | ✘ | ✘ | ◐ relative intensity |
| Strain database, search | ✔ local (IndexedDB) | ✔ (SQL), similarity search | ✔ | ✔ + reference library | ✘ | ✘ | ✘ | ✘ (project files) |
| Multi-user / inter-lab | ◐ labs, JSON backups | ✔ users, labs | ✔ (server versions) | ◐ ("not designed for" heavy shared use) | ✘ | ✘ | ✘ | ✘ |
| Exports | SVG, PNG, Newick, CSV, NEXUS, JSON | CSV, XSLT exports | many | PDF, Excel | Excel, reports | trees, matrices | ? | CSV/TSV, SVG, JPG, Newick |
| 21 CFR Part 11 | ✘ | ✘ | ? | ✔ (AuditSafe add-on) | ✔ (GxP edition) | ✘ | ✘ | ✘ |
| Data stay on the user's machine | ✔ (browser only) | ✔ | ✔ (local DB or own server) | ✔ | ✔ | ✔ | ✔ | ✔ (browser only) |
| No installation | ✔ | ✘ (Java) | ✘ | ✘ | ✘ | ✘ (Python) | ✘ | ✔ |
| Cost | Free | Free | Licence (no longer sold) | Licence (quote) | ≈ USD 4,600 | Free | Free | Free |

### Quantification tools (no fingerprint clustering)

None of these documents band matching across gels, similarity coefficients or dendrograms (ImageQuant TL,
listed above, is the exception).

| Feature | Image Lab | Image Studio / Empiria | iBright | GeneTools | AzureSpot Q | Phoretix 1D | Bio-1D | GelAnalyzer | ImageJ / Fiji | GelGenie |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Lane / band detection | ✔ | ✔ (adaptive lane finding) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ◐ manual | ✔ AI segmentation |
| Background subtraction | ✔ | ✔ | ✔ | ✔ | ✔ (rolling ball) | ✔ | ? | ✔ (profile) | ✔ | — (implicit) |
| MW / bp calibration | ✔ | ? | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ (+ Rf distortion correction) | ✘ | ✘ |
| Quantification and normalisation | ✔ (total protein) | ✔ (housekeeping, fold change) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| 21 CFR Part 11 option | ✔ Security Ed. | ✔ Part 11 edition | ✔ secure version | ◐ (2008 Part 11 integration, IQ/OQ/PQ) | ◐ optional modules (imagers) | ✔ | ✔ | ✘ | ✘ | ✘ |
| Cost | Free / paid | Free / USD 323+ | Free | Free with system | Quote | Quote | Bundled / ≈ EUR 1,730 | Free / Plus | Free | Free |

## Open and free tools

- **GelJ desktop** is the origin of this app: a Java/ImageJ tool for fingerprint gels with users, labs,
  studies and an SQL database. It still has steady downloads (41 per week on SourceForge in November
  2026) and appears in recent typing papers (for example RAPD typing of *Pseudomonas aeruginosa*, Taha,
  *Open Vet J* 2025, cited by TotalLab), but it was last updated in April 2021, needs a Java installation
  and has known usability issues (the two SourceForge reviews report problems creating a lab). GelJ Web
  covers its workflow, adds cut-off clusters, bootstrap and a band-matching matrix, and leaves out users,
  studies and the population-genetics XSLT exports (see [comparison.md](comparison.md)).
- **PyElph** (Pavel & Vasile, 2012) goes from a gel image to a phylogenetic tree (UPGMA, NJ) in Python.
  Its last file release is 1.4 (2012) and the project was last touched in 2015, so it depends on old
  Python libraries.
- **GelClust** (Khakabimamaghani et al., 2013) is a Windows C# tool for lane clustering and
  dendrograms. Its SourceForge project returned 404 when checked, so we could not confirm that it can
  still be downloaded.
- **Gel56** is the newest entrant and the closest in form to GelJ Web: a static web app (also usable
  offline from a downloaded `index.html`) with manual lanes, automatic bands, ladder sizing by log-linear
  or PCHIP interpolation, relative intensities and a *PolyBand Explorer* with fuzzy Dice/Jaccard, UPGMA,
  similarity heatmaps and PCoA, exporting CSV/TSV, SVG and Newick. It is three-lingual (Traditional
  Chinese, English, Japanese). It started in October 2026, describes itself as under development and has
  no licence file, no cross-gel normalisation, no linkage choice, no bootstrap and no database. It shows
  that the browser-only model is being adopted by others, and it is a useful benchmark for usability.
- **GelAnalyzer** (István Lázár Jr.) is the most polished free quantification tool: lane and peak
  detection, Rf-based distortion correction, MW and quantity calibration, on Windows, macOS, Debian and
  Java. It is closed source and freemium (undo/redo, labels and detailed exports need a *Plus* key). It
  has no band matching or clustering.
- **ImageJ / Fiji** offer manual densitometry (*Analyze ▸ Gels*: lane rectangles, profile plots, peak
  areas with the wand tool) and general image operations, but no automatic detection, MW calibration or
  clustering.
- **GelGenie** (Aquilina et al., *Nature Communications* 2025) segments bands with U-Nets trained on 500+
  labelled gels, inside QuPath, and exports band measurements. Code, data and weights are open
  (Apache-2.0); the last release is v1.1.0 (July 2025). It does no MW calibration or clustering. It is the
  reference for AI-based band detection.
- **GelInsight** (*PLOS ONE*, January 2026) is an open-source MATLAB tool for bulk quality control of DNA
  fragmentation in gel images. It is a niche tool, but it shows the interest in batch, scriptable
  analysis.

## Commercial tools

- **GelCompar II and BioNumerics** (Applied Maths, a bioMérieux company since 2016, merged into it in
  2023) defined the fingerprint-typing workflow: normalisation against reference systems, band matching
  with optimised tolerance and shift, many coefficients, UPGMA/NJ/MST, bootstrap, cophenetic correlation,
  identification against libraries and, in BioNumerics, a server database used by networks such as
  PulseNet. Over 20,000 papers cite them. GelCompar II has been discontinued (licences keep working, no
  maintenance or support) and BioNumerics was phased out by the end of 2024; the US VA lists it as
  decommissioned, with 8.1.1 (October 2022) as the last release. PulseNet itself replaced PFGE with
  whole-genome sequencing as its primary subtyping method in 2019. Many academic, veterinary, food and
  hospital labs still run PFGE, RAPD, rep-PCR or AFLP and are left without a supported tool.
- **TotalLab** sells **CLIQS 1D Pro** for band matching and dendrograms (Dice/Jaccard, UPGMA, similarity
  tables, cluster tables, a reference library to classify unknowns, PDF/Excel export, 21 CFR Part 11 and
  EU Annex 11 through AuditSafe; tested with about 500 lanes) and **Phoretix 1D** for quantification (all
  major desktop platforms, 21 CFR Part 11). In September 2026 TotalLab published a page positioning CLIQS
  1D Pro as the BioNumerics/GelCompar II alternative; it names GelJ as the free option some labs moved
  to, and says labs choose CLIQS when they need a growing multi-gel database, a reference library,
  vendor support or Part 11 controls. CLIQS 1D Pro is documented for Windows only and opens TIFF only.
  Prices are on request; older TotalLab 1D single-user licences were listed at USD 926–1,348 by
  resellers.
- **Bio-Rad Image Lab** (free standard edition; paid Security Edition for Part 11, Windows only) is the
  analysis software of Gel Doc and ChemiDoc imagers: automatic lanes and bands, MW, total-protein
  normalisation, reports. Bio-Rad lists Quantity One licences as discontinued. Neither offers fingerprint
  clustering in the documentation we could read.
- **LI-COR** gives away **Image Studio** (6.3; Windows and macOS; a Part 11-ready 7.x line) for its
  Odyssey and other imagers and sells **Empiria Studio** for quantitative Western blots (USD 323 per user,
  with yearly renewal for updates). The free **Image Studio Lite** was discontinued in 2021.
- **Thermo Fisher iBright Analysis Software** (desktop 5.3.0, free, Windows and macOS; a secure desktop
  version; analysis and sharing in the Thermo Fisher Connect cloud) supports iBright imagers.
- **Syngene GeneTools** (4.3.18) is free with every Syngene imager and covers lanes, bands, MW/bp,
  quantity calibration, multiplex fluorescence and colony counting. Syngene is now part of Synoptics,
  which also sells *Dymension* (≈ EUR 1,690 at resellers).
- **Azure Biosystems** replaced AzureSpot Pro (Windows and macOS) with **AzureSpot Q** (Windows):
  lane/band detection, MW and quantity calibration, housekeeping/total-protein normalisation; quote only.
- **Cytiva ImageQuant TL 10** (USD 4,605 US list; GxP edition for Part 11) is the only quantification
  package with a dendrogram: a *similarity score* that ranks lanes against a reference sample, aimed at
  protein purification and comparability studies.
- **Vilber Bio-1D** (bundled with E-Box, Quantum and Bio-Print systems; about EUR 1,730 per licence at a
  reseller; "CFR21 Part 11 ready") and **Kapelan LabImage 1D** (GBP 1,116–3,571 by edition, Part 11
  module) are quantification packages sold through instrument channels.
- **Analytik Jena / UVP VisionWorks**, **Kodak/Carestream Molecular Imaging** and **Cleaver Scientific**
  are legacy or uncertain: Carestream stopped producing its gel imagers in 2012, Analytik Jena's global
  site no longer lists any imaging product (distributors still list UVP systems with VisionWorks), and
  cleaverscientific.com now redirects to a distributor.

Common traits of the commercial offer: per-computer licences or bundling with an imager, Windows first
(macOS often second, Linux rare, no browser version), price on request, Part 11 as a paid edition or
module, and a clear focus on protein quantification. The only vendor still actively selling a
fingerprint-typing package is TotalLab.

## GelJ Web's positioning

### Strengths

1. **Unique combination**: the only tool that is free, open source, maintained, installation-free and
   covers the full fingerprint-typing pipeline from image to dendrogram.
2. **Analysis depth close to GelCompar II**: cross-gel normalisation with reference lanes and spline
   warping, nine similarity measures (band- and curve-based), six linkages, cut-off clusters, bootstrap,
   cophenetic correlation and a band-matching matrix; CLIQS 1D Pro, Gel56 and PyElph offer a subset.
3. **Privacy by design**: images and results stay in the browser (IndexedDB) and JSON backups; nothing
   is uploaded. This matters for clinical and outbreak isolates and contrasts with cloud offers such as
   Thermo Fisher Connect.
4. **No lock-in**: any scanner or camera, any operating system, open formats (TIFF 8/16-bit, PNG, JPEG,
   Newick, NEXUS, CSV, SVG).
5. **Continuity with GelJ**, a published tool (BMC Bioinformatics 2015) that is still cited and
   downloaded.

### Gaps

| Gap | Who has it | Impact |
| --- | --- | --- |
| Reference library and identification of new isolates | CLIQS 1D Pro, BioNumerics, GelJ desktop (similarity search) | High for routine typing labs |
| Tolerance / shift optimisation | GelCompar II, BioNumerics | Medium; affects reproducibility of types |
| Band quantification (volume, relative intensity) | every quantification tool, Gel56, GelGenie | Medium; out of the core typing use |
| Printable report with all settings | CLIQS (PDF/Excel), Image Lab, Empiria | Medium; needed for lab records |
| Shared database, multi-user, inter-lab exchange | BioNumerics (server), CLIQS (limited), GelJ desktop | Medium; hard without a server |
| 21 CFR Part 11 (audit trail, e-signatures, access control) | CLIQS + AuditSafe, Image Lab SE, Image Studio 7, iBright secure, ImageQuant TL GxP, Phoretix 1D | Low for research labs; blocking for GxP labs |
| NJ, minimum spanning tree, PCoA | BioNumerics, PyElph (NJ), Gel56 (PCoA) | Low–medium |
| AI band segmentation | GelGenie | Low–medium; helps difficult gels |
| Offline install, localisation | Gel56 (offline file, three languages); desktop tools | Low–medium |
| Vendor support and validation | all commercial tools | Medium for institutional adoption |

### Who it is for

- **Academic, teaching and small diagnostic laboratories** (clinical, veterinary, food, environmental
  microbiology; plant and animal genetics) that type isolates or genotypes with PFGE, RAPD, rep-PCR,
  ERIC/BOX-PCR, AFLP, ISSR or RFLP and have no budget for licences.
- **Former GelCompar II/BioNumerics users** with archived gels who need a supported, free way to
  reanalyse or extend them.
- **Laboratories in low-resource settings** or with locked-down computers, where installing software is
  difficult but a browser is available.
- **Not for**: GxP laboratories that need 21 CFR Part 11, Western blot or protein quantification (use the
  imager's software, Phoretix 1D or Empiria), or sequence-based typing (WGS/MLST tools).

## Opportunities for GelJ Web

Ranked by expected value for the users above, given the market evidence. Effort is a rough estimate for
one developer in this static, client-side app: **S** = days, **M** = one to three weeks, **L** = more
than a month. Items 5–7 and 11 of the improvement list in [comparison.md](comparison.md) overlap with
some of these and are referenced rather than repeated.

| # | Opportunity | Evidence | Effort |
| --- | --- | --- | --- |
| 1 | **Migration path for GelCompar II / BioNumerics users**: a guide mapping GelCompar concepts (reference systems, band matching, coefficients, cut-offs) to GelJ Web, and import of band tables (lane, label, MW list) exported as CSV from other tools so legacy datasets can be clustered together with new gels | Both products discontinued; TotalLab is actively targeting these users (September 2026) and names GelJ as the free alternative; 20,000+ papers used GelCompar/BioNumerics. *GelCompar II's own export formats were not verified* | S (guide) + M (import) |
| 2 | **Reference library and identification**: mark lanes as reference types and rank the closest types for a new lane (with similarity and the cut-off), reusing the stored experiments | CLIQS 1D Pro's reference library is TotalLab's first selling point; GelJ desktop had a similarity search; BioNumerics identification | M |
| 3 | **Tolerance optimisation** (scan tolerance/shift, maximise cophenetic correlation or agreement with known types) | GelCompar II/BioNumerics feature absent from CLIQS and all free tools; already proposed as item 5 in [comparison.md](comparison.md) | S |
| 4 | **Analysis report** (print/PDF): images, all processing parameters, markers and model, comparison settings, dendrogram, clusters, matrices, software version and image checksums | CLIQS (PDF/Excel), Image Lab and Empiria (reports) all have it; supports lab records and publication methods without Part 11 | S–M |
| 5 | **Installable, offline app** (web app manifest and service worker) | Privacy and locked-down or poorly connected labs; Gel56 advertises offline use; the app is already static | S |
| 6 | **Relative band quantification** (band area/volume and relative intensity from the lane profile, in exports, optionally used for weighting) | Universal in commercial tools, GelAnalyzer, Gel56 and GelGenie; already proposed as item 6 in [comparison.md](comparison.md) | M |
| 7 | **Inter-lab exchange without a server**: export/import of selected experiments as a package (images, markers, labs), merging of backups with conflict detection, optional sync to a user-chosen folder | BioNumerics/PulseNet model of shared databases; CLIQS shared databases are limited; GelJ desktop had users and labs | M |
| 8 | **Validation and credibility**: a published benchmark on public PFGE/rep-PCR gels against GelJ desktop and published GelCompar II results; documented test suite | Commercial tools sell on support and validation; TotalLab claims compatibility with PulseNet work | M |
| 9 | **More trees and ordination**: neighbour joining, minimum spanning tree, PCoA | BioNumerics (NJ, MST), PyElph (NJ), Gel56 (PCoA); already proposed as item 7 in [comparison.md](comparison.md) | M |
| 10 | **Traceability light**: per-experiment change history (who/when/what, stored with the data), lockable "final" experiments. Not full Part 11, which needs authenticated users and electronic signatures, i.e. a server | Part 11 is a paid add-on everywhere; a history helps research labs and accredited (ISO 17025) labs without claiming compliance | M |
| 11 | **Localisation** (Spanish first, then others) | Gel56 ships three languages; the typing community is largely outside English-speaking countries | M |
| 12 | **AI band segmentation** as an optional detector (GelGenie-style U-Net in ONNX Runtime Web, model downloaded on demand) | GelGenie shows matched or better quantitation than conventional tools; open weights (Apache-2.0); already proposed as item 11 in [comparison.md](comparison.md) | L |

Not recommended: a full 21 CFR Part 11 implementation (needs a server, identity management and
validation, against the project's client-only design), Western blot normalisation workflows (well served
by free vendor software), and sequence or WGS typing (a different market).

## Sources

All accessed on 23 November 2026. "Search" marks facts taken from search-engine summaries of the page
rather than from a full reading of it.

Fingerprint-typing products and their status:

- TotalLab, "BioNumerics and GelCompar II Alternative for Gel Fingerprinting and Dendrograms" (dated
  18 September 2026): <https://totallab.com/resources/bionumerics-gelcompar-ii-alternative/>
- TotalLab, CLIQS 1D Pro: <https://totallab.com/software/1d-gel-electrophoresis-band-matching-software/>
- TotalLab, Phoretix 1D: <https://totallab.com/software/1d-gel-western-blot-analysis-software/>
- Labmate Online, Phoretix 1D launch (January 2023, search):
  <https://www.labmate-online.com/news/software/144/totallab-ltd/new-software-empowers-scientists-in-life-science-laboratories/59539>
- Wikipedia, Applied Maths: <https://en.wikipedia.org/wiki/Applied_Maths>
- Wikipedia, BioNumerics: <https://en.wikipedia.org/wiki/BioNumerics>
- US Department of Veterans Affairs, Technical Reference Model, BioNumerics:
  <https://www.oit.va.gov/Services/TRM/ToolPage.aspx?tid=11148>
- Applied Maths notices cited by TotalLab (could not be opened: invalid TLS certificate):
  <https://www.applied-maths.com/news/attention-all-gelcompar-ii-users>,
  <https://www.applied-maths.com/bionumerics/category/bionumerics-phasing-out>
- CDC, "2019: PulseNet laboratories transition to whole genome sequencing" (search):
  <https://archive.cdc.gov/www_cdc_gov/amd/whats-new/pulsenet-transition.html>
- Cytiva, ImageQuant TL analysis software (price, search):
  <https://www.cytivalifesciences.com/en/us/products/items/imagequant-tl-analysis-software-p-28619>;
  similarity score application note (search):
  <https://www.cytivalifesciences.com/en/us/insights/optimization-of-purification-process-with-similarity-score-analysis>

Quantification products:

- Bio-Rad, Image Lab (search; the site returned HTTP 403 to direct reads):
  <https://www.bio-rad.com/imagelabsoftware>; Image Lab 6.1 user guide (Northeastern University copy):
  <https://cils.northeastern.edu/files/2024/01/Image-Lab-6.1-Guide-72816207bf789f91.pdf>; Security Edition:
  <https://www.bio-rad.com/en-us/sku/17006130-image-lab-software-security-edition-1-license>
- Bio-Rad, Quantity One 3-user network licence, listed as discontinued (search):
  <https://www.bio-rad.com/en-us/sku/1709603-quantity-one-3-user-network-license?ID=1709603>
- LI-COR, Image Studio: <https://www.licorbio.com/image-studio>, specifications
  <https://www.licorbio.com/image-studio/specifications>, release notes
  <https://www.licorbio.com/support/contents/software/image-studio/whats-new.html>
- LI-COR, Empiria Studio: <https://www.licorbio.com/empiria-studio>
- Wikipedia, Image Studio Lite: <https://en.wikipedia.org/wiki/Image_Studio_Lite>
- Thermo Fisher, iBright Analysis Software download (search):
  <https://www.thermofisher.com/io/en/home/global/forms/life-science/ibright-software-desktop-version-download-request.html>;
  updates and patches (search):
  <https://commerce.thermofisher.com/tw/en/home/technical-resources/software-downloads/ibright-western-imager.html>
- Syngene (Synoptics), software downloads: <https://www.synoptics.co.uk/syngene/support/software/>;
  Part 11 announcement (2008, search):
  <https://www.technologynetworks.com/genomics/product-news/new-software-and-iqoqpq-validation-procedures-for-syngene-systems-221050>
- Azure Biosystems, AzureSpot Pro: <https://azurebiosystems.com/software/azurespotpro/>; AzureSpot Q:
  <https://azurebiosystems.com/software/azurespotq/>; imaging systems (Part 11 modules):
  <https://azurebiosystems.com/products/azure-imaging-systems/>
- Vilber, E-Box systems (Bio-1D): <https://www.vilber.com/systems/e-box>; Avantor/VWR Bio-1D listing
  (search): <https://www.avantorsciences.com/uk/en/product/589795/gel-analysis-software-bio-1d>;
  Synoptics Dymension listing (search):
  <https://www.avantorsciences.com/be/en/product/14003216/gel-analysis-software-dymension>
- Universal Biologicals, LabImage 1D listings (search):
  <https://www.universalbiologicals.com/labimage-1d-l-360-a-gel-analysis-single-license-academic-l-360-a-01>
- Labnet TotalLab 1D reseller listings (search): <https://pipette.com/labnet-gds-tl1d-1.html>,
  <https://tequipment.net/Labnet-International/GDS-TL1D-1/Software>
- Analytik Jena, life science products: <https://www.analytik-jena.com/products/life-science/>; UVP
  GelSolo and Gel Compact with VisionWorks (distributor listings, search):
  <https://www.fishersci.com/shop/products/uvp-gel-compact-4/UVP84994704>
- Carestream, "Bruker to Acquire Carestream's Preclinical In-vivo Imaging Business" (2012, search):
  <https://www.carestream.com/en/us/newsandevents/news-releases/2012/bruker-to-acquire-carestreams-preclinical-in-vivo-imaging-busines>
- Cleaver Scientific (redirects to Thistle Scientific): <https://www.cleaverscientific.com/>

Free and open-source tools:

- GelJ: <https://sourceforge.net/projects/gelj/>; Heras et al., *BMC Bioinformatics* 16:270 (2015),
  <https://doi.org/10.1186/s12859-015-0703-0>
- PyElph: <https://sourceforge.net/projects/pyelph/>; Pavel & Vasile, *BMC Bioinformatics* 13:9 (2012),
  <https://doi.org/10.1186/1471-2105-13-9>
- GelClust: <https://sourceforge.net/projects/gelclust/> (HTTP 404); Khakabimamaghani et al., *Comput.
  Methods Programs Biomed.* (2013),
  <https://www.researchgate.net/publication/237003484_GelClust_A_software_tool_for_gel_electrophoresis_images_analysis_and_dendrogram_generation>
- Gel56: <https://github.com/Raingel/Gel56>, app <http://www.mycolab.uk/Gel56/> and PolyBand Explorer
  <http://www.mycolab.uk/Gel56/polyband.html>
- GelAnalyzer: <https://gelanalyzer.pages.dev/>, <https://gelanalyzer.com/plus/>
- ImageJ, *Analyze ▸ Gels*: <https://imagej.net/ij/docs/menus/analyze.html#gels>
- GelGenie: <https://github.com/mattaq31/GelGenie>; Aquilina et al., *Nature Communications* 16 (2025),
  <https://doi.org/10.1038/s41467-025-59189-0>
- GelInsight, *PLOS ONE* (2026), <https://doi.org/10.1371/journal.pone.0340374>
  (<https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12779122/>)
- GelApp (A\*STAR): <https://apps.apple.com/cr/app/gelapp/id978658582?l=en>
