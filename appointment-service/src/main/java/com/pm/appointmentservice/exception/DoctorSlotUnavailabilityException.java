package com.pm.appointmentservice.exception;

public class DoctorSlotUnavailabilityException extends RuntimeException {
    public DoctorSlotUnavailabilityException(String message) {
        super(message);
    }
}
