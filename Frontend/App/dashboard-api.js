// Cliente de la API para la bandeja; el backend conserva la fuente de verdad.
const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8080';
const apiToken = () => sessionStorage.getItem('gnAccessToken');
const currentRole = () => sessionStorage.getItem('gnRole');
const byId = (id) => document.getElementById(id);
const escapeHTML = (value) => {
  const node = document.createElement('div');
  node.textContent = value ?? '';
  return node.innerHTML;
};

const role = currentRole();
let tickets = [];
let areas = [];
let categories = [];
let priorities = [];
let priorityRules = [];
let technicians = [];
let users = [];
let selectedTicketId = null;
let selectedTicket = null;

const statusLabels = {
  NUEVO: 'Nuevo', ASIGNADO: 'Asignado', EN_ATENCION: 'En atención',
  PENDIENTE_USUARIO: 'Pendiente usuario', RESUELTO: 'Resuelto',
  CERRADO: 'Cerrado', REABIERTO: 'Reabierto'
};
const priorityLabels = { CRITICA: 'Crítica', ALTA: 'Alta', MEDIA: 'Media', BAJA: 'Baja' };
const impactLabels = { ALTO: 'Alto', MEDIO: 'Medio', BAJO: 'Bajo' };
const roleLabels = { ADMINISTRADOR: 'Administrador', COORDINADOR: 'Coordinador', TECNICO: 'Técnico', SOLICITANTE: 'Solicitante' };

async function apiRequest(path, options = {}) {
  const headers = { Authorization: `Bearer ${apiToken()}`, ...(options.headers || {}) };
  if (options.body) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  } catch {
    throw new Error('No se pudo conectar con Spring. Revisa que el backend esté iniciado.');
  }

  const result = response.status === 204 ? null : await response.json().catch(() => null);
  if (response.status === 401) {
    sessionStorage.removeItem('gnAccessToken');
    sessionStorage.removeItem('gnRole');
    sessionStorage.removeItem('gnUser');
    window.location.replace('login.html');
    throw new Error('La sesión venció; inicia sesión de nuevo.');
  }
  if (!response.ok) throw new Error(result?.error || result?.message || `Error HTTP ${response.status}`);
  return result;
}

function showMessage(text) {
  const message = byId('dashboardMessage');
  message.textContent = text;
  message.classList.remove('d-none');
}

function hideMessage() {
  byId('dashboardMessage').classList.add('d-none');
}

function showToast(text) {
  byId('toastMessage').textContent = text;
  bootstrap.Toast.getOrCreateInstance(byId('appToast')).show();
}

function fillSelect(select, rows, getValue, getLabel, emptyLabel) {
  select.innerHTML = `<option value="">${escapeHTML(emptyLabel)}</option>`;
  rows.forEach((row) => {
    const option = document.createElement('option');
    option.value = getValue(row);
    option.textContent = getLabel(row);
    select.append(option);
  });
}

function displayPriority(name) {
  return priorityLabels[name] || name;
}

function displayStatus(name) {
  return statusLabels[name] || name;
}

function toDisplayTicket(ticket) {
  return {
    id: ticket.id,
    code: `TK-${ticket.id}`,
    subject: ticket.title,
    area: ticket.area,
    requester: ticket.creator,
    priority: displayPriority(ticket.priority),
    priorityId: ticket.priorityId,
    technician: ticket.technician || 'Sin asignar',
    status: displayStatus(ticket.state),
    state: ticket.state,
    created: ticket.createdAt,
    targetAt: ticket.targetAt,
    source: ticket
  };
}

function badge(text, type) {
  const colors = type === 'priority'
    ? { 'Crítica': 'text-bg-danger', Alta: 'text-bg-warning', Media: 'text-bg-info', Baja: 'text-bg-success' }
    : {
      Nuevo: 'text-bg-primary', Asignado: 'text-bg-info', 'En atención': 'text-bg-warning',
      'Pendiente usuario': 'text-bg-secondary', Resuelto: 'text-bg-success',
      Cerrado: 'text-bg-dark', Reabierto: 'text-bg-primary'
    };
  return `<span class="badge ${colors[text] || 'text-bg-secondary'}">${escapeHTML(text)}</span>`;
}

