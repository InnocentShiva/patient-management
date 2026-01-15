package com.pm.analyticsservice.kafka;

import appointment.events.AppointmentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics="patient.created", groupId = "analytics-service")
    public void consumeEvent(byte[] event){
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            //    ............. Perform any business related to analytics here
            log.info("Received patient event: [PatientId = {}, PatientName = {}, PatientEmailId = {} ]",
                    patientEvent.getPatientId(),patientEvent.getName(),patientEvent.getEmail());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing the patient create event {} ",e.getMessage());
        }
    }

    @KafkaListener(topics = "appointment.created", groupId = "analytics-service")
    public void consumeAppointmentCreateEvent(byte[] event){
        try {
            AppointmentEvent appointmentUpdateEvent = AppointmentEvent.parseFrom(event);
            //----- ...............Perform any business logic related to analytics here
            log.info("Received appointment created event: [AppointmentId = {}," +
                            "DoctorId = {}, Start-time = {}, End-time = {} , reason = {} " +
                            "Event-Type = {}]",
                    appointmentUpdateEvent.getAppointmentId(), appointmentUpdateEvent.getDoctorId(),
                    appointmentUpdateEvent.getStartTime(), appointmentUpdateEvent.getEndTime(),
                    appointmentUpdateEvent.getReason(),  appointmentUpdateEvent.getEventType());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing the appointment create event {} ",e.getMessage());
        }
    }

    @KafkaListener(topics = "appointment.updated", groupId = "analytics-service")
    public void consumeAppointmentUpdateEvent(byte[] event){
        try {
            AppointmentEvent appointmentEvent = AppointmentEvent.parseFrom(event);
            //----- ...............Perform any business logic related to analytics here
            log.info("Received appointment updated event: [AppointmentId = {}," +
                    "DoctorId = {}, Start-time = {}, End-time = {} , reason = {} " +
                            "Event-Type = {}]",
                    appointmentEvent.getAppointmentId(), appointmentEvent.getDoctorId(),
                    appointmentEvent.getStartTime(), appointmentEvent.getEndTime(),
                    appointmentEvent.getReason(),  appointmentEvent.getEventType());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing the appointment update event {} ",e.getMessage());
        }
    }

}
