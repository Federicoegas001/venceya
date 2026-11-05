package ar.federicoegas.venceya.job;

import ar.federicoegas.venceya.domain.PoliticaDeAlertas;
import ar.federicoegas.venceya.domain.Vencimiento;
import ar.federicoegas.venceya.notify.Notificador;
import ar.federicoegas.venceya.repository.VencimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AlertaVencimientoJob {

    private static final Logger log = LoggerFactory.getLogger(AlertaVencimientoJob.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VencimientoRepository repo;
    private final Notificador notificador;
    private final Clock clock;

    public AlertaVencimientoJob(VencimientoRepository repo, Notificador notificador, Clock clock) {
        this.repo = repo;
        this.notificador = notificador;
        this.clock = clock;
    }

    /** Todos los días a las 8:00 (hora de Argentina). */
    @Scheduled(cron = "${venceya.job.cron:0 0 8 * * *}", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void programado() {
        ejecutar();
    }

    /** Devuelve cuántos avisos mandó. También se puede disparar a mano desde la API. */
    @Transactional
    public int ejecutar() {
        LocalDate hoy = LocalDate.now(clock);
        int enviados = 0;
        for (Vencimiento v : repo.findByFechaVencimientoLessThanEqual(hoy.plusDays(30))) {
            long dias = v.diasRestantes(hoy);
            var umbral = PoliticaDeAlertas.umbralAAvisar(dias, v.getUltimoUmbralAvisado());
            if (umbral.isEmpty()) continue;
            try {
                notificador.enviar(v.getResponsableEmail(), asunto(v, dias), cuerpo(v, dias));
                v.registrarAviso(umbral.get());
                enviados++;
            } catch (RuntimeException e) {
                // Un mail que falla no frena al resto; se reintenta en la próxima corrida.
                log.warn("No se pudo avisar el vencimiento {} a {}: {}", v.getId(), v.getResponsableEmail(), e.getMessage());
            }
        }
        log.info("AlertaVencimientoJob: {} avisos enviados", enviados);
        return enviados;
    }

    private String asunto(Vencimiento v, long dias) {
        if (dias < 0) return "[VenceYa] VENCIDO: " + v.getNombre();
        if (dias == 0) return "[VenceYa] Vence HOY: " + v.getNombre();
        return "[VenceYa] " + v.getNombre() + " vence en " + dias + (dias == 1 ? " día" : " días");
    }

    private String cuerpo(Vencimiento v, long dias) {
        return """
                Hola,

                %s (%s) %s el %s.
                Proveedor: %s

                Cuando lo renueves, marcalo como renovado en VenceYa para frenar los avisos.
                """.formatted(v.getNombre(), v.getTipo(), dias < 0 ? "venció" : "vence",
                v.getFechaVencimiento().format(FECHA), v.getProveedor() == null ? "-" : v.getProveedor());
    }
}
