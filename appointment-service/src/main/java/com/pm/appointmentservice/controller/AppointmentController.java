package com.pm.appointmentservice.controller;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;


    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponseDto> getAppointmentsByDateRange(
        @RequestParam LocalDateTime from,
        @RequestParam LocalDateTime to
    ){
        return appointmentService.getAppointmentByDateRange(from, to);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody AppointmentRequestDto appointmentRequestDTO){
        AppointmentResponseDto appointmentResponseDto = appointmentService.createAppointment(appointmentRequestDTO);
        return ResponseEntity.ok().body(appointmentResponseDto);
    }



}
