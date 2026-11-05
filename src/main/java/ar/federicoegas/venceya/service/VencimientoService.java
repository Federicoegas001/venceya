package ar.federicoegas.venceya.service;

import ar.federicoegas.venceya.domain.EstadoVencimiento;
import ar.federicoegas.venceya.domain.TipoVencimiento;
import ar.federicoegas.venceya.domain.Vencimiento;
import ar.federicoegas.venceya.repository.VencimientoRepository;
import ar.federicoegas.venceya.web.VencimientoDtos.ResumenResponse;
import ar.federicoegas.venceya.web.VencimientoDtos.VencimientoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class VencimientoService {

    private final VencimientoRepository repo;
    private final Clock clock;

    public VencimientoService(VencimientoRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    @Transactional(readOnly = true)
    public List<Vencimiento> listar(TipoVencimiento tipo) {
        return tipo == null ? repo.findAllByOrderByFechaVencimientoAsc() : repo.findByTipoOrderByFechaVencimientoAsc(tipo);
    }

    @Transactional(readOnly = true)
    public Vencimiento obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el vencimiento " + id));
    }

    @Transactional
    public Vencimiento crear(VencimientoRequest r) {
        return repo.save(new Vencimiento(r.nombre(), r.tipo(), r.fechaVencimiento(), r.responsableEmail(),
                r.proveedor(), r.costoAnual()));
    }

    @Transactional
    public Vencimiento actualizar(Long id, VencimientoRequest r) {
        Vencimiento v = obtener(id);
        v.actualizar(r.nombre(), r.tipo(), r.fechaVencimiento(), r.responsableEmail(), r.proveedor(), r.costoAnual());
        return v;
    }

    @Transactional
    public Vencimiento renovar(Long id) {
        Vencimiento v = obtener(id);
        v.renovar(hoy());
        return v;
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        LocalDate hoy = hoy();
        List<Vencimiento> todos = repo.findAll();
        BigDecimal costo = todos.stream().map(Vencimiento::getCostoAnual).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenResponse(
                contar(todos, hoy, EstadoVencimiento.VENCIDO),
                contar(todos, hoy, EstadoVencimiento.CRITICO),
                contar(todos, hoy, EstadoVencimiento.PROXIMO),
                contar(todos, hoy, EstadoVencimiento.EN_REGLA),
                costo);
    }

    private long contar(List<Vencimiento> l, LocalDate hoy, EstadoVencimiento e) {
        return l.stream().filter(v -> v.estado(hoy) == e).count();
    }
}
