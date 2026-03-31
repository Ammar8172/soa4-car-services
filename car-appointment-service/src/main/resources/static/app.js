let currentEtag = null;
let cachedAppointments = [];
let garages = [];
let editingAppointmentId = null;

const lastStatusEl = document.getElementById("lastStatus");
const etagValueEl = document.getElementById("etagValue");
const appointmentCountEl = document.getElementById("appointmentCount");
const garageCountEl = document.getElementById("garageCount");
const appointmentsBody = document.getElementById("appointmentsBody");
const noticeEl = document.getElementById("notice");
const addForm = document.getElementById("addForm");
const addGarageSelect = document.getElementById("addGarageId");
const addSubmitBtn = document.getElementById("addSubmitBtn");
const refreshBtn = document.getElementById("refreshBtn");
const refreshGaragesBtn = document.getElementById("refreshGaragesBtn");
const searchInput = document.getElementById("searchInput");

refreshBtn.addEventListener("click", () => refreshAppointments(true));
refreshGaragesBtn.addEventListener("click", () => loadGarages(true));
addForm.addEventListener("submit", addAppointment);
searchInput.addEventListener("input", () => renderAppointments(getFilteredAppointments()));
appointmentsBody.addEventListener("click", handleTableActions);

init();

async function init() {
    document.getElementById("addAppointmentDate").value = new Date().toISOString().split("T")[0];
    await refreshAppointments();
    await loadGarages();
}

async function loadGarages(showFeedback = false) {
    try {
        addGarageSelect.innerHTML = '<option value="">Loading garages...</option>';
        setAddFormEnabled(false);

        const response = await fetch("/appointments/garages");
        lastStatusEl.textContent = `${response.status} ${response.statusText}`;

        if (!response.ok) {
            throw new Error(await extractErrorMessage(response));
        }

        garages = await response.json();
        if (!Array.isArray(garages)) {
            garages = [];
        }

        garages.sort((a, b) => (a.garageName || "").localeCompare(b.garageName || ""));
        syncGarageSelects();
        garageCountEl.textContent = String(garages.length);
        setAddFormEnabled(garages.length > 0);

        if (showFeedback) {
            setNotice("Garage list loaded successfully.", "success");
        }
    } catch (error) {
        garages = [];
        garageCountEl.textContent = "0";
        addGarageSelect.innerHTML = '<option value="">No garages available</option>';
        setAddFormEnabled(false);
        setNotice(`Could not load garages. ${error.message}`, "error");
    }
}

async function refreshAppointments(showFeedback = false) {
    try {
        const headers = {};
        if (currentEtag) {
            headers["If-None-Match"] = currentEtag;
        }

        const response = await fetch("/appointments", { headers });
        lastStatusEl.textContent = `${response.status} ${response.statusText}`;

        const responseEtag = response.headers.get("ETag");
        if (responseEtag) {
            currentEtag = responseEtag;
            etagValueEl.textContent = currentEtag;
        }

        if (response.status === 200) {
            cachedAppointments = await response.json();
            renderAppointments(getFilteredAppointments());
            if (showFeedback) {
                setNotice("Appointments refreshed.", "success");
            }
            return;
        }

        if (response.status === 304) {
            renderAppointments(getFilteredAppointments());
            if (showFeedback) {
                setNotice("Appointments are already up to date.", "info");
            }
            return;
        }

        throw new Error(await extractErrorMessage(response));
    } catch (error) {
        setNotice(`Could not refresh appointments. ${error.message}`, "error");
    }
}

async function addAppointment(event) {
    event.preventDefault();

    const appointment = {
        customerName: document.getElementById("addCustomerName").value.trim(),
        carModel: document.getElementById("addCarModel").value.trim(),
        registrationNumber: document.getElementById("addRegistrationNumber").value.trim(),
        serviceType: document.getElementById("addServiceType").value.trim(),
        appointmentDate: document.getElementById("addAppointmentDate").value,
        garageId: Number(document.getElementById("addGarageId").value)
    };

    if (!appointment.garageId) {
        setNotice("Please select a garage.", "warning");
        return;
    }

    try {
        const response = await fetch("/appointments", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(appointment)
        });

        lastStatusEl.textContent = `${response.status} ${response.statusText}`;

        if (!response.ok) {
            throw new Error(await extractErrorMessage(response));
        }

        addForm.reset();
        document.getElementById("addAppointmentDate").value = new Date().toISOString().split("T")[0];
        invalidateEtag();
        setNotice("Appointment added successfully.", "success");
        await refreshAppointments();
    } catch (error) {
        setNotice(`Failed to add appointment. ${error.message}`, "error");
    }
}

function handleTableActions(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }

    const action = button.dataset.action;
    const appointmentId = Number(button.dataset.id);

    if (action === "edit") {
        editingAppointmentId = appointmentId;
        renderAppointments(getFilteredAppointments());
        return;
    }

    if (action === "cancel") {
        editingAppointmentId = null;
        renderAppointments(getFilteredAppointments());
        return;
    }

    if (action === "save") {
        saveEditedAppointment(appointmentId);
    }
}

