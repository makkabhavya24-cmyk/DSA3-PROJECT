/**
 * OptiLogix - Logistics Optimization System
 * Front-end Client Application
 */

const API_BASE = '/api';

// Application State
const state = {
  currentUser: null,
  activeTab: 'network',
  warehouses: [],
  products: [],
  inventories: [],
  routes: [],
  orders: [],
  networkSummary: {},
  graphData: { nodes: [], edges: [] },
  activeFlowHops: [] // Currently inspected order's route hops for canvas overlay
};

// Initialization
document.addEventListener('DOMContentLoaded', () => {
  loadStoredUser();
  setupNavigation();
  setupEventListeners();
});

function loadStoredUser() {
  // Clear any persistent local storage so every new browser session starts at login
  localStorage.removeItem('optilogix_user');

  const saved = sessionStorage.getItem('optilogix_user');
  if (saved) {
    try {
      state.currentUser = JSON.parse(saved);
    } catch (e) {
      console.error(e);
      state.currentUser = null;
    }
  } else {
    state.currentUser = null;
  }
  updateUserUI();
  if (state.currentUser) {
    refreshAllData();
  }
}

function saveUser(user) {
  state.currentUser = user;
  sessionStorage.setItem('optilogix_user', JSON.stringify(user));
  updateUserUI();
}

function updateUserUI() {
  const loginView = document.getElementById('loginView');
  const appView = document.getElementById('appView');

  if (!state.currentUser) {
    if (loginView) loginView.style.display = 'flex';
    if (appView) appView.style.display = 'none';
    return;
  }

  // User is authenticated
  if (loginView) loginView.style.display = 'none';
  if (appView) appView.style.display = 'block';

  const nameEl = document.getElementById('userName');
  const roleEl = document.getElementById('userRole');

  if (nameEl) nameEl.textContent = state.currentUser.fullName || state.currentUser.username;
  if (roleEl) {
    roleEl.textContent = state.currentUser.role;
    roleEl.className = `role-badge ${state.currentUser.role.toLowerCase()}`;
  }

  // Toggle navigation tabs based strictly on user role
  const adminTabs = document.querySelectorAll('.admin-only');
  const userTabs = document.querySelectorAll('.user-only');

  if (state.currentUser.role === 'ADMIN') {
    adminTabs.forEach(el => el.style.display = 'flex');
    userTabs.forEach(el => el.style.display = 'none');
    if (state.activeTab === 'place-order' || state.activeTab === 'my-orders') {
      switchTab('network');
    } else {
      switchTab(state.activeTab || 'network');
    }
  } else {
    adminTabs.forEach(el => el.style.display = 'none');
    userTabs.forEach(el => el.style.display = 'flex');
    if (state.activeTab !== 'place-order' && state.activeTab !== 'my-orders') {
      switchTab('place-order');
    } else {
      switchTab(state.activeTab || 'place-order');
    }
  }
}

// Dedicated Logout Handler
function handleLogout() {
  state.currentUser = null;
  sessionStorage.removeItem('optilogix_user');
  localStorage.removeItem('optilogix_user');
  updateUserUI();

  const loginPass = document.getElementById('loginPassword');
  if (loginPass) loginPass.value = '';
  const loginErr = document.getElementById('loginErrorMsg');
  if (loginErr) loginErr.style.display = 'none';

  showToast('Logged out successfully', 'info');
}

// Auth Tabs Toggle (Sign In / Register)
function switchAuthTab(mode) {
  const loginForm = document.getElementById('loginForm');
  const regForm = document.getElementById('registerForm');
  const tabLoginBtn = document.getElementById('tabLoginBtn');
  const tabRegisterBtn = document.getElementById('tabRegisterBtn');
  const loginErr = document.getElementById('loginErrorMsg');
  const regErr = document.getElementById('regErrorMsg');
  if (loginErr) loginErr.style.display = 'none';
  if (regErr) regErr.style.display = 'none';

  if (mode === 'login') {
    if (loginForm) loginForm.style.display = 'block';
    if (regForm) regForm.style.display = 'none';
    if (tabLoginBtn) tabLoginBtn.classList.add('active');
    if (tabRegisterBtn) tabRegisterBtn.classList.remove('active');
  } else {
    if (loginForm) loginForm.style.display = 'none';
    if (regForm) regForm.style.display = 'block';
    if (tabLoginBtn) tabLoginBtn.classList.remove('active');
    if (tabRegisterBtn) tabRegisterBtn.classList.add('active');
  }
}

// Sign In Form Submission
async function handleLoginSubmit(e) {
  if (e) e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();
  const errEl = document.getElementById('loginErrorMsg');
  const submitBtn = document.getElementById('loginSubmitBtn');

  if (errEl) errEl.style.display = 'none';
  if (!username || !password) {
    if (errEl) {
      errEl.textContent = 'Please enter both username and password.';
      errEl.style.display = 'block';
    }
    return;
  }

  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Signing in...';
  }

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.message || 'Invalid username or password.');
    }

    saveUser({
      userId: data.userId,
      username: data.username,
      fullName: data.fullName,
      role: data.role
    });

    showToast(`Welcome back, ${data.fullName || data.username}!`, 'success');
    refreshAllData();
  } catch (err) {
    if (errEl) {
      errEl.textContent = err.message || 'Login failed. Please check credentials.';
      errEl.style.display = 'block';
    }
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Sign In to Portal →';
    }
  }
}

// Registration Form Submission
async function handleRegisterSubmit(e) {
  if (e) e.preventDefault();
  const fullName = document.getElementById('regFullName').value.trim();
  const username = document.getElementById('regUsername').value.trim();
  const password = document.getElementById('regPassword').value.trim();
  const role = document.getElementById('regRole').value;
  const errEl = document.getElementById('regErrorMsg');
  const submitBtn = document.getElementById('regSubmitBtn');

  if (errEl) errEl.style.display = 'none';
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Creating Account...';
  }

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, username, password, role })
    });
    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.message || 'Registration failed.');
    }

    saveUser({
      userId: data.userId,
      username: data.username,
      fullName: data.fullName,
      role: data.role
    });

    showToast(`Account created! Welcome, ${data.fullName}!`, 'success');
    refreshAllData();
  } catch (err) {
    if (errEl) {
      errEl.textContent = err.message || 'Registration failed.';
      errEl.style.display = 'block';
    }
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Create Account & Sign In →';
    }
  }
}

