package com.ceiba.test.presentation.controller;

import com.ceiba.test.application.dto.PatientRequest;
import com.ceiba.test.application.dto.PatientResponse;
import com.ceiba.test.application.usecase.PatientUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "RF-02 Registro de pacientes")
public class PatientController {

    private final PatientUseCase patients;

    public PatientController(PatientUseCase patients) {
        this.patients = patients;
    }

    @PostMapping
    @Operation(summary = "Registrar un paciente")
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        PatientResponse created = patients.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar pacientes")
    public List<PatientResponse> list() {
        return patients.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un paciente por id")
    public PatientResponse getById(@PathVariable Long id) {
        return patients.getById(id);
    }
}
