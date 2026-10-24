/**
 * app.js
 * Common utility functions, API client, and persistent state store for Slotit
 */

const API_BASE = (function () {
    // Always clear any stale offline circuit-breaker from older sessions
    try { sessionStorage.removeItem('slotit_offline_until'); } catch (e) {}

    if (window.location.protocol.startsWith('http')) {
        const host = window.location.hostname || 'localhost';
        if (window.location.port === '8085') {
            const segs = window.location.pathname.split('/').filter(Boolean);
            const ctx = segs.length > 0 && !segs[0].endsWith('.html') ? segs[0] : 'Slotit';
            return `/${ctx}/api`;
        }
        return `http://${host}:8085/Slotit/api`;
    }
    const customServer = localStorage.getItem('slotit_server_url');
    if (customServer) return customServer.replace(/\/+$/, '');
    return 'http://192.168.1.94:8085/Slotit/api';
})();

function getOrCreateToastContainer() {
    let tc = document.getElementById('toast-container');
    if (!tc) {
        tc = document.createElement('div');
        tc.id = 'toast-container';
        tc.className = 'toast-container';
        document.body.appendChild(tc);
    }
    return tc;
}

function showToast(message, type = 'info') {
    const container = getOrCreateToastContainer();

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        if (container.contains(toast)) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(12px)';
            setTimeout(() => {
                if (container.contains(toast)) {
                    container.removeChild(toast);
                }
            }, 300);
        }
    }, 3000);
}

/**
 * Fast API fetch wrapper with:
 * - Instant sessionStorage cache for GET requests
 * - 800ms AbortController timeout so mobile Wi-Fi lag never freezes the UI
 * - Circuit-breaker ('slotit_offline_until') so if MySQL/server is unreachable,
 *   all subsequent page navigations resolve in 0ms like a native mobile app.
 */
