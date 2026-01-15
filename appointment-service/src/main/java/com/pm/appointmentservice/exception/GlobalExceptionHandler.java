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


    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWrongAppointmentNotFoundException(AppointmentNotFoundException ex) {
        log.warn("Appointment not found {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Appointment not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IdAndPatientIdNotCoexistsException.class)
    public ResponseEntity<Map<String, String>> handleWrongIdAndPatientIdNotCoexistsException(IdAndPatientIdNotCoexistsException ex) {
        log.warn("Appointment not related to Patient {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Appointment not related to Patient");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalSlotBoundaryException.class)
    public ResponseEntity<Map<String, String>> handleWrongIllegalSlotBoundaryException(IllegalSlotBoundaryException ex) {
        log.warn("Illegal boundary of start or end time {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Illegal boundary of start or end time");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(NoDoctorAvailableException.class)
    public ResponseEntity<Map<String, String>> handleWrongNoDoctorAvailableException(NoDoctorAvailableException ex) {
        log.warn("Doctor availability not found {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Doctor availability not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DoctorSlotUnavailabilityException.class)
    public ResponseEntity<Map<String, String>> handleWrongDoctorSlotUnavailabilityException(DoctorSlotUnavailabilityException ex) {
        log.warn("Slot outside doctor's working hours {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Slot outside doctor's working hours");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DoctorAlreadyAppointmentException.class)
    public ResponseEntity<Map<String, String>> handleWrongDoctorAlreadyAppointmentException(DoctorAlreadyAppointmentException ex) {
        log.warn("Doctor already has appointment in this slot {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Doctor already has appointment in this slot ");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PatientAlreadyHasAppointmentException.class)
    public ResponseEntity<Map<String, String>> handleWrongPatientAlreadyHasAppointmentException(PatientAlreadyHasAppointmentException ex) {
        log.warn("Patient already has appointment in this slot {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Patient already has appointment in this slot");
        return ResponseEntity.badRequest().body(errors);
    }
}
