'use strict';

// ── State ─────────────────────────────────────────────────────────────────────
const SESSION_KEY = 'bookshop_session';

function getSession() {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY)); } catch { return null; }
}
function setSession(s) { localStorage.setItem(SESSION_KEY, JSON.stringify(s)); }
function clearSession() { localStorage.removeItem(SESSION_KEY); }
function currentRole() { return getSession()?.role; }
function currentUserId() { return getSession()?.userId; }
function isAdmin() { return currentRole() === 'ADMINISTRATOR'; }
function isManagerOrAdmin() { return currentRole() === 'MANAGER' || isAdmin(); }

// ── API client ────────────────────────────────────────────────────────────────
async function api(path, opts = {}) {
    const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
    const sess = getSession();
    if (sess?.token) headers['Authorization'] = `Bearer ${sess.token}`;
    const res = await fetch(path, { ...opts, headers });
    if (res.status === 401) {
        clearSession();
        navigate('#/login');
        throw new Error('Session expired. Please log in again.');
    }
    if (res.status === 204) return null;
    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try { const b = await res.json(); msg = b.message || msg; } catch { /* keep default */ }
        throw new Error(msg);
    }
    return res.json();
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function escHtml(str) {
    return String(str ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function navigate(hash) { location.hash = hash; }

function setContent(html) { document.getElementById('app').innerHTML = html; }

function showAlert(containerId, msg, type = 'danger') {
    const el = document.getElementById(containerId);
    if (el) el.innerHTML =
        `<div class="alert alert-${type} alert-dismissible fade show">
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            ${escHtml(msg)}
         </div>`;
}

function statusBadge(status) {
    const map = { PENDING: 'warning text-dark', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'secondary' };
    return `<span class="badge bg-${map[status] || 'secondary'}">${escHtml(status)}</span>`;
}

function roleBadge(role) {
    const map = { CUSTOMER: 'info text-dark', MANAGER: 'primary', ADMINISTRATOR: 'danger' };
    return `<span class="badge bg-${map[role] || 'secondary'}">${escHtml(role)}</span>`;
}

function toList(r) { return Array.isArray(r) ? r : (r?.content || []); }

function spinner() { return '<div class="text-center py-5"><div class="spinner-border text-primary"></div></div>'; }

function showModal(id) { new bootstrap.Modal(document.getElementById(id)).show(); }
function hideModal(id) { bootstrap.Modal.getInstance(document.getElementById(id)).hide(); }

// ── Router ────────────────────────────────────────────────────────────────────
function route() {
    const sess = getSession();
    const hash = location.hash || '#/login';
    const pub = ['#/login', '#/register'];

    if (!sess && !pub.includes(hash)) { navigate('#/login'); return; }
    if (sess && pub.includes(hash)) { navigate('#/products'); return; }

    if (hash === '#/login') renderLogin();
    else if (hash === '#/register') renderRegister();
    else if (hash === '#/products') renderProducts();
    else if (hash === '#/bookings') renderBookings();
    else if (hash === '#/users') {
        if (!isManagerOrAdmin()) { navigate('#/products'); return; }
        renderUsers();
    } else {
        navigate(sess ? '#/products' : '#/login');
    }
}

window.addEventListener('hashchange', route);
document.addEventListener('DOMContentLoaded', route);

// ── Navbar ────────────────────────────────────────────────────────────────────
function navbar() {
    const sess = getSession();
    if (!sess) return '';
    const h = location.hash;
    return `
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand fw-bold" href="#/products">&#128218; Bookshop</a>
            <div class="d-flex align-items-center gap-3">
                <ul class="navbar-nav flex-row gap-2 mb-0">
                    <li class="nav-item">
                        <a class="nav-link${h === '#/products' ? ' active fw-semibold' : ''}" href="#/products">Products</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link${h === '#/bookings' ? ' active fw-semibold' : ''}" href="#/bookings">Bookings</a>
                    </li>
                    ${isManagerOrAdmin() ? `
                    <li class="nav-item">
                        <a class="nav-link${h === '#/users' ? ' active fw-semibold' : ''}" href="#/users">Users</a>
                    </li>` : ''}
                </ul>
                <span class="text-light small border-start ps-3">
                    User&nbsp;#${sess.userId}&nbsp;${roleBadge(sess.role)}
                </span>
                <button class="btn btn-outline-light btn-sm" onclick="doLogout()">Logout</button>
            </div>
        </div>
    </nav>`;
}

// ── Auth ──────────────────────────────────────────────────────────────────────
function renderLogin() {
    setContent(`
    <div class="d-flex justify-content-center align-items-center min-vh-100 bg-light">
        <div class="card shadow-sm" style="width:400px">
            <div class="card-body p-4">
                <h4 class="card-title text-center mb-4">&#128218; Bookshop</h4>
                <div id="login-alert"></div>
                <form onsubmit="doLogin(event)">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input id="l-email" type="email" class="form-control" placeholder="you@example.com" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input id="l-password" type="password" class="form-control" required>
                    </div>
                    <button class="btn btn-primary w-100" type="submit">Login</button>
                </form>
                <hr>
                <p class="text-center mb-0 small">No account? <a href="#/register">Register here</a></p>
            </div>
        </div>
    </div>`);
}

async function doLogin(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    btn.textContent = 'Logging in\u2026';
    try {
        await loginWith(
            document.getElementById('l-email').value.trim(),
            document.getElementById('l-password').value
        );
    } catch (err) {
        showAlert('login-alert', err.message);
        btn.disabled = false;
        btn.textContent = 'Login';
    }
}

function renderRegister() {
    setContent(`
    <div class="d-flex justify-content-center align-items-center min-vh-100 bg-light">
        <div class="card shadow-sm" style="width:400px">
            <div class="card-body p-4">
                <h4 class="card-title text-center mb-4">Create Account</h4>
                <div id="reg-alert"></div>
                <form onsubmit="doRegister(event)">
                    <div class="mb-3">
                        <label class="form-label">Name</label>
                        <input id="r-name" type="text" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input id="r-email" type="email" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input id="r-password" type="password" class="form-control" required>
                        <div class="form-text">Min 8 characters, at least 1 uppercase, 1 lowercase, 1 digit.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Confirm Password</label>
                        <input id="r-confirm" type="password" class="form-control" required>
                    </div>
                    <button class="btn btn-success w-100" type="submit">Register</button>
                </form>
                <hr>
                <p class="text-center mb-0 small">Already have an account? <a href="#/login">Login</a></p>
            </div>
        </div>
    </div>`);
}

async function doRegister(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    const email = document.getElementById('r-email').value.trim();
    const password = document.getElementById('r-password').value;
    if (password !== document.getElementById('r-confirm').value) {
        showAlert('reg-alert', 'Passwords do not match.');
        btn.disabled = false;
        return;
    }
    try {
        await api('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({
                name: document.getElementById('r-name').value.trim(),
                email,
                password
            })
        });
        await loginWith(email, password);
    } catch (err) {
        showAlert('reg-alert', err.message);
        btn.disabled = false;
    }
}

