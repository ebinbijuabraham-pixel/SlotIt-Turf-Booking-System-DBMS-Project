const CACHE_NAME = 'slotit-app-v6';
const ASSETS = [
  './splash.html',
  './login.html',
  './register.html',
  './location.html',
  './home.html',
  './venue-detail.html',
  './checkout.html',
  './my-bookings.html',
  './owner-dashboard.html',
  './tournaments.html',
  './tournament-live.html',
  './squad-finder.html',
  './profile.html',
  './css/styles.css',
  './js/app.js',
  './js/weather.js',
  './js/hotslot.js',
  './manifest.json',
  './icon.svg'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Network-First strategy so browsers always run the latest HTML/JS when server is online
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET' || event.request.url.includes('/api/')) {
    return;
  }
  event.respondWith(
    fetch(event.request)
      .then(networkResponse => {
        if (networkResponse && networkResponse.status === 200) {
          const clone = networkResponse.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone)).catch(() => {});
        }
        return networkResponse;
      })
      .catch(() => caches.match(event.request, { ignoreSearch: true }))
  );
});
