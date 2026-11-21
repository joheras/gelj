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
            <i>Export</i> on the Experiments page to back up your data or move it to another computer.
          </p>
        </div>
        <div className="card">
          <h3>Workflow</h3>
          <ol>
            <li>
              <b>Image preprocessing</b> — crop, invert, adjust contrast, flip or rotate the image so that lanes are vertical.
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
              <b>Comparison</b> — select lanes from any experiment and build a dendrogram with band-based coefficients (Dice, Jaccard,
              Ochiai, Jeffrey&apos;s X, band difference, with a tolerance) or curve-based measures (Pearson, cosine, Euclidean, Manhattan) and
              UPGMA, UPGMC, single, complete, mean or Ward linkage.
            </li>
          </ol>
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
