package cl.ipss.saborgourmet.exception;

/**
 * Se lanza cuando se busca un registro que no existe (ej: cliente o mesa con id inexistente).
 * El ManejadorGlobalErrores la convierte en una respuesta 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}