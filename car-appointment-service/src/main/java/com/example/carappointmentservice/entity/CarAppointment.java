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

    public CarAppointment() {
    }

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

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Long getGarageId() {
        return garageId;
    }

    public void setGarageId(Long garageId) {
        this.garageId = garageId;
    }
}