function deadlineText(ticket) {
  if (!ticket.targetAt) return 'Sin fecha objetivo';
  const target = new Date(ticket.targetAt);
  const remaining = Math.ceil((target - new Date()) / 3600000);
  if (remaining < 0 && !['RESUELTO', 'CERRADO'].includes(ticket.state)) {
    return '<span class="text-danger fw-semibold">Vencido</span>';
  }
  if (remaining <= 2 && remaining >= 0 && !['RESUELTO', 'CERRADO'].includes(ticket.state)) {
    return `<span class="text-danger fw-semibold">${remaining} h restantes</span>`;
  }
  return `${target.toLocaleDateString('es-PE', { day: '2-digit', month: 'short' })} <small class="text-secondary">(${remaining} h)</small>`;
}

function renderTickets() {
  const query = byId('searchTicket').value.toLowerCase().trim();
  const visible = tickets.filter((ticket) => !query
    || `${ticket.code} ${ticket.subject} ${ticket.requester}`.toLowerCase().includes(query));

  byId('tablaTickets').innerHTML = visible.map((ticket) => `
    <tr>
      <td><span class="ticket-code">${escapeHTML(ticket.code)}</span><span class="ticket-subject fw-semibold">${escapeHTML(ticket.subject)}</span></td>
      <td>${escapeHTML(ticket.area)}<small class="d-block text-secondary">${escapeHTML(ticket.requester)}</small></td>
      <td>${badge(ticket.priority, 'priority')}</td>
      <td>${escapeHTML(ticket.technician)}</td>
      <td>${badge(ticket.status, 'status')}</td>
      <td>${deadlineText(ticket)}</td>
      <td><button class="btn btn-sm btn-outline-primary manage-ticket" type="button" data-ticket-id="${ticket.id}">Ver / gestionar</button></td>
    </tr>`).join('') || '<tr><td colspan="7" class="text-center text-secondary py-4">No se encontraron tickets con esos filtros.</td></tr>';

  byId('ticketResult').textContent = `Mostrando ${visible.length} de ${tickets.length} tickets`;
  byId('countNew').textContent = tickets.filter((ticket) => ticket.state === 'NUEVO').length;
  byId('countActive').textContent = tickets.filter((ticket) =>
    ['ASIGNADO', 'EN_ATENCION', 'PENDIENTE_USUARIO', 'REABIERTO'].includes(ticket.state)).length;
  byId('countRisk').textContent = tickets.filter((ticket) => ticket.targetAt
    && new Date(ticket.targetAt) - new Date() < 2 * 3600000
    && !['RESUELTO', 'CERRADO'].includes(ticket.state)).length;
  byId('countDone').textContent = tickets.filter((ticket) =>
    ['RESUELTO', 'CERRADO'].includes(ticket.state)).length;
}

async function loadCatalogs() {
  const [areaRows, categoryRows, priorityRows, rules, technicianRows] = await Promise.all([
    apiRequest('/api/catalogos/areas'),
    apiRequest('/api/catalogos/categorias'),
    apiRequest('/api/catalogos/prioridades'),
    apiRequest('/api/catalogos/matriz-prioridad'),
    apiRequest('/api/usuarios/tecnicos')
  ]);
  areas = areaRows;
  categories = categoryRows;
  priorities = priorityRows;
  priorityRules = rules;
  technicians = technicianRows;

  fillSelect(byId('filtroArea'), areas, (item) => item.id, (item) => item.name, 'Todas');
  fillSelect(byId('filtroCategoria'), categories, (item) => item.id, (item) => item.name, 'Todas');
  fillSelect(byId('filtroPrioridad'), priorities, (item) => item.id,
    (item) => `${displayPriority(item.name)} (${item.slaHours} h)`, 'Todas');
  fillSelect(byId('filtroTecnico'), technicians, (item) => item.id, (item) => item.username, 'Todos');
  fillSelect(byId('ticketArea'), areas, (item) => item.id, (item) => item.name, 'Selecciona...');
  fillSelect(byId('ticketCategory'), categories, (item) => item.id, (item) => item.name, 'Selecciona...');
  fillSelect(byId('manageTechnician'), technicians, (item) => item.id, (item) => item.username, 'Sin asignar');
  fillSelect(byId('adminArea'), areas, (item) => item.id, (item) => item.name, 'Selecciona...');
  fillSelect(byId('matrixPriority'), priorities, (item) => item.id,
    (item) => displayPriority(item.name), 'Selecciona...');

  renderPriorityMatrix();
  renderCatalogItems();
  renderPriorityRules();
  byId('slaList').innerHTML = priorities.map((item) => `
    <li class="list-group-item px-0 d-flex justify-content-between">
      <span>${escapeHTML(displayPriority(item.name))}</span><strong>${item.slaHours} horas</strong>
    </li>`).join('');
}

