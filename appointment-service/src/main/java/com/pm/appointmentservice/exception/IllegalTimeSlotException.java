package com.pm.appointmentservice.exception;

public class IllegalTimeSlotException extends RuntimeException {
    public IllegalTimeSlotException(String message) {
        super(message);
    }
}