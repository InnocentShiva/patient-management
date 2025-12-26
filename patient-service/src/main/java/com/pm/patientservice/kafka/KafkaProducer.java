package com.pm.patientservice.kafka;

import billing.events.BillingAccountEvent;
import com.pm.patientservice.model.Patient;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;
import org.slf4j.Logger;



@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();

        try{
            kafkaTemplate.send("patient", patientEvent.toByteArray());
        }catch(Exception e){
            log.error("Error sending PatientCreated event: {}", patientEvent);
        }
    }

    public void sendBillingAccountEvent(String patientId, String name, String email){

        BillingAccountEvent event = BillingAccountEvent.newBuilder()
                .setPatientId(patientId)
                .setEmail(email)
                .setName(name)
                .setEventType("BILLING_ACCOUNT_CREATED_REQUESTED")
                .build();

        try{
            kafkaTemplate.send("billing-account", event.toByteArray());
        }catch(Exception e){
            log.error("Error sending BillingAccountCreated event: {}", e.getMessage());
        }

    }

}
