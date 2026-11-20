package cl.ipss.saborgourmet.model;

/**
 * Estados posibles de una reserva.
 * ACTIVA: ocupa la mesa en su horario.
 * CANCELADA: ya no ocupa la mesa, pero se conserva como historial.
 */

public enum EstadoReserva {
    ACTIVA,
    CANCELADA
}
