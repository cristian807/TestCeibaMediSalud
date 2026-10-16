package com.ceiba.test.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
public abstract class ApiIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1_000_000);

    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected long createDoctor() throws Exception {
        String response = postJson("/api/medicos", """
                {"nombreCompleto": "Dr. Test Medico", "especialidad": "Cardiología"}""")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected long createPatient() throws Exception {
        return createPatient(null);
    }

    protected long createPatient(String birthDate) throws Exception {
        int n = SEQUENCE.incrementAndGet();
        String birth = birthDate == null ? "" : ", \"fechaNacimiento\": \"%s\"".formatted(birthDate);
        String response = postJson("/api/pacientes", """
                {"nombreCompleto": "Paciente %d", "documentoIdentidad": "DOC%d",
                 "telefono": "555-%d", "email": "paciente%d@mail.com"%s}""".formatted(n, n, n, n, birth))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected ResultActions book(long patientId, long doctorId, String dateTime) throws Exception {
        return postJson("/api/citas", """
                {"pacienteId": %d, "medicoId": %d, "fechaHora": "%s"}""".formatted(patientId, doctorId, dateTime));
    }

    protected long bookOk(long patientId, long doctorId, String dateTime) throws Exception {
        String response = book(patientId, doctorId, dateTime).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
