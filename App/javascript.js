const defaultTickets = [
  { id: 'TK-1048', subject: 'Acceso bloqueado a ERP', area: 'Finanzas', requester: 'María Luna', priority: 'Crítica', technician: 'Carlos Rojas', status: 'En atención', created: '2026-08-30T08:10:00', history: ['Ticket creado por María Luna.', 'Carlos Rojas inició la atención.'] },
  { id: 'TK-1047', subject: 'Impresora no responde', area: 'Operaciones', requester: 'Gonzalo Limaylla', priority: 'Alta', technician: 'Ana Torres', status: 'Asignado', created: '2026-08-30T07:20:00', history: ['Ticket creado por Gonzalo Limaylla.', 'Asignado a Ana Torres.'] },
  { id: 'TK-1046', subject: 'Instalación de software', area: 'Comercial', requester: 'Lucía Vega', priority: 'Media', technician: 'Sin asignar', status: 'Nuevo', created: '2026-08-29T15:00:00', history: ['Ticket creado por Lucía Vega.'] },
  { id: 'TK-1045', subject: 'Actualización de datos de usuario', area: 'Recursos Humanos', requester: 'Diego Ruiz', priority: 'Baja', technician: 'Ana Torres', status: 'Resuelto', created: '2026-08-28T10:30:00', history: ['Ticket creado por Diego Ruiz.', 'Ticket resuelto por Ana Torres.'] }
];

const slaHours = { Crítica: 4, Alta: 8, Media: 24, Baja: 72 };
const priorityMatrix = { 'Alto-Alta': 'Crítica', 'Alto-Media': 'Alta', 'Alto-Baja': 'Media', 'Medio-Alta': 'Alta', 'Medio-Media': 'Media', 'Medio-Baja': 'Baja', 'Bajo-Alta': 'Media', 'Bajo-Media': 'Baja', 'Bajo-Baja': 'Baja' };
let tickets = JSON.parse(localStorage.getItem('gnTickets') || 'null') || defaultTickets;
let selectedTicketId = null;

const byId = (id) => document.getElementById(id);
const escapeHTML = (value) => { const node = document.createElement('div'); node.textContent = value; return node.innerHTML; };
const deadline = (ticket) => new Date(new Date(ticket.created).getTime() + slaHours[ticket.priority] * 3600000);
const saveTickets = () => localStorage.setItem('gnTickets', JSON.stringify(tickets));
const showToast = (message) => { byId('toastMessage').textContent = message; bootstrap.Toast.getOrCreateInstance(byId('appToast')).show(); };

function badge(text, type) {
  const colors = type === 'priority'
    ? { Crítica: 'text-bg-danger', Alta: 'text-bg-warning', Media: 'text-bg-info', Baja: 'text-bg-success' }
    : { Nuevo: 'text-bg-primary', Asignado: 'text-bg-info', 'En atención': 'text-bg-warning', 'Pendiente usuario': 'text-bg-secondary', Resuelto: 'text-bg-success' };
  return `<span class="badge ${colors[text] || 'text-bg-secondary'}">${text}</span>`;
}

function deadlineText(ticket) {
  const remaining = Math.round((deadline(ticket) - new Date()) / 3600000);
  if (remaining < 0 && ticket.status !== 'Resuelto') return '<span class="text-danger fw-semibold">Vencido</span>';
  if (remaining <= 2 && ticket.status !== 'Resuelto') return `<span class="text-danger fw-semibold">${remaining} h restantes</span>`;
  return `${deadline(ticket).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' })} <small class="text-secondary">(${remaining} h)</small>`;
}

function renderTickets() {
  const query = byId('searchTicket').value.toLowerCase().trim();
  const priority = byId('filtroPrioridad').value;
  const status = byId('filtroEstado').value;
  const area = byId('filtroArea').value;
  const visible = tickets.filter((ticket) => (!query || `${ticket.id} ${ticket.subject} ${ticket.requester}`.toLowerCase().includes(query)) && (!priority || ticket.priority === priority) && (!status || ticket.status === status) && (!area || ticket.area === area));

  byId('tablaTickets').innerHTML = visible.map((ticket) => `<tr><td><span class="ticket-code">${ticket.id}</span><span class="ticket-subject fw-semibold">${escapeHTML(ticket.subject)}</span></td><td>${ticket.area}<small class="d-block text-secondary">${escapeHTML(ticket.requester)}</small></td><td>${badge(ticket.priority, 'priority')}</td><td>${escapeHTML(ticket.technician)}</td><td>${badge(ticket.status, 'status')}</td><td>${deadlineText(ticket)}</td><td><button class="btn btn-sm btn-outline-primary manage-ticket" type="button" data-ticket-id="${ticket.id}">Ver / gestionar</button></td></tr>`).join('') || '<tr><td colspan="7" class="text-center text-secondary py-4">No se encontraron tickets con esos filtros.</td></tr>';
  byId('ticketResult').textContent = `Mostrando ${visible.length} de ${tickets.length} tickets`;
  byId('countNew').textContent = tickets.filter((ticket) => ticket.status === 'Nuevo').length;
  byId('countActive').textContent = tickets.filter((ticket) => ['Asignado', 'En atención', 'Pendiente usuario'].includes(ticket.status)).length;
  byId('countRisk').textContent = tickets.filter((ticket) => deadline(ticket) - new Date() < 2 * 3600000 && ticket.status !== 'Resuelto').length;
  byId('countDone').textContent = tickets.filter((ticket) => ticket.status === 'Resuelto').length;
}