// 1-Click Demo Login
async function quickFillAndLogin(username, password) {
  switchAuthTab('login');
  const uInput = document.getElementById('loginUsername');
  const pInput = document.getElementById('loginPassword');
  if (uInput) uInput.value = username;
  if (pInput) pInput.value = password;
  await handleLoginSubmit(null);
}

// Navigation & Tab Switching
function setupNavigation() {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const tabId = btn.getAttribute('data-tab');
      switchTab(tabId);
    });
  });
}

function switchTab(tabId) {
  if (state.currentUser && state.currentUser.role === 'USER') {
    if (tabId !== 'place-order' && tabId !== 'my-orders') {
      tabId = 'place-order';
    }
  }
  state.activeTab = tabId;
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-content').forEach(content => {
    content.classList.toggle('active', content.id === `tab-${tabId}`);
  });

  if (tabId === 'network') {
    drawNetworkGraph();
  } else if (tabId === 'compare-routes') {
    populateCompareDropdowns();
  } else if (tabId === 'transfers') {
    populateTransferDropdowns();
  } else if (tabId === 'place-order') {
    populatePlaceOrderDropdowns();
  }
}

// Toast Notifications
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span> <div>${message}</div>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 200);
  }, 3500);
}

// API Helpers
async function fetchJson(url, options = {}) {
  try {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
    if (!res.ok) {
      const errData = await res.json().catch(() => ({ error: res.statusText }));
      throw new Error(errData.error || errData.message || 'API request failed');
    }
    return await res.json();
  } catch (err) {
    showToast(err.message, 'error');
    throw err;
  }
}

// Refresh Data from Backend
async function refreshAllData() {
  try {
    const [summary, whs, prods, invs, rts, ords, graph] = await Promise.all([
      fetchJson(`${API_BASE}/network/summary`),
      fetchJson(`${API_BASE}/warehouses`),
      fetchJson(`${API_BASE}/products`),
      fetchJson(`${API_BASE}/inventories`),
      fetchJson(`${API_BASE}/routes`),
      fetchJson(`${API_BASE}/orders`),
      fetchJson(`${API_BASE}/network/graph`)
    ]);

    state.networkSummary = summary;
    state.warehouses = whs;
    state.products = prods;
    state.inventories = invs;
    state.routes = rts;
    state.orders = ords;
    state.graphData = graph;

    renderDashboardMetrics();
    renderWarehousesTable();
    renderInventoryTable();
    renderRoutesTable();
    renderOrdersTable();
    renderMyOrdersTable();
    drawNetworkGraph();
  } catch (err) {
    console.error('Error refreshing data:', err);
  }
}

// Render Dashboard Metrics
function renderDashboardMetrics() {
  const s = state.networkSummary;
  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val !== undefined ? val : '-';
  };

  setVal('m-warehouses', s.totalWarehouses);
  setVal('m-products', s.totalProducts);
  setVal('m-stock', s.totalPhysicalStock);
  setVal('m-low-stock', s.lowStockCount);
  setVal('m-routes', `${s.activeRoutesCount}/${s.totalRoutes}`);
  setVal('m-orders', `${s.pendingOrdersCount} Pending`);
}

// Canvas Network Visualization
function drawNetworkGraph() {
  const canvas = document.getElementById('networkCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  // Handle high-DPI displays
  const rect = canvas.getBoundingClientRect();
  canvas.width = rect.width * window.devicePixelRatio;
  canvas.height = rect.height * window.devicePixelRatio;
  ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

  const width = rect.width;
  const height = rect.height;

  ctx.clearRect(0, 0, width, height);

  const nodes = state.graphData.nodes || [];
  const edges = state.graphData.edges || [];

  if (nodes.length === 0) return;

  // Compute node layout coordinates
  // Place Warehouses in central circle/mesh, Destinations on perimeter
  const nodePositions = {};
  const warehouses = nodes.filter(n => n.type === 'WAREHOUSE');
  const destinations = nodes.filter(n => n.type === 'DESTINATION');

  const centerX = width * 0.42;
  const centerY = height * 0.5;
  const whRadius = Math.min(width, height) * 0.32;

  warehouses.forEach((wh, idx) => {
    const angle = (idx / warehouses.length) * 2 * Math.PI - Math.PI / 2;
    nodePositions[wh.id] = {
      x: centerX + whRadius * Math.cos(angle),
      y: centerY + whRadius * Math.sin(angle),
      node: wh
    };
  });

  const destX = width * 0.85;
  destinations.forEach((dest, idx) => {
    const step = height / (destinations.length + 1);
    nodePositions[dest.id] = {
      x: destX,
      y: step * (idx + 1),
      node: dest
    };
  });

  // Identify active flow hops if an order plan is highlighted
  const activeHopSet = new Set();
  state.activeFlowHops.forEach(h => {
    activeHopSet.add(`${h.fromNode}->${h.toNode}`);
  });

  // Draw Edges (Routes)
  edges.forEach(edge => {
    const p1 = nodePositions[edge.source];
    const p2 = nodePositions[edge.target];
    if (!p1 || !p2) return;

    const isFlowActive = activeHopSet.has(`${edge.source}->${edge.target}`);

    ctx.beginPath();
    ctx.moveTo(p1.x, p1.y);
    ctx.lineTo(p2.x, p2.y);

    if (isFlowActive) {
      ctx.strokeStyle = '#00f2fe';
      ctx.lineWidth = 3.5;
      ctx.setLineDash([]);
      ctx.shadowColor = '#00f2fe';
      ctx.shadowBlur = 10;
    } else if (!edge.active) {
      ctx.strokeStyle = '#ef4444';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([4, 4]);
      ctx.shadowBlur = 0;
    } else {
      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 1.8;
      ctx.setLineDash([]);
      ctx.shadowBlur = 0;
    }

    ctx.stroke();
    ctx.shadowBlur = 0;
    ctx.setLineDash([]);

    // Draw small directional arrow
    const midX = (p1.x + p2.x) / 2;
    const midY = (p1.y + p2.y) / 2;
    const angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);

    ctx.save();
    ctx.translate(midX, midY);
    ctx.rotate(angle);
    ctx.fillStyle = isFlowActive ? '#00f2fe' : (edge.active ? '#64748b' : '#ef4444');
    ctx.beginPath();
    ctx.moveTo(-6, -4);
    ctx.lineTo(6, 0);
    ctx.lineTo(-6, 4);
    ctx.closePath();
    ctx.fill();

    // Cost / Capacity label
    ctx.fillStyle = isFlowActive ? '#38bdf8' : '#94a3b8';
    ctx.font = '10px system-ui';
    ctx.textAlign = 'center';
    ctx.fillText(`₹${edge.cost}/u (c:${edge.capacity})`, 0, -8);
    ctx.restore();
  });

  // Draw Nodes
  nodes.forEach(n => {
    const p = nodePositions[n.id];
    if (!p) return;

    const isWh = n.type === 'WAREHOUSE';
    const radius = isWh ? 16 : 14;

    ctx.beginPath();
    ctx.arc(p.x, p.y, radius, 0, 2 * Math.PI);
    ctx.fillStyle = isWh ? '#1d4ed8' : '#059669';
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = isWh ? '#60a5fa' : '#34d399';
    ctx.stroke();

    // Node Name Label
    ctx.fillStyle = '#f8fafc';
    ctx.font = 'bold 11px system-ui';
    ctx.textAlign = 'center';
    ctx.fillText(n.label, p.x, p.y + radius + 14);

    if (isWh) {
      ctx.fillStyle = '#94a3b8';
      ctx.font = '9px system-ui';
      ctx.fillText(n.city || '', p.x, p.y + radius + 25);
    }
  });
}