async function loginWith(email, password) {
    const session = await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });
    setSession(session);
    navigate('#/products');
}

function doLogout() {
    clearSession();
    navigate('#/login');
}

// ── Products ──────────────────────────────────────────────────────────────────
async function renderProducts() {
    setContent(`
    ${navbar()}
    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Products</h5>
            ${isManagerOrAdmin() ? '<button class="btn btn-primary btn-sm" onclick="showProductModal()">+ New Product</button>' : ''}
        </div>
        <div id="products-alert"></div>
        <div id="products-body">
            ${spinner()}
        </div>
        <p class="text-muted small mt-2">Showing up to 20 results</p>
    </div>`);
    await loadProducts();
}

async function loadProducts() {
    try {
        const products = await api('/api/products?size=20');
        const list = toList(products);
        const tbody = list.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${escHtml(p.title)}</td>
                <td>${escHtml(p.author)}</td>
                <td>$${Number(p.price).toFixed(2)}</td>
                <td>${p.quantity ?? 0}</td>
                <td class="text-muted small text-truncate" style="max-width:200px">${escHtml(p.description || '')}</td>
                <td>
                    <div class="dropdown d-inline-block">
                        <button class="btn btn-outline-secondary btn-sm dropdown-toggle"
                            data-bs-toggle="dropdown">Actions</button>
                        <ul class="dropdown-menu">
                            <li><button type="button" class="dropdown-item"
                                onclick='showBookingModal(${JSON.stringify(p)})'>Book the product</button></li>
                            ${isManagerOrAdmin() ? `
                            <li><hr class="dropdown-divider"></li>
                            <li><button type="button" class="dropdown-item"
                                onclick='showProductModal(${JSON.stringify(p)})'>Edit</button></li>` : ''}
                            ${isAdmin() ? `
                            <li><button type="button" class="dropdown-item text-danger"
                                onclick="doDeleteProduct(${p.id})">Delete</button></li>` : ''}
                        </ul>
                    </div>
                </td>
            </tr>`).join('');
        document.getElementById('products-body').innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>#</th><th>Title</th><th>Author</th>
                            <th>Price</th><th>Qty</th><th>Description</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>${tbody || '<tr><td colspan="7" class="text-center text-muted py-4">No products found</td></tr>'}</tbody>
                </table>
            </div>`;
    } catch (err) {
        showAlert('products-alert', err.message);
    }
}

