package com.pm.patientservice.dto;

import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PatientRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max=100, message = "Name can not exceed above 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(groups= CreatePatientValidationGroup.class,message = "Registered date is required")
    private String registeredDate;

    public String getEmail() {
        return email;
    }

    public PatientRequestDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public PatientRequestDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public PatientRequestDTO setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public PatientRequestDTO setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public PatientRequestDTO setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
        return this;
    }



}
