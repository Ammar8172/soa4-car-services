package com.example.carappointmentservice.dto;

public class Garage {

    private Long garageId;
    private String garageName;
    private String location;
    private String speciality;
    private String phoneNumber;

    public Garage() {
    }

    public Garage(Long garageId, String garageName, String location, String speciality, String phoneNumber) {
        this.garageId = garageId;
        this.garageName = garageName;
        this.location = location;
        this.speciality = speciality;
        this.phoneNumber = phoneNumber;
    }

    public static Garage unavailable(Long garageId) {
        return new Garage(garageId, "Unavailable", "Unknown", "Unavailable", "Unavailable");
    }

    public Long getGarageId() {
        return garageId;
    }

    public void setGarageId(Long garageId) {
        this.garageId = garageId;
    }

    public String getGarageName() {
        return garageName;
    }

    public void setGarageName(String garageName) {
        this.garageName = garageName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
