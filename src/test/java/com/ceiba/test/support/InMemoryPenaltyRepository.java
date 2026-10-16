package com.ceiba.test.support;

import com.ceiba.test.domain.model.Penalty;
import com.ceiba.test.domain.repository.PenaltyRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryPenaltyRepository implements PenaltyRepository {

    private final List<Penalty> store = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Penalty save(Penalty penalty) {
        Penalty stored = Penalty.restore(sequence.incrementAndGet(), penalty.getPatientId(),
                penalty.getAppointmentId(), penalty.getCreatedAt());
        store.add(stored);
        return stored;
    }

    @Override
    public long countByPatientSince(Long patientId, LocalDateTime since) {
        return store.stream()
                .filter(p -> p.getPatientId().equals(patientId) && !p.getCreatedAt().isBefore(since))
                .count();
    }

    public void addPenaltyAt(Long patientId, LocalDateTime createdAt) {
        save(Penalty.issue(patientId, 0L, createdAt));
    }

    public int size() {
        return store.size();
    }
}
