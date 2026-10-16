package com.ceiba.test.presentation.controller;

import com.ceiba.test.application.dto.DoctorRequest;
import com.ceiba.test.application.dto.DoctorResponse;
import com.ceiba.test.application.usecase.DoctorUseCase;
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
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "RF-01 Registro de médicos")
public class DoctorController {

    private final DoctorUseCase doctors;

    public DoctorController(DoctorUseCase doctors) {
        this.doctors = doctors;
    }

    @PostMapping
    @Operation(summary = "Registrar un médico")
    public ResponseEntity<DoctorResponse> register(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse created = doctors.register(request);
        return ResponseEntity.created(location(created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar médicos")
    public List<DoctorResponse> list() {
        return doctors.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un médico por id")
    public DoctorResponse getById(@PathVariable Long id) {
        return doctors.getById(id);
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
