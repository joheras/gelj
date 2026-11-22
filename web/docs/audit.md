# Code audit of GelJ Web (November 2026)

Audit of `web/` for bugs and optimisation opportunities. Severity: **High** (crash or wrong results in
normal use), **Medium** (wrong results or lost work in plausible cases), **Low** (edge cases, polish).
Core bugs have regression tests in `src/core/audit.test.ts` (written failing first); backup import is
covered by `src/storage/backup.test.ts`.

Timings: Node 22 (vitest) for `src/core`, Chromium (dev server) for encoding and rendering. "Large gel" =
synthetic 4310×3000 gel with 33 lanes (`syntheticGel` with `laneWidth: 110, height: 3000`).

## Bugs

| # | Severity | Finding | Status |
|---|----------|---------|--------|
| 1 | High | The similarity/distance matrix computed its colour scale with `Math.max(...dist.flat())`. With ≳350 lanes (≥ ~125k values) this throws `RangeError: Maximum call stack size exceeded` and, with no error boundary, blanks the whole app. Confirmed in Chromium with 400 lanes. | Fixed (loop). |
| 2 | High | Migration models can return non-finite weights (log, power, Rodbard models at positions outside their domain, e.g. a band near the top of the lane with `y = a·ln(bx)`) or negative ones (built-in Lambda and MidRange markers, `y = a+b·ln(x-c)`, below the last marker band). A single `NaN` made `mwTolerance` return `NaN`, so **every** band-based distance of the comparison became 1; JSON backups also turned `NaN` into `null`. | Fixed: invalid weights fall back to log-linear interpolation between marker bands (`molecularWeight` in `normalise.ts`); the tolerance ignores non-finite weights; import maps `null` back to `NaN`. Documented in the README. |
| 3 | Medium | The natural spline used to warp lanes extrapolated the boundary cubics beyond the first/last marker band, so rows near the top and bottom of a lane could be shifted far away (e.g. `s(-100) = 4240` for knots spanning ±5). SSJ's `SmoothingCubicSpline`, used by GelJ, extends the spline **linearly** (checked in the bytecode of `dependencies/ssj.jar`). | Fixed (linear, C¹ extension). |
| 4 | Medium | Pearson and cosine gave distance 1 to identical flat (or all-zero) profiles, because the correlation is undefined. | Fixed: identical profiles → distance 0; flat vs. different → 1. |
| 5 | Low | Band matching was greedy first-fit: not maximal (`[10, 11]` vs `[11, 9.5]` with tolerance 1 gave 1 common band instead of 2) and not symmetric, hence the averaging of both directions in `distanceMatrix`. | Fixed: a scan of both sorted lists gives the maximum one-to-one matching and is symmetric, so each pair is computed once. |
| 6 | Medium | Wizard: a lane that becomes a reference (or stops being one) after band detection had its bands cleared and was never re-detected, because steps 3.1 / 4.1 only detect when *all* lanes of the group have no bands. Result: "the counts differ" for markers, or a sample lane silently saved with no bands. Same for lanes added in step 2.2 after detection. | Fixed: such a lane gets the group's mean threshold and its bands detected (`withGroupBands`). |
| 7 | Low | `enterStep` called `setPast`, `setFuture` and `setNewMarker` inside a `setWork` updater. Updaters must be pure (StrictMode runs them twice). | Fixed: the updater is pure; the other updates happen in `goTo`. |
| 8 | Low | Keyboard shortcuts (`Ctrl+Z`, `R`, `f`, `a`, `Del`) were ignored whenever any `<input>` had focus, including sliders, checkboxes and radios (e.g. right after ticking a reference lane). | Fixed: only text fields, textareas, selects and contenteditable block shortcuts. |
| 9 | Low | The lane undo history survived when the lanes were discarded by changing the background or undoing a crop/rotation, so `Ctrl+Z` restored lanes drawn on a different image geometry. | Fixed: history and selection are reset with the lanes. |
| 10 | Medium | Import accepted any JSON with `format: 'gelj-web'`: malformed records were written and crashed the Compare page or the assistant later; fields missing in older backups stayed `undefined`. | Fixed: `parseBackup` validates every record before writing anything (the error names the record) and fills defaults (lane metadata, `labId`, `originalImage`, …). A "complete" experiment without normalised lanes is imported as a draft. |
| 11 | Low | "Normalising…" was never painted: the heavy synchronous work started in the same task as `setBusy(true)`. | Fixed: yield one task first (save draft too). |
| 12 | Low | `pointercancel` was not handled, so a cancelled touch/pen gesture left a drag in progress. | Fixed: treated as a release. |
| 13 | Low (a11y) | The gel canvas had no accessible name; the background and marker-mode radios had no `name`, so they were not keyboard radio groups. | Fixed. |
| 14 | Low (memory) | The cache of rotated lane images in the Compare page grew without bound (entries of deleted or re-analysed experiments were never released). | Fixed: bounded to 2000 entries, oldest first. |