// Warehouses Table
function renderWarehousesTable() {
  const tbody = document.getElementById('warehousesTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  state.warehouses.forEach(wh => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${wh.code}</strong></td>
      <td>${wh.name}</td>
      <td>${wh.city}</td>
      <td>${wh.address || '-'}</td>
      <td><strong>${wh.storageCapacity.toLocaleString()} units</strong></td>
      <td><span class="text-muted">${wh.latitude.toFixed(2)}, ${wh.longitude.toFixed(2)}</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="inspectWarehouseConnections(${wh.id})">🔍 Connections</button>
        <button class="btn btn-secondary btn-sm" onclick="openEditWarehouseModal(${wh.id})">✏ Edit</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// Warehouse Connections Inspector
async function inspectWarehouseConnections(whId) {
  try {
    const data = await fetchJson(`${API_BASE}/warehouses/${whId}/connections`);
    const wh = data.warehouse;
    const modal = document.getElementById('connectionsModal');
    const title = document.getElementById('connectionsModalTitle');
    const body = document.getElementById('connectionsModalBody');

    title.textContent = `Logistics Node: ${wh.name} (${wh.code})`;

    let html = `
      <div style="margin-bottom: 1rem;">
        <p><strong>City:</strong> ${wh.city} | <strong>Capacity:</strong> ${wh.storageCapacity.toLocaleString()} units</p>
        <p><strong>Coordinates:</strong> ${wh.latitude}, ${wh.longitude}</p>
      </div>

      <h4 style="margin: 0.75rem 0 0.5rem; color: var(--accent-primary);">Outgoing Shipping Routes (${data.outgoingRoutes.length})</h4>
      <div class="table-responsive">
        <table>
          <thead>
            <tr><th>Route Code</th><th>Destination</th><th>Unit Cost</th><th>Max Capacity</th><th>Distance</th><th>Status</th></tr>
          </thead>
          <tbody>
            ${data.outgoingRoutes.map(r => `
              <tr>
                <td><strong>${r.routeCode}</strong></td>
                <td>${r.destinationName}</td>
                <td>₹${r.unitCost.toFixed(2)}/unit</td>
                <td>${r.capacity} units</td>
                <td>${r.distanceKm} km (${r.travelTimeHours}h)</td>
                <td><span class="badge ${r.active ? 'badge-active' : 'badge-disabled'}">${r.active ? 'ACTIVE' : 'BLOCKED'}</span></td>
              </tr>
            `).join('') || '<tr><td colspan="6" class="text-muted">No outgoing routes configured</td></tr>'}
          </tbody>
        </table>
      </div>

      <h4 style="margin: 1.25rem 0 0.5rem; color: var(--accent-success);">Incoming Shipping Routes (${data.incomingRoutes.length})</h4>
      <div class="table-responsive">
        <table>
          <thead>
            <tr><th>Route Code</th><th>Origin</th><th>Unit Cost</th><th>Max Capacity</th><th>Distance</th><th>Status</th></tr>
          </thead>
          <tbody>
            ${data.incomingRoutes.map(r => `
              <tr>
                <td><strong>${r.routeCode}</strong></td>
                <td>${r.sourceName}</td>
                <td>₹${r.unitCost.toFixed(2)}/unit</td>
                <td>${r.capacity} units</td>
                <td>${r.distanceKm} km (${r.travelTimeHours}h)</td>
                <td><span class="badge ${r.active ? 'badge-active' : 'badge-disabled'}">${r.active ? 'ACTIVE' : 'BLOCKED'}</span></td>
              </tr>
            `).join('') || '<tr><td colspan="6" class="text-muted">No incoming routes configured</td></tr>'}
          </tbody>
        </table>
      </div>

      <h4 style="margin: 1.25rem 0 0.5rem; color: var(--accent-purple);">Stored Inventory (${data.inventories.length} SKUs)</h4>
      <div class="table-responsive">
        <table>
          <thead>
            <tr><th>Product</th><th>SKU</th><th>Available Units</th><th>Reserved</th><th>Threshold</th><th>Alert</th></tr>
          </thead>
          <tbody>
            ${data.inventories.map(i => `
              <tr>
                <td><strong>${i.product.name}</strong></td>
                <td>${i.product.sku}</td>
                <td><strong>${i.availableQuantity}</strong></td>
                <td>${i.reservedQuantity}</td>
                <td>${i.product.minStockThreshold}</td>
                <td>${i.lowStock ? '<span class="badge badge-low-stock">LOW STOCK</span>' : '<span class="badge badge-active">OK</span>'}</td>
              </tr>
            `).join('') || '<tr><td colspan="6" class="text-muted">No products stored in this warehouse</td></tr>'}
          </tbody>
        </table>
      </div>
    `;

    body.innerHTML = html;
    openModal('connectionsModal');
  } catch (e) {
    console.error(e);
  }
}

// Inventory Table
function renderInventoryTable() {
  const tbody = document.getElementById('inventoryTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const whFilter = document.getElementById('invWarehouseFilter')?.value;
  const lowOnly = document.getElementById('invLowStockToggle')?.checked;

  let filtered = state.inventories;
  if (whFilter) {
    filtered = filtered.filter(i => i.warehouse.id == whFilter);
  }
  if (lowOnly) {
    filtered = filtered.filter(i => i.lowStock);
  }

  filtered.forEach(inv => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${inv.warehouse.name}</strong> <span class="text-muted">(${inv.warehouse.code})</span></td>
      <td><strong>${inv.product.name}</strong></td>
      <td>${inv.product.sku}</td>
      <td>${inv.product.category || '-'}</td>
      <td>₹${inv.product.unitPrice.toFixed(2)}</td>
      <td><strong style="font-size: 1.05rem; color: ${inv.lowStock ? '#f87171' : '#34d399'}">${inv.availableQuantity}</strong></td>
      <td>${inv.reservedQuantity}</td>
      <td>${inv.product.minStockThreshold}</td>
      <td>${inv.lowStock ? '<span class="badge badge-low-stock">LOW STOCK</span>' : '<span class="badge badge-active">HEALTHY</span>'}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="adjustInventoryStock(${inv.id}, 10)">+10</button>
        <button class="btn btn-secondary btn-sm" onclick="adjustInventoryStock(${inv.id}, -10)">-10</button>
        <button class="btn btn-secondary btn-sm" onclick="promptCustomStock(${inv.id})">Custom ±</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function adjustInventoryStock(invId, delta) {
  try {
    await fetchJson(`${API_BASE}/inventories/${invId}/adjust`, {
      method: 'PUT',
      body: JSON.stringify({ deltaQuantity: delta })
    });
    showToast(`Stock updated by ${delta > 0 ? '+' : ''}${delta}`, 'success');
    refreshAllData();
  } catch (e) {
    // Error toast shown by fetchJson
  }
}

function promptCustomStock(invId) {
  const deltaStr = prompt('Enter quantity change (e.g. +50 to restock, or -20 to remove):', '25');
  if (deltaStr !== null) {
    const delta = parseInt(deltaStr.trim(), 10);
    if (!isNaN(delta) && delta !== 0) {
      adjustInventoryStock(invId, delta);
    }
  }
}

// Routes Table
function renderRoutesTable() {
  const tbody = document.getElementById('routesTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  state.routes.forEach(route => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${route.routeCode}</strong></td>
      <td>${route.sourceName}</td>
      <td>${route.destinationName}</td>
      <td><span class="badge ${route.routeType === 'WAREHOUSE_TO_WAREHOUSE' ? 'badge-optimized' : 'badge-in_transit'}">${route.routeType.replace(/_/g, ' ')}</span></td>
      <td><strong>₹${route.unitCost.toFixed(2)} / unit</strong></td>
      <td><strong>${route.capacity} units</strong></td>
      <td>${route.distanceKm} km</td>
      <td>${route.travelTimeHours} hrs</td>
      <td>
        <span class="badge ${route.active ? 'badge-active' : 'badge-disabled'}">
          ${route.active ? 'ACTIVE' : 'BLOCKED'}
        </span>
      </td>
      <td>
        <button class="btn ${route.active ? 'btn-danger' : 'btn-success'} btn-sm" onclick="toggleRouteStatus(${route.id})">
          ${route.active ? 'Block / Disable' : 'Enable Route'}
        </button>
        <button class="btn btn-secondary btn-sm" onclick="openEditRouteModal(${route.id})">Edit</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function toggleRouteStatus(routeId) {
  try {
    const updated = await fetchJson(`${API_BASE}/routes/${routeId}/toggle`, { method: 'PATCH' });
    showToast(`Route ${updated.routeCode} is now ${updated.active ? 'ACTIVE' : 'DISABLED'}`, 'info');
    refreshAllData();
  } catch (e) {}
}

// Orders Management Table (Admin)
function renderOrdersTable() {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  state.orders.forEach(order => {
    const tr = document.createElement('tr');
    const isPending = order.status === 'PENDING';
    const isOptimized = order.status === 'OPTIMIZED';
    const isDispatched = order.status === 'DISPATCHED';
    const isInTransit = order.status === 'IN_TRANSIT';

    tr.innerHTML = `
      <td><strong>${order.orderNumber}</strong></td>
      <td>${order.user?.fullName || order.user?.username || 'Client'}</td>
      <td><strong>${order.product.name}</strong> (${order.product.sku})</td>
      <td><strong>${order.quantity} units</strong></td>
      <td><span class="badge ${order.orderType === 'TRANSFER' ? 'badge-dispatched' : 'badge-in_transit'}">${order.orderType}</span></td>
      <td>${order.sourceWarehouse ? order.sourceWarehouse.name : '<span class="text-muted">Auto (Cheapest MCMF)</span>'}</td>
      <td><strong>${order.orderType === 'TRANSFER' ? (order.destinationWarehouse?.name || '-') : order.destinationLocation}</strong></td>
      <td><span class="badge badge-${order.status.toLowerCase()}">${order.status}</span></td>
      <td>
        ${order.shipmentPlan ? `
          <button class="btn btn-primary btn-sm" onclick="viewShipmentPlan(${order.id})">
            Plan (₹${order.shipmentPlan.totalTransportationCost.toFixed(2)})
          </button>
        ` : '-'}
      </td>
      <td>
        ${isPending ? `
          <button class="btn btn-warning btn-sm" onclick="optimizeOrder(${order.id})">⚡ Run MCMF</button>
        ` : ''}
        ${isOptimized ? `
          <button class="btn btn-success btn-sm" onclick="updateOrderStatus(${order.id}, 'DISPATCHED')">🚚 Dispatch</button>
        ` : ''}
        ${isDispatched ? `
          <button class="btn btn-primary btn-sm" onclick="updateOrderStatus(${order.id}, 'IN_TRANSIT')">Transit</button>
        ` : ''}
        ${isInTransit ? `
          <button class="btn btn-success btn-sm" onclick="updateOrderStatus(${order.id}, 'DELIVERED')">✓ Mark Delivered</button>
        ` : ''}
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// My Orders Table (User Portal)
function renderMyOrdersTable() {
  const tbody = document.getElementById('myOrdersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const userOrders = state.currentUser ? state.orders.filter(o => o.user?.id === state.currentUser.userId) : [];

  userOrders.forEach(order => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${order.orderNumber}</strong></td>
      <td>${order.product.name}</td>
      <td><strong>${order.quantity} units</strong></td>
      <td>${order.orderType}</td>
      <td>${order.orderType === 'TRANSFER' ? (order.destinationWarehouse?.name || '-') : order.destinationLocation}</td>
      <td><span class="badge badge-${order.status.toLowerCase()}">${order.status}</span></td>
      <td>
        ${order.shipmentPlan ? `
          <span style="color: var(--accent-success); font-weight: 600;">
            ₹${order.shipmentPlan.totalTransportationCost.toFixed(2)} (${order.shipmentPlan.fulfilledQuantity}/${order.quantity} units)
          </span>
          <button class="btn btn-secondary btn-sm" style="margin-left: 0.5rem;" onclick="viewShipmentPlan(${order.id})">View Route</button>
        ` : '<span class="text-muted">Awaiting Admin MCMF Optimization</span>'}
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// MCMF Optimization
async function optimizeOrder(orderId) {
  try {
    showToast('Executing Min-Cost Max-Flow algorithm...', 'info');
    const order = await fetchJson(`${API_BASE}/orders/${orderId}/optimize`, { method: 'POST' });
    showToast(`Order ${order.orderNumber} successfully optimized!`, 'success');
    await refreshAllData();
    viewShipmentPlan(orderId);
  } catch (e) {}
}

async function updateOrderStatus(orderId, status) {
  try {
    const updated = await fetchJson(`${API_BASE}/orders/${orderId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    });
    showToast(`Order status updated to ${status}`, 'success');
    refreshAllData();
  } catch (e) {}
}

// Shipment Plan Modal Inspector
function viewShipmentPlan(orderId) {
  const order = state.orders.find(o => o.id === orderId);
  if (!order || !order.shipmentPlan) return;

  const plan = order.shipmentPlan;
  const modal = document.getElementById('shipmentPlanModal');
  const title = document.getElementById('shipmentPlanTitle');
  const body = document.getElementById('shipmentPlanBody');

  title.textContent = `MCMF Optimal Shipping Plan: ${order.orderNumber}`;

  // Store active flow hops for canvas highlighting
  state.activeFlowHops = plan.hops;
  drawNetworkGraph();

  let html = `
    <div class="optimization-box">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
        <span class="badge ${plan.feasibilityStatus === 'OPTIMAL_FULFILLED' ? 'badge-delivered' : 'badge-pending'}" style="font-size: 0.85rem;">
          ${plan.feasibilityStatus.replace(/_/g, ' ')}
        </span>
        <span class="text-muted" style="font-size: 0.8rem;">Calculated at: ${new Date(plan.calculatedAt).toLocaleTimeString()}</span>
      </div>
      <p style="margin: 0.5rem 0; font-size: 0.95rem;">${plan.bottleneckSummary}</p>
      <div class="route-stats-grid" style="margin-top: 0.75rem;">
        <div class="route-stat-box"><span>Fulfilled / Requested</span><strong>${plan.fulfilledQuantity} / ${plan.requestedQuantity} units</strong></div>
        <div class="route-stat-box"><span>Unfulfilled Demand</span><strong style="color: ${plan.unfulfilledQuantity > 0 ? '#ef4444' : '#10b981'}">${plan.unfulfilledQuantity} units</strong></div>
        <div class="route-stat-box"><span>Total Transportation Cost</span><strong style="color: #38bdf8;">₹${plan.totalTransportationCost.toFixed(2)}</strong></div>
        <div class="route-stat-box"><span>Average Cost / Unit</span><strong>₹${plan.averageUnitCost.toFixed(2)} / unit</strong></div>
      </div>
    </div>

    <h4 style="margin: 1.25rem 0 0.5rem; color: #fff;">Flow Decomposition Along Selected Routes</h4>
    <div class="table-responsive">
      <table>
        <thead>
          <tr><th>From Origin</th><th>To Destination</th><th>Quantity Shipped</th><th>Unit Cost</th><th>Subtotal Cost</th><th>Distance & Time</th></tr>
        </thead>
        <tbody>
          ${plan.hops.map(h => `
            <tr>
              <td><strong>${h.fromNode}</strong></td>
              <td><strong>${h.toNode}</strong></td>
              <td><span class="badge badge-optimized" style="font-size: 0.85rem;">${h.quantity} units</span></td>
              <td>₹${h.unitCost.toFixed(2)}</td>
              <td><strong>₹${h.subtotalCost.toFixed(2)}</strong></td>
              <td>${h.distanceKm} km (${h.travelTimeHours} hrs)</td>
            </tr>
          `).join('') || '<tr><td colspan="6" class="text-muted">No route flow allocated</td></tr>'}
        </tbody>
      </table>
    </div>
  `;

  body.innerHTML = html;
  openModal('shipmentPlanModal');
}

// Alternative Route Comparison Tool
function populateCompareDropdowns() {
  const srcSelect = document.getElementById('compareSourceSelect');
  const dstSelect = document.getElementById('compareDestSelect');
  if (!srcSelect || !dstSelect) return;

  srcSelect.innerHTML = '';
  dstSelect.innerHTML = '';

  state.warehouses.forEach(w => {
    srcSelect.innerHTML += `<option value="${w.name}">${w.name} (${w.city})</option>`;
  });

  // Destinations can be other warehouses or delivery centers
  const destinations = new Set();
  state.routes.forEach(r => {
    destinations.add(r.getDestinationName ? r.getDestinationName() : r.destinationName);
  });

  destinations.forEach(d => {
    dstSelect.innerHTML += `<option value="${d}">${d}</option>`;
  });
}

async function runRouteComparison() {
  const source = document.getElementById('compareSourceSelect').value;
  const destination = document.getElementById('compareDestSelect').value;
  if (!source || !destination) return;

  try {
    const res = await fetchJson(`${API_BASE}/routes/compare?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`);
    renderRouteComparisonResults(res);
  } catch (e) {}
}

function renderRouteComparisonResults(res) {
  const container = document.getElementById('compareResultsContainer');
  if (!container) return;

  const best = res.bestRoute;
  const alt = res.secondBestRoute;

  let html = `
    <div style="background-color: #172338; border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 1rem; margin-top: 1rem;">
      <h4 style="color: var(--accent-cyan); margin-bottom: 0.4rem;">Graph & MCMF Comparative Analysis</h4>
      <p style="font-size: 0.9rem; color: #e2e8f0;">${res.analysis}</p>
    </div>

    <div class="comparison-container">
      <!-- Best Route Card -->
      <div class="route-card best">
        <span class="route-card-tag">Cheapest Best Route</span>
        <h3 style="color: #60a5fa; margin-bottom: 0.5rem;">${best ? best.routeDescription : 'None Available'}</h3>
        ${best ? `
          <div class="route-stats-grid">
            <div class="route-stat-box"><span>Unit Shipping Cost</span><strong style="color: #60a5fa;">₹${best.totalUnitCost.toFixed(2)} / unit</strong></div>
            <div class="route-stat-box"><span>Bottleneck Capacity</span><strong>${best.bottleneckCapacity} units</strong></div>
            <div class="route-stat-box"><span>Total Route Distance</span><strong>${best.totalDistanceKm} km</strong></div>
            <div class="route-stat-box"><span>Estimated Travel Time</span><strong>${best.totalTravelTimeHours} hrs</strong></div>
          </div>
          <div style="margin-top: 1rem;">
            <p style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.4rem;">Simulate Route Disruption:</p>
            ${best.routeIds.map(rid => {
              const r = state.routes.find(x => x.id === rid);
              return r ? `<button class="btn btn-danger btn-sm" style="margin-right: 0.5rem; margin-bottom: 0.5rem;" onclick="toggleRouteAndRecompare(${r.id})">Simulate Block (${r.routeCode})</button>` : '';
            }).join('')}
          </div>
        ` : '<p class="text-muted" style="margin-top: 1rem;">No viable path found.</p>'}
      </div>

      <!-- Second Best Route Card -->
      <div class="route-card second">
        <span class="route-card-tag">2nd-Best Alternative</span>
        <h3 style="color: #c084fc; margin-bottom: 0.5rem;">${alt ? alt.routeDescription : 'No Alternative Path'}</h3>
        ${alt ? `
          <div class="route-stats-grid">
            <div class="route-stat-box"><span>Unit Shipping Cost</span><strong style="color: #c084fc;">₹${alt.totalUnitCost.toFixed(2)} / unit</strong></div>
            <div class="route-stat-box"><span>Bottleneck Capacity</span><strong>${alt.bottleneckCapacity} units</strong></div>
            <div class="route-stat-box"><span>Total Route Distance</span><strong>${alt.totalDistanceKm} km</strong></div>
            <div class="route-stat-box"><span>Estimated Travel Time</span><strong>${alt.totalTravelTimeHours} hrs</strong></div>
          </div>
        ` : '<p class="text-muted" style="margin-top: 1rem;">No alternative path under current active route constraints.</p>'}
      </div>
    </div>
  `;

  container.innerHTML = html;
}

async function toggleRouteAndRecompare(routeId) {
  await toggleRouteStatus(routeId);
  runRouteComparison();
}

// Inter-Warehouse Transfer Wizard
function populateTransferDropdowns() {
  const destSelect = document.getElementById('transferDestWhSelect');
  const prodSelect = document.getElementById('transferProductSelect');
  if (!destSelect || !prodSelect) return;

  destSelect.innerHTML = '<option value="">Select Destination Warehouse</option>';
  state.warehouses.forEach(w => {
    destSelect.innerHTML += `<option value="${w.id}">${w.name} (${w.city})</option>`;
  });

  prodSelect.innerHTML = '<option value="">Select Product</option>';
  state.products.forEach(p => {
    prodSelect.innerHTML += `<option value="${p.id}">${p.name} (${p.sku})</option>`;
  });

  destSelect.onchange = checkTransferCandidates;
  prodSelect.onchange = checkTransferCandidates;
}

async function checkTransferCandidates() {
  const destId = document.getElementById('transferDestWhSelect').value;
  const prodId = document.getElementById('transferProductSelect').value;
  const candidatesBox = document.getElementById('transferCandidatesBox');
  const srcSelect = document.getElementById('transferSourceWhSelect');

  if (!destId || !prodId) {
    if (candidatesBox) candidatesBox.innerHTML = '<p class="text-muted">Select destination warehouse and product to scan available stock sources.</p>';
    return;
  }

  try {
    const candidates = await fetchJson(`${API_BASE}/transfers/candidates?productId=${prodId}&destinationWarehouseId=${destId}`);

    if (srcSelect) {
      srcSelect.innerHTML = '<option value="">Auto-Select Optimal Source via MCMF</option>';
      candidates.forEach(c => {
        srcSelect.innerHTML += `<option value="${c.warehouseId}">${c.warehouseName} (Stock: ${c.availableQuantity})</option>`;
      });
    }

    if (candidatesBox) {
      if (candidates.length === 0) {
        candidatesBox.innerHTML = '<p style="color: var(--accent-danger);">No other warehouses currently have stock for this product!</p>';
      } else {
        candidatesBox.innerHTML = `
          <div style="font-size: 0.85rem; margin-top: 0.5rem;">
            <p style="color: var(--accent-success); font-weight: 600; margin-bottom: 0.4rem;">Candidate Source Warehouses with Inventory:</p>
            <ul style="padding-left: 1.25rem;">
              ${candidates.map(c => `
                <li><strong>${c.warehouseName}</strong> (${c.city}): <span style="color: #38bdf8;">${c.availableQuantity} available units</span></li>
              `).join('')}
            </ul>
          </div>
        `;
      }
    }
  } catch (e) {}
}

async function executeInterWarehouseTransfer() {
  const destId = document.getElementById('transferDestWhSelect').value;
  const prodId = document.getElementById('transferProductSelect').value;
  const qty = parseInt(document.getElementById('transferQtyInput').value, 10);
  const srcIdVal = document.getElementById('transferSourceWhSelect').value;
  const srcId = srcIdVal ? parseInt(srcIdVal, 10) : null;

  if (!destId || !prodId || !qty || qty <= 0) {
    showToast('Please fill in all required fields with a valid quantity.', 'error');
    return;
  }

  try {
    showToast('Executing MCMF transfer route calculation...', 'info');
    const order = await fetchJson(`${API_BASE}/transfers/execute`, {
      method: 'POST',
      body: JSON.stringify({
        productId: prodId,
        destinationWarehouseId: destId,
        quantity: qty,
        sourceWarehouseId: srcId
      })
    });

    showToast(`Transfer order ${order.orderNumber} created and optimized!`, 'success');
    await refreshAllData();
    viewShipmentPlan(order.id);
  } catch (e) {}
}

// User Place Order Portal
function populatePlaceOrderDropdowns() {
  const prodSelect = document.getElementById('orderProductSelect');
  const destSelect = document.getElementById('orderDestSelect');
  const srcSelect = document.getElementById('orderSourceSelect');
  if (!prodSelect || !destSelect || !srcSelect) return;

  prodSelect.innerHTML = '<option value="">Select a Product</option>';
  state.products.forEach(p => {
    prodSelect.innerHTML += `<option value="${p.id}">${p.name} (₹${p.unitPrice.toFixed(2)})</option>`;
  });

  destSelect.innerHTML = '<option value="">Select Delivery Destination</option>';
  // Add unique destinations from routes
  const destinations = new Set();
  state.routes.forEach(r => {
    destinations.add(r.destinationName);
  });
  destinations.forEach(d => {
    destSelect.innerHTML += `<option value="${d}">${d}</option>`;
  });

  srcSelect.innerHTML = '<option value="">Auto-Assign (MCMF Least-Cost Optimization)</option>';
  state.warehouses.forEach(w => {
    srcSelect.innerHTML += `<option value="${w.id}">${w.name} (${w.city})</option>`;
  });

  prodSelect.onchange = updateStockPreview;
}

function updateStockPreview() {
  const prodId = document.getElementById('orderProductSelect').value;
  const preview = document.getElementById('orderStockPreview');
  if (!preview) return;

  if (!prodId) {
    preview.innerHTML = '';
    return;
  }

  const invs = state.inventories.filter(i => i.product.id == prodId);
  const totalStock = invs.reduce((sum, i) => sum + i.availableQuantity, 0);

  preview.innerHTML = `
    <div style="background-color: #121d2f; border: 1px solid var(--border-color); padding: 0.75rem; border-radius: var(--radius-sm); margin-top: 0.5rem;">
      <p style="font-size: 0.85rem; color: #38bdf8;"><strong>Total Network Stock Available:</strong> ${totalStock} units across ${invs.length} warehouses</p>
    </div>
  `;
}

async function submitUserOrder() {
  const prodId = document.getElementById('orderProductSelect').value;
  const dest = document.getElementById('orderDestSelect').value;
  const srcIdVal = document.getElementById('orderSourceSelect').value;
  const qty = parseInt(document.getElementById('orderQtyInput').value, 10);

  if (!prodId || !dest || !qty || qty <= 0) {
    showToast('Please select product, destination, and valid quantity.', 'error');
    return;
  }

  try {
    const order = await fetchJson(`${API_BASE}/orders`, {
      method: 'POST',
      body: JSON.stringify({
        userId: state.currentUser.userId,
        productId: prodId,
        quantity: qty,
        orderType: 'DELIVERY',
        destinationLocation: dest,
        sourceWarehouseId: srcIdVal ? parseInt(srcIdVal, 10) : null
      })
    });

    showToast(`Order ${order.orderNumber} placed successfully!`, 'success');
    await refreshAllData();
    switchTab('my-orders');
  } catch (e) {}
}

// Modal Helpers
function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('active');
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('active');
}

// Form Modals (Add Warehouse, Add Route, Add Product, Assign Stock)
function setupEventListeners() {
  // Modal background click to close
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('active');
      }
    });
  });



  // Form Submissions
  const addWhForm = document.getElementById('addWarehouseForm');
  if (addWhForm) {
    addWhForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await fetchJson(`${API_BASE}/warehouses`, {
          method: 'POST',
          body: JSON.stringify({
            code: document.getElementById('whCode').value.trim(),
            name: document.getElementById('whName').value.trim(),
            city: document.getElementById('whCity').value.trim(),
            address: document.getElementById('whAddress').value.trim(),
            storageCapacity: parseInt(document.getElementById('whCapacity').value, 10),
            latitude: parseFloat(document.getElementById('whLat').value),
            longitude: parseFloat(document.getElementById('whLon').value)
          })
        });
        showToast('Warehouse added successfully', 'success');
        closeModal('addWarehouseModal');
        addWhForm.reset();
        refreshAllData();
      } catch (err) {}
    });
  }

  const addRouteForm = document.getElementById('addRouteForm');
  if (addRouteForm) {
    addRouteForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await fetchJson(`${API_BASE}/routes`, {
          method: 'POST',
          body: JSON.stringify({
            routeCode: document.getElementById('rtCode').value.trim(),
            sourceName: document.getElementById('rtSource').value.trim(),
            destinationName: document.getElementById('rtDest').value.trim(),
            routeType: document.getElementById('rtType').value,
            unitCost: parseFloat(document.getElementById('rtCost').value),
            capacity: parseInt(document.getElementById('rtCap').value, 10),
            distanceKm: parseFloat(document.getElementById('rtDist').value),
            travelTimeHours: parseFloat(document.getElementById('rtTime').value),
            active: true
          })
        });
        showToast('Route created successfully', 'success');
        closeModal('addRouteModal');
        addRouteForm.reset();
        refreshAllData();
      } catch (err) {}
    });
  }

  const addProductForm = document.getElementById('addProductForm');
  if (addProductForm) {
    addProductForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await fetchJson(`${API_BASE}/products`, {
          method: 'POST',
          body: JSON.stringify({
            sku: document.getElementById('pSku').value.trim(),
            name: document.getElementById('pName').value.trim(),
            category: document.getElementById('pCategory').value.trim(),
            unitPrice: parseFloat(document.getElementById('pPrice').value),
            unitWeightKg: parseFloat(document.getElementById('pWeight').value),
            minStockThreshold: parseInt(document.getElementById('pThreshold').value, 10)
          })
        });
        showToast('Product registered successfully', 'success');
        closeModal('addProductModal');
        addProductForm.reset();
        refreshAllData();
      } catch (err) {}
    });
  }

  const assignStockForm = document.getElementById('assignStockForm');
  if (assignStockForm) {
    assignStockForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await fetchJson(`${API_BASE}/inventories`, {
          method: 'POST',
          body: JSON.stringify({
            warehouseId: document.getElementById('assignWhSelect').value,
            productId: document.getElementById('assignProdSelect').value,
            quantity: parseInt(document.getElementById('assignQty').value, 10)
          })
        });
        showToast('Inventory assigned successfully', 'success');
        closeModal('assignStockModal');
        assignStockForm.reset();
        refreshAllData();
      } catch (err) {}
    });
  }
}

