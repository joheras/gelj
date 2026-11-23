# Moving from GelCompar II / BioNumerics to GelJ Web

GelCompar II is discontinued and BioNumerics has been decommissioned (its last release, 8.1.1, came out in
October 2022). Laboratories that typed strains with them by PFGE, RAPD, rep-PCR or AFLP still have years
of band data in those databases. This guide explains how to bring that data into GelJ Web and how the
concepts you know map onto GelJ Web. GelJ Web is free, open source and runs in the browser, with no
server and no installation.

In short:

1. Export band data from GelCompar II / BioNumerics as a text table (a band-matching table, or a list of
   bands per entry) and save it as CSV or tab-delimited text.
2. In GelJ Web, open **Experiments → Import band table**, check the column mapping and the preview, and
   import. Each imported table becomes an experiment of *virtual lanes* (no gel image).
3. Analyse new gels in GelJ Web as usual and compare them with the imported lanes on the **Compare**
   page, using a band-based coefficient (Dice, Jaccard…).

## Concepts

| GelCompar II / BioNumerics | GelJ Web | Notes |
| --- | --- | --- |
| Database, entries, entry **key** | Experiments (stored in the browser, IndexedDB) and their lanes; the lane name is the key | Use *Export* / *Export all* for JSON backups; *Import* restores them on another computer. |
| Information fields (strain, genus, species, country…) | Lane metadata: strain number, genus, species, country, MLST, comments; laboratory per experiment | Other fields can go in *Comments*. When the imported table has a laboratory column, one experiment is created per laboratory. |
| Fingerprint type / experiment type | Experiment (one gel image, one marker) | Settings live in each experiment rather than in a type. |
| Reference system, reference positions, standard (marker) lanes | Markers (*Markers* page) and the normalisation step of the assistant (reference lanes, marker bands, migration model) | GelJ Web warps every lane onto the marker reference, correcting smiles and distortions. |
| Normalised position (% of the gel) and **metrics** (molecular size) from a regression curve | Normalised band positions and **molecular weights** from the migration model (ImageJ curve-fitter models: polynomial, exponential, power, Rodbard…) | Comparisons use molecular weights. Positions in % are only comparable inside one reference system (see below). |
| Band search and band editing | Band detection (global and per-lane threshold) and manual band picking | |
| Densitometric curves | Lane profiles of gel experiments | Imported band tables have no curves. |
| Band-based coefficients: Dice, Jaccard, Ochiai, Jeffrey's X, band difference | The same coefficients | Kulczynski and simple matching are not available. |
| Curve-based coefficients: Pearson correlation, cosine | Pearson correlation, cosine coefficient, Euclidean and Manhattan distances | Only between gel lanes; GelJ Web has no curve "optimisation" shift. |
| **Position tolerance** and **optimisation** (%) | **Tolerance** (% of the molecular-weight range of the compared lanes) | Not numerically equivalent: GelCompar's tolerance is a % of the normalised gel length. Start with 1–3 % and check that obvious duplicates match. There is no optimisation shift. |
| Clustering: UPGMA, Ward, single and complete linkage, neighbour joining | UPGMA, UPGMC, single, complete, mean linkage and Ward | Neighbour joining is not available; the Newick export can be loaded into other tools. |
| Cophenetic correlation, cluster error flags | Cophenetic correlation and **bootstrap** support of the branches | |
| Cut-off on the dendrogram (e.g. 80 % for PFGE pulsotypes) | **Cut-off at** (%) on the Compare page; clusters are coloured, numbered and exported as CSV | Saved with the comparison. |
| Band matching, band classes, polymorphic bands, binary character table | **Band matching** tab: band classes built with the tolerance, presence/absence matrix exported as CSV or NEXUS | |
| Comparisons saved in the database | *Save comparison* on the Compare page | |
| Composite data sets, minimum spanning trees, identification libraries | Not available | |

## Exporting from GelCompar II / BioNumerics

