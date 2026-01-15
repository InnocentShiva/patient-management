package com.pm.appointmentservice.service;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.dto.UpdateAppointmentRequestDto;
import com.pm.appointmentservice.entity.Appointment;
import com.pm.appointmentservice.entity.CachedPatient;
import com.pm.appointmentservice.entity.DoctorAvailability;
import com.pm.appointmentservice.exception.*;
import com.pm.appointmentservice.kafka.KafkaProducer;
import com.pm.appointmentservice.mapper.AppointmentMapper;
import com.pm.appointmentservice.repository.AppointmentRepository;
import com.pm.appointmentservice.repository.CachedPatientRepository;
import com.pm.appointmentservice.repository.DoctorAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentRepository appointmentRepository;
    private final CachedPatientRepository cachedPatientRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final KafkaProducer kafkaProducer;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              CachedPatientRepository cachedPatientRepository,
                              DoctorAvailabilityRepository doctorAvailabilityRepository,
                              KafkaProducer kafkaProducer) {
        this.appointmentRepository = appointmentRepository;
        this.cachedPatientRepository = cachedPatientRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequestDto) {

//        To validate if patient Exists or not before creating appointment of it
        validatePatientExists(appointmentRequestDto.getPatientId());
        log.info("Patient exists in cached_patient table");

//        To validate if doctor exists of not before creating appointment
        validateDoctorExists(appointmentRequestDto.getDoctorId());
        log.info("Doctor exists in doctor_appointment table");



//        To validate whether the duration chosen is having a value exactly 30 minutes timeslot
        validateSlotDuration(appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());
        log.info("Slot duration is valid");

//        To Identify the Slot Availability according to patient
        validateSlotAvailability(
                appointmentRequestDto.getPatientId(),
                appointmentRequestDto.getStartTime(),
                appointmentRequestDto.getEndTime()
        );
        log.info("Slot is available");

//        To validate time booked is matching doctors time of availability
        validateDoctorWorkingHours(
                appointmentRequestDto.getDoctorId(),
                appointmentRequestDto.getStartTime(),
                appointmentRequestDto.getEndTime()
        );


//        To validate doctors time choosen is not intersecting with the doctors already booked times
        validateDoctorSlotAvailabilityAsPerOtherAppointments(
                appointmentRequestDto.getDoctorId(),
                appointmentRequestDto.getStartTime(),
                appointmentRequestDto.getEndTime()
        );



//        Appointment appointment = appointmentRepository.save(AppointmentMapper.toModel(appointmentRequestDto));

        Appointment appointment;
        try {
            appointment = appointmentRepository.saveAndFlush(AppointmentMapper.toModel(appointmentRequestDto));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Overlapping appointment exists ", e);
        }

        kafkaProducer.sendAppointmentCreatedEvent(appointment);

        return AppointmentMapper.toDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto updateAppointment(UpdateAppointmentRequestDto updateAppointmentRequestDto,
                                                    UUID id, UUID patientId) {
//        Validation for existing appointmentId
        Appointment existing = appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: {"+id+"}"));

//        Validation for valid patientId
        cachedPatientRepository.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Patient not found for this appointment with ID: {"+patientId+"}"));

//        Validation for appointmentId linked with patientId
        if (!appointmentRepository.existsByIdAndPatientId(id,patientId)){
            throw new IdAndPatientIdNotCoexistsException("This Appointment{"+id+"} is not fixed with this patient{"+patientId+"}");
        }

//        Validation for rest of the fields
        validateSlotDuration(updateAppointmentRequestDto.getStartTime(), updateAppointmentRequestDto.getEndTime());

//        Validate Slot Boundary
        validateSlotBoundary(updateAppointmentRequestDto.getStartTime(), updateAppointmentRequestDto.getEndTime());

//        Validate doctor availability window
        validateDoctorWorkingHours(
                updateAppointmentRequestDto.getDoctorId(),
                updateAppointmentRequestDto.getStartTime(),
                updateAppointmentRequestDto.getEndTime()
        );

//        Check Doctor overlap (excluding current)
        validateDoctorSlotAvailability(
                updateAppointmentRequestDto.getDoctorId(),
                existing.getId(),
                updateAppointmentRequestDto.getStartTime(),
                updateAppointmentRequestDto.getEndTime()
        );

//        Check Patient overlap (excluding current)
        validatePatientSlotAvailability(
                existing.getPatientId(),
                existing.getId(),
                updateAppointmentRequestDto.getStartTime(),
                updateAppointmentRequestDto.getEndTime()
        );

//        Apply the update
        existing.setPatientId(updateAppointmentRequestDto.getPatientId());
        existing.setDoctorId(updateAppointmentRequestDto.getDoctorId());
        existing.setStartTime(updateAppointmentRequestDto.getStartTime());
        existing.setEndTime(updateAppointmentRequestDto.getEndTime());
        existing.setReason(updateAppointmentRequestDto.getReason());

        Appointment appointment;
        try {
            appointment = appointmentRepository.saveAndFlush(existing);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Overlapping appointment exists ", e);
        }

        String fullName = String.valueOf(cachedPatientRepository
                .findById(appointment.getPatientId())
                .map(CachedPatient::getFullName)
                .orElse("Unknown")

        );

        kafkaProducer.sendAppointmentUpdatedEvent(appointment);

        AppointmentResponseDto appointmentResponseDto = AppointmentMapper.toDto(appointment);
        appointmentResponseDto.setPatientName(fullName);

        return appointmentResponseDto;





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
            throw new IllegalTimeSlotException("Time slot having start time: {} and end time: {} is illegal." +
                    " Please enter legal time having 30 minutes time slot."+startTime+endTime);
        }
    }

    private void validateSlotBoundary(LocalDateTime start, LocalDateTime end){
        if(start.getMinute() % 30 !=0 || end.getMinute() % 30 !=0){
            throw new IllegalSlotBoundaryException("Slot must allign to 30-minute boundary");
        }
    }
