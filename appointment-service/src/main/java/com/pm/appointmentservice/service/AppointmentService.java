package com.pm.appointmentservice.service;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.entity.Appointment;
import com.pm.appointmentservice.entity.CachedPatient;
import com.pm.appointmentservice.exception.IllegalTimeSlotException;
import com.pm.appointmentservice.exception.PatientNotFoundException;
import com.pm.appointmentservice.exception.WrongTimeSlotException;
import com.pm.appointmentservice.mapper.AppointmentMapper;
import com.pm.appointmentservice.repository.AppointmentRepository;
import com.pm.appointmentservice.repository.CachedPatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentRepository appointmentRepository;
    private final CachedPatientRepository cachedPatientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, CachedPatientRepository cachedPatientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.cachedPatientRepository = cachedPatientRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequestDto) {

//        To validate if patient Exists or not before creating appointment of it
        validatePatientExists(appointmentRequestDto.getPatientId());
        log.info("Patient exists in cached_patient table");

//        To validate whether the duration chosen is having a value exactly 30 minutes timeslot
        validateSlotDuration(appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());
        log.info("Slot duration is valid");

//        To Identify the Slot Availability
        validateSlotAvailability(
                appointmentRequestDto.getPatientId(),
                appointmentRequestDto.getStartTime(),
                appointmentRequestDto.getEndTime()
        );
        log.info("Slot is available");

//        Appointment appointment = appointmentRepository.save(AppointmentMapper.toModel(appointmentRequestDto));

        Appointment appointment;
        try {
            appointment = appointmentRepository.saveAndFlush(AppointmentMapper.toModel(appointmentRequestDto));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Overlapping appointment exists ", e);
        }
        return AppointmentMapper.toDto(appointment);
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

    private void validatePatientExists(UUID patientId) {
        if (!cachedPatientRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found with ID: {}"+patientId);
        }
    }

    private void validateSlotDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)  //-------To verify whether end time is after start time
                && !Duration.between(startTime, endTime).equals(Duration.ofMinutes(30))) // -- To identify time selection is exactly have 30 minutes gap
        {
            throw new IllegalTimeSlotException("Time slot having start time: {} and end time: {} is illegal. Please enter legal time having 30 minutes time slot."+startTime+endTime);
        }
    }

    private void validateSlotAvailability(
            UUID patientId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        boolean existsOverlap = !appointmentRepository
                .findOverlappingAppointments(patientId, startTime, endTime)
                .isEmpty();
        log.info("Value of existsOverlap param is {}",existsOverlap);
        if(existsOverlap) {
            throw new WrongTimeSlotException("Time slot booked for patient having id : {} is overlapping with his existing booked timeslots",patientId);
        }
    }

}
