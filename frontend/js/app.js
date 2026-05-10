/* KenIT Frontend — app.js
   Vanilla JS, no framework needed. Keep it lean.
   All API calls go to localhost:8080 by default.
*/

const API_BASE = (window.KENIT_API_URL || "http://localhost:8080") + "/api";

// ─── ROUTER ─────────────────────────────────────────────────────────────────

const views = {
  dashboard: { el: "view-dashboard", title: "Dashboard", sub: "Overview · Africa/Nairobi (EAT)" },
  tickets:   { el: "view-tickets",   title: "Helpdesk",  sub: "Support ticket management" },
  assets:    { el: "view-assets",    title: "IT Assets", sub: "Inventory & tracking" },
  network:   { el: "view-network",   title: "Network",   sub: "Live device monitoring" },
};

function navigate(viewKey) {
  // Hide all views
  document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
  document.querySelectorAll(".nav-link").forEach(l => l.classList.remove("active"));

  const view = views[viewKey];
  if (!view) return;

  document.getElementById(view.el).classList.add("active");
  document.getElementById("page-title").textContent = view.title;
  document.getElementById("page-sub").textContent   = view.sub;

  const link = document.querySelector(`[data-view="${viewKey}"]`);
  if (link) link.classList.add("active");

  // Load data for the active view
  if (viewKey === "dashboard") loadDashboard();
  if (viewKey === "tickets")   loadTickets();
  if (viewKey === "assets")    loadAssets();
  if (viewKey === "network")   loadNetwork();
}

// ─── API HELPERS ─────────────────────────────────────────────────────────────