function renderPriorityMatrix() {
  document.querySelectorAll('[data-impact][data-urgency]').forEach((cell) => {
    const rule = priorityRules.find((item) =>
      item.impact === cell.dataset.impact && item.urgency === cell.dataset.urgency);
    cell.textContent = rule ? displayPriority(rule.priority) : 'Sin regla';
  });
}

function catalogItems(type = byId('catalogType').value) {
  return { areas, categorias: categories, prioridades: priorities }[type] || [];
}

function renderCatalogItems() {
  if (role !== 'ADMINISTRADOR') return;
  const rows = catalogItems();
  fillSelect(byId('catalogExistingItem'), rows, (item) => item.id,
    (item) => item.name, 'Nuevo elemento');
  byId('catalogTable').innerHTML = rows.map((item) => `
    <tr>
      <td>${escapeHTML(item.name)}</td>
      <td>${item.slaHours ? `${item.slaHours} h` : '—'}</td>
      <td class="text-end"><button class="btn btn-sm btn-outline-primary edit-catalog" type="button" data-item-id="${item.id}">Editar</button></td>
    </tr>`).join('') || '<tr><td colspan="3" class="text-secondary">Sin elementos.</td></tr>';
}

function renderPriorityRules() {
  if (role !== 'ADMINISTRADOR') return;
  byId('priorityRuleTable').innerHTML = priorityRules.map((rule) => `
    <tr>
      <td>${escapeHTML(impactLabels[rule.impact])}</td>
      <td>${escapeHTML(impactLabels[rule.urgency])}</td>
      <td>${escapeHTML(displayPriority(rule.priority))}</td>
      <td class="text-end text-nowrap"><button class="btn btn-sm btn-outline-primary edit-rule" type="button" data-rule-id="${rule.id}">Editar</button> <button class="btn btn-sm btn-outline-danger delete-rule" type="button" data-rule-id="${rule.id}">Eliminar</button></td>
    </tr>`).join('') || '<tr><td colspan="4" class="text-secondary">Sin reglas configuradas.</td></tr>';
}

function resetCatalogForm() {
  byId('catalogId').value = '';
  byId('catalogExistingItem').value = '';
  byId('catalogName').value = '';
  byId('catalogSlaHours').value = '';
  byId('catalogSaveButton').textContent = 'Crear';
  byId('catalogDeleteButton').hidden = true;
}

function updateCatalogType() {
  const isPriority = byId('catalogType').value === 'prioridades';
  byId('catalogSlaGroup').hidden = !isPriority;
  byId('catalogSlaHours').required = isPriority;
  resetCatalogForm();
  renderCatalogItems();
}

function selectCatalogItem(id) {
  const item = catalogItems().find((entry) => entry.id === Number(id));
  if (!item) return resetCatalogForm();
  byId('catalogId').value = item.id;
  byId('catalogName').value = item.name;
  byId('catalogSlaHours').value = item.slaHours || '';
  byId('catalogSaveButton').textContent = 'Guardar cambios';
  byId('catalogDeleteButton').hidden = false;
}

