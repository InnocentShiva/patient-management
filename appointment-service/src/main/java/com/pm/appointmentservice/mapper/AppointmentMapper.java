package com.pm.appointmentservice.mapper;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.entity.Appointment;

import java.util.UUID;

public class AppointmentMapper {

    public static Appointment toModel(AppointmentRequestDto appointmentRequestDto) {

        Appointment appointment = new Appointment();
//        appointment.setId(UUID.randomUUID());
        appointment.setReason(appointmentRequestDto.getReason());
        appointment.setPatientId(appointmentRequestDto.getPatientId());
        appointment.setStartTime(appointmentRequestDto.getStartTime());
        appointment.setEndTime(appointmentRequestDto.getEndTime());
        return appointment;
    }

    public static AppointmentResponseDto toDto(Appointment appointment) {
        AppointmentResponseDto appointmentResponseDto = new AppointmentResponseDto();
        appointmentResponseDto.setId(appointment.getId());
        appointmentResponseDto.setReason(appointment.getReason());
        appointmentResponseDto.setPatientId(appointment.getPatientId());
        appointmentResponseDto.setStartTime(appointment.getStartTime());
        appointmentResponseDto.setEndTime(appointment.getEndTime());
        return appointmentResponseDto;
    }


}