async function saveEditedAppointment(appointmentId) {
    const row = document.querySelector(`[data-edit-row="${appointmentId}"]`);
    if (!row) {
        return;
    }

    const appointment = {
        customerName: row.querySelector('[data-field="customerName"]').value.trim(),
        carModel: row.querySelector('[data-field="carModel"]').value.trim(),
        registrationNumber: row.querySelector('[data-field="registrationNumber"]').value.trim(),
        serviceType: row.querySelector('[data-field="serviceType"]').value.trim(),
        appointmentDate: row.querySelector('[data-field="appointmentDate"]').value,
        garageId: Number(row.querySelector('[data-field="garageId"]').value)
    };

    try {
        const response = await fetch(`/appointments/${appointmentId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(appointment)
        });

        lastStatusEl.textContent = `${response.status} ${response.statusText}`;

        if (!response.ok) {
            throw new Error(await extractErrorMessage(response));
        }

        editingAppointmentId = null;
        invalidateEtag();
        setNotice("Appointment updated successfully.", "success");
        await refreshAppointments();
    } catch (error) {
        setNotice(`Failed to update appointment. ${error.message}`, "error");
    }
}

function renderAppointments(appointments) {
    appointmentCountEl.textContent = String(appointments.length);

    if (!appointments.length) {
        appointmentsBody.innerHTML = `
            <tr>
                <td colspan="10" class="empty-message">No appointments found.</td>
            </tr>
        `;
        return;
    }

    appointmentsBody.innerHTML = appointments.map(appointment =>
        editingAppointmentId === appointment.appointmentId
            ? renderEditableRow(appointment)
            : renderReadOnlyRow(appointment)
    ).join("");
}

function renderReadOnlyRow(appointment) {
    return `
        <tr>
            <td>${appointment.appointmentId ?? ""}</td>
            <td>${escapeHtml(appointment.customerName ?? "")}</td>
            <td class="actions-cell">
                <button type="button" class="btn-ghost action-btn" data-action="edit" data-id="${appointment.appointmentId}">
                    Edit
                </button>
            </td>
            <td>${escapeHtml(appointment.carModel ?? "")}</td>
            <td>${escapeHtml(appointment.registrationNumber ?? "")}</td>
            <td>${escapeHtml(appointment.serviceType ?? "")}</td>
            <td>${escapeHtml(appointment.appointmentDate ?? "")}</td>
            <td>${escapeHtml(appointment.garage?.garageName ?? "N/A")}</td>
            <td>${escapeHtml(appointment.garage?.location ?? "N/A")}</td>
            <td>${escapeHtml(appointment.garage?.speciality ?? "N/A")}</td>
        </tr>
    `;
}

function renderEditableRow(appointment) {
    return `
        <tr data-edit-row="${appointment.appointmentId}" class="editing-row">
            <td>${appointment.appointmentId ?? ""}</td>
            <td><input class="inline-input" data-field="customerName" value="${escapeAttribute(appointment.customerName ?? "")}"></td>
            <td class="actions-cell">
                <div class="row-actions">
                    <button type="button" data-action="save" data-id="${appointment.appointmentId}">Save</button>
                    <button type="button" class="btn-secondary" data-action="cancel" data-id="${appointment.appointmentId}">Cancel</button>
                </div>
            </td>
            <td><input class="inline-input" data-field="carModel" value="${escapeAttribute(appointment.carModel ?? "")}"></td>
            <td><input class="inline-input" data-field="registrationNumber" value="${escapeAttribute(appointment.registrationNumber ?? "")}"></td>
            <td><input class="inline-input" data-field="serviceType" value="${escapeAttribute(appointment.serviceType ?? "")}"></td>
            <td><input class="inline-input" type="date" data-field="appointmentDate" value="${escapeAttribute(appointment.appointmentDate ?? "")}"></td>
            <td>
                <select class="inline-select" data-field="garageId">
                    ${renderGarageOptions(appointment.garage?.garageId)}
                </select>
            </td>
            <td>${escapeHtml(appointment.garage?.location ?? "")}</td>
            <td>${escapeHtml(appointment.garage?.speciality ?? "")}</td>
        </tr>
    `;
}

function renderGarageOptions(selectedGarageId = "") {
    const options = ['<option value="">Select a garage</option>'];

    for (const garage of garages) {
        const selected = String(garage.garageId) === String(selectedGarageId) ? "selected" : "";
        options.push(
            `<option value="${garage.garageId}" ${selected}>${escapeHtml(garage.garageName)}</option>`
        );
    }

    return options.join("");
}

function syncGarageSelects() {
    if (!garages.length) {
        addGarageSelect.innerHTML = '<option value="">No garages available</option>';
        return;
    }

    addGarageSelect.innerHTML = renderGarageOptions();
}

function getFilteredAppointments() {
    const query = searchInput.value.trim().toLowerCase();
    if (!query) {
        return [...cachedAppointments];
    }

    return cachedAppointments.filter(appointment => {
        const text = [
            appointment.customerName,
            appointment.carModel,
            appointment.registrationNumber,
            appointment.serviceType,
            appointment.appointmentDate,
            appointment.garage?.garageName,
            appointment.garage?.location,
            appointment.garage?.speciality
        ].join(" ").toLowerCase();

        return text.includes(query);
    });
}

function setNotice(message, type = "info") {
    noticeEl.className = `notice ${type}`;
    noticeEl.textContent = message;
}

function invalidateEtag() {
    currentEtag = null;
    etagValueEl.textContent = "Invalidated after update";
}

function setAddFormEnabled(enabled) {
    addSubmitBtn.disabled = !enabled;
    addGarageSelect.disabled = !enabled;
}

async function extractErrorMessage(response) {
    const text = await response.text();
    return text ? text.trim() : `${response.status} ${response.statusText}`;
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value);
}