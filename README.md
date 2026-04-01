# SOA4 Car Services

This repository contains a complete SOA4 project based on a car-services theme:

- **Service A (consumer):** Car Appointment Service
- **Service B (producer):** Garage Service

The project demonstrates:

- Spring Boot + Spring Data JPA
- Service-to-service communication using **WebClient** (request-response, asynchronous nonblocking style)
- **ETag HTTP caching** on the collection endpoint only
- Static **HTML/CSS/JavaScript** client
- Dockerized deployment with a single command

## Folder structure

```text
soa4-car-services/
  garage-service/
  car-appointment-service/
  docker-compose.yml
  README.md
```

## Services

### Garage Service (Producer)
Runs on **http://localhost:8081**

Main endpoints:
- `GET /garages`
- `GET /garages/{id}`
- `POST /garages`
- `PUT /garages/{id}`

### Car Appointment Service (Consumer)
Runs on **http://localhost:8080**

Main endpoints:
- `GET /appointments`
- `GET /appointments/{id}`
- `POST /appointments`
- `PUT /appointments/{id}`

Static client:
- `http://localhost:8080/index.html`

## Run the project

From the repository root:

```bash
docker compose up --build
```

Then open:

- Client UI: `http://localhost:8080/index.html`
- Garage API: `http://localhost:8081/garages`
- Appointment API: `http://localhost:8080/appointments`

## Demo steps

1. Open the UI.
2. Click **Refresh Appointments**.
   - First call should return **200**
   - The ETag value is shown
3. Click **Refresh Appointments** again without changing data.
   - Should return **304 Not Modified**
4. Add a new appointment.
5. Click refresh again.
   - Should return **200**
   - Table updates and ETag changes
6. Edit an appointment.
7. Refresh again to show **200** and the new ETag.

## Notes

- The frontend is served from the **Car Appointment Service** in `src/main/resources/static`.
- Each service uses its own PostgreSQL database container.
- Docker health checks are included so the app services wait for their databases.
