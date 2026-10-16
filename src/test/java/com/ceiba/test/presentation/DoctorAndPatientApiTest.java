package com.ceiba.test.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ceiba.test.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;

class DoctorAndPatientApiTest extends ApiIntegrationTest {

    @Test
    void registersDoctor() throws Exception {
        postJson("/api/medicos", """
                {"nombreCompleto": "Dra. María González", "especialidad": "Cardiología",
                 "telefono": "555-1001", "email": "maria.gonzalez@medisalud.com"}""")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/medicos/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombreCompleto").value("Dra. María González"))
                .andExpect(jsonPath("$.especialidad").value("Cardiología"));
    }

    @Test
    void doctorOnlyRequiresNameAndSpecialty() throws Exception {
        postJson("/api/medicos", """
                {"nombreCompleto": "Dr. Sin Contacto", "especialidad": "Pediatría"}""")
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsInvalidDoctor() throws Exception {
        postJson("/api/medicos", """
                {"nombreCompleto": "Dr", "telefono": "123", "email": "no-es-email"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/medicos"))
                .andExpect(jsonPath("$.details[*].field")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("nombreCompleto", "especialidad",
                                "telefono", "email")));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        postJson("/api/medicos", "{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void getsDoctorById() throws Exception {
        long id = createDoctor();

        mockMvc.perform(get("/api/medicos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void unknownDoctorReturns404() throws Exception {
        mockMvc.perform(get("/api/medicos/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOCTOR_NOT_FOUND"));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/medicos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void listsDoctors() throws Exception {
        createDoctor();

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(hasSize(0))));
    }

    @Test
    void registersPatient() throws Exception {
        postJson("/api/pacientes", """
                {"nombreCompleto": "Laura Gómez", "documentoIdentidad": "ab-1234567",
                 "telefono": "+57 300 123 4567", "email": "laura@mail.com", "fechaNacimiento": "20-05-1990"}""")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/pacientes/")))
                .andExpect(jsonPath("$.documentoIdentidad").value("AB-1234567"))
                .andExpect(jsonPath("$.fechaNacimiento").value("20-05-1990"));
    }

    @Test
    void rejectsDuplicatedDocumentIgnoringCase() throws Exception {
        String body = """
                {"nombreCompleto": "Laura Gómez", "documentoIdentidad": "%s",
                 "telefono": "3001234567", "email": "laura@mail.com"}""";
        postJson("/api/pacientes", body.formatted("dup-998877")).andExpect(status().isCreated());

        postJson("/api/pacientes", body.formatted("DUP-998877"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_DOCUMENT"));
    }

    @Test
    void rejectsInvalidPatient() throws Exception {
        postJson("/api/pacientes", """
                {"nombreCompleto": "Lu", "documentoIdentidad": "123", "telefono": "12", "email": "x"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(4)));
    }

    @Test
    void patientRequiresPhoneAndEmail() throws Exception {
        postJson("/api/pacientes", """
                {"nombreCompleto": "Laura Gómez", "documentoIdentidad": "1234567"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("telefono", "email")));
    }

    @Test
    void rejectsDocumentWithInjectionCharacters() throws Exception {
        postJson("/api/pacientes", """
                {"nombreCompleto": "Laura Gómez", "documentoIdentidad": "1234567'; DROP TABLE patients;--",
                 "telefono": "3001234567", "email": "laura@mail.com"}""")
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPatientReturns404() throws Exception {
        mockMvc.perform(get("/api/pacientes/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATIENT_NOT_FOUND"));
    }
}
