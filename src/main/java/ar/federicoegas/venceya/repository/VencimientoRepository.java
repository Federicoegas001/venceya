package ar.federicoegas.venceya.repository;

import ar.federicoegas.venceya.domain.TipoVencimiento;
import ar.federicoegas.venceya.domain.Vencimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VencimientoRepository extends JpaRepository<Vencimiento, Long> {

    List<Vencimiento> findAllByOrderByFechaVencimientoAsc();

    List<Vencimiento> findByTipoOrderByFechaVencimientoAsc(TipoVencimiento tipo);

    /** Candidatos a aviso: todo lo que vence dentro de los próximos 30 días o ya venció. */
    List<Vencimiento> findByFechaVencimientoLessThanEqual(LocalDate limite);
}
