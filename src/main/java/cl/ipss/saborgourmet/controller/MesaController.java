package cl.ipss.saborgourmet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.service.MesaService;
import jakarta.validation.Valid;

/**
 * Controlador REST de mesas: recibe las solicitudes HTTP en /api/mesas.
 * No contiene reglas de negocio: llama a MesaService y devuelve la respuesta
 * envuelta en ResponseEntity (JSON + código HTTP).
 * La consulta de disponibilidad (/api/mesas/disponibles) está en
 * DisponibilidadController, porque depende de las reservas.
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    // Spring entrega el MesaService automáticamente (inyección por constructor)
    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /**
     * GET /api/mesas
     * Devuelve todas las mesas (activas e inactivas). Responde 200.
     */
    @GetMapping
    public ResponseEntity<List<Mesa>> listarMesas() {
        return ResponseEntity.ok(mesaService.listar());
    }

    /**
     * GET /api/mesas/{id}
     * Devuelve una mesa por su id. Responde 200, o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Mesa> buscarMesa(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    /**
     * POST /api/mesas
     * Crea una mesa. Responde 201 con la mesa creada.
     * @Valid revisa que número y capacidad existan y sean positivos (400 si fallan).
     * El Service responde 409 si el número de mesa ya existe.
     */
    @PostMapping
    public ResponseEntity<Mesa> crearMesa(@Valid @RequestBody Mesa mesa) {
        Mesa mesaCreada = mesaService.crear(mesa);
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaCreada);
    }

    /**
     * PUT /api/mesas/{id}
     * Reemplaza los datos de una mesa (también sirve para activarla o desactivarla).
     * Responde 200; 404 si no existe; 409 si el número lo usa otra mesa; 400 si los datos no son válidos.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Mesa> actualizarMesa(@PathVariable Long id,
                                               @Valid @RequestBody Mesa mesa) {
        return ResponseEntity.ok(mesaService.actualizar(id, mesa));
    }

    /**
     * DELETE /api/mesas/{id}
     * Elimina una mesa. Responde 204 (sin cuerpo); 404 si no existe;
     * 409 si tiene reservas asociadas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}