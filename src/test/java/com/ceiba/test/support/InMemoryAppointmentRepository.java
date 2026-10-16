package com.ceiba.test.support;

import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentFilter;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.domain.repository.AppointmentRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final Map<Long, Appointment> store = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Appointment save(Appointment appointment) {
        Appointment stored = appointment.getId() != null ? appointment : Appointment.restore(
                sequence.incrementAndGet(), appointment.getPatientId(), appointment.getDoctorId(),
                appointment.getStartTime(), appointment.getStatus(), appointment.getCancelledAt(),
                appointment.getCreatedAt(), appointment.getRescheduledFromId());
        store.put(stored.getId(), stored);
        return stored;
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Appointment> search(AppointmentFilter filter) {
        return store.values().stream()
                .filter(a -> filter.doctorId() == null || a.getDoctorId().equals(filter.doctorId()))
                .filter(a -> filter.patientId() == null || a.getPatientId().equals(filter.patientId()))
                .filter(a -> filter.status() == null || a.getStatus() == filter.status())
                .filter(a -> filter.from() == null || !a.getStartTime().isBefore(filter.from()))
                .filter(a -> filter.to() == null || a.getStartTime().isBefore(filter.to()))
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .toList();
    }

    @Override
    public boolean existsScheduledByDoctorAt(Long doctorId, LocalDateTime startTime) {
        return store.values().stream().anyMatch(a -> a.isScheduled()
                && a.getDoctorId().equals(doctorId) && a.getStartTime().equals(startTime));
    }

    @Override
    public boolean existsScheduledByPatientAt(Long patientId, LocalDateTime startTime) {
        return store.values().stream().anyMatch(a -> a.isScheduled()
                && a.getPatientId().equals(patientId) && a.getStartTime().equals(startTime));
    }

    @Override
    public Set<LocalDateTime> findScheduledStartTimes(Long doctorId, LocalDateTime from, LocalDateTime to) {
        return store.values().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PROGRAMADA && a.getDoctorId().equals(doctorId))
                .map(Appointment::getStartTime)
                .filter(t -> !t.isBefore(from) && t.isBefore(to))
                .collect(Collectors.toSet());
    }
}
