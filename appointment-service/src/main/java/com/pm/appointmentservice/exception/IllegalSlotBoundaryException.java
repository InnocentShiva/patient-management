package com.pm.appointmentservice.exception;

public class IllegalSlotBoundaryException extends RuntimeException {
    public IllegalSlotBoundaryException(String message) {
        super(message);
    }
}