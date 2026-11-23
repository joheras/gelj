import { OfflineCard } from './AppStatus';

export function HelpPage() {
  return (
    <div className="page" style={{ maxWidth: 860 }}>
      <h2>About GelJ Web</h2>
      <div className="stack" style={{ gap: 16 }}>
        <div className="card">
          <p>
            GelJ Web is a browser version of <b>GelJ</b>, a tool for analysing DNA fingerprint gel images (PFGE, RAPD, rep-PCR, AFLP…). It
            detects lanes and bands, normalises gels with molecular-weight markers and compares lanes with similarity coefficients and
            hierarchical clustering.
          </p>
          <p className="small muted" style={{ margin: 0 }}>
            <b>Privacy:</b> everything runs in your browser. Images and results are stored locally (IndexedDB) and are never uploaded. Use{' '}
            <i>Export</i> on the Experiments page to back up your data or move it to another computer. After the first visit GelJ Web
            also works offline and can be installed as an app (see <i>Offline use and storage</i> below).
          </p>
        </div>
        <div className="card">
          <h3>Workflow</h3>
          <ol>
            <li>
              <b>Image preprocessing</b> — crop, invert, adjust contrast, flip or rotate the image so that lanes are vertical. In{' '}
              <i>Other operations</i>, <i>Subtract background</i> removes uneven illumination and smears with a rolling ball (tick{' '}
              <i>Light background</i> for dark bands on a light gel; use a radius at least as large as the widest band); gamma and a 3×3
              median filter are also available.
            </li>
            <li>
              <b>Lane detection</b> — lanes are detected from the minima of the horizontal intensity profile; add, move, resize or delete
              them, and fix the reference (marker) lanes.
            </li>
            <li>
              <b>Normalisation</b> — detect and edit the bands of the markers, then choose a marker (or define a new one). Marker positions are
              interpolated across the gel and every lane is warped onto the marker reference, correcting smiles and distortions.
            </li>
            <li>
              <b>Band detection</b> — bands are the local maxima of each lane profile above a threshold (global or per lane); edit them
              manually if needed.
            </li>
            <li>
              <b>Laboratory</b> — in the last step (or later, from the experiment page) record the laboratory where the gel was run. Manage
              laboratories in the <i>Labs</i> section; the one marked as <i>my lab</i> is preselected for new experiments.
            </li>
            <li>
              <b>Comparison</b> — select lanes from any experiment and build a dendrogram with band-based coefficients (Dice, Jaccard,
              Ochiai, Jeffrey&apos;s X, band difference, with a tolerance) or curve-based measures (Pearson, cosine, Euclidean, Manhattan) and
              UPGMA, UPGMC, single, complete, mean or Ward linkage.
            </li>
          </ol>
        </div>
        <div className="card">
          <h3>Interpreting a comparison</h3>
          <ul>
            <li>
              <b>Cut-off</b> — tick <i>Cut-off at</i> and give a similarity (e.g. 80% for PFGE pulsotypes) or, for Euclidean and Manhattan,
              a distance. Lanes joined above the cut-off form a cluster: its branches are coloured and its number appears in the{' '}
              <i>Cluster</i> column. <i>Clusters CSV</i> exports the assignment; the cut-off is saved with the comparison.
            </li>
            <li>
              <b>Bootstrap</b> — resamples the characters (band classes for band-based measures, profile points for curve-based ones) and
              rebuilds the tree. The blue number at each branch is the percentage of replicates that contain the same group of lanes; values
              above about 70% indicate a robust group. Together with the cophenetic correlation it tells how well the dendrogram represents
              the data.
            </li>
            <li>
              <b>Optimisation and tolerance</b> — the <i>tolerance</i> is the largest difference between two bands counted as the same band;
              the <i>optimisation</i> (as in GelCompar II and BioNumerics) lets two whole lanes shift by up to that amount before their bands
              are matched, compensating for lanes that ran slightly faster or slower. Both are percentages of the molecular-weight range.{' '}
              <i>Optimise tolerance…</i> builds the dendrogram of the selected lanes for a grid of tolerances and optimisations and scores
              each setting by the cophenetic correlation (how faithfully the dendrogram represents the similarities) and, if you choose a
              field with known groups (e.g. MLST or a reference typing), by the adjusted Rand index between those groups and the clusters at
              the cut-off (1 = identical, about 0 = chance). Lanes with an empty field are left out of the index. The heatmap and the curve
              show the scores; ties go to the smallest optimisation and tolerance. <i>Apply and compare</i> uses the selected setting, which
              is saved with the comparison and shown in the dendrogram title and export names. Band classes and the bootstrap ignore the
              optimisation.
            </li>
            <li>
              <b>Band matching</b> — groups the bands of all compared lanes into band classes using the tolerance (two bands of the same lane
              are never in the same class). The table shows which lane has a band in each class; export it as CSV or as a NEXUS 0/1 matrix
              for PAUP*, SplitsTree, MrBayes, PAST or other population-genetics software.
            </li>
          </ul>
        </div>
        <OfflineCard />
        <div className="card">
          <h3>Reference library and identification</h3>
          <ul>
            <li>
              <b>Building the library</b> — on the <i>Library</i> page, select lanes of known type from complete experiments and add them
              with a type name (pulsotype, ribotype, species…), or take the type from each lane&apos;s genus and species, species, MLST or
              strain number. Marker lanes cannot be references. Filter the library by type, click a type to change it for one lane, use{' '}
              <i>Rename type…</i> to rename every lane of a type, and <i>Remove</i> to take a lane out of the library (the experiment is not
              changed). The library is included in backups.
            </li>
            <li>
              <b>Identifying lanes</b> — select one or several lanes and press <i>Identify</i> (also available per lane on the experiment page,
              and for the selected lanes on the comparison page). Every library lane is ranked by its similarity to each lane with the chosen
              measure; band-based measures use the tolerance as a percentage of the molecular-weight range of the lane and the library, so
              lanes normalised with different markers are compared through their molecular weights. A lane is <i>identified</i> as the type
              of its closest reference when the similarity reaches the cut-off (or, for Euclidean and Manhattan, the distance stays below it);
              otherwise it is shown as unidentified. A lane in the library is never matched with itself.
            </li>
            <li>
              <b>Reading the result</b> — with several lanes, a summary gives the identified type, the best match and the closest reference
              of <i>another</i> type: a small gap between the two means an uncertain identification. Click a lane to see its ranked matches
              with the matched and unmatched bands (molecular weights) and the lane next to its closest references (green: matched band, red:
              unmatched). <i>Summary CSV</i> and <i>Matches CSV</i> export the results.
            </li>
          </ul>
        </div>
        <div className="card">
          <h3>Coming from GelCompar II or BioNumerics</h3>
          <p>
            Use <i>Import band table</i> on the Experiments page to bring band data exported from GelCompar II / BioNumerics (band-matching or
            character tables, lists of bands), GelJ or a spreadsheet as CSV or tab-delimited text. Map the columns (lane key, strain, genus,
            species, country, MLST, comments, laboratory, bands), check the preview and import: each table becomes an experiment of lanes
            drawn from their bands.
          </p>
          <p style={{ margin: 0 }}>
            Imported lanes have molecular weights but no densitometric curve, so they are compared with band-based coefficients only
            (Dice, Jaccard…). Weights estimated by different software differ by a few per cent: raise the tolerance until known duplicates
            match. The{' '}
            <a href="https://github.com/joheras/gelj/blob/main/web/docs/migration-from-gelcompar.md" target="_blank" rel="noreferrer">
              migration guide
            </a>{' '}
            maps reference systems, tolerance, coefficients, clustering and cut-offs to GelJ Web and lists the supported layouts.
          </p>
        </div>
        <div className="card">
          <h3>Keyboard shortcuts</h3>
          <table className="grid small">
            <tbody>
              <tr>
                <td>
                  <span className="kbd">a</span>
                </td>
                <td>Toggle add mode (lanes or bands)</td>
              </tr>
              <tr>
                <td>
                  <span className="kbd">Del</span>
                </td>
                <td>Delete the selected lane or band</td>
              </tr>
              <tr>
                <td>
                  <span className="kbd">f</span>
                </td>
                <td>Fix / unset the selected lane as reference</td>
              </tr>
              <tr>
                <td>
                  <span className="kbd">Ctrl+Z</span> / <span className="kbd">r</span>
                </td>
                <td>Undo / redo</td>
              </tr>
              <tr>
                <td>Double-click</td>
                <td>Add a band at that position (band-picking steps)</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div className="card">
          <h3>Citation</h3>
          <p style={{ margin: 0 }}>
            Heras J, Domínguez C, Mata E, Pascual V, Lozano C, Torres C, Zarazaga M. GelJ – a tool for analyzing DNA fingerprint gel
            images. <i>BMC Bioinformatics</i> 16:270 (2015).
          </p>
        </div>
      </div>
    </div>
  );
}
