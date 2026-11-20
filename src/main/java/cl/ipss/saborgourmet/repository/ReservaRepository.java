package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Datos;
import cl.ipss.saborgourmet.model.Reserva;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio de reservas: lee y guarda reservas en datos.json usando AlmacenJson.
 * No contiene reglas de negocio (eso va en el Service), solo acceso a datos.
 * Las reservas canceladas NO se eliminan: el Service les cambia el estado y las guarda con save().
 */
@Repository
public class ReservaRepository {

    private final AlmacenJson almacen;

    // Spring entrega el AlmacenJson automáticamente (inyección por constructor)
    public ReservaRepository(AlmacenJson almacen) {
        this.almacen = almacen;
    }

    /** Devuelve todas las reservas guardadas (activas y canceladas). */
    public List<Reserva> findAll() {
        return almacen.leer().getReservas();
    }

    /** Busca una reserva por id. Devuelve Optional vacío si no existe. */
    public Optional<Reserva> findById(Long id) {
        return findAll().stream()
                .filter(r -> Objects.equals(r.getId(), id))
                .findFirst();
    }

    /**
     * Guarda una reserva.
     * - Si no tiene id: es nueva, se le asigna el siguiente id y se agrega.
     * - Si tiene id: reemplaza a la reserva existente con ese id (modificar o cancelar).
     */
    public Reserva save(Reserva reserva) {
        Datos datos = almacen.leer();
        List<Reserva> reservas = datos.getReservas();

        if (reserva.getId() == null) {
            reserva.setId(siguienteId(reservas));
            reservas.add(reserva);
        } else {
            // Recorre la lista y reemplaza la que tenga el mismo id
            for (int i = 0; i < reservas.size(); i++) {
                if (Objects.equals(reservas.get(i).getId(), reserva.getId())) {
                    reservas.set(i, reserva);
                    break;
                }
            }
        }

        almacen.guardar(datos);
        return reserva;
    }

    /** Elimina una reserva por id. Devuelve true si existía y se eliminó. */
    public boolean deleteById(Long id) {
        Datos datos = almacen.leer();
        boolean eliminada = datos.getReservas().removeIf(r -> Objects.equals(r.getId(), id));
        if (eliminada) {
            almacen.guardar(datos);
        }
        return eliminada;
    }

    /** Calcula el próximo id: el mayor id actual + 1 (o 1 si la lista está vacía). */
    private Long siguienteId(List<Reserva> reservas) {
        return reservas.stream()
                .mapToLong(Reserva::getId)
                .max()
                .orElse(0) + 1;
    }
}