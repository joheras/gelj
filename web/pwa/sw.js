/* GelJ Web service worker (template).
 * pwa/plugin.js (used in vite.config.ts) fills in VERSION and PRECACHE at build time and writes the result to
 * dist/sw.js. It precaches the whole app (HTML, hashed JS/CSS, icons, manifest) so that GelJ Web works
 * offline after the first visit. It never touches IndexedDB, where experiments and comparisons are kept.
 *
 * Updates: a new deployment produces a new sw.js (different VERSION), which installs in the background and
 * waits. The page then offers "Reload"; only when the user accepts does it send SKIP_WAITING, so a running
 * session never mixes files of two versions.
 */
const VERSION = '__VERSION__';
const PRECACHE = __PRECACHE__;
const CACHE = `gelj-${VERSION}`;
const INDEX = new URL('index.html', self.registration.scope).href;

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE).then((cache) =>
      // `cache: 'reload'` bypasses the HTTP cache so the stored files belong to this version.
      cache.addAll(PRECACHE.map((path) => new Request(new URL(path, self.registration.scope), { cache: 'reload' }))),
    ),
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((k) => k.startsWith('gelj-') && k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (request.method !== 'GET' || !request.url.startsWith(self.registration.scope)) return;
  if (request.mode === 'navigate') {
    // Single-page app with hash routes: every navigation inside the scope gets the cached index.html.
    event.respondWith(
      caches
        .open(CACHE)
        .then((cache) => cache.match(INDEX))
        .then((cached) => cached || fetch(request)),
    );
    return;
  }
  event.respondWith(
    caches
      .open(CACHE)
      .then((cache) => cache.match(request, { ignoreSearch: true }))
      .then((cached) => cached || fetch(request)),
  );
});
