package com.pm.patientservice.dto;

public class PatientResponseDTO {
    private String id;
    private String name;
    private String email;
    private String address;


    private String dateOfBirth;

    public String getId() {
        return id;
    }

    public PatientResponseDTO setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PatientResponseDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PatientResponseDTO setEmail(String email) {
        this.email = email;
        return this;
    }



    public String getAddress() {
        return address;
    }

    public PatientResponseDTO setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public PatientResponseDTO setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }


}