## Optimisations (kept only with a measured gain)

| Change | Before | After |
|--------|--------|-------|
| Greyscale PNG encoder (`grayToPng`: 8-bit grey, Sub filter, `CompressionStream` deflate) instead of the RGB PNG produced by `canvas.toDataURL`. Lossless round trip checked. | Large gel: 17.35 MB data URL, 658 ms | 7.97 MB (−54 %), 673 ms, deflate off the main thread |
| Cache of encoded/decoded images (`WeakMap<GrayImage, string>`): every draft save re-encoded the unchanged original image and the processed image. | ≈ 1.3 s per save of a large gel (2 × 658 ms) | 0 ms for unchanged images |
| Wizard lane profiles computed on demand and cached by lane rectangle, instead of recomputing every lane on each lane move/resize event or threshold slider change. | 13.7 ms per event (large gel, 33 lanes) | only the lane shown (≈ 0.4 ms) |
| Mean linkage keeps the sums of distances between clusters (Lance–Williams style) instead of re-summing all member pairs for every candidate. | 86 ms (500 lanes) | 33 ms |
| Band-based distance matrix: optimal matching computed once per pair (see bug 5). | 149 ms (500 lanes, Dice) | 110 ms |

Mean linkage gives the same merge heights as before on Euclidean data (max difference 9·10⁻¹⁶); with
many exact ties (Dice) the different floating-point summation order can break ties differently, which
is as valid as before.

## Reviewed, not changed

- **Web Worker** for normalisation / detection: on the large gel `normaliseLanes` takes 63 ms,
  `detectLanes` 95 ms, all lane profiles 14 ms, so a worker would not pay off. PNG deflate already runs
  asynchronously.
- **Clustering** is O(n³) (full scan for the closest pair): 33–112 ms for 500 lanes depending on the
  linkage. A nearest-neighbour cache would change tie-breaking for little gain.
- **Similarity matrix with hundreds of lanes** is a table of n² cells (160 000 for 400 lanes): it renders
  now, but a canvas heat map would be lighter. Left, as it changes the output.
- **Contrast preview** recomputes and uploads the whole image on each slider event (~30 ms + ~60 ms on the
  large gel). Usable; a LUT applied while building the RGBA buffer would roughly halve it.
- **Saving a draft of a complete experiment** (Edit analysis → Save draft) stores it as a draft without
  normalised lanes, so its lanes leave the Compare page until it is finished again. Consistent with what
  a draft is, but possibly surprising: a product decision.
- **Storing images as Blobs** instead of base64 data URLs would save a further 25 % but changes the
  database schema and the backup format.
- Molecular-weight fallback (bug 2) is not continuous with the model at the point where the model stops
  being valid; it only replaces values that were unusable.
- Unsorted or duplicated band positions: the assistant keeps bands sorted, the spline drops duplicated
  knots, and imported lanes are now sorted. Lanes with 0 or 1 bands, a single reference lane, empty or
  1-pixel images and tolerance 0 were checked in the core functions and do not crash.
- Clustering inversions (UPGMC, and Ward on non-Euclidean distances) are kept in the cophenetic matrix
  and drawn clamped in the dendrogram, which is correct.
- With 6 or more reference lanes the marker positions are interpolated with a 4th-degree polynomial;
  GelJ's `doFit(n - 2)` would pick ImageJ model 4 (exponential) for 6 lanes, which looks accidental.
- IndexedDB upgrade path (v1 → v2 adds `labs`), `blocking` handling, deletion of markers in use
  (disabled) and of labs (detached from experiments), object URLs (revoked), `ResizeObserver`
  (disconnected) and window listeners (removed) are correct.
