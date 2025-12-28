package com.pm.patientservice.aspects;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PatientServiceMetrics {

    private final MeterRegistry meterRegistry;

    public PatientServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
//  Point cut written below which tells from here we need to execute some set of methods before executing the BELOW method.
//  This change and the other changes at that instant will be monitored with the help of joint-points
    @Around("execution(* com.pm.patientservice.service.PatientService.getPatients(..))")
    public Object monitorGetPatients(ProceedingJoinPoint jointPoint) throws Throwable {
        meterRegistry.counter("custom.redis.cache.miss", "cache", "patients").increment();

        Object result = jointPoint.proceed();

        return result;
    }
}