async function loadAdminUsers() {
  if (role !== 'ADMINISTRADOR') return;
  byId('adminPanel').hidden = false;
  users = await apiRequest('/api/usuarios');
  byId('userTable').innerHTML = users.map((user) => `
    <tr>
      <td>${escapeHTML(user.username)}</td>
      <td>${escapeHTML(user.email)}</td>
      <td>${escapeHTML(roleLabels[user.role] || user.role)}</td>
      <td>${escapeHTML(user.area || 'Sin área')}</td>
      <td>${user.enabled ? 'Habilitado' : 'Deshabilitado'}</td>
      <td class="text-end text-nowrap"><button class="btn btn-sm btn-outline-primary edit-user" type="button" data-user-id="${user.id}">Editar</button> <button class="btn btn-sm btn-outline-danger delete-user" type="button" data-user-id="${user.id}" ${user.username === sessionStorage.getItem('gnUser') ? 'disabled title="No puedes eliminar tu propia cuenta aquí"' : ''}>Eliminar</button></td>
    </tr>`).join('') || '<tr><td colspan="6" class="text-secondary">Sin usuarios.</td></tr>';
}

function resetUserForm() {
  byId('userForm').reset();
  byId('adminUserId').value = '';
  byId('adminRole').value = 'SOLICITANTE';
  byId('adminEnabled').checked = true;
  byId('userSaveButton').textContent = 'Crear usuario';
  byId('adminPassword').placeholder = 'Mínimo 8 caracteres';
}

function editUser(id) {
  const user = users.find((item) => item.id === Number(id));
  if (!user) return;
  byId('adminUserId').value = user.id;
  byId('adminUsername').value = user.username;
  byId('adminEmail').value = user.email;
  byId('adminPassword').value = '';
  byId('adminPassword').placeholder = 'Escribe una nueva contraseña';
  byId('adminRole').value = user.role;
  byId('adminArea').value = user.areaId || '';
  byId('adminEnabled').checked = user.enabled;
  byId('userSaveButton').textContent = 'Guardar cambios';
}

async function handleCatalogSave(event) {
  event.preventDefault();
  const type = byId('catalogType').value;
  const id = byId('catalogId').value;
  const request = {
    name: byId('catalogName').value.trim(),
    slaHours: type === 'prioridades' ? Number(byId('catalogSlaHours').value) : null
  };
  const path = `/api/catalogos/${type}${id ? `/${id}` : ''}`;
  try {
    await apiRequest(path, { method: id ? 'PUT' : 'POST', body: JSON.stringify(request) });
    await loadCatalogs();
    resetCatalogForm();
    showToast('Catálogo actualizado.');
  } catch (error) {
    showMessage(error.message);
  }
}

async function deleteCatalogItem() {
  const id = byId('catalogId').value;
  if (!id || !window.confirm('¿Eliminar este elemento del catálogo?')) return;
  try {
    await apiRequest(`/api/catalogos/${byId('catalogType').value}/${id}`, { method: 'DELETE' });
    await loadCatalogs();
    resetCatalogForm();
    showToast('Elemento eliminado.');
  } catch (error) {
    showMessage(error.message);
  }
}

function selectPriorityRule(id) {
  const rule = priorityRules.find((item) => item.id === Number(id));
  if (!rule) return;
  byId('matrixRuleId').value = rule.id;
  byId('matrixImpact').value = rule.impact;
  byId('matrixUrgency').value = rule.urgency;
  byId('matrixPriority').value = rule.priorityId;
}

async function handleMatrixSave(event) {
  event.preventDefault();
  const impact = byId('matrixImpact').value;
  const urgency = byId('matrixUrgency').value;
  const existing = priorityRules.find((rule) => rule.impact === impact && rule.urgency === urgency);
  const id = byId('matrixRuleId').value || existing?.id || '';
  const body = JSON.stringify({ impact, urgency, priorityId: Number(byId('matrixPriority').value) });
  try {
    await apiRequest(`/api/catalogos/matriz-prioridad${id ? `/${id}` : ''}`, {
      method: id ? 'PUT' : 'POST', body
    });
    byId('matrixRuleId').value = '';
    await loadCatalogs();
    showToast('Matriz y preview de prioridad actualizados.');
  } catch (error) {
    showMessage(error.message);
  }
}

async function deletePriorityRule(id) {
  if (!window.confirm('¿Eliminar esta regla de prioridad?')) return;
  try {
    await apiRequest(`/api/catalogos/matriz-prioridad/${id}`, { method: 'DELETE' });
    await loadCatalogs();
    showToast('Regla eliminada.');
  } catch (error) {
    showMessage(error.message);
  }
}

