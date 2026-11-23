// Printable analysis reports: a dedicated print view (A4 or Letter) opened over the app and printed
// with window.print(), so that the browser's print dialog can also save it as PDF.
import { useEffect, useState, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { formatDateTime } from '../core/report';
import { BUILD } from './buildInfo';
import type { Checksum, ReportImage } from './useChecksums';

type Paper = 'A4' | 'Letter';

const PAPER_KEY = 'gelj.reportPaper';

function storedPaper(): Paper {
  try {
    return localStorage.getItem(PAPER_KEY) === 'Letter' ? 'Letter' : 'A4';
  } catch {
    return 'A4';
  }
}


interface ShellProps {
  /** Kind of report, e.g. "Gel analysis report". */
  kind: string;
  /** Name of what is reported (experiment or comparison). */
  title: string;
  /** Generation date. */
  generated: Date;
  /** Printing waits for this (e.g. checksums being computed). */
  ready: boolean;
  /** Shown in the toolbar (not printed). */
  note?: string;
  onClose: () => void;
  children: ReactNode;
}

/**
 * Full-page print view. On screen it shows the report as a sheet of paper with a toolbar; when printed only
 * the report is output, with a running header and footer on every page.
 */
export function ReportShell({ kind, title, generated, ready, note, onClose, children }: ShellProps) {
  const [paper, setPaper] = useState<Paper>(storedPaper);

  useEffect(() => {
    const previousTitle = document.title;
    // Browsers propose the document title as the PDF file name.
    document.title = `${kind} - ${title}`;
    document.body.classList.add('report-open');
    window.scrollTo(0, 0);
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => {
      document.title = previousTitle;
      document.body.classList.remove('report-open');
      window.removeEventListener('keydown', onKey);
    };
  }, [kind, title, onClose]);

  const choosePaper = (p: Paper) => {
    setPaper(p);
    try {
      localStorage.setItem(PAPER_KEY, p);
    } catch {
      // Private mode: the choice is simply not remembered.
    }
  };

  const software = `${BUILD.name} ${BUILD.version}${BUILD.commit ? ` (${BUILD.commit})` : ''}`;

  return createPortal(
    <div className={`report-root paper-${paper.toLowerCase()}`}>
      <style>{`@page { size: ${paper === 'A4' ? 'A4' : 'letter'} portrait; margin: 14mm 13mm 15mm;
  @bottom-right { content: "Page " counter(page) " of " counter(pages); font: 7.5pt system-ui, sans-serif; color: #444; } }`}</style>
      <div className="report-toolbar">
        <button onClick={onClose}>‹ Back</button>
        <strong>{kind}</strong>
        <span className="spacer" />
        {note && <span className="small muted">{note}</span>}
        <label className="row small">
          Paper
          <select value={paper} onChange={(e) => choosePaper(e.target.value as Paper)}>
            <option value="A4">A4</option>
            <option value="Letter">US Letter</option>
          </select>
        </label>
        <button className="primary" disabled={!ready} onClick={() => window.print()} title="Choose “Save as PDF” as the destination to obtain a PDF">
          {ready ? 'Print / Save as PDF' : 'Preparing…'}
        </button>
      </div>
      <article className="report-sheet" aria-label={`${kind}: ${title}`}>
        <table className="report-frame">
          <thead>
            <tr>
              <td>
                <div className="report-running report-running-head">
                  <span>{kind}</span>
                  <span>{title}</span>
                </div>
              </td>
            </tr>
          </thead>
          <tfoot>
            <tr>
              <td>
                <div className="report-running report-running-foot">
                  <span>
                    {software} · {BUILD.url}
                  </span>
                  <span>Generated {formatDateTime(generated)}</span>
                </div>
              </td>
            </tr>
          </tfoot>
          <tbody>
            <tr>
              <td>
                <header className="report-title">
                  <h1>{kind}</h1>
                  <div className="report-subtitle">{title}</div>
                </header>
                {children}
                <SoftwareSection generated={generated} />
              </td>
            </tr>
          </tbody>
        </table>
      </article>
    </div>,
    document.body,
  );
}

function SoftwareSection({ generated }: { generated: Date }) {
  return (
    <ReportSection title="Software">
      <KeyValues
        rows={[
          ['Software', `${BUILD.name} (browser version of GelJ)`],
          ['Version', BUILD.version],
          ['Build', BUILD.commit ? `commit ${BUILD.commit}, built ${BUILD.date.slice(0, 10)}` : `local build of ${BUILD.date.slice(0, 10)}`],
          ['Report generated', `${formatDateTime(generated, true)}, ${generated.toISOString()}`],
          ['Web site', BUILD.url],
          ['Source code', BUILD.source],
          [
            'Reference',
            'Heras J, Domínguez C, Mata E, Pascual V, Lozano C, Torres C, Zarazaga M. GelJ – a tool for analyzing DNA fingerprint gel images. BMC Bioinformatics 16:270 (2015).',
          ],
        ]}
      />
    </ReportSection>
  );
}

export function ReportSection({ title, children, pageBreak }: { title: string; children: ReactNode; pageBreak?: boolean }) {
  return (
    <section className={`report-section${pageBreak ? ' page-break' : ''}`}>
      <h2>{title}</h2>
      {children}
    </section>
  );
}

/** Two-column table of labelled values. */
export function KeyValues({ rows }: { rows: [string, ReactNode][] }) {
  return (
    <table className="report-kv">
      <tbody>
        {rows.map(([k, v]) => (
          <tr key={k}>
            <th>{k}</th>
            <td>{v}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export function ChecksumTable({ images, sums }: { images: ReportImage[]; sums: Map<string, Checksum> | null }) {
  return (
    <>
      <p className="report-note">
        SHA-256 of each image file (greyscale PNG) exactly as stored by GelJ Web and included in its JSON backups. Recomputing the checksum of
        an exported image verifies that it is the one analysed.
      </p>
      <table className="report-table report-checksums">
        <thead>
          <tr>
            <th>Image</th>
            <th>Pixels</th>
            <th>Bytes</th>
            <th>SHA-256</th>
          </tr>
        </thead>
        <tbody>
          {images.map((img, i) => {
            const s = sums?.get(img.url);
            return (
              <tr key={i}>
                <td>{img.label}</td>
                <td className="num">{Number.isFinite(img.width) ? `${img.width}×${img.height}` : '…'}</td>
                <td className="num">{s ? s.bytes : '…'}</td>
                <td className="mono">{s ? (s.sha256 ?? 'unavailable (Web Crypto needs HTTPS)') : 'computing…'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </>
  );
}