function showProductModal(product) {
    const isEdit = !!product;
    document.getElementById('modal-container').innerHTML = `
    <div class="modal fade" id="productModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${isEdit ? 'Edit Product' : 'New Product'}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="pm-alert"></div>
                    <form onsubmit="doSaveProduct(event, ${isEdit ? product.id : 'null'})">
                        <div class="mb-3">
                            <label class="form-label">Title *</label>
                            <input id="pm-title" type="text" class="form-control"
                                value="${isEdit ? escHtml(product.title) : ''}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Author *</label>
                            <input id="pm-author" type="text" class="form-control"
                                value="${isEdit ? escHtml(product.author) : ''}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Price *</label>
                            <input id="pm-price" type="number" step="0.01" min="0.01"
                                class="form-control" value="${isEdit ? product.price : ''}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Quantity</label>
                            <input id="pm-qty" type="number" min="0" class="form-control"
                                value="${isEdit ? (product.quantity ?? 0) : '0'}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Description</label>
                            <textarea id="pm-desc" class="form-control" rows="2">${isEdit ? escHtml(product.description || '') : ''}</textarea>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">
                            ${isEdit ? 'Save Changes' : 'Create Product'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>`;
    showModal('productModal');
}

async function doSaveProduct(e, productId) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const body = {
            title: document.getElementById('pm-title').value.trim(),
            author: document.getElementById('pm-author').value.trim(),
            price: parseFloat(document.getElementById('pm-price').value),
            quantity: parseInt(document.getElementById('pm-qty').value) || 0,
            description: document.getElementById('pm-desc').value.trim() || null
        };
        const method = productId ? 'PATCH' : 'POST';
        const path = productId ? `/api/products/${productId}` : '/api/products';
        await api(path, { method, body: JSON.stringify(body) });
        hideModal('productModal');
        await loadProducts();
    } catch (err) {
        showAlert('pm-alert', err.message);
        btn.disabled = false;
    }
}

async function doDeleteProduct(id) {
    if (!confirm('Delete this product? This cannot be undone.')) return;
    try {
        await api(`/api/products/${id}`, { method: 'DELETE' });
        await loadProducts();
    } catch (err) {
        showAlert('products-alert', err.message);
    }
}

// ── Bookings ──────────────────────────────────────────────────────────────────
async function renderBookings() {
    const heading = isManagerOrAdmin() ? 'All Bookings' : 'My Bookings';
    setContent(`
    ${navbar()}
    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">${heading}</h5>
            <button class="btn btn-primary btn-sm" onclick="showBookingModal()">+ New Booking</button>
        </div>
        <div id="bookings-alert"></div>
        <div id="bookings-body">
            ${spinner()}
        </div>
        <p class="text-muted small mt-2">Showing up to 20 results</p>
    </div>`);
    await loadBookings();
}

