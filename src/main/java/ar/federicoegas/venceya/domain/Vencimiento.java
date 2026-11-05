package ar.federicoegas.venceya.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "vencimiento", indexes = @Index(name = "idx_vencimiento_fecha", columnList = "fecha_vencimiento"))
public class Vencimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVencimiento tipo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "responsable_email", nullable = false, length = 150)
    private String responsableEmail;

    @Column(length = 100)
    private String proveedor;

    @Column(name = "costo_anual", precision = 14, scale = 2)
    private BigDecimal costoAnual;

    /** Último umbral (30, 15, 7, 1, 0) para el que ya se mandó aviso. Se reinicia al renovar. */
    @Column(name = "ultimo_umbral_avisado")
    private Integer ultimoUmbralAvisado;

    protected Vencimiento() {
    }

    public Vencimiento(String nombre, TipoVencimiento tipo, LocalDate fechaVencimiento,
                       String responsableEmail, String proveedor, BigDecimal costoAnual) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.fechaVencimiento = fechaVencimiento;
        this.responsableEmail = responsableEmail;
        this.proveedor = proveedor;
        this.costoAnual = costoAnual;
    }

    public long diasRestantes(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, fechaVencimiento);
    }

    public EstadoVencimiento estado(LocalDate hoy) {
        return EstadoVencimiento.desdeDiasRestantes(diasRestantes(hoy));
    }

    /** Renueva por un año desde la fecha actual de vencimiento (o desde hoy si ya venció). */
    public void renovar(LocalDate hoy) {
        LocalDate base = fechaVencimiento.isBefore(hoy) ? hoy : fechaVencimiento;
        this.fechaVencimiento = base.plusYears(1);
        this.ultimoUmbralAvisado = null;
    }

    public void registrarAviso(int umbral) {
        if (umbral >= 0) {
            this.ultimoUmbralAvisado = umbral;
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoVencimiento getTipo() { return tipo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public String getResponsableEmail() { return responsableEmail; }
    public String getProveedor() { return proveedor; }
    public BigDecimal getCostoAnual() { return costoAnual; }
    public Integer getUltimoUmbralAvisado() { return ultimoUmbralAvisado; }

    public void actualizar(String nombre, TipoVencimiento tipo, LocalDate fecha, String email,
                           String proveedor, BigDecimal costoAnual) {
        this.nombre = nombre;
        this.tipo = tipo;
        if (!fecha.equals(this.fechaVencimiento)) {
            this.ultimoUmbralAvisado = null;
        }
        this.fechaVencimiento = fecha;
        this.responsableEmail = email;
        this.proveedor = proveedor;
        this.costoAnual = costoAnual;
    }
}
