package ar.federicoegas.venceya.web;

import ar.federicoegas.venceya.domain.TipoVencimiento;
import ar.federicoegas.venceya.job.AlertaVencimientoJob;
import ar.federicoegas.venceya.service.VencimientoService;
import ar.federicoegas.venceya.web.VencimientoDtos.ResumenResponse;
import ar.federicoegas.venceya.web.VencimientoDtos.VencimientoRequest;
import ar.federicoegas.venceya.web.VencimientoDtos.VencimientoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vencimientos")
public class VencimientoController {

    private final VencimientoService service;
    private final AlertaVencimientoJob job;

    public VencimientoController(VencimientoService service, AlertaVencimientoJob job) {
        this.service = service;
        this.job = job;
    }

    @GetMapping
    public List<VencimientoResponse> listar(@RequestParam(required = false) TipoVencimiento tipo) {
        var hoy = service.hoy();
        return service.listar(tipo).stream().map(v -> VencimientoResponse.de(v, hoy)).toList();
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return service.resumen();
    }

    @GetMapping("/{id}")
    public VencimientoResponse obtener(@PathVariable Long id) {
        return VencimientoResponse.de(service.obtener(id), service.hoy());
    }

    @PostMapping
    public ResponseEntity<VencimientoResponse> crear(@Valid @RequestBody VencimientoRequest req) {
        var v = service.crear(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(v.getId()).toUri();
        return ResponseEntity.created(uri).body(VencimientoResponse.de(v, service.hoy()));
    }

    @PutMapping("/{id}")
    public VencimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody VencimientoRequest req) {
        return VencimientoResponse.de(service.actualizar(id, req), service.hoy());
    }

    @PatchMapping("/{id}/renovar")
    public VencimientoResponse renovar(@PathVariable Long id) {
        return VencimientoResponse.de(service.renovar(id), service.hoy());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    /** Dispara el job a mano (útil para probar sin esperar a las 8:00). */
    @PostMapping("/alertas/ejecutar")
    public Map<String, Integer> ejecutarAlertas() {
        return Map.of("enviados", job.ejecutar());
    }
}
