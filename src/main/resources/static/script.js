const API = '';  // same origin

// ==================== UTILITY ====================

function showNotification(message, type = 'success') {
    const n = document.getElementById('notification');
    n.textContent = message;
    n.className = 'notification ' + type;
    clearTimeout(n._timer);
    n._timer = setTimeout(() => { n.className = 'notification hidden'; }, 5000);
}

function showError(message) {
    showNotification('❌ ' + message, 'error');
}

function showSuccess(message) {
    showNotification('✅ ' + message, 'success');
}

async function apiCall(method, url, body = null) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API + url, opts);
    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
        const msg = data.message || data.error || `HTTP ${res.status}`;
        throw new Error(msg);
    }
    return data;
}

function badge(status) {
    const key = (status || '').toLowerCase().replace(' ', '_');
    return `<span class="badge badge-${key}">${status}</span>`;
}

function fmtDt(dt) {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('en-IN', { hour12: false });
}

// ==================== TABS ====================

function showTab(name) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
    document.getElementById('tab-' + name).classList.add('active');
    event.currentTarget.classList.add('active');

    if (name === 'rooms') loadRooms();
    else if (name === 'housekeepers') loadHousekeepers();
    else if (name === 'tasks') loadTasks();
}

// ==================== DASHBOARD ====================

async function loadDashboard() {
    try {
        const [stats, hkWorkload, turnaround] = await Promise.all([
            apiCall('GET', '/api/rooms/statistics'),
            apiCall('GET', '/api/housekeepers/workload'),
            apiCall('GET', '/api/rooms/turnaround/average')
        ]);

        document.getElementById('statTotal').textContent     = stats.total ?? '—';
        document.getElementById('statDirty').textContent     = stats.dirty ?? '—';
        document.getElementById('statCleaning').textContent  = stats.cleaning ?? '—';
        document.getElementById('statCleaned').textContent   = stats.cleaned ?? '—';
        document.getElementById('statInspected').textContent = stats.inspected ?? '—';
        document.getElementById('statReady').textContent     = stats.ready ?? '—';

        const available = hkWorkload.filter(h => h.status === 'AVAILABLE').length;
        const busy      = hkWorkload.filter(h => h.status === 'BUSY').length;
        document.getElementById('statAvail').textContent    = available;
        document.getElementById('statBusy').textContent     = busy;
        document.getElementById('statTurnaround').textContent =
            turnaround.averageTurnaroundMinutes !== undefined
                ? turnaround.averageTurnaroundMinutes + ' min'
                : '—';
    } catch (e) {
        console.warn('Dashboard load partial error:', e.message);
    }
}

// ==================== ROOMS ====================

async function loadRooms() {
    try {
        const rooms = await apiCall('GET', '/api/rooms');
        const tbody = document.getElementById('roomsBody');
        if (!rooms.length) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty">No rooms registered yet.</td></tr>';
            return;
        }
        tbody.innerHTML = rooms.map(r => `
            <tr>
                <td>${r.id}</td>
                <td><strong>${r.roomNumber}</strong></td>
                <td>${r.roomType}</td>
                <td>${badge(r.status)}</td>
                <td>
                    <button class="action-btn ab-danger" onclick="deleteRoom(${r.id}, '${r.roomNumber}')">🗑️ Delete</button>
                </td>
            </tr>
        `).join('');
        loadDashboard();
    } catch (e) {
        showError('Failed to load rooms: ' + e.message);
    }
}

async function createRoom() {
    const roomNumber = document.getElementById('roomNumber').value.trim();
    const roomType   = document.getElementById('roomType').value.trim();
    const status     = document.getElementById('roomStatus').value;

    if (!roomNumber || !roomType) {
        showError('Room number and type are required.');
        return;
    }

    try {
        await apiCall('POST', '/api/rooms', { roomNumber, roomType, status });
        showSuccess(`Room ${roomNumber} created successfully!`);
        document.getElementById('roomNumber').value = '';
        document.getElementById('roomType').value   = '';
        loadRooms();
    } catch (e) {
        showError(e.message);
    }
}

async function updateRoomStatus() {
    const id        = document.getElementById('updateRoomId').value;
    const newStatus = document.getElementById('updateRoomStatus').value;

    if (!id) { showError('Please enter a Room ID.'); return; }

    try {
        const room = await apiCall('PUT', `/api/rooms/${id}/status`, { status: newStatus });
        showSuccess(`Room ${room.roomNumber} status updated to ${room.status}.`);
        loadRooms();
    } catch (e) {
        showError(e.message);
    }
}