async function loadBookings() {
    try {
        const bookingsPath = isManagerOrAdmin()
            ? '/api/bookings?size=20'
            : `/api/bookings/user/${currentUserId()}`;
        const usersPath = isManagerOrAdmin()
            ? '/api/users'
            : `/api/users/${currentUserId()}`;

        const [bookings, usersRaw] = await Promise.all([
            api(bookingsPath),
            api(usersPath).catch(() => null)
        ]);
        const list = toList(bookings);
        const usersList = isManagerOrAdmin() ? toList(usersRaw) : (usersRaw ? [usersRaw] : []);
        const userMap = Object.fromEntries(usersList.map(u => [u.id, u.email]));

        const allStatuses = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

        const tbody = list.map(b => {
            const statusCell = isManagerOrAdmin()
                ? `<select class="form-select form-select-sm status-select"
                       onchange="doUpdateStatus(${b.id}, this.value)">
                       ${allStatuses.map(s =>
                           `<option value="${s}"${b.status === s ? ' selected' : ''}>${s}</option>`
                       ).join('')}
                   </select>`
                : statusBadge(b.status);
            return `
            <tr>
                <td>${b.id}</td>
                <td class="small">${escHtml(userMap[b.userId] || String(b.userId))}</td>
                <td>${b.productId}</td>
                <td>${b.quantity}</td>
                <td>${statusCell}</td>
                <td class="text-muted small">
                    ${b.createdAt ? new Date(b.createdAt).toLocaleDateString() : ''}
                </td>
                <td>
                    ${isAdmin()
                        ? `<button class="btn btn-outline-danger btn-sm"
                               onclick="doDeleteBooking(${b.id})">Delete</button>`
                        : ''}
                </td>
            </tr>`;
        }).join('');

        document.getElementById('bookings-body').innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>#</th><th>User Email</th><th>Product</th>
                            <th>Qty</th><th>Status</th><th>Date</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>${tbody || '<tr><td colspan="7" class="text-center text-muted py-4">No bookings found</td></tr>'}</tbody>
                </table>
            </div>`;
    } catch (err) {
        showAlert('bookings-alert', err.message);
    }
}

function productPickerHtml(products, preselected) {
    if (preselected) return `
        <input type="hidden" id="bm-product" value="${preselected.id}">
        <div class="form-control bg-light">
            ${escHtml(preselected.title)} &mdash; $${Number(preselected.price).toFixed(2)}
            <span class="text-muted">(${preselected.quantity} in stock)</span>
        </div>`;
    return `
        <select id="bm-product" class="form-select" required>
            <option value="">Select a product&hellip;</option>
            ${products.map(p =>
                `<option value="${p.id}">${escHtml(p.title)} &mdash; $${Number(p.price).toFixed(2)} (${p.quantity} in stock)</option>`
            ).join('')}
        </select>`;
}

async function showBookingModal(preselected = null) {
    let products = [];
    if (!preselected) {
        try {
            products = toList(await api('/api/products?size=100'));
        } catch { /* proceed with empty list */ }
    }

    document.getElementById('modal-container').innerHTML = `
    <div class="modal fade" id="bookingModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">New Booking</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="bm-alert"></div>
                    <form onsubmit="doCreateBooking(event)">
                        <div class="mb-3">
                            <label class="form-label">Product *</label>
                            ${productPickerHtml(products, preselected)}
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Quantity *</label>
                            <input id="bm-qty" type="number" min="1" class="form-control" value="1" required>
                        </div>
                        ${isManagerOrAdmin() ? `
                        <div class="mb-3">
                            <label class="form-label">User ID *</label>
                            <input id="bm-userid" type="number" min="1" class="form-control"
                                value="${currentUserId()}" required>
                        </div>` : ''}
                        <button class="btn btn-primary w-100" type="submit">Book</button>
                    </form>
                </div>
            </div>
        </div>
    </div>`;
    showModal('bookingModal');
}

async function doCreateBooking(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const userIdEl = document.getElementById('bm-userid');
        const body = {
            userId: userIdEl ? parseInt(userIdEl.value) : currentUserId(),
            productId: parseInt(document.getElementById('bm-product').value),
            quantity: parseInt(document.getElementById('bm-qty').value)
        };
        await api('/api/bookings', { method: 'POST', body: JSON.stringify(body) });
        hideModal('bookingModal');
        await loadBookings();
    } catch (err) {
        showAlert('bm-alert', err.message);
        btn.disabled = false;
    }
}

async function doUpdateStatus(id, status) {
    try {
        await api(`/api/bookings/${id}`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    } catch (err) {
        showAlert('bookings-alert', err.message);
        await loadBookings();
    }
}

async function doDeleteBooking(id) {
    if (!confirm('Delete this booking? This cannot be undone.')) return;
    try {
        await api(`/api/bookings/${id}`, { method: 'DELETE' });
        await loadBookings();
    } catch (err) {
        showAlert('bookings-alert', err.message);
    }
}

// ── Users ─────────────────────────────────────────────────────────────────────
async function renderUsers() {
    setContent(`
    ${navbar()}
    <div class="container mt-4">
        <h5 class="mb-3">Users</h5>
        <div id="users-alert"></div>
        <div id="users-body">
            ${spinner()}
        </div>
    </div>`);
    await loadUsers();
}

async function loadUsers() {
    try {
        const users = await api('/api/users');
        const list = toList(users);
        const tbody = list.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${escHtml(u.name)}</td>
                <td>${escHtml(u.email)}</td>
                <td>${roleBadge(u.role)}</td>
                <td>
                    <button class="btn btn-outline-secondary btn-sm me-1"
                        onclick='showUserModal(${JSON.stringify(u)})'>Edit</button>
                    ${isAdmin()
                        ? `<button class="btn btn-outline-danger btn-sm"
                               onclick="doDeleteUser(${u.id})">Delete</button>`
                        : ''}
                </td>
            </tr>`).join('');
        document.getElementById('users-body').innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr><th>#</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr>
                    </thead>
                    <tbody>${tbody || '<tr><td colspan="5" class="text-center text-muted py-4">No users found</td></tr>'}</tbody>
                </table>
            </div>`;
    } catch (err) {
        showAlert('users-alert', err.message);
    }
}

function showUserModal(user) {
    document.getElementById('modal-container').innerHTML = `
    <div class="modal fade" id="userModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Edit User #${user.id}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="um-alert"></div>
                    <form onsubmit="doSaveUser(event, ${user.id})">
                        <div class="mb-3">
                            <label class="form-label">Name *</label>
                            <input id="um-name" type="text" class="form-control"
                                value="${escHtml(user.name)}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email *</label>
                            <input id="um-email" type="email" class="form-control"
                                value="${escHtml(user.email)}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Role *</label>
                            <select id="um-role" class="form-select" ${!isAdmin() ? 'disabled' : ''}>
                                <option value="CUSTOMER"${user.role === 'CUSTOMER' ? ' selected' : ''}>CUSTOMER</option>
                                <option value="MANAGER"${user.role === 'MANAGER' ? ' selected' : ''}>MANAGER</option>
                                <option value="ADMINISTRATOR"${user.role === 'ADMINISTRATOR' ? ' selected' : ''}>ADMINISTRATOR</option>
                            </select>
                            ${!isAdmin() ? '<div class="form-text">Only administrators can change roles.</div>' : ''}
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Password *</label>
                            <input id="um-password" type="password" class="form-control"
                                placeholder="Enter to confirm changes" required>
                            <div class="form-text">
                                Required by the API. Min 8 chars, 1 uppercase, 1 lowercase, 1 digit.
                            </div>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">Save Changes</button>
                    </form>
                </div>
            </div>
        </div>
    </div>`;
    showModal('userModal');
}

async function doSaveUser(e, userId) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
        const roleEl = document.getElementById('um-role');
        const body = {
            name: document.getElementById('um-name').value.trim(),
            email: document.getElementById('um-email').value.trim(),
            password: document.getElementById('um-password').value,
            ...(!roleEl.disabled && { role: roleEl.value })
        };
        await api(`/api/users/${userId}`, { method: 'PUT', body: JSON.stringify(body) });
        hideModal('userModal');
        await loadUsers();
    } catch (err) {
        showAlert('um-alert', err.message);
        btn.disabled = false;
    }
}

async function doDeleteUser(id) {
    if (!confirm('Delete this user? This cannot be undone.')) return;
    try {
        await api(`/api/users/${id}`, { method: 'DELETE' });
        await loadUsers();
    } catch (err) {
        showAlert('users-alert', err.message);
    }
}
