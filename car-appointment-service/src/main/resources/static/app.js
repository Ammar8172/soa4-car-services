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
        setAddFormEnabled(false);
        addGarageSelect.innerHTML = '<option value="">Loading garages...</option>';

        const response = await fetch("/appointments/garages");
        lastStatusEl.textContent = `${response.status} ${response.statusText}`;

        if (!response.ok) {
            throw new Error(await extractErrorMessage(response));
        }

        const loadedGarages = await response.json();
        garages = Array.isArray(loadedGarages) ? loadedGarages : [];

        if (!garages.length) {
            garages = buildGarageListFromAppointments(cachedAppointments);
        }

        garages.sort((left, right) => {
            const leftName = left?.garageName || "";
            const rightName = right?.garageName || "";
            return leftName.localeCompare(rightName);
        });

        syncGarageSelects();
        setAddFormEnabled(garages.length > 0);
        garageCountEl.textContent = String(garages.length);

        if (showFeedback) {
            setNotice("Garage list refreshed.", "success");
        }
    } catch (error) {
        garages = buildGarageListFromAppointments(cachedAppointments);
        syncGarageSelects();
        setAddFormEnabled(garages.length > 0);
        garageCountEl.textContent = String(garages.length);

        if (garages.length > 0) {
            setNotice("Garage endpoint is unavailable, so the dropdown was built from the garages already attached to existing appointments.", "warning");
            return;
        }

        addGarageSelect.innerHTML = '<option value="">No garages available</option>';
        setAddFormEnabled(false);
        setNotice(`Could not load garages. ${error.message || "Please make sure the garage service and appointment service are both running."}`, "error");
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

            if (!garages.length) {
                const garageFallback = buildGarageListFromAppointments(cachedAppointments);
                if (garageFallback.length) {
                    garages = garageFallback;
                    syncGarageSelects();
                    setAddFormEnabled(true);
                    garageCountEl.textContent = String(garages.length);
                }
            }

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
        setNotice(`Could not refresh appointments. ${error.message || "Try again."}`, "error");
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
        garageId: Number(addGarageSelect.value)
    };

    if (!appointment.garageId) {
        setNotice("Please choose a garage name before adding the appointment.", "warning");
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
        syncGarageSelects();
        invalidateEtag();
        setNotice("Appointment added successfully.", "success");
        await refreshAppointments();
    } catch (error) {
        setNotice(`Failed to add appointment. ${error.message || "Try again."}`, "error");
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

    if (!appointment.garageId) {
        setNotice("Please choose a valid garage name before saving the appointment.", "warning");
        return;
    }

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
        setNotice(`Failed to update appointment. ${error.message || "Try again."}`, "error");
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

    appointmentsBody.innerHTML = appointments
        .map(appointment => editingAppointmentId === appointment.appointmentId
            ? renderEditableRow(appointment)
            : renderReadOnlyRow(appointment))
        .join("");
}

function renderReadOnlyRow(appointment) {
    const garageName = appointment.garage?.garageName || "Unavailable";
    const garageLocation = appointment.garage?.location || "Unknown";
    const garageSpeciality = appointment.garage?.speciality || "Unavailable";

    return `
        <tr>
            <td class="nowrap">${escapeHtml(String(appointment.appointmentId))}</td>
            <td>
                <div class="customer-cell">
                    <div>
                        <span class="customer-name">${escapeHtml(appointment.customerName || "")}</span>
                        <span class="table-sub">${escapeHtml(appointment.garage?.phoneNumber || "Garage phone unavailable")}</span>
                    </div>
                </div>
            </td>
            <td class="actions-cell">
                <button type="button" class="btn-ghost action-btn" data-action="edit" data-id="${appointment.appointmentId}">Edit</button>
            </td>
            <td>${escapeHtml(appointment.carModel || "")}</td>
            <td class="nowrap">${escapeHtml(appointment.registrationNumber || "")}</td>
            <td class="nowrap">${escapeHtml(appointment.serviceType || "")}</td>
            <td class="nowrap">${escapeHtml(appointment.appointmentDate || "")}</td>
            <td>${escapeHtml(garageName)}</td>
            <td>${escapeHtml(garageLocation)}</td>
            <td>${escapeHtml(garageSpeciality)}</td>
        </tr>
    `;
}

