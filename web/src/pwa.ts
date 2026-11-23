// Installable / offline app support: service worker registration, update prompt, install prompt,
// online state and persistent storage. The service worker (dist/sw.js) is generated at build time by
// pwa/plugin.js; in development no service worker is registered.

interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export interface PwaState {
  /** A new version has been installed and is waiting for the user to reload. */
  updateReady: boolean;
  /** The browser offers to install the app (Chromium-based browsers). */
  canInstall: boolean;
  online: boolean;
  /** The app is running as an installed app (standalone window). */
  standalone: boolean;
  /** A service worker controls the page, so the app works offline. */
  offlineReady: boolean;
}

let state: PwaState = {
  updateReady: false,
  canInstall: false,
  online: navigator.onLine,
  standalone: window.matchMedia?.('(display-mode: standalone)').matches ?? false,
  offlineReady: false,
};
const listeners = new Set<() => void>();
let waiting: ServiceWorker | null = null;
let installEvent: BeforeInstallPromptEvent | null = null;

function set(patch: Partial<PwaState>) {
  state = { ...state, ...patch };
  listeners.forEach((l) => l());
}

export function subscribePwa(l: () => void): () => void {
  listeners.add(l);
  return () => listeners.delete(l);
}

export function getPwaState(): PwaState {
  return state;
}

/** Activates the waiting version; the page reloads once it has taken control. */
export function applyUpdate() {
  if (waiting) waiting.postMessage('SKIP_WAITING');
  else window.location.reload();
}

export async function installApp() {
  if (!installEvent) return;
  const e = installEvent;
  installEvent = null;
  set({ canInstall: false });
  await e.prompt();
  if ((await e.userChoice).outcome === 'accepted') void requestPersistentStorage();
}

export interface StorageInfo {
  supported: boolean;
  persisted: boolean;
  usage?: number;
  quota?: number;
}

export async function getStorageInfo(): Promise<StorageInfo> {
  const s = navigator.storage;
  if (!s?.estimate) return { supported: false, persisted: false };
  const [est, persisted] = await Promise.all([s.estimate(), s.persisted ? s.persisted() : Promise.resolve(false)]);
  return { supported: true, persisted, usage: est.usage, quota: est.quota };
}

/** Asks the browser not to evict GelJ's data under storage pressure. Returns whether it is now persistent. */
export async function requestPersistentStorage(): Promise<boolean> {
  try {
    return (await navigator.storage?.persist?.()) ?? false;
  } catch {
    return false;
  }
}

function watchWaiting(reg: ServiceWorkerRegistration) {
  const found = (w: ServiceWorker) => {
    waiting = w;
    set({ updateReady: true });
  };
  // An update is only offered when a previous version controls the page; the first install is silent.
  if (reg.waiting && navigator.serviceWorker.controller) found(reg.waiting);
  reg.addEventListener('updatefound', () => {
    const w = reg.installing;
    if (!w) return;
    w.addEventListener('statechange', () => {
      if (w.state === 'installed') {
        if (navigator.serviceWorker.controller) found(w);
        else set({ offlineReady: true });
      }
    });
  });
}

export function initPwa() {
  window.addEventListener('online', () => set({ online: true }));
  window.addEventListener('offline', () => set({ online: false }));
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    installEvent = e as BeforeInstallPromptEvent;
    set({ canInstall: true });
  });
  window.addEventListener('appinstalled', () => {
    installEvent = null;
    set({ canInstall: false });
  });
  window.matchMedia?.('(display-mode: standalone)').addEventListener?.('change', (e) => set({ standalone: e.matches }));

  if (!('serviceWorker' in navigator)) return;
  if (!import.meta.env.PROD) {
    // Never let a service worker serve stale files to the dev server.
    navigator.serviceWorker.getRegistrations().then((regs) => regs.forEach((r) => r.unregister()));
    return;
  }
  set({ offlineReady: !!navigator.serviceWorker.controller });
  let reloading = false;
  navigator.serviceWorker.addEventListener('controllerchange', () => {
    // Reload only when the user accepted an update; the first install just takes control quietly.
    if (!waiting || reloading) {
      set({ offlineReady: true });
      return;
    }
    reloading = true;
    window.location.reload();
  });
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('./sw.js')
      .then((reg) => {
        watchWaiting(reg);
        // Look for a new deployment every hour and when the app comes back to the foreground.
        setInterval(() => reg.update().catch(() => {}), 60 * 60 * 1000);
        document.addEventListener('visibilitychange', () => {
          if (document.visibilityState === 'visible' && navigator.onLine) reg.update().catch(() => {});
        });
      })
      .catch((err) => console.warn('Service worker registration failed:', err));
  });
}

export function formatBytes(n: number): string {
  if (n < 1024) return `${n} B`;
  const units = ['KB', 'MB', 'GB', 'TB'];
  let v = n / 1024;
  let i = 0;
  while (v >= 1024 && i < units.length - 1) {
    v /= 1024;
    i++;
  }
  return `${v.toFixed(v < 10 ? 1 : 0)} ${units[i]}`;
}
