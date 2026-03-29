package com.example.carappointmentservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

@JsonPropertyOrder({
        "appointmentId",
        "customerName",
        "carModel",
        "registrationNumber",
        "serviceType",
        "appointmentDate",
        "garage"
})
public class AppointmentResponse {

    private Long appointmentId;
    private String customerName;
    private String carModel;
    private String registrationNumber;
    private String serviceType;
    private LocalDate appointmentDate;
    private Garage garage;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long appointmentId, String customerName, String carModel,
                               String registrationNumber, String serviceType,
                               LocalDate appointmentDate, Garage garage) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.carModel = carModel;
        this.registrationNumber = registrationNumber;
        this.serviceType = serviceType;
        this.appointmentDate = appointmentDate;
        this.garage = garage;
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

    public Garage getGarage() {
        return garage;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }
}
