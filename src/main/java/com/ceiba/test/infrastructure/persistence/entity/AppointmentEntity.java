package com.ceiba.test.infrastructure.persistence.entity;

import com.ceiba.test.domain.model.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_appointment_doctor_slot", columnNames = "doctor_slot_key"),
                @UniqueConstraint(name = "uk_appointment_patient_slot", columnNames = "patient_slot_key")
        },
        indexes = {
                @Index(name = "idx_appointment_doctor_start", columnList = "doctor_id, start_time"),
                @Index(name = "idx_appointment_patient_start", columnList = "patient_id, start_time")
        })
@Getter
@Setter
@NoArgsConstructor
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "rescheduled_from_id")
    private Long rescheduledFromId;

    @Column(name = "doctor_slot_key", length = 60)
    private String doctorSlotKey;

    @Column(name = "patient_slot_key", length = 60)
    private String patientSlotKey;
}
