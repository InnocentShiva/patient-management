package com.pm.appointmentservice.repository;

import com.pm.appointmentservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.patientId = :patientId
            AND a.startTime < :endTime
            AND a.endTime > :startTime
            """)
    List<Appointment> findOverlappingAppointments(
            UUID patientId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.id <> :appointmentId
              AND a.startTime < :endTime
              AND a.endTime > :startTime""")
    List<Appointment> findDoctorOverlapsExcludingCurrent(
            UUID doctorId,
            UUID appointmentId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );


    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.startTime < :endTime
              AND a.endTime > :startTime""")
    List<Appointment> findDoctorOverlaps(
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.patientId = :patientId
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    List<Appointment> findPatientOverlapsExcludingCurrent(
            UUID patientId,
            UUID appointmentId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    List<Appointment> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    boolean existsByIdAndPatientId(UUID id,UUID patientId);


}
