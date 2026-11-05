package ar.federicoegas.venceya.web;

import ar.federicoegas.venceya.notify.Notificador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VencimientoApiTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    Notificador notificador;

    @Test
    void creaYAvisaUnDominioQueVenceEnCincoDias() throws Exception {
        String body = """
                {"nombre":"rivas.com.ar","tipo":"DOMINIO","fechaVencimiento":"%s",
                 "responsableEmail":"it@rivas.com.ar","proveedor":"NIC Argentina","costoAnual":18000}
                """.formatted(LocalDate.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).plusDays(5));

        mvc.perform(post("/api/vencimientos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CRITICO"));

        mvc.perform(post("/api/vencimientos/alertas/ejecutar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(1));

        verify(notificador).enviar(eq("it@rivas.com.ar"), anyString(), anyString());

        // Segunda corrida el mismo día: ya se avisó el umbral de 7 días.
        mvc.perform(post("/api/vencimientos/alertas/ejecutar"))
                .andExpect(jsonPath("$.enviados").value(0));
    }

    @Test
    void rechazaEmailInvalido() throws Exception {
        String body = """
                {"nombre":"x","tipo":"LICENCIA","fechaVencimiento":"2027-01-01","responsableEmail":"no-es-mail"}
                """;
        mvc.perform(post("/api/vencimientos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.responsableEmail").exists());
    }

    @Test
    void devuelve404SiNoExiste() throws Exception {
        mvc.perform(get("/api/vencimientos/9999")).andExpect(status().isNotFound());
    }
}