async function handleUserSave(event) {
  event.preventDefault();
  const id = byId('adminUserId').value;
  const request = {
    username: byId('adminUsername').value.trim(),
    email: byId('adminEmail').value.trim(),
    password: byId('adminPassword').value,
    role: byId('adminRole').value,
    areaId: Number(byId('adminArea').value),
    enabled: byId('adminEnabled').checked
  };
  try {
    await apiRequest(`/api/usuarios${id ? `/${id}` : ''}`, {
      method: id ? 'PUT' : 'POST', body: JSON.stringify(request)
    });
    resetUserForm();
    await loadAdminUsers();
    await loadCatalogs();
    showToast('Usuario guardado.');
  } catch (error) {
    showMessage(error.message);
  }
}

async function deleteUser(id) {
  if (!window.confirm('¿Eliminar este usuario?')) return;
  try {
    await apiRequest(`/api/usuarios/${id}`, { method: 'DELETE' });
    await loadAdminUsers();
    await loadCatalogs();
    showToast('Usuario eliminado.');
  } catch (error) {
    showMessage(error.message);
  }
}

async function refreshTickets() {
  hideMessage();
  const params = new URLSearchParams();
  const filters = [
    ['tecnicoId', 'filtroTecnico'], ['areaId', 'filtroArea'],
    ['prioridadId', 'filtroPrioridad'], ['categoriaId', 'filtroCategoria'],
    ['estado', 'filtroEstado']
  ];
  filters.forEach(([parameter, elementId]) => {
    const value = byId(elementId).value;
    if (value) params.set(parameter, value);
  });
  const query = params.size ? `?${params.toString()}` : '';
  tickets = (await apiRequest(`/api/tickets${query}`)).map(toDisplayTicket);
  renderTickets();
}

function updatePriorityPreview() {
  const impact = byId('ticketImpact').value;
  const urgency = byId('ticketUrgency').value;
  const rule = priorityRules.find((item) => item.impact === impact && item.urgency === urgency);
  const priority = priorities.find((item) => item.id === rule?.priorityId);
  byId('priorityPreview').textContent = priority
    ? `Prioridad calculada: ${displayPriority(priority.name)}. SLA objetivo: ${priority.slaHours} horas.`
    : 'Selecciona impacto y urgencia para calcular la prioridad y SLA.';
}

function allowedNextStates(state) {
  const transitions = {
    ASIGNADO: ['EN_ATENCION'],
    EN_ATENCION: ['PENDIENTE_USUARIO', 'RESUELTO'],
    PENDIENTE_USUARIO: ['EN_ATENCION', 'RESUELTO'],
    REABIERTO: ['EN_ATENCION']
  };
  return transitions[state] || [];
}

function setManagementControls(ticket) {
  const canCoordinate = role === 'COORDINADOR';
  const canTechnician = role === 'TECNICO' && ticket.source.technician === sessionStorage.getItem('gnUser');
  const isRequester = role === 'SOLICITANTE';
  const canComment = ['COORDINADOR', 'TECNICO', 'SOLICITANTE'].includes(role);

  byId('manageAssignmentGroup').hidden = !canCoordinate;
  byId('manageStatusGroup').hidden = !canTechnician;
  byId('manageCommentGroup').hidden = !canComment;
  byId('manageSaveButton').hidden = !(canCoordinate || canTechnician || canComment);
  byId('reopenGroup').hidden = !(isRequester && ['RESUELTO', 'CERRADO'].includes(ticket.state));
  byId('closeTicketButton').hidden = !((canCoordinate || canTechnician) && ticket.state === 'RESUELTO');

  const statusSelect = byId('manageStatus');
  const nextStates = allowedNextStates(ticket.state);
  const options = [ticket.state, ...nextStates];
  statusSelect.innerHTML = options.map((state) =>
    `<option value="${state}">${escapeHTML(displayStatus(state))}</option>`).join('');
  statusSelect.value = ticket.state;
}