function updatePriorityPreview() {
  const priority = priorityMatrix[`${byId('ticketImpact').value}-${byId('ticketUrgency').value}`];
  byId('priorityPreview').textContent = priority ? `Prioridad calculada: ${priority}. SLA objetivo: ${slaHours[priority]} horas.` : 'Selecciona impacto y urgencia para calcular la prioridad y SLA.';
}

function openManagement(ticketId) {
  const ticket = tickets.find((item) => item.id === ticketId);
  if (!ticket) return;
  selectedTicketId = ticketId;
  byId('manageCode').textContent = `${ticket.id} · ${ticket.priority} · SLA: ${slaHours[ticket.priority]} h`;
  byId('manageSubject').textContent = ticket.subject;
  byId('manageTechnician').value = ticket.technician;
  byId('manageStatus').value = ticket.status;
  byId('manageComment').value = '';
  byId('ticketHistory').innerHTML = ticket.history.map((item) => `<li>${escapeHTML(item)}</li>`).join('');
  bootstrap.Modal.getOrCreateInstance(byId('manageModal')).show();
}

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = byId('loginForm');
  if (loginForm) {
    const savedUser = localStorage.getItem('gnUser');
    if (savedUser) { byId('user').value = savedUser; byId('remember').checked = true; }
    byId('togglePassword').addEventListener('click', () => { const input = byId('password'); input.type = input.type === 'password' ? 'text' : 'password'; });
    loginForm.addEventListener('submit', (event) => { event.preventDefault(); if (!loginForm.checkValidity()) { loginForm.classList.add('was-validated'); return; } if (byId('remember').checked) localStorage.setItem('gnUser', byId('user').value); else localStorage.removeItem('gnUser'); window.location.href = 'dashboard.html'; });
    return;
  }

  if (!byId('tablaTickets')) return;
  ['searchTicket', 'filtroPrioridad', 'filtroEstado', 'filtroArea'].forEach((id) => byId(id).addEventListener(id === 'searchTicket' ? 'input' : 'change', renderTickets));
  byId('clearFilters').addEventListener('click', () => { ['searchTicket', 'filtroPrioridad', 'filtroEstado', 'filtroArea'].forEach((id) => { byId(id).value = ''; }); renderTickets(); });
  byId('ticketImpact').addEventListener('change', updatePriorityPreview); byId('ticketUrgency').addEventListener('change', updatePriorityPreview);
  byId('tablaTickets').addEventListener('click', (event) => { const button = event.target.closest('.manage-ticket'); if (button) openManagement(button.dataset.ticketId); });

  byId('ticketForm').addEventListener('submit', (event) => {
    event.preventDefault(); const form = event.currentTarget;
    if (!form.checkValidity()) { form.classList.add('was-validated'); return; }
    const priority = priorityMatrix[`${byId('ticketImpact').value}-${byId('ticketUrgency').value}`];
    const newTicket = { id: `TK-${1049 + tickets.length}`, subject: byId('ticketSubject').value.trim(), area: byId('ticketArea').value, requester: localStorage.getItem('gnUser') || 'Usuario solicitante', priority, technician: 'Sin asignar', status: 'Nuevo', created: new Date().toISOString(), history: ['Ticket creado desde el formulario de registro.'] };
    tickets.unshift(newTicket); saveTickets(); bootstrap.Modal.getInstance(byId('ticketModal')).hide(); form.reset(); form.classList.remove('was-validated'); updatePriorityPreview(); renderTickets(); showToast(`Ticket ${newTicket.id} registrado con prioridad ${priority} y SLA de ${slaHours[priority]} horas.`);
  });

  byId('manageForm').addEventListener('submit', (event) => {
    event.preventDefault(); const ticket = tickets.find((item) => item.id === selectedTicketId); if (!ticket) return;
    const oldTechnician = ticket.technician; const oldStatus = ticket.status; const newTechnician = byId('manageTechnician').value; const newStatus = byId('manageStatus').value; const comment = byId('manageComment').value.trim();
    ticket.technician = newTechnician; ticket.status = newStatus;
    if (oldTechnician !== newTechnician) ticket.history.push(`Asignación actualizada: ${oldTechnician} → ${newTechnician}.`);
    if (oldStatus !== newStatus) ticket.history.push(`Estado actualizado: ${oldStatus} → ${newStatus}.`);
    if (comment) ticket.history.push(`Comentario: ${comment}`);
    saveTickets(); bootstrap.Modal.getInstance(byId('manageModal')).hide(); renderTickets(); showToast(`Los cambios de ${ticket.id} se guardaron correctamente.`);
  });
  renderTickets();
});
