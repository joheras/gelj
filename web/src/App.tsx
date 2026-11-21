import { useCallback, useEffect, useState } from 'react';
import type { Experiment, Lab, Marker } from './core/types';
import { getDefaultLabId, listExperiments, listLabs, listMarkers, onChange } from './storage/db';
import { ComparePage } from './ui/ComparePage';
import { workFromExperiment, type WorkState } from './ui/experimentio';
import { ExperimentsPage } from './ui/ExperimentsPage';
import { ExperimentView } from './ui/ExperimentView';
import { HelpPage } from './ui/HelpPage';
import { LabsPage } from './ui/Labs';
import { MarkersPage } from './ui/MarkersPage';
import { STEP_COUNT, Wizard } from './ui/Wizard';

type Route =
  | { page: 'experiments' }
  | { page: 'experiment'; id: string }
  | { page: 'wizard'; work: WorkState; step: number }
  | { page: 'compare' }
  | { page: 'markers' }
  | { page: 'labs' }
  | { page: 'help' };

const TABS: [Route['page'], string][] = [
  ['experiments', 'Experiments'],
  ['compare', 'Compare'],
  ['markers', 'Markers'],
  ['labs', 'Labs'],
  ['help', 'Help'],
];

function routeFromHash(): Route {
  const h = window.location.hash.replace(/^#\/?/, '');
  if (h === 'compare' || h === 'markers' || h === 'labs' || h === 'help') return { page: h };
  return { page: 'experiments' };
}

export default function App() {
  const [route, setRoute] = useState<Route>(routeFromHash);
  const [experiments, setExperiments] = useState<Experiment[]>([]);
  const [markers, setMarkers] = useState<Marker[]>([]);
  const [labs, setLabs] = useState<Lab[]>([]);
  const [defaultLabId, setDefaultLab] = useState<string | null>(getDefaultLabId);
  const [toast, setToast] = useState<string | null>(null);
  const [loadingExp, setLoadingExp] = useState(false);

  const refresh = useCallback(async () => {
    const [e, m, l] = await Promise.all([listExperiments(), listMarkers(), listLabs()]);
    setExperiments(e);
    setMarkers(m);
    setLabs(l);
    setDefaultLab(getDefaultLabId());
  }, []);

  useEffect(() => {
    refresh();
    return onChange(refresh);
  }, [refresh]);

  useEffect(() => {
    const onHash = () => {
      setRoute((r) => (r.page === 'wizard' ? r : routeFromHash()));
    };
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, []);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3500);
    return () => clearTimeout(t);
  }, [toast]);

  // Warn before leaving the page in the middle of an analysis.
  useEffect(() => {
    if (route.page !== 'wizard') return;
    const h = (e: BeforeUnloadEvent) => e.preventDefault();
    window.addEventListener('beforeunload', h);
    return () => window.removeEventListener('beforeunload', h);
  }, [route.page]);

  const navigate = (page: Route['page']) => {
    if (route.page === 'wizard' && !window.confirm('Leave the assistant? Unsaved changes will be lost.')) return;
    window.location.hash = page === 'experiments' ? '' : page;
    setRoute(page === 'experiment' || page === 'wizard' ? { page: 'experiments' } : { page });
  };

  const openExperiment = async (e: Experiment, step?: number) => {
    if (e.status === 'complete' && step === undefined) {
      setRoute({ page: 'experiment', id: e.id });
      return;
    }
    setLoadingExp(true);
    try {
      const work = await workFromExperiment(e);
      setRoute({ page: 'wizard', work, step: step ?? Math.min(e.step, STEP_COUNT - 1) });
    } finally {
      setLoadingExp(false);
    }
  };

  const current = route.page === 'experiment' ? experiments.find((e) => e.id === route.id) : undefined;
  const activeTab = route.page === 'experiment' || route.page === 'wizard' ? 'experiments' : route.page;

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden="true">
            <rect x="2" y="2" width="20" height="20" rx="4" fill="var(--accent)" />
            <g fill="var(--accent-contrast)">
              <rect x="5.5" y="6" width="3" height="1.6" rx="0.5" />
              <rect x="5.5" y="10" width="3" height="1.6" rx="0.5" />
              <rect x="5.5" y="15" width="3" height="1.6" rx="0.5" />
              <rect x="10.5" y="7" width="3" height="1.6" rx="0.5" />
              <rect x="10.5" y="12.5" width="3" height="1.6" rx="0.5" />
              <rect x="15.5" y="6" width="3" height="1.6" rx="0.5" />
              <rect x="15.5" y="9" width="3" height="1.6" rx="0.5" />
              <rect x="15.5" y="15" width="3" height="1.6" rx="0.5" />
            </g>
          </svg>
          GelJ <small>Web</small>
        </div>
        <nav className="nav">
          {TABS.map(([page, label]) => (
            <button key={page} className={activeTab === page ? 'active' : ''} onClick={() => navigate(page)}>
              {label}
            </button>
          ))}
        </nav>
      </header>
      {loadingExp && <div className="toast">Loading experiment…</div>}
      {toast && <div className="toast">{toast}</div>}
      {route.page === 'experiments' && (
        <ExperimentsPage
          experiments={experiments}
          markers={markers}
          labs={labs}
          notify={setToast}
          onNew={(work) => setRoute({ page: 'wizard', work, step: 0 })}
          onOpen={(e) => openExperiment(e)}
        />
      )}
      {route.page === 'wizard' && (
        <Wizard
          key={route.work.id}
          labs={labs}
          initial={route.work}
          startStep={route.step}
          onClose={(id) => {
            refresh();
            setRoute(id ? { page: 'experiment', id } : { page: 'experiments' });
            if (id) setToast('Experiment saved.');
          }}
        />
      )}
      {route.page === 'experiment' &&
        (current ? (
          current.status === 'complete' ? (
            <ExperimentView
              key={current.id + current.updatedAt}
              experiment={current}
              markers={markers}
              labs={labs}
              notify={setToast}
              onBack={() => navigate('experiments')}
              onEdit={() => openExperiment(current, STEP_COUNT - 2)}
            />
          ) : (
            <ExperimentsPage experiments={experiments} markers={markers} labs={labs} notify={setToast} onNew={(work) => setRoute({ page: 'wizard', work, step: 0 })} onOpen={(e) => openExperiment(e)} />
          )
        ) : (
          <div className="page empty">Loading…</div>
        ))}
      {route.page === 'compare' && <ComparePage experiments={experiments} labs={labs} />}
      {route.page === 'markers' && <MarkersPage markers={markers} experiments={experiments} />}
      {route.page === 'labs' && <LabsPage labs={labs} experiments={experiments} defaultLabId={defaultLabId} />}
      {route.page === 'help' && <HelpPage />}
    </div>
  );
}
