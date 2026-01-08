package com.pm.appointmentservice.exception;

import java.util.UUID;

public class WrongTimeSlotException extends RuntimeException {
    public WrongTimeSlotException(String message, UUID patientId) {
        super(message+","+patientId);
    }
}
