package com.ceiba.test.infrastructure.persistence.mapper;

import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.infrastructure.persistence.entity.AppointmentEntity;
import com.ceiba.test.infrastructure.persistence.entity.DoctorEntity;
import com.ceiba.test.infrastructure.persistence.entity.PatientEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentPersistenceMapper {

    public void copyToEntity(
            Appointment appointment,
            AppointmentEntity entity,
            PatientEntity patient,
            DoctorEntity doctor
    ) {
        entity.setPatient(patient);
        entity.setDoctor(doctor);
        entity.setStartTime(appointment.getStartTime());
        entity.setStatus(appointment.getStatus());
        entity.setCancelledAt(appointment.getCancelledAt());
        entity.setCreatedAt(appointment.getCreatedAt());
        entity.setRescheduledFromId(appointment.getRescheduledFromId());
        boolean active = appointment.getStatus() == AppointmentStatus.PROGRAMADA;
        entity.setDoctorSlotKey(active ? appointment.getDoctorId() + "|" + appointment.getStartTime() : null);
        entity.setPatientSlotKey(active ? appointment.getPatientId() + "|" + appointment.getStartTime() : null);
    }

    public Appointment toDomain(AppointmentEntity entity) {
        return Appointment.restore(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getDoctor().getId(),
                entity.getStartTime(),
                entity.getStatus(),
                entity.getCancelledAt(),
                entity.getCreatedAt(),
                entity.getRescheduledFromId());
    }
}
