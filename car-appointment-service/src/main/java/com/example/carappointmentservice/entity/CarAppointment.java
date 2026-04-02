package com.example.carappointmentservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "car_appointments")
public class CarAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String carModel;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String serviceType;

    @NotNull
    @FutureOrPresent
    private LocalDate appointmentDate;

    @NotNull
    private Long garageId;

    // Default no-argument constructor required by JPA
    public CarAppointment() {
    }

    // Creates a CarAppointment entity with all fields set
    public CarAppointment(Long appointmentId, String customerName, String carModel, String registrationNumber,
                          String serviceType, LocalDate appointmentDate, Long garageId) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.carModel = carModel;
        this.registrationNumber = registrationNumber;
        this.serviceType = serviceType;
        this.appointmentDate = appointmentDate;
        this.garageId = garageId;
    }

    // Returns the unique appointment ID
    public Long getAppointmentId() {
        return appointmentId;
    }

    // Sets the unique appointment ID
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    // Returns the name of the customer
    public String getCustomerName() {
        return customerName;
    }

    // Sets the name of the customer
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Returns the car model for this appointment
    public String getCarModel() {
        return carModel;
    }

    // Sets the car model for this appointment
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    // Returns the car registration number
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    // Sets the car registration number
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    // Returns the type of service requested
    public String getServiceType() {
        return serviceType;
    }

    // Sets the type of service requested
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    // Returns the scheduled date of the appointment
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    // Sets the scheduled date of the appointment
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    // Returns the ID of the garage assigned to this appointment
    public Long getGarageId() {
        return garageId;
    }

    // Sets the ID of the garage assigned to this appointment
    public void setGarageId(Long garageId) {
        this.garageId = garageId;
    }
}
