let currentEtag = null;
let cachedAppointments = [];

const lastStatusEl = document.getElementById("lastStatus");
const etagValueEl = document.getElementById("etagValue");
const appointmentsBody = document.getElementById("appointmentsBody");

document.getElementById("refreshBtn").addEventListener("click", refreshAppointments);
document.getElementById("addForm").addEventListener("submit", addAppointment);
document.getElementById("editForm").addEventListener("submit", editAppointment);

async function refreshAppointments() {
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
        renderAppointments(cachedAppointments);
    } else if (response.status === 304) {
        renderAppointments(cachedAppointments);
        alert("304 Not Modified - data came from the cached table view.");
    } else {
        alert("Unexpected response while refreshing appointments.");
    }
}

async function addAppointment(event) {
    event.preventDefault();

    const appointment = {
        customerName: document.getElementById("addCustomerName").value,
        carModel: document.getElementById("addCarModel").value,
        registrationNumber: document.getElementById("addRegistrationNumber").value,
        serviceType: document.getElementById("addServiceType").value,
        appointmentDate: document.getElementById("addAppointmentDate").value,
        garageId: Number(document.getElementById("addGarageId").value)
    };

    const response = await fetch("/appointments", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(appointment)
    });

    lastStatusEl.textContent = `${response.status} ${response.statusText}`;

    if (response.ok) {
        alert("Appointment added successfully.");
        document.getElementById("addForm").reset();
        currentEtag = null;
        etagValueEl.textContent = "Invalidated after POST";
    } else {
        const message = await response.text();
        alert("Failed to add appointment: " + message);
    }
}

async function editAppointment(event) {
    event.preventDefault();

    const id = Number(document.getElementById("editAppointmentId").value);

    const appointment = {
        customerName: document.getElementById("editCustomerName").value,
        carModel: document.getElementById("editCarModel").value,
        registrationNumber: document.getElementById("editRegistrationNumber").value,
        serviceType: document.getElementById("editServiceType").value,
        appointmentDate: document.getElementById("editAppointmentDate").value,
        garageId: Number(document.getElementById("editGarageId").value)
    };

    const response = await fetch(`/appointments/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(appointment)
    });

    lastStatusEl.textContent = `${response.status} ${response.statusText}`;

    if (response.ok) {
        alert("Appointment edited successfully.");
        currentEtag = null;
        etagValueEl.textContent = "Invalidated after PUT";
    } else {
        const message = await response.text();
        alert("Failed to edit appointment: " + message);
    }
}

function renderAppointments(appointments) {
    if (!appointments || appointments.length === 0) {
        appointmentsBody.innerHTML = `
            <tr>
                <td colspan="9" class="empty-message">No appointments found.</td>
            </tr>
        `;
        return;
    }

    appointmentsBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentId}</td>
            <td>${appointment.customerName}</td>
            <td>${appointment.carModel}</td>
            <td>${appointment.registrationNumber}</td>
            <td>${appointment.serviceType}</td>
            <td>${appointment.appointmentDate}</td>
            <td>${appointment.garage?.garageName ?? "N/A"}</td>
            <td>${appointment.garage?.location ?? "N/A"}</td>
            <td>${appointment.garage?.speciality ?? "N/A"}</td>
        </tr>
    `).join("");
}