function renderEditableRow(appointment) {
    return `
        <tr data-edit-row="${appointment.appointmentId}" class="editing-row">
            <td class="nowrap">${escapeHtml(String(appointment.appointmentId))}</td>
            <td><input class="inline-input" data-field="customerName" value="${escapeAttribute(appointment.customerName || "")}" required></td>
            <td class="actions-cell">
                <div class="row-actions">
                    <button type="button" class="action-btn" data-action="save" data-id="${appointment.appointmentId}">Save</button>
                    <button type="button" class="btn-secondary action-btn" data-action="cancel" data-id="${appointment.appointmentId}">Cancel</button>
                </div>
            </td>
            <td><input class="inline-input" data-field="carModel" value="${escapeAttribute(appointment.carModel || "")}" required></td>
            <td><input class="inline-input" data-field="registrationNumber" value="${escapeAttribute(appointment.registrationNumber || "")}" required></td>
            <td><input class="inline-input" data-field="serviceType" value="${escapeAttribute(appointment.serviceType || "")}" required></td>
            <td><input class="inline-input" type="date" data-field="appointmentDate" value="${escapeAttribute(appointment.appointmentDate || "")}" required></td>
            <td>
                <select class="inline-select" data-field="garageId">
                    ${renderGarageOptions(appointment.garage?.garageId)}
                </select>
            </td>
            <td>${escapeHtml(appointment.garage?.location || "")}</td>
            <td>${escapeHtml(appointment.garage?.speciality || "")}</td>
        </tr>
    `;
}

function renderGarageOptions(selectedGarageId = "") {
    const placeholder = '<option value="">Select a garage</option>';
    const options = garages.map(garage => {
        const isSelected = String(garage.garageId) === String(selectedGarageId) ? "selected" : "";
        const label = `${garage.garageName} • ${garage.location} • ${garage.speciality}`;
        return `<option value="${garage.garageId}" ${isSelected}>${escapeHtml(label)}</option>`;
    });

    return [placeholder, ...options].join("");
}

function syncGarageSelects() {
    if (!garages.length) {
        addGarageSelect.innerHTML = '<option value="">No garages available</option>';
        return;
    }

    addGarageSelect.innerHTML = renderGarageOptions();
}

function buildGarageListFromAppointments(appointments) {
    const uniqueGarages = new Map();

    appointments.forEach(appointment => {
        const garage = appointment?.garage;
        if (!garage?.garageId) {
            return;
        }

        uniqueGarages.set(String(garage.garageId), {
            garageId: garage.garageId,
            garageName: garage.garageName || `Garage ${garage.garageId}`,
            location: garage.location || "Unknown",
            speciality: garage.speciality || "Unavailable",
            phoneNumber: garage.phoneNumber || "Unavailable"
        });
    });

    return Array.from(uniqueGarages.values());
}

function getFilteredAppointments() {
    const query = searchInput.value.trim().toLowerCase();
    if (!query) {
        return [...cachedAppointments];
    }

    return cachedAppointments.filter(appointment => {
        const haystack = [
            appointment.customerName,
            appointment.carModel,
            appointment.registrationNumber,
            appointment.serviceType,
            appointment.appointmentDate,
            appointment.garage?.garageName,
            appointment.garage?.location,
            appointment.garage?.speciality,
            appointment.garage?.phoneNumber
        ].join(" ").toLowerCase();

        return haystack.includes(query);
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

function setAddFormEnabled(isEnabled) {
    addSubmitBtn.disabled = !isEnabled;
    addGarageSelect.disabled = !isEnabled;
}

async function extractErrorMessage(response) {
    const text = await response.text();

    if (!text) {
        return `${response.status} ${response.statusText}`;
    }

    try {
        const parsed = JSON.parse(text);
        if (typeof parsed === "string") {
            return parsed;
        }
        if (parsed.message) {
            return parsed.message;
        }
        if (parsed.error) {
            return parsed.error;
        }
    } catch (error) {
        // ignore JSON parsing errors and use raw text below
    }

    return text.replace(/\s+/g, " ").trim();
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value);
}