async function loadHistory(ticketId) {
  const history = await apiRequest(`/api/tickets/${ticketId}/historial`);
  byId('ticketHistory').innerHTML = history.map((item) => {
    const action = item.eventType === 'COMENTARIO'
      ? `Comentario: ${item.details}`
      : `${item.eventType}: ${item.oldValue || '—'} → ${item.newValue || '—'}${item.details ? ` (${item.details})` : ''}`;
    const time = new Date(item.createdAt).toLocaleString('es-PE');
    return `<li><strong>${escapeHTML(item.author)}</strong> · ${escapeHTML(time)}<br>${escapeHTML(action)}</li>`;
  }).join('') || '<li>Este ticket aún no tiene eventos.</li>';
}

async function openManagement(ticketId) {
  selectedTicketId = Number(ticketId);
  selectedTicket = tickets.find((item) => item.id === selectedTicketId);
  if (!selectedTicket) return;

  const ticket = selectedTicket.source;
  byId('manageCode').textContent = `${selectedTicket.code} · ${selectedTicket.priority} · SLA: ${ticket.slaHours} h`;
  byId('manageSubject').textContent = ticket.title;
  byId('manageTechnician').value = ticket.technicianId || '';
  byId('manageComment').value = '';
  byId('reopenReason').value = '';
  setManagementControls(selectedTicket);
  bootstrap.Modal.getOrCreateInstance(byId('manageModal')).show();

  try {
    await loadHistory(selectedTicketId);
  } catch (error) {
    showMessage(error.message);
  }
}

async function handleTicketCreate(event) {
  event.preventDefault();
  const form = event.currentTarget;
  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }

  const button = form.querySelector('button[type="submit"]');
  button.disabled = true;
  try {
    const created = await apiRequest('/api/tickets', {
      method: 'POST',
      body: JSON.stringify({
        title: byId('ticketSubject').value.trim(),
        description: byId('ticketDescription').value.trim(),
        areaId: Number(byId('ticketArea').value),
        categoryId: Number(byId('ticketCategory').value),
        impact: byId('ticketImpact').value,
        urgency: byId('ticketUrgency').value
      })
    });
    bootstrap.Modal.getInstance(byId('ticketModal')).hide();
    form.reset();
    form.classList.remove('was-validated');
    updatePriorityPreview();
    await refreshTickets();
    showToast(`Ticket TK-${created.id} registrado con prioridad ${displayPriority(created.priority)}.`);
  } catch (error) {
    showMessage(error.message);
  } finally {
    button.disabled = false;
  }
}

async function handleManagementSave(event) {
  event.preventDefault();
  if (!selectedTicket) return;

  const button = byId('manageSaveButton');
  button.disabled = true;
  try {
    let changed = false;
    if (role === 'COORDINADOR') {
      const technicianId = byId('manageTechnician').value;
      if (technicianId && Number(technicianId) !== selectedTicket.source.technicianId) {
        await apiRequest(`/api/tickets/${selectedTicketId}/asignacion`, {
          method: 'PUT', body: JSON.stringify({ technicianId: Number(technicianId) })
        });
        changed = true;
      }
    }

    if (role === 'TECNICO' && byId('manageStatus').value !== selectedTicket.state) {
      await apiRequest(`/api/tickets/${selectedTicketId}/estado`, {
        method: 'PUT', body: JSON.stringify({ state: byId('manageStatus').value })
      });
      changed = true;
    }

    const comment = byId('manageComment').value.trim();
    if (comment) {
      await apiRequest(`/api/tickets/${selectedTicketId}/comentarios`, {
        method: 'POST', body: JSON.stringify({ comment })
      });
      changed = true;
    }
    if (!changed) throw new Error('No hay cambios o comentarios para guardar.');

    bootstrap.Modal.getInstance(byId('manageModal')).hide();
    await refreshTickets();
    showToast(`Los cambios de TK-${selectedTicketId} se guardaron.`);
  } catch (error) {
    showMessage(error.message);
  } finally {
    button.disabled = false;
  }
}

async function closeSelectedTicket() {
  if (!selectedTicketId) return;
  try {
    await apiRequest(`/api/tickets/${selectedTicketId}/cierre`, { method: 'PUT' });
    bootstrap.Modal.getInstance(byId('manageModal')).hide();
    await refreshTickets();
    showToast(`TK-${selectedTicketId} quedó cerrado.`);
  } catch (error) {
    showMessage(error.message);
  }
}

