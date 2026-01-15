package com.pm.appointmentservice.kafka;

import appointment.events.AppointmentEvent;
import com.pm.appointmentservice.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]>  kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAppointmentCreatedEvent(Appointment  appointment) {
        AppointmentEvent appointmentCreationEvent = AppointmentEvent.newBuilder()
                .setAppointmentId(appointment.getId().toString())
                .setDoctorId(appointment.getDoctorId().toString())
                .setStartTime(appointment.getStartTime().toString())
                .setEndTime(appointment.getEndTime().toString())
                .setReason(appointment.getReason())
                .setEventType("appointment_created")
                .build();
        try{
            kafkaTemplate.send("appointment.updated", appointmentCreationEvent.toByteArray());
        }catch(Exception e){
            log.error("Error sending PatientCreated event: {}", appointmentCreationEvent);
        }
    }

    public void sendAppointmentUpdatedEvent(Appointment appointment) {
        AppointmentEvent appointmentUpdateEvent = AppointmentEvent.newBuilder()
                .setAppointmentId(appointment.getId().toString())
                .setDoctorId(appointment.getDoctorId().toString())
                .setStartTime(appointment.getStartTime().toString())
                .setEndTime(appointment.getEndTime().toString())
                .setReason(appointment.getReason())
                .setEventType("appointment_updated")
                .build();
        try{
            kafkaTemplate.send("appointment.updated", appointmentUpdateEvent.toByteArray());
        }catch(Exception e){
            log.error("Error sending PatientUpdated event: {}", appointmentUpdateEvent);
        }
    }


}