function openAddWarehouseModal() {
  openModal('addWarehouseModal');
}

function openAddRouteModal() {
  openModal('addRouteModal');
}

function openAddProductModal() {
  openModal('addProductModal');
}

function openAssignStockModal() {
  const whSel = document.getElementById('assignWhSelect');
  const prdSel = document.getElementById('assignProdSelect');
  if (whSel && prdSel) {
    whSel.innerHTML = state.warehouses.map(w => `<option value="${w.id}">${w.name}</option>`).join('');
    prdSel.innerHTML = state.products.map(p => `<option value="${p.id}">${p.name} (${p.sku})</option>`).join('');
  }
  openModal('assignStockModal');
}

function openEditWarehouseModal(id) {
  const wh = state.warehouses.find(w => w.id === id);
  if (!wh) return;
  document.getElementById('editWhId').value = wh.id;
  document.getElementById('editWhCode').value = wh.code;
  document.getElementById('editWhName').value = wh.name;
  document.getElementById('editWhCity').value = wh.city;
  document.getElementById('editWhAddress').value = wh.address || '';
  document.getElementById('editWhCapacity').value = wh.storageCapacity;
  document.getElementById('editWhLat').value = wh.latitude;
  document.getElementById('editWhLon').value = wh.longitude;
  openModal('editWarehouseModal');
}

