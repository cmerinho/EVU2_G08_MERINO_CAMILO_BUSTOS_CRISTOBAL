package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Datos;
import cl.ipss.saborgourmet.model.Mesa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio de mesas: lee y guarda mesas en datos.json usando AlmacenJson.
 * No contiene reglas de negocio (eso va en el Service), solo acceso a datos.
 */
@Repository
public class MesaRepository {

    private final AlmacenJson almacen;

    // Spring entrega el AlmacenJson automáticamente (inyección por constructor)
    public MesaRepository(AlmacenJson almacen) {
        this.almacen = almacen;
    }

    /** Devuelve todas las mesas guardadas. */
    public List<Mesa> findAll() {
        return almacen.leer().getMesas();
    }

    /** Busca una mesa por id. Devuelve Optional vacío si no existe. */
    public Optional<Mesa> findById(Long id) {
        return findAll().stream()
                .filter(m -> Objects.equals(m.getId(), id))
                .findFirst();
    }

    /**
     * Guarda una mesa.
     * - Si no tiene id: es nueva, se le asigna el siguiente id y se agrega.
     * - Si tiene id: reemplaza a la mesa existente con ese id.
     */
    public Mesa save(Mesa mesa) {
        Datos datos = almacen.leer();
        List<Mesa> mesas = datos.getMesas();

        if (mesa.getId() == null) {
            mesa.setId(siguienteId(mesas));
            mesas.add(mesa);
        } else {
            // Recorre la lista y reemplaza la que tenga el mismo id
            for (int i = 0; i < mesas.size(); i++) {
                if (Objects.equals(mesas.get(i).getId(), mesa.getId())) {
                    mesas.set(i, mesa);
                    break;
                }
            }
        }

        almacen.guardar(datos);
        return mesa;
    }

    /** Elimina una mesa por id. Devuelve true si existía y se eliminó. */
    public boolean deleteById(Long id) {
        Datos datos = almacen.leer();
        boolean eliminada = datos.getMesas().removeIf(m -> Objects.equals(m.getId(), id));
        if (eliminada) {
            almacen.guardar(datos);
        }
        return eliminada;
    }

    /** Calcula el próximo id: el mayor id actual + 1 (o 1 si la lista está vacía). */
    private Long siguienteId(List<Mesa> mesas) {
        return mesas.stream()
                .mapToLong(Mesa::getId)
                .max()
                .orElse(0) + 1;
    }
}