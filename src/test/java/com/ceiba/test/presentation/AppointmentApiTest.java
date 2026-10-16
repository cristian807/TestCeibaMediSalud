package com.ceiba.test.presentation;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ceiba.test.support.ApiIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

class AppointmentApiTest extends ApiIntegrationTest {

    private static final String TUESDAY_9 = "02-06-2026 9:00";

    @Test
    void booksAnAppointment() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();

        book(patient, doctor, TUESDAY_9)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/citas/")))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.pacienteId").value(patient))
                .andExpect(jsonPath("$.medicoId").value(doctor))
                .andExpect(jsonPath("$.fechaHora").value(TUESDAY_9))
                .andExpect(jsonPath("$.fechaHoraFin").value("02-06-2026 9:30"));
    }

    @Test
    void getsAnAppointmentById() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), TUESDAY_9);

        mockMvc.perform(get("/api/citas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void unknownAppointmentReturns404() throws Exception {
        mockMvc.perform(get("/api/citas/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_NOT_FOUND"));
    }

    @Test
    void rejectsUnknownPatientAndDoctor() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();

        book(987654, doctor, TUESDAY_9).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATIENT_NOT_FOUND"));
        book(patient, 987654, TUESDAY_9).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOCTOR_NOT_FOUND"));
    }

    @Test
    void rejectsMissingFieldsAndMalformedDate() throws Exception {
        postJson("/api/citas", "{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(3)));
        postJson("/api/citas", """
                {"pacienteId": 1, "medicoId": 1, "fechaHora": "mañana a las 9"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsSundayHolidayAndOutOfHours() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();

        book(patient, doctor, "07-06-2026 9:00").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NON_WORKING_DAY"));
        book(patient, doctor, "08-06-2026 9:00").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NON_WORKING_DAY"));
        book(patient, doctor, "02-06-2026 18:00").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUTSIDE_WORKING_HOURS"));
        book(patient, doctor, "06-06-2026 13:00").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUTSIDE_WORKING_HOURS"));
        book(patient, doctor, "02-06-2026 9:10").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SLOT_ALIGNMENT"));
        book(patient, doctor, "29-05-2026 9:00").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_IN_THE_PAST"));
    }

    @Test
    void acceptsLastSaturdaySlot() throws Exception {
        book(createPatient(), createDoctor(), "06-06-2026 12:30").andExpect(status().isCreated());
    }

    @Test
    void rejectsDoctorDoubleBooking() throws Exception {
        long doctor = createDoctor();
        bookOk(createPatient(), doctor, TUESDAY_9);

        book(createPatient(), doctor, TUESDAY_9)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DOCTOR_SLOT_TAKEN"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsPatientDoubleBookingWithAnotherDoctor() throws Exception {
        long patient = createPatient();
        bookOk(patient, createDoctor(), TUESDAY_9);

        book(patient, createDoctor(), TUESDAY_9)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PATIENT_SLOT_TAKEN"));
    }

    @Test
    void patientWithoutBirthDateCanBook() throws Exception {
        book(createPatient(), createDoctor(), TUESDAY_9).andExpect(status().isCreated());
    }

    @Test
    void patientWithFutureBirthDateCannotBook() throws Exception {
        long patient = createPatient("01-01-2030");

        book(patient, createDoctor(), TUESDAY_9)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BIRTH_DATE"));
    }

    @Test
    void availabilityExcludesOccupiedSlots() throws Exception {
        long doctor = createDoctor();
        bookOk(createPatient(), doctor, TUESDAY_9);

        mockMvc.perform(get("/api/citas/disponibles")
                        .param("medicoId", String.valueOf(doctor))
                        .param("fechaInicio", "02-06-2026").param("fechaFin", "02-06-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(19)))
                .andExpect(jsonPath("$[*].inicio", not(hasItem(TUESDAY_9))))
                .andExpect(jsonPath("$[0].inicio").value("02-06-2026 8:00"))
                .andExpect(jsonPath("$[0].fin").value("02-06-2026 8:30"));
    }

    @Test
    void availabilityCoversRangeAndSkipsSundayAndHoliday() throws Exception {
        long doctor = createDoctor();

        mockMvc.perform(get("/api/citas/disponibles")
                        .param("medicoId", String.valueOf(doctor))
                        .param("fechaInicio", "06-06-2026").param("fechaFin", "09-06-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)));
    }

    @Test
    void availabilityValidatesInput() throws Exception {
        long doctor = createDoctor();

        mockMvc.perform(get("/api/citas/disponibles").param("medicoId", String.valueOf(doctor))
                        .param("fechaInicio", "05-06-2026").param("fechaFin", "02-06-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
        mockMvc.perform(get("/api/citas/disponibles").param("medicoId", String.valueOf(doctor)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/citas/disponibles").param("medicoId", String.valueOf(doctor))
                        .param("fechaInicio", "2026-06-02").param("fechaFin", "02-06-2026"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/citas/disponibles").param("medicoId", "987654")
                        .param("fechaInicio", "02-06-2026").param("fechaFin", "02-06-2026"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelsAppointmentWithoutPenaltyWhenEarly() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), TUESDAY_9);

        mockMvc.perform(patch("/api/citas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cita.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.cita.fechaCancelacion").value("01-06-2026 10:00"))
                .andExpect(jsonPath("$.penalizacionAplicada").value(false));
    }

    @Test
    void lateCancellationAppliesPenalty() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), "01-06-2026 11:00");

        mockMvc.perform(patch("/api/citas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.penalizacionAplicada").value(true));
    }

    @Test
    void cancellationExactlyTwoHoursBeforeHasNoPenalty() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), "01-06-2026 12:00");

        mockMvc.perform(patch("/api/citas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.penalizacionAplicada").value(false));
    }

    @Test
    void cannotCancelTwiceNorUnknown() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), TUESDAY_9);
        mockMvc.perform(patch("/api/citas/{id}/cancelar", id)).andExpect(status().isOk());

        mockMvc.perform(patch("/api/citas/{id}/cancelar", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_NOT_CANCELLABLE"));
        mockMvc.perform(patch("/api/citas/{id}/cancelar", 987654)).andExpect(status().isNotFound());
    }

    @Test
    void cancelledSlotBecomesAvailableAgain() throws Exception {
        long doctor = createDoctor();
        long id = bookOk(createPatient(), doctor, TUESDAY_9);
        mockMvc.perform(patch("/api/citas/{id}/cancelar", id)).andExpect(status().isOk());

        bookOk(createPatient(), doctor, TUESDAY_9);
    }

    @Test
    void threePenaltiesBlockNewBookings() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();
        for (String slot : new String[] {"01-06-2026 10:30", "01-06-2026 11:00", "01-06-2026 11:30"}) {
            long id = bookOk(patient, doctor, slot);
            mockMvc.perform(patch("/api/citas/{id}/cancelar", id))
                    .andExpect(jsonPath("$.penalizacionAplicada").value(true));
        }

        book(patient, doctor, TUESDAY_9)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PATIENT_BLOCKED_BY_PENALTIES"));
        book(createPatient(), doctor, TUESDAY_9).andExpect(status().isCreated());
    }

    @Test
    void twoPenaltiesDoNotBlock() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();
        for (String slot : new String[] {"01-06-2026 10:30", "01-06-2026 11:00"}) {
            long id = bookOk(patient, doctor, slot);
            mockMvc.perform(patch("/api/citas/{id}/cancelar", id)).andExpect(status().isOk());
        }

        book(patient, doctor, TUESDAY_9).andExpect(status().isCreated());
    }

    @Test
    void reschedulesAnAppointment() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();
        long id = bookOk(patient, doctor, TUESDAY_9);

        String response = postJson("/api/citas/" + id + "/reprogramar", """
                {"nuevaFechaHora": "03-06-2026 14:00"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citaCancelada.id").value(id))
                .andExpect(jsonPath("$.citaCancelada.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.citaNueva.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.citaNueva.fechaHora").value("03-06-2026 14:00"))
                .andExpect(jsonPath("$.citaNueva.medicoId").value(doctor))
                .andExpect(jsonPath("$.citaNueva.citaOriginalId").value(id))
                .andExpect(jsonPath("$.penalizacionAplicada").value(false))
                .andReturn().getResponse().getContentAsString();
        int newId = JsonPath.read(response, "$.citaNueva.id");

        mockMvc.perform(get("/api/citas/{id}", id)).andExpect(jsonPath("$.estado").value("CANCELADA"));
        mockMvc.perform(get("/api/citas/{id}", newId)).andExpect(jsonPath("$.estado").value("PROGRAMADA"));
        book(createPatient(), doctor, TUESDAY_9).andExpect(status().isCreated());
    }

    @Test
    void rescheduleToOccupiedSlotIsRejectedAndKeepsOriginalAppointment() throws Exception {
        long doctor = createDoctor();
        long id = bookOk(createPatient(), doctor, TUESDAY_9);
        bookOk(createPatient(), doctor, "03-06-2026 14:00");

        postJson("/api/citas/" + id + "/reprogramar", """
                {"nuevaFechaHora": "03-06-2026 14:00"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DOCTOR_SLOT_TAKEN"));

        mockMvc.perform(get("/api/citas/{id}", id)).andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void rescheduleRejectsInvalidSlotsAndUnknownAppointment() throws Exception {
        long id = bookOk(createPatient(), createDoctor(), TUESDAY_9);

        postJson("/api/citas/" + id + "/reprogramar", """
                {"nuevaFechaHora": "07-06-2026 10:00"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NON_WORKING_DAY"));
        postJson("/api/citas/" + id + "/reprogramar", "{}").andExpect(status().isBadRequest());
        postJson("/api/citas/987654/reprogramar", """
                {"nuevaFechaHora": "03-06-2026 14:00"}""").andExpect(status().isNotFound());
        mockMvc.perform(get("/api/citas/{id}", id)).andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void lateRescheduleAppliesPenalty() throws Exception {
        long id = bookOk(createPatient(),         createDoctor(), "01-06-2026 11:00");

        postJson("/api/citas/" + id + "/reprogramar", """
                        {"nuevaFechaHora": "03-06-2026 14:00"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.penalizacionAplicada").value(true));
    }

    @Test
    void listsAppointmentsWithFilters() throws Exception {
        long doctorA = createDoctor();
        long doctorB = createDoctor();
        long patientA = createPatient();
        long patientB = createPatient();
        bookOk(patientA, doctorA, "02-06-2026 9:00");
        long cancelled = bookOk(patientB, doctorA, "03-06-2026 9:00");
        bookOk(patientA, doctorB, "04-06-2026 9:00");
        mockMvc.perform(patch("/api/citas/{id}/cancelar", cancelled)).andExpect(status().isOk());

        mockMvc.perform(get("/api/citas").param("medicoId", String.valueOf(doctorA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/citas").param("pacienteId", String.valueOf(patientA)))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/citas").param("medicoId", String.valueOf(doctorA)).param("estado", "CANCELADA"))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id").value(cancelled));
        mockMvc.perform(get("/api/citas").param("medicoId", String.valueOf(doctorA)).param("estado", "ATENDIDA"))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/citas").param("pacienteId", String.valueOf(patientA))
                        .param("fechaInicio", "03-06-2026").param("fechaFin", "04-06-2026"))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].fechaHora").value("04-06-2026 9:00"));
        mockMvc.perform(get("/api/citas").param("pacienteId", String.valueOf(patientA))
                        .param("fechaInicio", "02-06-2026").param("fechaFin", "02-06-2026"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listRejectsInvalidFilters() throws Exception {
        mockMvc.perform(get("/api/citas").param("estado", "INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/citas").param("fechaInicio", "05-06-2026").param("fechaFin", "01-06-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
    }

    @Test
    void unsupportedMethodReturns405WithUniformBody() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/citas/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
