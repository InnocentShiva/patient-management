package com.pm.appointmentservice.repository;

import com.pm.appointmentservice.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, UUID> {

    boolean existsByDoctorId(UUID doctorId);
}
