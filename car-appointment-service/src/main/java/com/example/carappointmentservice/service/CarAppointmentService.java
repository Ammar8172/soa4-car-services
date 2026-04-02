package com.example.carappointmentservice.service;

import com.example.carappointmentservice.client.GarageClient;
import com.example.carappointmentservice.dto.AppointmentResponse;
import com.example.carappointmentservice.dto.Garage;
import com.example.carappointmentservice.entity.CarAppointment;
import com.example.carappointmentservice.repository.CarAppointmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CarAppointmentService {

    private final CarAppointmentRepository repository;
    private final GarageClient garageClient;

    // Injects the appointment repository and garage HTTP client
    public CarAppointmentService(CarAppointmentRepository repository, GarageClient garageClient) {
        this.repository = repository;
        this.garageClient = garageClient;
    }

    // Retrieves all appointments and enriches each one with its garage details
    public Mono<List<AppointmentResponse>> getAllAppointmentsWithGarage() {
        List<CarAppointment> appointments = repository.findAll();

        return Flux.fromIterable(appointments)
                .flatMapSequential(this::buildResponse)
                .collectList();
    }

    // Fetches all garages from the garage service, sorted alphabetically by name
    public Mono<List<Garage>> getAllGarages() {
        return garageClient.getAllGarages()
                .map(allGarages -> {
                    allGarages.sort(Comparator.comparing(
                            garage -> garage.getGarageName() == null ? "" : garage.getGarageName(),
                            String.CASE_INSENSITIVE_ORDER
                    ));
                    return allGarages;
                });
    }

    // Retrieves a single appointment by ID and attaches its garage details; returns empty if not found
    public Mono<AppointmentResponse> getAppointmentByIdWithGarage(Long id) {
        Optional<CarAppointment> appointment = repository.findById(id);
        if (appointment.isEmpty()) {
            return Mono.empty();
        }
        return buildResponse(appointment.get());
    }

    // Validates the garage ID then saves and returns the new appointment
    public Mono<CarAppointment> createAppointment(CarAppointment appointment) {
        appointment.setAppointmentId(null);

        return validateGarage(appointment.getGarageId())
                .map(valid -> repository.save(appointment));
    }

    // Updates an existing appointment's fields after validating the garage; returns empty if not found
    public Mono<CarAppointment> editAppointment(Long id, CarAppointment updatedAppointment) {
        Optional<CarAppointment> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Mono.empty();
        }

        CarAppointment appointment = existing.get();
        appointment.setCustomerName(updatedAppointment.getCustomerName());
        appointment.setCarModel(updatedAppointment.getCarModel());
        appointment.setRegistrationNumber(updatedAppointment.getRegistrationNumber());
        appointment.setServiceType(updatedAppointment.getServiceType());
        appointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        appointment.setGarageId(updatedAppointment.getGarageId());

        return validateGarage(appointment.getGarageId())
                .map(valid -> repository.save(appointment));
    }

    // Builds an AppointmentResponse by combining appointment data with its garage details
    private Mono<AppointmentResponse> buildResponse(CarAppointment appointment) {
        return garageClient.getGarageById(appointment.getGarageId())
                .onErrorResume(ex -> Mono.just(Garage.unavailable(appointment.getGarageId())))
                .map(garage -> new AppointmentResponse(
                        appointment.getAppointmentId(),
                        appointment.getCustomerName(),
                        appointment.getCarModel(),
                        appointment.getRegistrationNumber(),
                        appointment.getServiceType(),
                        appointment.getAppointmentDate(),
                        garage
                ));
    }

    // Checks that the garage exists; throws an error if it does not
    private Mono<Boolean> validateGarage(Long garageId) {
        return garageClient.garageExists(garageId)
                .flatMap(exists -> exists
                        ? Mono.just(true)
                        : Mono.error(new IllegalArgumentException("Garage ID " + garageId + " was not found. Please choose a garage from the list.")));
    }
}
