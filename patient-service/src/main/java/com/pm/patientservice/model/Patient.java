package com.pm.patientservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Patient {
       @Id
       @GeneratedValue(strategy = GenerationType.AUTO)
       private UUID id;

       @NotNull
       private String name;

       @NotNull
       @Email
       @Column(unique = true)
       private String email;

       @NotNull
       private String address;


        @NotNull
        private LocalDate dateOfBirth;

        @NotNull
        private LocalDate registeredDate;

        public UUID getId() {
            return id;
        }

        public Patient setId(UUID id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Patient setName(String name) {
            this.name = name;
            return this;
        }

        public String getEmail() {
            return email;
        }

        public Patient setEmail(String email) {
            this.email = email;
            return this;
        }

        public String getAddress() {
            return address;
        }

        public Patient setAddress(String address) {
            this.address = address;
            return this;
        }



        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public Patient setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public LocalDate getRegisteredDate() {
            return registeredDate;
        }

        public Patient setRegisteredDate(LocalDate registeredDate) {
            this.registeredDate = registeredDate;
            return this;
        }
}

