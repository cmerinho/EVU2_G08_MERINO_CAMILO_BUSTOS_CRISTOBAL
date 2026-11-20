package cl.ipss.saborgourmet.exception;

/**
 * Se lanza cuando la operación choca con el estado actual de los datos
 * (ej: mesa ya reservada en ese horario, número de mesa repetido,
 * eliminar un cliente que tiene reservas).
 * El ManejadorGlobalErrores la convierte en una respuesta 409 Conflict.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}