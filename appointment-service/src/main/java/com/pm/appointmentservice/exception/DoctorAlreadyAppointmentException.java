package com.pm.appointmentservice.exception;

public class DoctorAlreadyAppointmentException extends RuntimeException {
    public DoctorAlreadyAppointmentException(String message) {
        super(message);
    }
}
