package com.pm.appointmentservice.exception;

public class PatientAlreadyHasAppointmentException extends RuntimeException {
    public PatientAlreadyHasAppointmentException(String message) {
        super(message);
    }
}
