import { useCallback, useEffect, useState, useSyncExternalStore } from 'react';
import {
  applyUpdate,
  formatBytes,
  getPwaState,
  getStorageInfo,
  installApp,
  requestPersistentStorage,
  subscribePwa,
  type StorageInfo,
} from '../pwa';

function usePwa() {
  return useSyncExternalStore(subscribePwa, getPwaState);
}

/** Offline badge and "Install app" button for the top bar, plus the update banner. */
export function AppStatus() {
  const pwa = usePwa();
  return (
    <>
      <div className="app-status">
        {!pwa.online && (
          <span className="badge warn" title="No network connection. GelJ Web keeps working and your data stays on this device.">
            Offline
          </span>
        )}
        {pwa.canInstall && (
          <button className="small" onClick={installApp} title="Install GelJ Web as an app that works without a network connection">
            Install app
          </button>
        )}
      </div>
      {pwa.updateReady && (
        <div className="update-banner" role="status">
          A new version of GelJ Web is available.
          <button className="primary small" onClick={applyUpdate}>
            Reload
          </button>
        </div>
      )}
    </>
  );
}

/** Offline use, installation and storage state, for the Help page. */
export function OfflineCard() {
  const pwa = usePwa();
  const [info, setInfo] = useState<StorageInfo | null>(null);
  const [asked, setAsked] = useState(false);

  const refresh = useCallback(() => {
    getStorageInfo().then(setInfo, () => setInfo({ supported: false, persisted: false }));
  }, []);
  useEffect(refresh, [refresh]);

  const persist = async () => {
    await requestPersistentStorage();
    setAsked(true);
    refresh();
  };

  return (
    <div className="card">
      <h3>Offline use and storage</h3>
      <p>
        GelJ Web can be installed as an app and works without a network connection: after the first visit all of its files are kept by the
        browser. Use <i>Install app</i> in the top bar (Chrome, Edge) or your browser&apos;s <i>Install</i> / <i>Add to Home Screen</i> menu
        item. When a new version is published you are asked to <i>Reload</i>; your data is not affected by updates.
      </p>
      <table className="grid small">
        <tbody>
          <tr>
            <td>Offline</td>
            <td>
              {pwa.offlineReady ? (
                <span className="badge ok">Ready</span>
              ) : (
                <span className="badge">Not available yet{import.meta.env.PROD ? ' — reload the page once' : ' in development mode'}</span>
              )}
            </td>
          </tr>
          <tr>
            <td>Running as</td>
            <td>{pwa.standalone ? 'Installed app' : 'Browser tab'}</td>
          </tr>
          <tr>
            <td>Data stored</td>
            <td>
              {info?.supported && info.usage !== undefined
                ? `${formatBytes(info.usage)}${info.quota ? ` of ${formatBytes(info.quota)} available` : ''}`
                : 'Unknown'}
            </td>
          </tr>
          <tr>
            <td>Persistent storage</td>
            <td>
              {!info ? (
                '…'
              ) : !info.supported ? (
                'Not supported by this browser'
              ) : info.persisted ? (
                <span className="badge ok">Granted</span>
              ) : (
                <span style={{ display: 'inline-flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  <span className="badge warn">Not granted</span>
                  <button className="small" onClick={persist}>
                    Keep data on this device
                  </button>
                  {asked && <span className="muted">The browser declined; installing the app or bookmarking the site usually helps.</span>}
                </span>
              )}
            </td>
          </tr>
        </tbody>
      </table>
      <p className="small muted" style={{ marginBottom: 0 }}>
        Without persistent storage the browser may delete the data of rarely used sites when the disk is nearly full. Data is also lost if
        you clear the site data of this browser, so keep regular JSON backups (<i>Export</i> on the Experiments page).
      </p>
    </div>
  );
}
