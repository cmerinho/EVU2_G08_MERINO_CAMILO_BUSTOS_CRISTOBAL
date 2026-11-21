package cl.ipss.saborgourmet.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;

/**
 * Capa de servicio de clientes.
 * Contiene las reglas de negocio; no conoce HTTP ni el archivo JSON.
 * Las validaciones de formato (campos obligatorios, email) las hace @Valid en el controller.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;

    // Inyección por constructor: Spring entrega las instancias de los repositorios.
    public ClienteService(ClienteRepository clienteRepository,
                          ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
    }

    /** Devuelve todos los clientes registrados. */
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    /**
     * Busca un cliente por id.
     * @throws RecursoNoEncontradoException (404) si no existe.
     * También lo usa ReservaService para validar que el cliente exista.
     */
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente con id " + id));
    }

    /** Crea un cliente nuevo. El id lo asigna el repositorio (max + 1). */
    public Cliente crear(Cliente cliente) {
        cliente.setId(null); // se ignora cualquier id enviado en el body
        return clienteRepository.save(cliente);
    }

    /**
     * Reemplaza los datos de un cliente existente.
     * @throws RecursoNoEncontradoException (404) si no existe.
     */
    public Cliente actualizar(Long id, Cliente datos) {
        buscarPorId(id);      // valida que exista
        datos.setId(id);      // el id de la URL manda sobre el del body
        return clienteRepository.save(datos);
    }

    /**
     * Elimina un cliente.
     * @throws RecursoNoEncontradoException (404) si no existe.
     * @throws ConflictoException (409) si tiene reservas asociadas.
     */
    public void eliminar(Long id) {
        buscarPorId(id);

        // Se consideran todas las reservas (activas y canceladas):
        // si se borra el cliente, esas reservas quedarían apuntando a un id inexistente.
        boolean tieneReservas = reservaRepository.findAll().stream()
                .anyMatch(r -> Objects.equals(r.getClienteId(), id));

        if (tieneReservas) {
            throw new ConflictoException(
                    "No se puede eliminar el cliente " + id + " porque tiene reservas asociadas");
        }

        clienteRepository.deleteById(id);
    }
}