async function allocateRoom() {
    const id = document.getElementById('allocateRoomId').value;
    if (!id) { showError('Please enter a Room ID.'); return; }

    try {
        const res = await apiCall('PUT', `/api/rooms/${id}/allocate`);
        showSuccess(res.message || 'Room allocated successfully!');
    } catch (e) {
        showError(e.message);
    }
}

async function deleteRoom(id, roomNumber) {
    if (!confirm(`Delete Room ${roomNumber}? This cannot be undone.`)) return;

    try {
        await fetch(`${API}/api/rooms/${id}`, { method: 'DELETE' });
        showSuccess(`Room ${roomNumber} deleted successfully.`);
        loadRooms();
    } catch (e) {
        showError(e.message);
    }
}

// ==================== HOUSEKEEPERS ====================

async function loadHousekeepers() {
    try {
        const workload = await apiCall('GET', '/api/housekeepers/workload');
        const tbody    = document.getElementById('hkBody');
        if (!workload.length) {
            tbody.innerHTML = '<tr><td colspan="9" class="empty">No housekeepers registered yet.</td></tr>';
            return;
        }
        tbody.innerHTML = workload.map(h => `
            <tr>
                <td>${h.id}</td>
                <td><strong>${h.name}</strong></td>
                <td>${h.phone || '—'}</td>
                <td>${badge(h.status)}</td>
                <td>${h.active ? '✅ Yes' : '❌ No'}</td>
                <td>${h.assignedTasks}</td>
                <td>${h.inProgressTasks}</td>
                <td>${h.completedTasks}</td>
                <td>
                    <button class="action-btn ab-danger" onclick="deleteHousekeeper(${h.id}, '${h.name}')">🗑️ Delete</button>
                </td>
            </tr>
        `).join('');
        loadDashboard();
    } catch (e) {
        showError('Failed to load housekeepers: ' + e.message);
    }
}

async function createHousekeeper() {
    const name   = document.getElementById('hkName').value.trim();
    const phone  = document.getElementById('hkPhone').value.trim();
    const active = document.getElementById('hkActive').value === 'true';

    if (!name || !phone) { showError('Name and phone are required.'); return; }

    try {
        await apiCall('POST', '/api/housekeepers', { name, phone, active });
        showSuccess(`Housekeeper ${name} registered successfully!`);
        document.getElementById('hkName').value  = '';
        document.getElementById('hkPhone').value = '';
        loadHousekeepers();
    } catch (e) {
        showError(e.message);
    }
}

async function updateHousekeeper() {
    const id     = document.getElementById('editHkId').value;
    const name   = document.getElementById('editHkName').value.trim();
    const phone  = document.getElementById('editHkPhone').value.trim();
    const active = document.getElementById('editHkActive').value === 'true';

    if (!id || !name || !phone) { showError('ID, name and phone are required.'); return; }

    try {
        await apiCall('PUT', `/api/housekeepers/${id}`, { name, phone, active });
        showSuccess(`Housekeeper ${name} updated successfully!`);
        loadHousekeepers();
    } catch (e) {
        showError(e.message);
    }
}

async function deleteHousekeeper(id, name) {
    if (!confirm(`Delete housekeeper ${name}? This cannot be undone.`)) return;

    try {
        const res = await fetch(`${API}/api/housekeepers/${id}`, { method: 'DELETE' });
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error(data.message || `HTTP ${res.status}`);
        }
        showSuccess(`Housekeeper ${name} deleted successfully.`);
        loadHousekeepers();
    } catch (e) {
        showError(e.message);
    }
}

// ==================== CLEANING TASKS ====================

