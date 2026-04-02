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

    // Default no-argument constructor required by Jackson for deserialization
    public AppointmentResponse() {
    }

    // Creates a fully populated appointment response with all fields
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

    // Returns the garage associated with this appointment
    public Garage getGarage() {
        return garage;
    }

    // Sets the garage associated with this appointment
    public void setGarage(Garage garage) {
        this.garage = garage;
    }
}