//Validation on the basis of patients availability as per the other patients appointment records
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

    private void validateDoctorWorkingHours(
            UUID doctorId,
            LocalDateTime start,
            LocalDateTime end
    ){
        DoctorAvailability availability =
                doctorAvailabilityRepository.findById(doctorId)
                        .orElseThrow(() ->
                                new NoDoctorAvailableException("Doctor availability not found")
                        );
        log.info("Value of availability param is {}",availability.getDoctorName());
        LocalTime slotStart = start.toLocalTime();
        LocalTime slotEnd = end.toLocalTime();
        log.info("Slot start time for patient {},\n" +
                "Slot end time for patient {},\n" +
                "Slot start time for doctor {},\n" +
                "Slot end time for doctor {}",slotStart,slotEnd,
                availability.getAvailableFrom().toLocalTime(),
                availability.getAvailableTo().toLocalTime());

        if(slotStart.isBefore(availability.getAvailableFrom().toLocalTime())
            || slotEnd.isAfter(availability.getAvailableTo().toLocalTime())){
            throw new DoctorSlotUnavailabilityException(
                    "Slot outside doctor's working hours");
        }
    }

    private void validateDoctorSlotAvailability(
            UUID doctorId,
            UUID appointmentId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        boolean conflict =
                !appointmentRepository
                        .findDoctorOverlapsExcludingCurrent(
                                doctorId, appointmentId, startTime, endTime
                        ).isEmpty();

        if(conflict) {
            throw new DoctorAlreadyAppointmentException(
                    "Doctor already has appointment in this slot"
            );
        }
    }

    public void validateDoctorSlotAvailabilityAsPerOtherAppointments(
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime
                        ){
        boolean conflict =
                !appointmentRepository
                        .findDoctorOverlaps(
                                doctorId, startTime, endTime
                        ).isEmpty();

        if(conflict) {
            throw new DoctorAlreadyAppointmentException(
                    "Doctor already has appointment in this slot"
            );
        }
    }

    public void validateDoctorExists(
            UUID doctorId
    ){
        if (!doctorAvailabilityRepository.existsById(doctorId)) {
            throw new NoDoctorAvailableException("Doctor not found with : {}"+doctorId);
        }
    }

    private void validatePatientSlotAvailability(
            UUID patientId,
                UUID appointmentId,
                        LocalDateTime startTime,
                        LocalDateTime endTime
                        ){
        boolean conflict =
                !appointmentRepository
                        .findPatientOverlapsExcludingCurrent(
                                patientId, appointmentId, startTime, endTime
                        ).isEmpty();

        if(conflict) {
            throw new PatientAlreadyHasAppointmentException(
                    "Patient already has appointment in this slot"
            );
        }

    }

}
