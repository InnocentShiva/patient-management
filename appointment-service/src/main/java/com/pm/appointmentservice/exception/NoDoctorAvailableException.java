package com.pm.appointmentservice.exception;

public class NoDoctorAvailableException extends RuntimeException {
    public NoDoctorAvailableException(String message) {
        super(message);
    }
}
