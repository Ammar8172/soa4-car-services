package com.example.garageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "garages")
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long garageId;

    @NotBlank
    private String garageName;

    @NotBlank
    private String location;

    @NotBlank
    private String speciality;

    @NotBlank
    private String phoneNumber;

    // Default no-argument constructor required by JPA
    public Garage() {
    }

    // Creates a Garage entity with all fields populated
    public Garage(Long garageId, String garageName, String location, String speciality, String phoneNumber) {
        this.garageId = garageId;
        this.garageName = garageName;
        this.location = location;
        this.speciality = speciality;
        this.phoneNumber = phoneNumber;
    }

    // Returns the unique garage ID
    public Long getGarageId() {
        return garageId;
    }

    // Sets the unique garage ID
    public void setGarageId(Long garageId) {
        this.garageId = garageId;
    }

    // Returns the name of the garage
    public String getGarageName() {
        return garageName;
    }

    // Sets the name of the garage
    public void setGarageName(String garageName) {
        this.garageName = garageName;
    }

    // Returns the location of the garage
    public String getLocation() {
        return location;
    }

    // Sets the location of the garage
    public void setLocation(String location) {
        this.location = location;
    }

    // Returns the service speciality of the garage
    public String getSpeciality() {
        return speciality;
    }

    // Sets the service speciality of the garage
    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    // Returns the contact phone number of the garage
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Sets the contact phone number of the garage
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
