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

    public CarAppointmentService(CarAppointmentRepository repository, GarageClient garageClient) {
        this.repository = repository;
        this.garageClient = garageClient;
    }

    public Mono<List<AppointmentResponse>> getAllAppointmentsWithGarage() {
        List<CarAppointment> appointments = repository.findAll();

        return Flux.fromIterable(appointments)
                .flatMapSequential(this::buildResponse)
                .collectList();
    }

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

    public Mono<AppointmentResponse> getAppointmentByIdWithGarage(Long id) {
        Optional<CarAppointment> appointment = repository.findById(id);
        if (appointment.isEmpty()) {
            return Mono.empty();
        }
        return buildResponse(appointment.get());
    }

    public Mono<CarAppointment> createAppointment(CarAppointment appointment) {
        appointment.setAppointmentId(null);

        return validateGarage(appointment.getGarageId())
                .map(valid -> repository.save(appointment));
    }

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

    private Mono<Boolean> validateGarage(Long garageId) {
        return garageClient.garageExists(garageId)
                .flatMap(exists -> exists
                        ? Mono.just(true)
                        : Mono.error(new IllegalArgumentException("Garage ID " + garageId + " was not found. Please choose a garage from the list.")));
    }
}
