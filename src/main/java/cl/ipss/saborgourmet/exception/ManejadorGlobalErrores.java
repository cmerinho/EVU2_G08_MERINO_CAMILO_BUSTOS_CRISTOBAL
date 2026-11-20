package cl.ipss.saborgourmet.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Atrapa los errores de TODOS los controladores y los convierte en respuestas JSON
 * con el código HTTP correcto (400, 404, 409, 500), sin mostrar trazas internas.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    // Registra los errores inesperados en la consola (para el equipo), no en la respuesta
    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    // ---------- Nuestras excepciones ----------

    /** 404: el registro buscado no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                              HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    /** 400: los datos no cumplen una regla de negocio. */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuesta> manejarDatosInvalidos(DatosInvalidosException ex,
                                                                HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }

    /** 409: la operación choca con datos existentes. */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflicto(ConflictoException ex,
                                                           HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    /** 500: no se pudo leer o guardar datos.json. */
    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErrorRespuesta> manejarPersistencia(PersistenciaException ex,
                                                              HttpServletRequest request) {
        log.error("Error de persistencia", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, Map.of());
    }

    // ---------- Errores que lanza Spring automáticamente ----------

    /** 400: falló una validación de los modelos (@NotBlank, @Positive, etc.). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        // Junta cada campo con su mensaje de error
        Map<String, String> detalles = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> detalles.put(error.getField(), error.getDefaultMessage()));

        return construir(HttpStatus.BAD_REQUEST, "Hay campos con datos inválidos", request, detalles);
    }

    /** 400: el JSON está mal escrito o un dato tiene el formato incorrecto (ej: fecha). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON válido o tiene datos con formato incorrecto "
                        + "(fecha: AAAA-MM-DD, hora: HH:mm)",
                request, Map.of());
    }

    /** 400: un parámetro de la URL tiene el tipo incorrecto (ej: /api/mesas/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", request, Map.of());
    }

    /** 400: falta un parámetro obligatorio en la URL (ej: ?fecha=...). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request, Map.of());
    }

    /** 404: la ruta no existe en la API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaInexistente(NoResourceFoundException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request, Map.of());
    }

    /** 405: la ruta existe pero no acepta ese método HTTP (ej: DELETE donde solo hay GET). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "El método " + ex.getMethod() + " no está permitido en esta ruta", request, Map.of());
    }

    /** 500: cualquier error inesperado. Se registra en consola, pero al cliente no se le muestran detalles. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intente nuevamente.", request, Map.of());
    }

    // ---------- Método auxiliar ----------

    /** Arma la respuesta de error con el formato de ErrorRespuesta (evita repetir código). */
    private ResponseEntity<ErrorRespuesta> construir(HttpStatus estado, String mensaje,
                                                     HttpServletRequest request,
                                                     Map<String, String> detalles) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(estado).body(cuerpo);
    }
}