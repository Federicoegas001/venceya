package ar.federicoegas.venceya.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaDeAlertasTest {

    @ParameterizedTest(name = "faltan {0} días sin avisos previos → umbral {1}")
    @CsvSource({"30,30", "20,30", "15,15", "10,15", "7,7", "3,7", "1,1", "0,0"})
    void eligeElUmbralCorrecto(long dias, int esperado) {
        assertThat(PoliticaDeAlertas.umbralAAvisar(dias, null)).contains(esperado);
    }

    @Test
    void noAvisaDosVecesElMismoUmbral() {
        assertThat(PoliticaDeAlertas.umbralAAvisar(12, 15)).isEmpty();
    }

    @Test
    void pasaAlSiguienteUmbralCuandoCorresponde() {
        assertThat(PoliticaDeAlertas.umbralAAvisar(7, 15)).contains(7);
    }

    @Test
    void masDeTreintaDiasNoAvisa() {
        assertThat(PoliticaDeAlertas.umbralAAvisar(31, null)).isEmpty();
    }

    @Test
    void vencidoAvisaTodosLosDias() {
        assertThat(PoliticaDeAlertas.umbralAAvisar(-3, 0)).isEqualTo(Optional.of(-1));
    }

    @Test
    void estadoSegunDias() {
        assertThat(EstadoVencimiento.desdeDiasRestantes(-1)).isEqualTo(EstadoVencimiento.VENCIDO);
        assertThat(EstadoVencimiento.desdeDiasRestantes(7)).isEqualTo(EstadoVencimiento.CRITICO);
        assertThat(EstadoVencimiento.desdeDiasRestantes(30)).isEqualTo(EstadoVencimiento.PROXIMO);
        assertThat(EstadoVencimiento.desdeDiasRestantes(31)).isEqualTo(EstadoVencimiento.EN_REGLA);
    }
}