async function loadTasks() {
    try {
        const tasks = await apiCall('GET', '/api/cleaning-tasks');
        const tbody = document.getElementById('tasksBody');
        if (!tasks.length) {
            tbody.innerHTML = '<tr><td colspan="8" class="empty">No cleaning tasks found.</td></tr>';
            return;
        }
        tbody.innerHTML = tasks.map(t => `
            <tr>
                <td>${t.id}</td>
                <td>${t.roomNumber ? `<strong>${t.roomNumber}</strong> <small>(#${t.roomId})</small>` : '—'}</td>
                <td>${t.housekeeperName || '<em class="gray">Unassigned</em>'}</td>
                <td>${badge(t.status)}</td>
                <td>${fmtDt(t.assignedAt)}</td>
                <td>${fmtDt(t.startedAt)}</td>
                <td>${fmtDt(t.completedAt)}</td>
                <td>
                    ${t.status === 'ASSIGNED'     ? `<button class="action-btn ab-warning" onclick="startTask(${t.id})">▶ Start</button>` : ''}
                    ${t.status === 'IN_PROGRESS'  ? `<button class="action-btn ab-success" onclick="completeTask(${t.id})">✓ Complete</button>` : ''}
                    ${t.status === 'COMPLETED'    ? '<span style="color:#94a3b8;font-size:12px;">Done</span>' : ''}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showError('Failed to load tasks: ' + e.message);
    }
}

async function startTask(taskId) {
    try {
        const task = await apiCall('PUT', `/api/cleaning-tasks/${taskId}/start`);
        showSuccess(`Task #${taskId} started — Room ${task.roomNumber} is now CLEANING.`);
        loadTasks();
        loadDashboard();
    } catch (e) {
        showError(e.message);
    }
}

async function completeTask(taskId) {
    try {
        const task = await apiCall('PUT', `/api/cleaning-tasks/${taskId}/complete`);
        showSuccess(`Task #${taskId} completed — Room ${task.roomNumber} is now CLEANED. Housekeeper is AVAILABLE.`);
        loadTasks();
        loadDashboard();
    } catch (e) {
        showError(e.message);
    }
}

// ==================== INSPECTIONS ====================

function toggleRemarksRequired() {
    const result = document.getElementById('inspectionResult').value;
    const reqSpan = document.getElementById('remarksReq');
    const textarea = document.getElementById('inspectionRemarks');
    if (result === 'FAILED') {
        reqSpan.style.display = 'inline';
        textarea.placeholder = 'Remarks are REQUIRED when inspection fails. Describe what needs to be fixed.';
    } else {
        reqSpan.style.display = 'none';
        textarea.placeholder = 'Optional remarks about the inspection.';
    }
}

async function submitInspection() {
    const roomId         = document.getElementById('inspectRoomId').value;
    const supervisorName = document.getElementById('supervisorName').value.trim();
    const result         = document.getElementById('inspectionResult').value;
    const remarks        = document.getElementById('inspectionRemarks').value.trim();

    if (!roomId || !supervisorName) {
        showError('Room ID and supervisor name are required.');
        return;
    }
    if (result === 'FAILED' && !remarks) {
        showError('Remarks are required when inspection fails.');
        return;
    }

    try {
        const inspection = await apiCall('POST', `/api/rooms/${roomId}/inspections`, {
            supervisorName, result, remarks
        });
        showSuccess(
            result === 'PASSED'
                ? `✅ Inspection PASSED for Room ${inspection.roomNumber}. Room is now READY for guests!`
                : `❌ Inspection FAILED for Room ${inspection.roomNumber}. A new cleaning task has been created.`
        );
        document.getElementById('supervisorName').value       = '';
        document.getElementById('inspectionRemarks').value    = '';
        loadDashboard();
    } catch (e) {
        showError(e.message);
    }
}

async function loadInspectionHistory() {
    const roomId = document.getElementById('historyRoomId').value;
    if (!roomId) { showError('Please enter a Room ID.'); return; }

    try {
        const inspections = await apiCall('GET', `/api/rooms/${roomId}/inspections`);
        const container   = document.getElementById('inspectionHistoryContainer');
        if (!inspections.length) {
            container.innerHTML = '<p class="empty">No inspections found for this room.</p>';
            return;
        }
        container.innerHTML = `<p style="font-size:13px;font-weight:700;margin-bottom:10px;">Room #${roomId} — ${inspections.length} inspection(s)</p>` +
            inspections.map(i => `
                <div class="inspection-item ${i.result.toLowerCase()}">
                    <div class="insp-header">
                        <span class="insp-supervisor">👤 ${i.supervisorName}</span>
                        <span>${badge(i.result)}</span>
                    </div>
                    <div class="insp-time">🕐 ${fmtDt(i.inspectedAt)}</div>
                    ${i.remarks ? `<div class="insp-remarks">💬 ${i.remarks}</div>` : ''}
                </div>
            `).join('');
    } catch (e) {
        showError(e.message);
    }
}

// ==================== INITIAL LOAD ====================

window.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    loadRooms();
});