We could not verify the menus and file layouts below against the software or its manuals (see
[What could not be verified](#what-could-not-be-verified)), so the importer accepts several layouts and
lets you map the columns yourself. Any table with one lane per row, or one band per row, works.

**Band-matching table (recommended).** Select the entries of the fingerprint type in a comparison, run the
band matching, and export or copy the band-matching table as a binary (presence/absence) table, together
with the key and the information fields you want to keep. Paste it in a spreadsheet if needed and save it
as tab-delimited text or CSV. Column headers of the band classes should be their molecular size (metric);
if they are positions (% of the normalised gel), choose *Positions* when importing.

**List of bands per entry.** If you can export the bands of each entry with their metrics (molecular
size), save a table with one row per band (key, information fields, size) or one row per entry (key,
fields, then the sizes in several columns or in one cell).

**Character data.** BioNumerics 7 and later can export character values of the selected entries with
*File → Export* in the main window ([Applied Maths knowledge base](http://www.applied-maths.com/knowledge-base/exporting-character-values)).
A binary character table with band classes as characters is read like a band-matching table.

Keep the units consistent: GelJ Web's built-in markers are in base pairs (100bp Plus ladder) or kilobases
(PFGE markers). The importer converts kb to bp (or the reverse) when you ask it to.

## Importing into GelJ Web

Open **Experiments → Import band table** and choose a `.csv`, `.tsv` or `.txt` file (or one of the
samples).

1. **Layout and columns.** The delimiter (tab, semicolon or comma), the header row, the layout and the
   columns are guessed from the headers; correct them if needed.
   - *One row per band*: a lane column and a band column. Rows with an empty lane label belong to the lane
     above. This is also the layout of GelJ Web's own *Bands CSV*.
   - *One row per lane*: band columns (`Band 1`, `Band 2`…) or a single cell holding a list
     (`2610; 1395; 948`).
   - *Band-matching / character table*: one column per band class, headed by its molecular weight
     (`2600`, or GelJ desktop's `Locus1(2600.5)`); `1`, `+`, `x` or an intensity above 0 mean present, `0`
     or empty mean absent.
   - Optional columns: strain number, genus, species, country, MLST, comments, laboratory and a marker-lane
     flag. Synonyms such as *Key*, *Isolate*, *Sample*, *ST*, *Institution* are recognised.
   - *Band values are* molecular weights or positions (%); *Units* converts kb ↔ bp. Decimal commas
     (`2,61`) and units in the cells (`850 bp`) are accepted.
2. **Preview and validation.** Every lane is listed with its metadata and bands. Errors (values that are
   not numbers, non-positive weights, missing band columns) block the import and name the line; warnings
   (lanes without bands, a lane appearing twice, conflicting metadata, unknown cells such as `?`) do not.
3. **Save.** Give the experiment a name and a laboratory. With a laboratory column, one experiment is
   created per laboratory, and missing laboratories are created.

Imported experiments appear in the Experiments list with a *Band table* badge. Their lanes are drawn
from the bands (a *virtual gel* on a log molecular-weight scale) so that they can be shown in dendrograms,
but these drawings are not gel images: their band positions do not line up with normalised gel lanes.
Lane names and metadata can be edited on the experiment page; the bands cannot.

## Comparing imported lanes with new gels

- Select imported and gel lanes on the **Compare** page and use a **band-based** coefficient. Curve-based
  measures (Pearson, cosine, Euclidean, Manhattan) are disabled while imported lanes are selected,
  because those lanes have no densitometric curve; GelJ Web does not invent one.
- Molecular weights estimated by two programs, two reference systems or two markers differ by a few per
  cent, more at the ends of the marker range. Increase the tolerance (e.g. from 1 % to 2–3 %) until known
  duplicates match, and check the *Band matching* tab. In the sample data, imported lanes match the
  corresponding lanes of the sample gel at 100 % with a 3 % tolerance, but only at 33–60 % with 1 %.
- Lanes imported with **positions** (% of a normalised gel) can only be compared with lanes imported from
  the same reference system; GelJ Web warns when they are mixed with other lanes.
- Use the marker that the original data was normalised with when you analyse new gels (or define it on
  the *Markers* page) to keep molecular weights comparable.

## Sample files

The importer has buttons that load these files, served from `web/public/samples/`:

- [`band-matching-table.txt`](../public/samples/band-matching-table.txt): tab-delimited binary
  band-matching table with a key, information fields and band classes headed by molecular weight (the
  layout we expect from GelCompar II / BioNumerics band-matching exports).
- [`bands-one-row-per-band.csv`](../public/samples/bands-one-row-per-band.csv): GelJ Web's *Bands CSV*,
  one row per band, with a laboratory column (creates two experiments).
- [`bands-one-row-per-lane.csv`](../public/samples/bands-one-row-per-lane.csv): spreadsheet with
  semicolons, decimal commas and band sizes in kb in numbered columns.

The six isolates of the samples correspond to the lanes of *Try a sample gel* (`IMP-01` ≈ lanes 2–3,
`IMP-02` ≈ lane 4, `IMP-03` ≈ lanes 6–7, `IMP-04` ≈ lane 8), plus two lanes that match no gel lane.

## What could not be verified

The documentation of GelCompar II and BioNumerics is not publicly reachable any more (bio-rad.com refused
our requests and applied-maths.com serves an invalid certificate; checked in November 2026), and we did
not have a licence to produce real export files. Therefore:

- The exact menus, file names and column headers of the band-matching, band and character exports of
  GelCompar II and BioNumerics are not verified. The importer does not depend on specific header names:
  any header can be mapped by hand.
- We do not know whether band-class headers are exported as metrics, as positions or as class names
  without values. Class names without a number in brackets cannot be imported as a band-matching table;
  export the bands per entry instead, or rename the headers to their sizes.
- XML exports, the BioNumerics database itself (SQL Server / Access / SQLite back ends), `.bnz` /
  `.bnp` packages, gel images with their normalisation and densitometric curves cannot be imported.
- Quantitative band-matching tables (band intensities instead of 0/1) are read as presence/absence.
- `1,200` is read as 1.2 (decimal comma), not as one thousand two hundred; remove thousands separators
  before importing.

If you have real exports from GelCompar II or BioNumerics and they do not import, please open an issue
at <https://github.com/joheras/gelj/issues> with an anonymised sample of the file.