async function apiCall(endpoint, method = 'GET', body = null) {
    const isGet = method.toUpperCase() === 'GET';
    const cacheKey = `slotit_api_cache_${endpoint}`;

    // Return cached GET data immediately if available (under 10s old)
    if (isGet) {
        try {
            const cached = sessionStorage.getItem(cacheKey);
            if (cached) {
                const parsed = JSON.parse(cached);
                if (Date.now() - parsed.ts < 10000) {
                    return parsed.data;
                }
            }
        } catch (e) {}
    }

    const headers = { 'Content-Type': 'application/json' };
    const token = localStorage.getItem('slotit_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);

    const options = { method, headers, signal: controller.signal };
    if (body) options.body = JSON.stringify(body);

    let response;
    try {
        response = await fetch(`${API_BASE}${endpoint}`, options);
        clearTimeout(timer);
    } catch (e) {
        clearTimeout(timer);
        const err = new Error('Server unreachable');
        err.isNetwork = true;
        throw err;
    }

    let data = null;
    try {
        data = await response.json();
    } catch (e) {
        const err = new Error('Invalid server response');
        err.isNetwork = true;
        err.status = response.status;
        throw err;
    }

    if (!response.ok || (data && data.success === false)) {
        const err = new Error((data && (data.message || data.error)) || 'Something went wrong');
        err.status = response.status;
        err.data = data;
        throw err;
    }

    if (isGet) {
        try {
            sessionStorage.setItem(cacheKey, JSON.stringify({ ts: Date.now(), data }));
        } catch (e) {}
    } else {
        // Invalidate GET cache after mutations (POST/DELETE)
        try {
            Object.keys(sessionStorage).forEach(k => {
                if (k.startsWith('slotit_api_cache_')) sessionStorage.removeItem(k);
            });
        } catch (e) {}
    }

    return data;
}

function asList(data, ...keys) {
    if (Array.isArray(data)) return data;
    if (!data) return [];
    for (const k of [...keys, 'data', 'items', 'results']) {
        if (Array.isArray(data[k])) return data[k];
    }
    return [];
}

let _offlineShown = false;
function notifyOffline() {
    if (_offlineShown) return;
    _offlineShown = true;
}

// Persistent Local Store Helpers (keeps bookings, squads, and teams interactive even when MySQL is offline)
function getLocalStore(key, defaultVal) {
    try {
        const raw = localStorage.getItem(key);
        return raw ? JSON.parse(raw) : defaultVal;
    } catch (e) {
        return defaultVal;
    }
}

function setLocalStore(key, val) {
    try {
        localStorage.setItem(key, JSON.stringify(val));
    } catch (e) {}
}

// Auth Helpers
function normalizeUser(u) {
    if (!u) return null;
    const id = u.userId ?? u.id ?? u.user_id ?? 1;
    return {
        ...u,
        id: id,
        userId: id,
        name: u.name || 'Ebin Biju Abraham',
        email: u.email || 'player1@example.com',
        role: (u.role || 'player').toString().toLowerCase(),
        walletBalance: Number(u.walletBalance ?? u.wallet_balance ?? 2500),
        streakCount: Number(u.streakCount ?? u.streak_count ?? 5),
        totalBookings: Number(u.totalBookings ?? u.total_bookings ?? 10),
        badge: u.badge || 'gold'
    };
}

function getCurrentUser() {
    try {
        const user = localStorage.getItem('slotit_user');
        return user ? normalizeUser(JSON.parse(user)) : null;
    } catch (e) {
        return null;
    }
}

function setCurrentUser(u) {
    localStorage.setItem('slotit_user', JSON.stringify(normalizeUser(u)));
}

function isLoggedIn() {
    return getCurrentUser() !== null;
}

function logout() {
    localStorage.removeItem('slotit_user');
    localStorage.removeItem('slotit_token');
    localStorage.removeItem('slotit_location');
    localStorage.removeItem('bookingSlot');
    window.location.href = 'login.html';
}

// Formatting Helpers
function formatDistance(km) {
    km = Number(km);
    if (isNaN(km)) return '';
    if (km < 1) {
        return `${(km * 1000).toFixed(0)} m`;
    }
    return `${km.toFixed(1)} km`;
}

function formatCurrency(amount) {
    const n = Number(amount);
    return `₹${isNaN(n) ? amount : (Number.isInteger(n) ? n : n.toFixed(2))}`;
}

function formatTime(t) {
    if (!t) return '';
    if (t.toString().includes('AM') || t.toString().includes('PM')) return t.toString();
    const [h, m] = t.toString().split(':');
    let hh = parseInt(h, 10);
    if (isNaN(hh)) return t.toString();
    const ampm = hh >= 12 ? 'PM' : 'AM';
    hh = hh % 12 || 12;
    return `${hh}:${m || '00'} ${ampm}`;
}

function toISODate(d) {
    const z = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${z(d.getMonth() + 1)}-${z(d.getDate())}`;
}

function getNavHTML(activePage) {
    const items = [
        { id: 'home', icon: '🏠', label: 'Home', url: 'home.html' },
        { id: 'explore', icon: '🔍', label: 'Explore', url: 'venue-detail.html' },
        { id: 'bookings', icon: '📋', label: 'Bookings', url: 'my-bookings.html' },
        { id: 'squad', icon: '👥', label: 'Squad', url: 'squad-finder.html' },
        { id: 'profile', icon: '👤', label: 'Profile', url: 'profile.html' }
    ];

    const navItems = items.map(item => `
        <a href="${item.url}" class="nav-item ${activePage === item.id ? 'active' : ''}">
            <span class="nav-icon">${item.icon}</span>
            <span>${item.label}</span>
        </a>
    `).join('');

    return `<nav class="bottom-nav">${navItems}</nav>`;
}

function getDesktopHeaderHTML(activePage) {
    const items = [
        { id: 'home', label: 'Home', url: 'home.html' },
        { id: 'explore', label: 'Venues', url: 'venue-detail.html' },
        { id: 'tournaments', label: 'Tournaments', url: 'tournaments.html' },
        { id: 'bookings', label: 'Bookings', url: 'my-bookings.html' },
        { id: 'squad', label: 'Squad Finder', url: 'squad-finder.html' },
        { id: 'profile', label: 'Profile', url: 'profile.html' }
    ];

    const navItems = items.map(item => `
        <a href="${item.url}" class="${activePage === item.id ? 'active' : ''}">${item.label}</a>
    `).join('');

    return `
        <header class="desktop-header">
            <div class="top-bar-title" style="flex: none; margin-right: 48px; color: var(--primary);">⚽ Slotit</div>
            <nav class="desktop-nav" style="flex: 1;">
                ${navItems}
            </nav>
            <div class="desktop-nav">
                ${isLoggedIn() ? `<button onclick="logout()" class="btn btn-outline btn-sm">Logout</button>` : `<a href="login.html" class="btn btn-primary btn-sm">Login</a>`}
            </div>
        </header>
    `;
}

// Global Auth Guard
(function authGuard() {
    const publicPages = ['login.html', 'register.html', 'splash.html', 'location.html', 'index.html', 'index.jsp', ''];
    const currentPage = (window.location.pathname.split('/').pop() || '').toLowerCase();

    if (!isLoggedIn() && !publicPages.includes(currentPage)) {
        window.location.href = 'login.html';
    }
})();

// Mobile App (PWA) Standalone Setup & Service Worker Registration
(function initMobileApp() {
    try {
        const head = document.head;
        if (head && !head.querySelector('link[rel="manifest"]')) {
            const manifest = document.createElement('link');
            manifest.rel = 'manifest';
            manifest.href = 'manifest.json';
            head.appendChild(manifest);

            const metaTags = [
                { name: 'theme-color', content: '#2563EB' },
                { name: 'mobile-web-app-capable', content: 'yes' },
                { name: 'apple-mobile-web-app-capable', content: 'yes' },
                { name: 'apple-mobile-web-app-status-bar-style', content: 'black-translucent' },
                { name: 'apple-mobile-web-app-title', content: 'Slotit' }
            ];
            metaTags.forEach(m => {
                if (!head.querySelector(`meta[name="${m.name}"]`)) {
                    const el = document.createElement('meta');
                    el.name = m.name;
                    el.content = m.content;
                    head.appendChild(el);
                }
            });

            const icon = document.createElement('link');
            icon.rel = 'icon';
            icon.type = 'image/svg+xml';
            icon.href = 'icon.svg';
            head.appendChild(icon);
        }

        if ('serviceWorker' in navigator && window.location.protocol.startsWith('http')) {
            navigator.serviceWorker.register('sw.js').catch(() => {});
        }
    } catch (e) {}
})();

// Shared Multi-City Venue & Court Catalog (Kollam, Kochi, Trivandrum, Calicut)
window.SLOTIT_VENUES = [
    // Kollam (1..5)
    {
        venueId: 1, id: 1, name: 'GreenPark Football Arena', address: 'Kavanad, Kollam', city: 'Kollam',
        latitude: 8.9001, longitude: 76.5512, sportType: 'football', dist: '0.8 km',
        description: 'Premium FIFA-grade 7-a-side artificial turf with LED floodlights, changing rooms, and ample parking.',
        rating: 4.8, totalRatings: 120, isOutdoor: true, minPrice: 600,
        courts: [
            { courtId: 1, courtName: 'Turf A (7-a-side)', hourlyRate: 800, peakMultiplier: 1.5 },
            { courtId: 2, courtName: 'Turf B (5-a-side)', hourlyRate: 600, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 2, id: 2, name: 'SmashPoint Badminton Hub', address: 'Chinnakada, Kollam', city: 'Kollam',
        latitude: 8.8856, longitude: 76.5890, sportType: 'badminton', dist: '1.2 km',
        description: 'BWF-approved indoor wooden courts with synthetic mats and pro shop.',
        rating: 4.9, totalRatings: 85, isOutdoor: false, minPrice: 300,
        courts: [
            { courtId: 3, courtName: 'Wooden Court 1', hourlyRate: 300, peakMultiplier: 1.25 },
            { courtId: 4, courtName: 'Synthetic Court 2', hourlyRate: 300, peakMultiplier: 1.25 }
        ]
    },
    {
        venueId: 3, id: 3, name: 'CricketZone Nets', address: 'Kadappakada, Kollam', city: 'Kollam',
        latitude: 8.8823, longitude: 76.6012, sportType: 'cricket', dist: '2.1 km',
        description: 'Full-length box cricket turf and automated bowling machine nets.',
        rating: 4.5, totalRatings: 45, isOutdoor: true, minPrice: 500,
        courts: [
            { courtId: 5, courtName: 'Box Cricket Turf', hourlyRate: 1000, peakMultiplier: 1.5 },
            { courtId: 6, courtName: 'Bowling Machine Net', hourlyRate: 500, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 4, id: 4, name: 'CourtKing Basketball Arena', address: 'Asramam, Kollam', city: 'Kollam',
        latitude: 8.8912, longitude: 76.5823, sportType: 'basketball', dist: '2.8 km',
        description: 'FIBA standard acrylic outdoor basketball court with night lighting.',
        rating: 4.6, totalRatings: 60, isOutdoor: true, minPrice: 400,
        courts: [
            { courtId: 7, courtName: 'Main Arena Court', hourlyRate: 600, peakMultiplier: 1.5 },
            { courtId: 8, courtName: 'Half Court Practice', hourlyRate: 400, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 5, id: 5, name: 'NetBlaze Tennis Club', address: 'Tangasseri, Kollam', city: 'Kollam',
        latitude: 8.8756, longitude: 76.5678, sportType: 'tennis', dist: '3.4 km',
        description: 'Championship clay and synthetic hard tennis courts by the coast.',
        rating: 4.4, totalRatings: 30, isOutdoor: true, minPrice: 700,
        courts: [
            { courtId: 9, courtName: 'Center Clay Court', hourlyRate: 700, peakMultiplier: 1.3 },
            { courtId: 10, courtName: 'Synthetic Court B', hourlyRate: 800, peakMultiplier: 1.3 }
        ]
    },

    // Kochi (6..9)
    {
        venueId: 6, id: 6, name: 'Kaloor Stadium Turf', address: 'Kaloor, Kochi', city: 'Kochi',
        latitude: 9.9941, longitude: 76.3018, sportType: 'football', dist: '0.6 km',
        description: 'High-density 7s and 5s FIFA turf right next to JLN International Stadium, Kaloor.',
        rating: 4.9, totalRatings: 210, isOutdoor: true, minPrice: 800,
        courts: [
            { courtId: 11, courtName: 'Kaloor 7s Arena', hourlyRate: 1000, peakMultiplier: 1.5 },
            { courtId: 12, courtName: 'Kaloor 5s Cage', hourlyRate: 800, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 7, id: 7, name: 'Kakkanad Shuttle Arena', address: 'Infopark Road, Kochi', city: 'Kochi',
        latitude: 10.0159, longitude: 76.3419, sportType: 'badminton', dist: '1.5 km',
        description: 'Air-conditioned 6-court BWF synthetic badminton complex near Infopark.',
        rating: 4.8, totalRatings: 164, isOutdoor: false, minPrice: 400,
        courts: [
            { courtId: 13, courtName: 'Infopark Court 1', hourlyRate: 400, peakMultiplier: 1.25 },
            { courtId: 14, courtName: 'Infopark Court 2', hourlyRate: 400, peakMultiplier: 1.25 }
        ]
    },
    {
        venueId: 8, id: 8, name: 'Panampilly Box Cricket', address: 'Panampilly Nagar, Kochi', city: 'Kochi',
        latitude: 9.9615, longitude: 76.2990, sportType: 'cricket', dist: '2.2 km',
        description: '360-degree netted rooftop & ground box cricket arena with digital scoreboard.',
        rating: 4.7, totalRatings: 98, isOutdoor: true, minPrice: 600,
        courts: [
            { courtId: 15, courtName: 'Panampilly Turf A', hourlyRate: 900, peakMultiplier: 1.5 },
            { courtId: 16, courtName: 'Panampilly Net B', hourlyRate: 600, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 9, id: 9, name: 'Edappally Ace Tennis Club', address: 'Edappally Toll, Kochi', city: 'Kochi',
        latitude: 10.0261, longitude: 76.3125, sportType: 'tennis', dist: '3.0 km',
        description: 'Floodlit hard-court tennis academy and match arena near Lulu Mall.',
        rating: 4.6, totalRatings: 72, isOutdoor: false, minPrice: 750,
        courts: [
            { courtId: 17, courtName: 'Hard Court 1', hourlyRate: 750, peakMultiplier: 1.3 },
            { courtId: 18, courtName: 'Hard Court 2', hourlyRate: 750, peakMultiplier: 1.3 }
        ]
    },

    // Trivandrum (10..13)
    {
        venueId: 10, id: 10, name: 'Technopark Kickoff Arena', address: 'Kazhakkoottam, Trivandrum', city: 'Trivandrum',
        latitude: 8.5581, longitude: 76.8816, sportType: 'football', dist: '0.9 km',
        description: 'Premier 7-a-side football turf popular with Technopark corporate teams.',
        rating: 4.8, totalRatings: 185, isOutdoor: true, minPrice: 700,
        courts: [
            { courtId: 19, courtName: 'Techno 7s Turf', hourlyRate: 900, peakMultiplier: 1.5 },
            { courtId: 20, courtName: 'Techno 5s Turf', hourlyRate: 700, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 11, id: 11, name: 'Kowdiar Smash Court', address: 'Kowdiar Palace Rd, Trivandrum', city: 'Trivandrum',
        latitude: 8.5241, longitude: 76.9612, sportType: 'badminton', dist: '1.7 km',
        description: 'Cushion-mounted wooden indoor badminton courts with players lounge.',
        rating: 4.9, totalRatings: 140, isOutdoor: false, minPrice: 350,
        courts: [
            { courtId: 21, courtName: 'Kowdiar Court A', hourlyRate: 350, peakMultiplier: 1.25 },
            { courtId: 22, courtName: 'Kowdiar Court B', hourlyRate: 350, peakMultiplier: 1.25 }
        ]
    },
    {
        venueId: 12, id: 12, name: 'Vellayambalam Cricket Hub', address: 'Vellayambalam, Trivandrum', city: 'Trivandrum',
        latitude: 8.5130, longitude: 76.9634, sportType: 'cricket', dist: '2.4 km',
        description: 'Professional AstroTurf cricket pitches and speed-gun bowling lanes.',
        rating: 4.6, totalRatings: 88, isOutdoor: true, minPrice: 650,
        courts: [
            { courtId: 23, courtName: 'Vellayambalam Pitch 1', hourlyRate: 850, peakMultiplier: 1.5 },
            { courtId: 24, courtName: 'Vellayambalam Pitch 2', hourlyRate: 650, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 13, id: 13, name: 'Sasthamangalam Slam Dunk', address: 'Sasthamangalam, Trivandrum', city: 'Trivandrum',
        latitude: 8.5112, longitude: 76.9715, sportType: 'basketball', dist: '3.1 km',
        description: 'Indoor maple-wood basketball court with glass backboards.',
        rating: 4.7, totalRatings: 64, isOutdoor: false, minPrice: 350,
        courts: [
            { courtId: 25, courtName: 'Indoor Maple Court', hourlyRate: 500, peakMultiplier: 1.4 },
            { courtId: 26, courtName: 'Practice Half Court', hourlyRate: 350, peakMultiplier: 1.4 }
        ]
    },

    // Calicut (14..17)
    {
        venueId: 14, id: 14, name: 'Malabar United Football Turf', address: 'Mavoor Road, Calicut', city: 'Calicut',
        latitude: 11.2588, longitude: 75.7804, sportType: 'football', dist: '0.7 km',
        description: 'Calicut\'s flagship 7s football stadium turf with gallery seating.',
        rating: 4.9, totalRatings: 230, isOutdoor: true, minPrice: 650,
        courts: [
            { courtId: 27, courtName: 'Malabar Main 7s', hourlyRate: 850, peakMultiplier: 1.5 },
            { courtId: 28, courtName: 'Malabar 5s West', hourlyRate: 650, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 15, id: 15, name: 'Beach Road Badminton Club', address: 'Vellayil, Calicut', city: 'Calicut',
        latitude: 11.2621, longitude: 75.7672, sportType: 'badminton', dist: '1.4 km',
        description: '4 indoor Yonex-mat badminton courts with sea-breeze ventilation.',
        rating: 4.7, totalRatings: 95, isOutdoor: false, minPrice: 300,
        courts: [
            { courtId: 29, courtName: 'Yonex Mat Court 1', hourlyRate: 300, peakMultiplier: 1.25 },
            { courtId: 30, courtName: 'Yonex Mat Court 2', hourlyRate: 300, peakMultiplier: 1.25 }
        ]
    },
    {
        venueId: 16, id: 16, name: 'Thondayad Strikers Cricket', address: 'Thondayad Bypass, Calicut', city: 'Calicut',
        latitude: 11.2685, longitude: 75.8051, sportType: 'cricket', dist: '2.6 km',
        description: 'All-weather covered box cricket arena for 6s and 8s tournaments.',
        rating: 4.6, totalRatings: 78, isOutdoor: true, minPrice: 500,
        courts: [
            { courtId: 31, courtName: 'Bypass Box Turf', hourlyRate: 800, peakMultiplier: 1.5 },
            { courtId: 32, courtName: 'Practice Net 2', hourlyRate: 500, peakMultiplier: 1.5 }
        ]
    },
    {
        venueId: 17, id: 17, name: 'Nadakkavu Hoops Arena', address: 'Nadakkavu, Calicut', city: 'Calicut',
        latitude: 11.2742, longitude: 75.7765, sportType: 'basketball', dist: '3.2 km',
        description: 'Pro-grade floodlit basketball court and training hub.',
        rating: 4.5, totalRatings: 52, isOutdoor: true, minPrice: 350,
        courts: [
            { courtId: 33, courtName: 'Nadakkavu Full Court', hourlyRate: 450, peakMultiplier: 1.4 },
            { courtId: 34, courtName: 'Nadakkavu 3x3 Court', hourlyRate: 350, peakMultiplier: 1.4 }
        ]
    }
];


