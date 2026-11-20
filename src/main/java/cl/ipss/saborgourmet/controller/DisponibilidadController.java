package cl.ipss.saborgourmet.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.service.ReservaService;

/**
 * Controlador REST para consultar la disponibilidad de mesas.
 * Está separado de MesaController porque cruza dos recursos:
 * devuelve mesas, pero depende de las reservas para saber si están ocupadas.
 * Comparte el prefijo /api/mesas con MesaController sin conflicto:
 * Spring prioriza la ruta literal "/disponibles" sobre "/{id}".
 */
@RestController
@RequestMapping("/api/mesas")
public class DisponibilidadController {
    private final ReservaService reservaService;

    // Spring entrega el ReservaService automáticamente (inyección por constructor)
    public DisponibilidadController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * GET /api/mesas/disponibles?fecha=2026-12-05&hora=20:00&personas=4
     * Devuelve las mesas activas, con capacidad suficiente y sin reservas activas
     * que se crucen en ese horario. Responde 200 (lista vacía si no hay ninguna).
     * Responde 400 si falta un parámetro, si la fecha u hora tienen mal formato
     * o si personas no es mayor a 0.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<Mesa>> mesasDisponibles(
            // Fecha en formato AAAA-MM-DD (ej: 2026-12-05)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            // Hora en formato HH:mm (ej: 20:00)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            // Cantidad de personas que asistirán
            @RequestParam Integer personas) {
        return ResponseEntity.ok(reservaService.mesasDisponibles(fecha, hora, personas));
    }
}
