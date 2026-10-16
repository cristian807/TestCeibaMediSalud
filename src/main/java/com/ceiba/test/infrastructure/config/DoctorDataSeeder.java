package com.ceiba.test.infrastructure.config;

import com.ceiba.test.application.dto.DoctorRequest;
import com.ceiba.test.application.usecase.DoctorUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DoctorDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DoctorDataSeeder.class);

    private final DoctorUseCase doctors;

    public DoctorDataSeeder(DoctorUseCase doctors) {
        this.doctors = doctors;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!doctors.list().isEmpty()) {
            return;
        }
        List.of(
                new DoctorRequest("Dra. María González", "Cardiología", "555-1001", "maria.gonzalez@medisalud.com"),
                new DoctorRequest("Dr. Carlos Ruiz", "Pediatría", "555-1002", "carlos.ruiz@medisalud.com"),
                new DoctorRequest("Dra. Ana López", "Dermatología", "555-1003", "ana.lopez@medisalud.com")
        ).forEach(doctors::register);
        log.info("Datos de ejemplo cargados: 3 médicos");
    }
}
