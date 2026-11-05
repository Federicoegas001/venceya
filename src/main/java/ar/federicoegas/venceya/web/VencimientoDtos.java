package ar.federicoegas.venceya.web;

import ar.federicoegas.venceya.domain.EstadoVencimiento;
import ar.federicoegas.venceya.domain.TipoVencimiento;
import ar.federicoegas.venceya.domain.Vencimiento;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class VencimientoDtos {

    private VencimientoDtos() {
    }

    public record VencimientoRequest(
            @NotBlank @Size(max = 150) String nombre,
            @NotNull TipoVencimiento tipo,
            @NotNull LocalDate fechaVencimiento,
            @NotBlank @Email String responsableEmail,
            @Size(max = 100) String proveedor,
            @PositiveOrZero BigDecimal costoAnual) {
    }

    public record VencimientoResponse(
            Long id, String nombre, TipoVencimiento tipo, LocalDate fechaVencimiento,
            long diasRestantes, EstadoVencimiento estado, String responsableEmail,
            String proveedor, BigDecimal costoAnual) {

        public static VencimientoResponse de(Vencimiento v, LocalDate hoy) {
            return new VencimientoResponse(v.getId(), v.getNombre(), v.getTipo(), v.getFechaVencimiento(),
                    v.diasRestantes(hoy), v.estado(hoy), v.getResponsableEmail(), v.getProveedor(), v.getCostoAnual());
        }
    }

    public record ResumenResponse(long vencidos, long criticos, long proximos, long enRegla, BigDecimal costoAnualTotal) {
    }
}
