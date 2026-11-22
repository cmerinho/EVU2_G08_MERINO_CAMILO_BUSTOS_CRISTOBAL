package cl.ipss.saborgourmet.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;

/**
 * Servicio de mesas: concentra las reglas de negocio de las mesas.
 * No lee ni escribe el archivo directamente: para eso usa los repositorios.
 * Las validaciones de formato (número y capacidad obligatorios y positivos)
 * las hace @Valid en el controller.
 */
@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ReservaRepository reservaRepository;

    // Spring entrega los repositorios automáticamente (inyección por constructor).
    // ReservaRepository se usa solo para revisar si la mesa tiene reservas antes de borrarla.
    public MesaService(MesaRepository mesaRepository, ReservaRepository reservaRepository) {
        this.mesaRepository = mesaRepository;
        this.reservaRepository = reservaRepository;
    }

    /** Devuelve todas las mesas (activas e inactivas). */
    public List<Mesa> listar() {
        return mesaRepository.findAll();
    }

    /**
     * Busca una mesa por id.
     * Si no existe, lanza RecursoNoEncontradoException y la API responde 404.
     */
    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("La mesa con id " + id + " no existe en los registros."));
    }

    /**
     * Crea una mesa nueva.
     * El id lo asigna el repositorio. Si no se envía "activa", queda en true por defecto.
     * Responde 409 si ya existe otra mesa con el mismo número.
     */
    public Mesa crear(Mesa mesa) {
        // Se ignora el id que venga en el body: el repositorio asigna el siguiente
        mesa.setId(null);
        // Como es una mesa nueva, no hay id que excluir
        validarNumeroUnico(mesa.getNumero(), null);
        return mesaRepository.save(mesa);
    }

    /**
     * Reemplaza los datos de una mesa existente (PUT: se envía la mesa completa).
     * El id se toma de la URL, no del body.
     * Responde 404 si no existe y 409 si el número nuevo ya lo usa otra mesa.
     */
    public Mesa actualizar(Long id, Mesa datosNuevos) {
        // Primero se valida que exista (404) y después el número (409)
        buscarPorId(id);
        // Se excluye la propia mesa: mantener su mismo número no es un conflicto
        validarNumeroUnico(datosNuevos.getNumero(), id);
        datosNuevos.setId(id);
        return mesaRepository.save(datosNuevos);
    }

    /**
     * Elimina una mesa.
     * Responde 404 si no existe y 409 si tiene reservas asociadas.
     * Se cuentan también las canceladas, porque siguen guardando el mesaId
     * y quedarían apuntando a una mesa inexistente.
     */
    public void eliminar(Long id) {
        buscarPorId(id);

        boolean tieneReservas = reservaRepository.findAll().stream()
                .anyMatch(r -> Objects.equals(r.getMesaId(), id));

        if (tieneReservas) {
            throw new ConflictoException(
                    "La mesa con id " + id + " tiene reservas asociadas y no se puede eliminar.");
        }
        mesaRepository.deleteById(id);
    }

    // ---------- Métodos auxiliares ----------

    /**
     * Revisa que ningún otra mesa use el mismo número.
     * - idExcluir: null al crear; el id de la mesa al actualizar,
     *   para que no choque consigo misma.
     */
    private void validarNumeroUnico(Integer numero, Long idExcluir) {
        boolean repetido = mesaRepository.findAll().stream()
                .anyMatch(m -> Objects.equals(m.getNumero(), numero)
                        && !Objects.equals(m.getId(), idExcluir));

        if (repetido) {
            throw new ConflictoException("Ya existe una mesa con el número " + numero + ".");
        }
    }
}