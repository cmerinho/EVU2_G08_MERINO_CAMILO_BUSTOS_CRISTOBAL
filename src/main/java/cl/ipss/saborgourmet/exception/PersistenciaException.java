package cl.ipss.saborgourmet.exception;

/**
 * Error al leer o escribir el archivo datos.json.
 * Extiende RuntimeException para no tener que declarar "throws" en cada método.
 */
public class PersistenciaException extends RuntimeException {

    // mensaje: explicación para el usuario. causa: el error original de Java.
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}