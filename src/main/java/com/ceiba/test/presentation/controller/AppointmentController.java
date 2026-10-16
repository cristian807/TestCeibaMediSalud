package com.ceiba.test.presentation.controller;

import com.ceiba.test.application.dto.AppointmentRequest;
import com.ceiba.test.application.dto.AppointmentResponse;
import com.ceiba.test.application.dto.AppointmentSearchCriteria;
import com.ceiba.test.application.dto.CancellationResponse;
import com.ceiba.test.application.dto.RescheduleRequest;
import com.ceiba.test.application.dto.RescheduleResponse;
import com.ceiba.test.application.dto.TimeSlotResponse;
import com.ceiba.test.application.usecase.AppointmentUseCase;
import com.ceiba.test.domain.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentUseCase appointments;

    @PostMapping
    @Operation(summary = "Reservar una cita")
    public ResponseEntity<AppointmentResponse> book(
            @Valid
            @RequestBody AppointmentRequest request
    ) {
        AppointmentResponse created = appointments.create(request);
        return ResponseEntity.created(location(created.id())).body(created);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Consultar franjas de 30 minutos disponibles de un médico (RF-04)")
    public List<TimeSlotResponse> availableSlots(
            @RequestParam Long medicoId,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        return appointments.findAvailableSlots(medicoId, fechaInicio, fechaFin);
    }

    @GetMapping
    @Operation(summary = "Listar citas con filtros opcionales (RF-06)")
    public List<AppointmentResponse> search(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) AppointmentStatus estado,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        return appointments.search(
                new AppointmentSearchCriteria(medicoId, pacienteId, estado, fechaInicio, fechaFin));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cita por id")
    public AppointmentResponse getById(@PathVariable Long id) {
        return appointments.getById(id);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una cita programada (RF-05, RN-05)")
    public CancellationResponse cancel(@PathVariable Long id) {
        return appointments.cancel(id);
    }

    @PostMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar una cita: cancela la actual y crea una nueva (RN-06)")
    public ResponseEntity<RescheduleResponse> reschedule(@PathVariable Long id,
                                                         @Valid @RequestBody RescheduleRequest request) {
        RescheduleResponse response = appointments.reschedule(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/citas/{id}")
                .buildAndExpand(response.citaNueva().id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
