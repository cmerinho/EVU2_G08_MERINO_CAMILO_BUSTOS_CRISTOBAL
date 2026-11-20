package cl.ipss.saborgourmet.exception;

/**
 * Se lanza cuando los datos recibidos no cumplen una regla de negocio
 * (ej: más personas que la capacidad de la mesa, fecha en el pasado).
 * El ManejadorGlobalErrores la convierte en una respuesta 400 Bad Request.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}