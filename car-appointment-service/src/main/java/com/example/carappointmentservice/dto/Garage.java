package com.example.carappointmentservice.dto;

public class Garage {

    private Long garageId;
    private String garageName;
    private String location;
    private String speciality;
    private String phoneNumber;

    // Default no-argument constructor required by Jackson for deserialization
    public Garage() {
    }

    // Creates a Garage DTO with all fields populated
    public Garage(Long garageId, String garageName, String location, String speciality, String phoneNumber) {
        this.garageId = garageId;
        this.garageName = garageName;
        this.location = location;
        this.speciality = speciality;
        this.phoneNumber = phoneNumber;
    }

    // Returns a placeholder Garage when the garage service is unavailable for the given ID
    public static Garage unavailable(Long garageId) {
        return new Garage(garageId, "Unavailable", "Unknown", "Unavailable", "Unavailable");
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
