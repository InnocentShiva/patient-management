package com.pm.appointmentservice.service;

import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.entity.CachedPatient;
import com.pm.appointmentservice.repository.AppointmentRespository;
import com.pm.appointmentservice.repository.CachedPatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRespository appointmentRepository;
    private final CachedPatientRepository cachedPatientRepository;

    public AppointmentService(AppointmentRespository appointmentRespository,  CachedPatientRepository cachedPatientRepository) {
        this.appointmentRepository = appointmentRespository;
        this.cachedPatientRepository = cachedPatientRepository;
    }

    public List<AppointmentResponseDto> getAppointmentByDateRange(
            LocalDateTime from , LocalDateTime to
    ){
        return appointmentRepository.findByStartTimeBetween(from , to).stream()
                .map(appointment -> {
//                    Logic for retreiving the name from CachedPatients table
                    String fullName = String.valueOf(cachedPatientRepository
                            .findById(appointment.getPatientId())
                            .map(CachedPatient::getFullName)
                            .orElse("Unknown")

                    );

                    AppointmentResponseDto appointmentResponseDto = new AppointmentResponseDto();
                    appointmentResponseDto.setId(appointment.getId());
                    appointmentResponseDto.setPatientId(appointment.getPatientId());
                    appointmentResponseDto.setStartTime(appointment.getStartTime());
                    appointmentResponseDto.setEndTime(appointment.getEndTime());
                    appointmentResponseDto.setReason(appointment.getReason());
                    appointmentResponseDto.setVersion(appointment.getVersion());
                    appointmentResponseDto.setPatientName(fullName);

                    return appointmentResponseDto;
                }).toList();
    }

}
