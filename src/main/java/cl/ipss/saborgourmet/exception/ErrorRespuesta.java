package cl.ipss.saborgourmet.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato JSON que tienen todas las respuestas de error de la API.
 *
 * Es un "record": una clase corta de Java que solo guarda datos.
 * Java crea automáticamente el constructor y los métodos para leer cada campo.
 *
 * Ejemplo de respuesta:
 * {
 *   "fecha": "2026-11-19T21:50:00",
 *   "estado": 404,
 *   "error": "Not Found",
 *   "mensaje": "No existe una mesa con id 99",
 *   "ruta": "/api/mesas/99",
 *   "detalles": {}
 * }
 */
public record ErrorRespuesta(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles // errores por campo (ej: "nombre": "El nombre es obligatorio")
) {
}