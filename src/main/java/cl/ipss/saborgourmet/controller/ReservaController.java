package cl.ipss.saborgourmet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.service.ReservaService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controlador REST de reservas: recibe las solicitudes HTTP en /api/reservas.
 * No contiene reglas de negocio: solo recibe la petición, llama a
 * ReservaService
 * y devuelve la respuesta envuelta en ResponseEntity (JSON + código HTTP).
 * Los errores (400, 404, 409) los lanza el Service y los responde
 * ManejadorGlobalErrores.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservaService;

    // Spring entrega el ReservaService automáticamente (inyección por constructor)
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * GET /api/reservas
     * Devuelve todas las reservas (activas y canceladas). Responde 200.
     */
    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodasLasReservas() {
        return ResponseEntity.ok(reservaService.listar());
    }

    /**
     * GET /api/reservas/{id}
     * Devuelve una reserva por su id. Responde 200, o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    /**
     * POST /api/reservas
     * Crea una reserva nueva con los datos del body. Responde 201 con la reserva
     * creada.
     * 
     * @Valid revisa los campos obligatorios y positivos del modelo (400 si fallan).
     *        El Service valida cliente y mesa (404), mesa activa y capacidad (400)
     *        y choque de horario (409).
     */
    @PostMapping
    public ResponseEntity<Reserva> nuevaReserva(@Valid @RequestBody Reserva reserva) {
        Reserva reservaCreada = reservaService.crear(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaCreada);
    }

    /**
     * PUT /api/reservas/{id}
     * Modifica una reserva existente. El id se toma de la URL, no del body.
     * Responde 200 con la reserva actualizada; 404 si no existe; 409 si está
     * cancelada
     * o si choca con otra reserva; 400 si los datos no son válidos.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(@PathVariable Long id, @Valid @RequestBody Reserva reserva) {
        Reserva reservaActualizada = reservaService.modificar(id, reserva);
        return ResponseEntity.ok(reservaActualizada);
    }

    /**
     * PATCH /api/reservas/{id}/cancelar
     * Cancela una reserva: cambia su estado a CANCELADA, NO la elimina.
     * Se usa PATCH porque solo se modifica una parte del recurso (el estado).
     * Responde 200 con la reserva cancelada; 404 si no existe; 409 si ya estaba
     * cancelada.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelarReserva(@PathVariable Long id) {
        Reserva reservaCancelada = reservaService.cancelar(id);
        return ResponseEntity.ok(reservaCancelada);
    }
}
