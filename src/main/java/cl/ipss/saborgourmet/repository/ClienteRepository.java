package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.model.Datos;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio de clientes: lee y guarda clientes en datos.json usando AlmacenJson.
 * No contiene reglas de negocio (eso va en el Service), solo acceso a datos.
 */
@Repository
public class ClienteRepository {

    private final AlmacenJson almacen;

    // Spring entrega el AlmacenJson automáticamente (inyección por constructor)
    public ClienteRepository(AlmacenJson almacen) {
        this.almacen = almacen;
    }

    /** Devuelve todos los clientes guardados. */
    public List<Cliente> findAll() {
        return almacen.leer().getClientes();
    }

    /** Busca un cliente por id. Devuelve Optional vacío si no existe. */
    public Optional<Cliente> findById(Long id) {
        return findAll().stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst();
    }

    /**
     * Guarda un cliente.
     * - Si no tiene id: es nuevo, se le asigna el siguiente id y se agrega.
     * - Si tiene id: reemplaza al cliente existente con ese id.
     */
    public Cliente save(Cliente cliente) {
        Datos datos = almacen.leer();
        List<Cliente> clientes = datos.getClientes();

        if (cliente.getId() == null) {
            cliente.setId(siguienteId(clientes));
            clientes.add(cliente);
        } else {
            // Recorre la lista y reemplaza el que tenga el mismo id
            for (int i = 0; i < clientes.size(); i++) {
                if (Objects.equals(clientes.get(i).getId(), cliente.getId())) {
                    clientes.set(i, cliente);
                    break;
                }
            }
        }

        almacen.guardar(datos);
        return cliente;
    }

    /** Elimina un cliente por id. Devuelve true si existía y se eliminó. */
    public boolean deleteById(Long id) {
        Datos datos = almacen.leer();
        boolean eliminado = datos.getClientes().removeIf(c -> Objects.equals(c.getId(), id));
        if (eliminado) {
            almacen.guardar(datos);
        }
        return eliminado;
    }

    /** Calcula el próximo id: el mayor id actual + 1 (o 1 si la lista está vacía). */
    private Long siguienteId(List<Cliente> clientes) {
        return clientes.stream()
                .mapToLong(Cliente::getId)
                .max()
                .orElse(0) + 1;
    }
}