async function apiFetch(path, options = {}) {
  try {
    const res = await fetch(API_BASE + path, {
      headers: { "Content-Type": "application/json" },
      ...options,
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  } catch (err) {
    // In demo mode (no backend running), return null and use mock data
    console.warn(`API call failed for ${path}:`, err.message);
    return null;
  }
}

// ─── MOCK DATA — used when backend isn't running ──────────────────────────────

const MOCK_STATS = {
  openTickets: 5, inProgressTickets: 2, resolvedTodayTickets: 3,
  criticalTickets: 1, totalAssets: 5, activeAssets: 5,
  assetsInRepair: 0, totalAssetValueKes: 348000,
  totalDevices: 8, onlineDevices: 6, offlineDevices: 2,
  lastNetworkScan: "09 May 2026, 08:45"
};

const MOCK_TICKETS = [
  { id:1, ticketNumber:"TKT-2026-00001", title:"Printer on Floor 3 not responding",
    priority:"HIGH", status:"OPEN", category:"PRINTER", department:"Finance",
    raisedBy:"Wanjiku M.", assignedTo:null },
  { id:2, ticketNumber:"TKT-2026-00002", title:"Cannot access KRA iTax portal",
    priority:"CRITICAL", status:"OPEN", category:"NETWORK", department:"Finance",
    raisedBy:"Otieno K.", assignedTo:null },
  { id:3, ticketNumber:"TKT-2026-00003", title:"Laptop screen flickering",
    priority:"MEDIUM", status:"IN_PROGRESS", category:"HARDWARE", department:"HR",
    raisedBy:"Akinyi N.", assignedTo:"Kamau T." },
  { id:4, ticketNumber:"TKT-2026-00004", title:"Outlook keeps crashing on Windows 11",
    priority:"MEDIUM", status:"OPEN", category:"SOFTWARE", department:"Registry",
    raisedBy:"Muthoni W.", assignedTo:null },
  { id:5, ticketNumber:"TKT-2026-00005", title:"Need new user account for new hire",
    priority:"LOW", status:"RESOLVED", category:"ACCOUNT_ACCESS", department:"HR",
    raisedBy:"Kamau J.", assignedTo:"Njoroge P.",
    resolutionNotes:"Account created in AD and email provisioned." },
];

const MOCK_ASSETS = [
  { id:1, assetTag:"KEN-2026-001", name:"Dell Latitude 5540", type:"LAPTOP",
    brand:"Dell", model:"Latitude 5540", status:"ACTIVE", department:"ICT",
    location:"Server Room", purchaseCostKes:145000 },
  { id:2, assetTag:"KEN-2026-002", name:"HP LaserJet Pro M404dn", type:"PRINTER",
    brand:"HP", model:"LaserJet M404dn", status:"ACTIVE", department:"Finance",
    location:"Floor 2", purchaseCostKes:38000 },
  { id:3, assetTag:"KEN-2026-003", name:"Cisco RV340 Router", type:"ROUTER",
    brand:"Cisco", model:"RV340", status:"ACTIVE", department:"ICT",
    location:"Server Room", purchaseCostKes:25000 },
  { id:4, assetTag:"KEN-2026-004", name:"APC Smart-UPS 1500", type:"UPS",
    brand:"APC", model:"Smart-UPS 1500", status:"ACTIVE", department:"ICT",
    location:"Server Room", purchaseCostKes:42000 },
  { id:5, assetTag:"KEN-2026-005", name:"HP ProBook 450 G9", type:"LAPTOP",
    brand:"HP", model:"ProBook 450 G9", status:"ACTIVE", department:"HR",
    location:"Floor 1 - HR Desk", purchaseCostKes:98000 },
];

const MOCK_DEVICES = [
  { id:1, ipAddress:"192.168.1.1",  hostname:"main-router", vendor:"Cisco",      isOnline:true,  pingMs:2 },
  { id:2, ipAddress:"192.168.1.10", hostname:"file-server", vendor:"Dell",       isOnline:true,  pingMs:4 },
  { id:3, ipAddress:"192.168.1.20", hostname:"print-srv",   vendor:"HP",         isOnline:false, pingMs:null },
  { id:4, ipAddress:"192.168.1.50", hostname:"ws-finance01",vendor:"Dell",       isOnline:true,  pingMs:8 },
  { id:5, ipAddress:"192.168.1.51", hostname:"ws-hr01",     vendor:"HP",         isOnline:true,  pingMs:11 },
  { id:6, ipAddress:"192.168.1.100",hostname:"nas-storage", vendor:"Synology",   isOnline:true,  pingMs:6 },
  { id:7, ipAddress:"8.8.8.8",      hostname:"google-dns",  vendor:"Google",     isOnline:true,  pingMs:14 },
  { id:8, ipAddress:"192.168.1.99", hostname:"old-desktop", vendor:"Unknown",    isOnline:false, pingMs:null },
];

// ─── DASHBOARD ────────────────────────────────────────────────────────────────

async function loadDashboard() {
  const [stats, tickets, devices] = await Promise.all([
    apiFetch("/dashboard/stats"),
    apiFetch("/tickets"),
    apiFetch("/network/devices"),
  ]);

  renderStats(stats || MOCK_STATS);
  renderRecentTickets((tickets || MOCK_TICKETS).slice(0, 6));
  renderNetworkBars((devices || MOCK_DEVICES).slice(0, 8));

  // Update the sidebar badge
  const openCount = (stats || MOCK_STATS).openTickets;
  const badge = document.getElementById("nav-open-count");
  if (badge) badge.textContent = openCount;
}

function renderStats(s) {
  const formatKes = n => {
    if (!n) return "KES 0";
    if (n >= 1_000_000) return `KES ${(n/1_000_000).toFixed(1)}M`;
    if (n >= 1000)      return `KES ${(n/1000).toFixed(0)}K`;
    return `KES ${n}`;
  };

  setText("stat-critical-val", s.criticalTickets);
  setText("stat-open-val",     s.openTickets);
  setText("stat-resolved-val", s.resolvedTodayTickets);
  setText("stat-assets-val",   s.activeAssets);
  setText("stat-online-val",   `${s.onlineDevices}/${s.totalDevices}`);
  setText("stat-value-val",    formatKes(s.totalAssetValueKes));
  setText("last-scan-time",    `Scanned: ${s.lastNetworkScan}`);
  setText("online-count-text", s.onlineDevices);
  setText("offline-count-text", s.offlineDevices);
}

function renderRecentTickets(tickets) {
  const list = document.getElementById("recent-tickets-list");
  if (!tickets.length) {
    list.innerHTML = `<div style="padding:20px;text-align:center;color:var(--ink-muted);font-size:13px;">No tickets yet — quiet day!</div>`;
    return;
  }

  list.innerHTML = tickets.map(t => `
    <div class="ticket-item">
      <span class="ticket-num">${t.ticketNumber}</span>
      <span class="ticket-title">${t.title}</span>
      <span class="badge badge-${t.priority.toLowerCase()}">${t.priority}</span>
      <span class="badge badge-${t.status.toLowerCase()}">${formatStatus(t.status)}</span>
    </div>
  `).join("");
}

function renderNetworkBars(devices) {
  const container = document.getElementById("network-bars");
  container.innerHTML = devices.map(d => `
    <div class="network-device-row">
      <div class="device-status ${d.isOnline ? 'up' : 'down'}"></div>
      <span class="device-ip">${d.ipAddress}</span>
      <span class="device-hostname">${d.hostname || "—"}</span>
      <span class="device-ping">${d.isOnline ? (d.pingMs + "ms") : "—"}</span>
    </div>
  `).join("");
}

// ─── TICKETS ──────────────────────────────────────────────────────────────────

let allTickets = [];
let activeFilter = "ALL";

async function loadTickets() {
  const data = await apiFetch("/tickets");
  allTickets = data || MOCK_TICKETS;
  renderTicketsTable(allTickets);
}

function renderTicketsTable(tickets) {
  const filtered = activeFilter === "ALL"
    ? tickets
    : tickets.filter(t => t.status === activeFilter);

  const tbody = document.getElementById("tickets-tbody");

  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--ink-muted)">No tickets in this category</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(t => `
    <tr>
      <td class="ticket-num-cell">${t.ticketNumber}</td>
      <td style="max-width:260px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${t.title}</td>
      <td><span class="badge badge-${t.priority.toLowerCase()}">${t.priority}</span></td>
      <td><span class="badge badge-${t.status.toLowerCase()}">${formatStatus(t.status)}</span></td>
      <td>${t.department || "—"}</td>
      <td>${t.raisedBy || "—"}</td>
      <td>${t.assignedTo || '<span style="color:var(--ink-muted)">Unassigned</span>'}</td>
      <td>
        ${t.status === "OPEN" ? `<button class="action-btn" onclick="quickAssign(${t.id})">Assign</button>` : ""}
        ${t.status === "IN_PROGRESS" ? `<button class="action-btn" onclick="quickResolve(${t.id})">Resolve</button>` : ""}
        ${t.status === "RESOLVED" ? `<span style="font-size:11px;color:var(--status-green)">✓ Done</span>` : ""}
      </td>
    </tr>
  `).join("");
}

async function quickAssign(ticketId) {
  const name = prompt("Assign to (technician name):");
  if (!name) return;

  const result = await apiFetch(`/tickets/${ticketId}/assign`, {
    method: "PATCH",
    body: JSON.stringify({ assignedTo: name }),
  });

  if (result) {
    showToast(`Assigned to ${name}`, "success");
    loadTickets();
  } else {
    // Demo mode — update locally
    allTickets = allTickets.map(t =>
      t.id === ticketId ? { ...t, assignedTo: name, status: "IN_PROGRESS" } : t
    );
    renderTicketsTable(allTickets);
    showToast(`Assigned to ${name} (demo mode)`, "success");
  }
}

async function quickResolve(ticketId) {
  const notes = prompt("Resolution notes:");
  if (!notes) return;

  const result = await apiFetch(`/tickets/${ticketId}/resolve`, {
    method: "PATCH",
    body: JSON.stringify({ resolutionNotes: notes }),
  });

  if (result) {
    showToast("Ticket resolved ✓", "success");
    loadTickets();
  } else {
    allTickets = allTickets.map(t =>
      t.id === ticketId ? { ...t, status: "RESOLVED", resolutionNotes: notes } : t
    );
    renderTicketsTable(allTickets);
    showToast("Ticket resolved ✓ (demo mode)", "success");
  }
}

// ─── ASSETS ───────────────────────────────────────────────────────────────────

let allAssets = [];

async function loadAssets() {
  const data = await apiFetch("/assets");
  allAssets = data || MOCK_ASSETS;
  renderAssets(allAssets);
}

function renderAssets(assets) {
  const grid = document.getElementById("assets-grid");

  if (!assets.length) {
    grid.innerHTML = `<div class="loading-pulse">No assets found</div>`;
    return;
  }

  const typeIcons = {
    LAPTOP: "💻", DESKTOP: "🖥️", PRINTER: "🖨️", ROUTER: "📡",
    SWITCH: "🔀", SERVER: "🗄️", UPS: "🔋", MONITOR: "🖥️",
    PHONE: "📱", TABLET: "📱", LICENSE: "📋", OTHER: "📦"
  };

  grid.innerHTML = assets.map(a => `
    <div class="asset-card">
      <div class="asset-tag">${a.assetTag}</div>
      <div class="asset-name">${typeIcons[a.type] || "📦"} ${a.name}</div>
      <div class="asset-meta">${a.brand} ${a.model || ""} · ${a.department || "—"}</div>
      <span class="badge badge-${a.status === 'ACTIVE' ? 'resolved' : 'on_hold'}">${a.status}</span>
      <div class="asset-footer">
        <span class="asset-location">📍 ${a.location || "Unknown"}</span>
        ${a.purchaseCostKes
          ? `<span class="asset-cost">KES ${Number(a.purchaseCostKes).toLocaleString()}</span>`
          : ''}
      </div>
    </div>
  `).join("");
}

// ─── NETWORK ─────────────────────────────────────────────────────────────────

async function loadNetwork() {
  const data = await apiFetch("/network/devices");
  renderNetworkGrid(data || MOCK_DEVICES);
}

function renderNetworkGrid(devices) {
  const grid = document.getElementById("network-grid");

  if (!devices.length) {
    grid.innerHTML = `<div class="loading-pulse">No devices discovered. Run scanner.py to populate.</div>`;
    return;
  }

  // Sort: online first, then by IP
  const sorted = [...devices].sort((a, b) => {
    if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1;
    return a.ipAddress.localeCompare(b.ipAddress);
  });

  grid.innerHTML = sorted.map(d => `
    <div class="device-card ${d.isOnline ? 'online' : 'offline'}">
      <div class="device-card-status">${d.isOnline ? '● ONLINE' : '● OFFLINE'}</div>
      <div class="device-card-ip">${d.ipAddress}</div>
      <div class="device-card-host">${d.hostname || "No hostname"}</div>
      <div class="device-card-vendor">
        ${d.vendor || "Unknown vendor"}
        ${d.macAddress ? `<br><span style="font-family:'IBM Plex Mono',monospace;font-size:10px">${d.macAddress}</span>` : ''}
        ${d.pingMs ? `<br><span style="color:var(--mint-dark);font-size:11px">⚡ ${d.pingMs}ms</span>` : ''}
      </div>
    </div>
  `).join("");
}

// ─── NEW TICKET MODAL ─────────────────────────────────────────────────────────

function openTicketModal() {
  document.getElementById("ticket-modal").classList.add("open");
}

function closeTicketModal() {
  document.getElementById("ticket-modal").classList.remove("open");
  // Clear form
  ["t-title","t-department","t-raised-by","t-description"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });
  document.getElementById("t-priority").value = "MEDIUM";
  document.getElementById("t-category").value = "HARDWARE";
}

async function submitTicket() {
  const title    = document.getElementById("t-title").value.trim();
  const dept     = document.getElementById("t-department").value;
  const priority = document.getElementById("t-priority").value;
  const category = document.getElementById("t-category").value;
  const raisedBy = document.getElementById("t-raised-by").value.trim();
  const desc     = document.getElementById("t-description").value.trim();

  if (!title || !dept || !raisedBy) {
    showToast("Please fill in the required fields", "error");
    return;
  }

  const payload = { title, department: dept, priority, category, raisedBy, description: desc };

  const result = await apiFetch("/tickets", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  if (result) {
    showToast(`Ticket ${result.ticketNumber} created ✓`, "success");
  } else {
    // Demo mode — fake a ticket number
    const fakeNum = `TKT-2026-${String(Math.floor(Math.random() * 99999)).padStart(5,"0")}`;
    const newTicket = { id: Date.now(), ticketNumber: fakeNum, status: "OPEN",
                        title, priority, category, department: dept, raisedBy };
    MOCK_TICKETS.unshift(newTicket);
    showToast(`Ticket ${fakeNum} created ✓ (demo mode)`, "success");
  }

  closeTicketModal();
  if (document.getElementById("view-tickets").classList.contains("active")) {
    loadTickets();
  }
}

// ─── UTILITIES ────────────────────────────────────────────────────────────────

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function formatStatus(status) {
  const labels = {
    OPEN: "Open", IN_PROGRESS: "In Progress",
    RESOLVED: "Resolved", CLOSED: "Closed", ON_HOLD: "On Hold"
  };
  return labels[status] || status;
}

let toastTimer;
function showToast(msg, type = "") {
  const toast = document.getElementById("toast");
  toast.textContent = msg;
  toast.className = `toast show ${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.className = "toast"; }, 3500);
}