async function reopenSelectedTicket() {
  const reason = byId('reopenReason').value.trim();
  if (!reason) {
    byId('reopenReason').classList.add('is-invalid');
    return;
  }
  byId('reopenReason').classList.remove('is-invalid');
  try {
    await apiRequest(`/api/tickets/${selectedTicketId}/reapertura`, {
      method: 'PUT', body: JSON.stringify({ reason })
    });
    bootstrap.Modal.getInstance(byId('manageModal')).hide();
    await refreshTickets();
    showToast(`TK-${selectedTicketId} fue reabierto.`);
  } catch (error) {
    showMessage(error.message);
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  if (!byId('tablaTickets')) return;
  if (!apiToken()) {
    window.location.replace('login.html');
    return;
  }

  byId('sessionUser').textContent = `${sessionStorage.getItem('gnUser')} · ${roleLabels[role] || role}`;
  byId('createTicketButton').hidden = role !== 'SOLICITANTE';
  byId('logoutButton').addEventListener('click', (event) => {
    event.preventDefault();
    sessionStorage.clear();
    window.location.replace('login.html');
  });

  ['searchTicket'].forEach((id) => byId(id).addEventListener('input', renderTickets));
  ['filtroTecnico', 'filtroArea', 'filtroPrioridad', 'filtroCategoria', 'filtroEstado']
    .forEach((id) => byId(id).addEventListener('change', () => refreshTickets().catch((error) => showMessage(error.message))));
  byId('clearFilters').addEventListener('click', () => {
    ['searchTicket', 'filtroTecnico', 'filtroArea', 'filtroPrioridad', 'filtroCategoria', 'filtroEstado']
      .forEach((id) => { byId(id).value = ''; });
    refreshTickets().catch((error) => showMessage(error.message));
  });
  ['ticketImpact', 'ticketUrgency'].forEach((id) => byId(id).addEventListener('change', updatePriorityPreview));
  byId('tablaTickets').addEventListener('click', (event) => {
    const button = event.target.closest('.manage-ticket');
    if (button) openManagement(button.dataset.ticketId);
  });
  byId('ticketForm').addEventListener('submit', handleTicketCreate);
  byId('manageForm').addEventListener('submit', handleManagementSave);
  byId('closeTicketButton').addEventListener('click', closeSelectedTicket);
  byId('reopenTicketButton').addEventListener('click', reopenSelectedTicket);

  if (role === 'ADMINISTRADOR') {
    updateCatalogType();
    byId('catalogType').addEventListener('change', updateCatalogType);
    byId('catalogExistingItem').addEventListener('change', (event) => selectCatalogItem(event.target.value));
    byId('catalogForm').addEventListener('submit', handleCatalogSave);
    byId('catalogDeleteButton').addEventListener('click', deleteCatalogItem);
    byId('catalogResetButton').addEventListener('click', resetCatalogForm);
    byId('catalogTable').addEventListener('click', (event) => {
      const button = event.target.closest('.edit-catalog');
      if (button) {
        byId('catalogExistingItem').value = button.dataset.itemId;
        selectCatalogItem(button.dataset.itemId);
      }
    });
    byId('matrixForm').addEventListener('submit', handleMatrixSave);
    byId('matrixResetButton').addEventListener('click', () => {
      byId('matrixRuleId').value = '';
      byId('matrixPriority').value = '';
    });
    byId('priorityRuleTable').addEventListener('click', (event) => {
      const edit = event.target.closest('.edit-rule');
      const remove = event.target.closest('.delete-rule');
      if (edit) selectPriorityRule(edit.dataset.ruleId);
      if (remove) deletePriorityRule(remove.dataset.ruleId);
    });
    byId('userForm').addEventListener('submit', handleUserSave);
    byId('userResetButton').addEventListener('click', resetUserForm);
    byId('userTable').addEventListener('click', (event) => {
      const edit = event.target.closest('.edit-user');
      const remove = event.target.closest('.delete-user');
      if (edit) editUser(edit.dataset.userId);
      if (remove) deleteUser(remove.dataset.userId);
    });
  }

  try {
    await loadCatalogs();
    await loadAdminUsers();
    await refreshTickets();
  } catch (error) {
    showMessage(error.message);
    byId('ticketResult').textContent = 'No fue posible cargar los datos del servidor.';
  }
});