async function saveEditWarehouse(e) {
  e.preventDefault();
  const id = document.getElementById('editWhId').value;
  try {
    await fetchJson(`${API_BASE}/warehouses/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
        code: document.getElementById('editWhCode').value.trim(),
        name: document.getElementById('editWhName').value.trim(),
        city: document.getElementById('editWhCity').value.trim(),
        address: document.getElementById('editWhAddress').value.trim(),
        storageCapacity: parseInt(document.getElementById('editWhCapacity').value, 10),
        latitude: parseFloat(document.getElementById('editWhLat').value),
        longitude: parseFloat(document.getElementById('editWhLon').value)
      })
    });
    showToast('Warehouse updated successfully', 'success');
    closeModal('editWarehouseModal');
    refreshAllData();
  } catch (err) {}
}

function openEditRouteModal(id) {
  const r = state.routes.find(x => x.id === id);
  if (!r) return;
  document.getElementById('editRtId').value = r.id;
  document.getElementById('editRtCode').value = r.routeCode;
  document.getElementById('editRtSource').value = r.sourceName;
  document.getElementById('editRtDest').value = r.destinationName;
  document.getElementById('editRtType').value = r.routeType;
  document.getElementById('editRtCost').value = r.unitCost;
  document.getElementById('editRtCap').value = r.capacity;
  document.getElementById('editRtDist').value = r.distanceKm;
  document.getElementById('editRtTime').value = r.travelTimeHours;
  document.getElementById('editRtActive').checked = r.active;
  openModal('editRouteModal');
}

async function saveEditRoute(e) {
  e.preventDefault();
  const id = document.getElementById('editRtId').value;
  try {
    await fetchJson(`${API_BASE}/routes/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
        routeCode: document.getElementById('editRtCode').value.trim(),
        sourceName: document.getElementById('editRtSource').value.trim(),
        destinationName: document.getElementById('editRtDest').value.trim(),
        routeType: document.getElementById('editRtType').value,
        unitCost: parseFloat(document.getElementById('editRtCost').value),
        capacity: parseInt(document.getElementById('editRtCap').value, 10),
        distanceKm: parseFloat(document.getElementById('editRtDist').value),
        travelTimeHours: parseFloat(document.getElementById('editRtTime').value),
        active: document.getElementById('editRtActive').checked
      })
    });
    showToast('Route updated successfully', 'success');
    closeModal('editRouteModal');
    refreshAllData();
  } catch (err) {}
}
