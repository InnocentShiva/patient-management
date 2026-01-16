package com.pm.appointmentservice.controller;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.dto.UpdateAppointmentRequestDto;
import com.pm.appointmentservice.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;


    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/by-date")
    public List<AppointmentResponseDto> getAppointmentsByDateRange(
        @RequestParam LocalDateTime from,
        @RequestParam LocalDateTime to
    ){
        return appointmentService.getAppointmentByDateRange(from, to);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentrecords() {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointments();
            return ResponseEntity.ok().body(appointments);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody AppointmentRequestDto appointmentRequestDTO){
        AppointmentResponseDto appointmentResponseDto = appointmentService.createAppointment(appointmentRequestDTO);
        return ResponseEntity.ok().body(appointmentResponseDto);
    }

    @PutMapping
    public ResponseEntity<AppointmentResponseDto> updateAppointment(@RequestBody UpdateAppointmentRequestDto updateAppointmentRequestDTO){
        AppointmentResponseDto appointmentResponseDto = appointmentService.updateAppointment(updateAppointmentRequestDTO,
                updateAppointmentRequestDTO.getId(), updateAppointmentRequestDTO.getPatientId());
        return ResponseEntity.ok().body(appointmentResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> deleteAppointment(@PathVariable UUID id){
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }



}