function updateClock() {
  const now = new Date();
  const options = { timeZone: "Africa/Nairobi", hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false };
  const timeStr = now.toLocaleTimeString("en-KE", options);
  const dateStr = now.toLocaleDateString("en-KE", { timeZone: "Africa/Nairobi", day: "2-digit", month: "short", year: "numeric" });
  setText("time-display", `${dateStr} · ${timeStr} EAT`);
}

// ─── EVENT LISTENERS ──────────────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", () => {
  // Navigation
  document.querySelectorAll("[data-view]").forEach(el => {
    el.addEventListener("click", e => {
      e.preventDefault();
      navigate(el.dataset.view);
    });
  });

  // Ticket modal
  document.getElementById("btn-new-ticket").addEventListener("click", openTicketModal);
  document.getElementById("btn-new-ticket-2")?.addEventListener("click", openTicketModal);
  document.getElementById("close-ticket-modal").addEventListener("click", closeTicketModal);
  document.getElementById("cancel-ticket").addEventListener("click", closeTicketModal);
  document.getElementById("submit-ticket").addEventListener("click", submitTicket);

  // Close modal on overlay click
  document.getElementById("ticket-modal").addEventListener("click", e => {
    if (e.target === e.currentTarget) closeTicketModal();
  });

  // Ticket filter tabs
  document.querySelectorAll(".filter-tab").forEach(btn => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".filter-tab").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      activeFilter = btn.dataset.filter;
      renderTicketsTable(allTickets);
    });
  });

  // Asset search
  document.getElementById("asset-search")?.addEventListener("input", e => {
    const q = e.target.value.toLowerCase();
    const filtered = allAssets.filter(a =>
      a.name.toLowerCase().includes(q) ||
      a.assetTag.toLowerCase().includes(q) ||
      (a.department || "").toLowerCase().includes(q) ||
      (a.brand || "").toLowerCase().includes(q)
    );
    renderAssets(filtered);
  });

  // Network refresh
  document.getElementById("btn-refresh-network")?.addEventListener("click", loadNetwork);

  // Start clock
  updateClock();
  setInterval(updateClock, 1000);

  // Auto-refresh dashboard every 60s
  setInterval(() => {
    if (document.getElementById("view-dashboard").classList.contains("active")) {
      loadDashboard();
    }
  }, 60_000);

  // Auto-refresh network every 30s
  setInterval(() => {
    if (document.getElementById("view-network").classList.contains("active")) {
      loadNetwork();
    }
  }, 30_000);

  // Initial load
  navigate("dashboard");
});
