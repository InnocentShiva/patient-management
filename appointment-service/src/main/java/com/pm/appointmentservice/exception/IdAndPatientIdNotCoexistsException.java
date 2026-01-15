package com.pm.appointmentservice.exception;

public class IdAndPatientIdNotCoexistsException extends RuntimeException {
    public IdAndPatientIdNotCoexistsException(String message) {
        super(message);
    }
}
