package com.pm.appointmentservice.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidException(MethodArgumentNotValidException ex)
    {
            Map<String,String> errors = new HashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(
                    error -> errors.put(error.getField(), error.getDefaultMessage()));

            return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(PatientNotFoundException ex) {
        log.warn("Patient Not Found {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Patient not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalTimeSlotException.class)
    public ResponseEntity<Map<String, String>> handleIllegalTimeSlotException(IllegalTimeSlotException ex) {
        log.warn("Illegal timeslot {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Illegal timeslot");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(WrongTimeSlotException.class)
    public ResponseEntity<Map<String, String>> handleWrongTimeSlotException(WrongTimeSlotException ex) {
        log.warn("Wrong Timeslot {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Wrong Timeslot");
        return ResponseEntity.badRequest().body(errors);
    }

}
