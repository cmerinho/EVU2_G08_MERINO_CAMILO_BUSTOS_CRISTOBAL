package cl.ipss.saborgourmet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.DatosInvalidosException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;

/**
 * Servicio de reservas: concentra las reglas de negocio de las reservas.
 * El Controller lo llama para crear, listar, buscar, modificar y cancelar
 * reservas,
 * y para consultar las mesas disponibles.
 * No lee ni escribe el archivo directamente: para eso usa los repositorios.
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final ClienteRepository clienteRepository;
    // Duración fija de cada reserva. Se usa para calcular el fin de una reserva
    // (inicio + 120 min) y así detectar choques de horario en la misma mesa.
    // Ej: una reserva a las 20:00 ocupa la mesa hasta las 22:00.
    private static final int DURACION_MINUTOS = 120;

    // Spring entrega los tres repositorios automáticamente (inyección por
    // constructor).
    // Se usan los repositorios de cliente y mesa solo para consultar si existen.
    public ReservaService(ReservaRepository reservaRepository, MesaRepository mesaRepository,
            ClienteRepository clienteRepository) {
        this.reservaRepository = reservaRepository;
        this.mesaRepository = mesaRepository;
        this.clienteRepository = clienteRepository;
    }

    /** Devuelve todas las reservas guardadas (activas y canceladas). */
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    /**
     * Busca una reserva por id.
     * Si no existe, lanza RecursoNoEncontradoException y la API responde 404.
     */
    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("La reserva con id " + id + " no existe en los registros."));
    }

    /**
     * Crea una nueva reserva después de validarla.
     * synchronized: si llegan dos reservas al mismo tiempo para la misma mesa,
     * se atiende una a la vez, así ambas no pasan la validación de choque juntas.
     */
    public synchronized Reserva crear(Reserva reserva) {
        // Se ignoran el id y el estado que vengan desde Postman/llamado API:
        // el id lo asigna el repositorio y toda reserva nueva parte ACTIVA.
        reserva.setId(null);
        reserva.setEstado(EstadoReserva.ACTIVA);

        // Como es una nueva reserva, no necesitamos excluir un Id
        validarReserva(reserva, null);

        return reservaRepository.save(reserva);
    }

    /**
     * Modifica una reserva existente con los datos nuevos que llegan en el body.
     * - reservaActual: la reserva tal como está guardada en datos.json.
     * - datosNuevos: lo que el usuario quiere que pase a ser la reserva.
     * El id se toma de la URL y el estado se conserva (el PUT no cancela).
     */
    public synchronized Reserva modificar(Long id, Reserva datosNuevos) {
        // Si la reserva no existe, buscarPorId responde 404
        Reserva reservaActual = buscarPorId(id);

        // Una reserva cancelada ya no se puede modificar (409)
        if (reservaActual.getEstado() == EstadoReserva.CANCELADA) {
            throw new ConflictoException("La reserva con id " + id + " está cancelada y no se puede modificar.");
        }
        // Como va a modificar una reserva, aqui si necesitamos excluir el id existente
        // de la reserva para que no choque consigo misma
        validarReserva(datosNuevos, id);

        // Mismo id: save() reemplaza la reserva existente en vez de crear una nueva
        datosNuevos.setId(id);
        datosNuevos.setEstado(reservaActual.getEstado());
        return reservaRepository.save(datosNuevos);
    }

    /**
     * Cancela una reserva: cambia su estado a CANCELADA y la guarda.
     * NO se elimina del archivo, se conserva como historial.
     * Al quedar CANCELADA deja de contar en los choques, así que la mesa queda
     * libre.
     */
    public synchronized Reserva cancelar(Long id) {
        // Si la reserva no existe, buscarPorId responde 404
        Reserva reservaACancelar = buscarPorId(id);
        // No se puede cancelar dos veces la misma reserva (409)
        if (reservaACancelar.getEstado() == EstadoReserva.CANCELADA) {
            throw new ConflictoException("La reserva con id " + id + " ya se encontraba cancelada.");
        }
        reservaACancelar.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reservaACancelar);
    }

    /**
     * Devuelve las mesas que se pueden reservar en una fecha y hora para cierta
     * cantidad de personas.
     * Una mesa está disponible si:
     * - está activa,
     * - su capacidad alcanza para las personas,
     * - no tiene otra reserva ACTIVA que se cruce en ese horario.
     * Si ninguna cumple, devuelve una lista vacía.
     */
    public List<Mesa> mesasDisponibles(LocalDate fecha, LocalTime hora, Integer personas) {

        // Se valida antes de leer el archivo: si el dato es inválido no tiene sentido
        // buscar (400)
        if (personas == null || personas <= 0) {
            throw new DatosInvalidosException("La cantidad de personas para reservar una mesa deben ser mayor a 0.");
        }
        List<Mesa> mesasTotales = mesaRepository.findAll();
        List<Mesa> mesasDisponibles = new ArrayList<>();

        // Recorre todas las mesas y agrega solo las que cumplen las tres condiciones
        for (Mesa m : mesasTotales) {
            if (m.isActiva() && m.getCapacidad() >= personas && !existeChoqueHorario(m.getId(), fecha, hora, null)) {
                mesasDisponibles.add(m);
            }
        }
        return mesasDisponibles;
    }

    // ---------- Métodos auxiliares ----------
    /**
     * Revisa si una mesa ya tiene una reserva ACTIVA que se cruce con el horario
     * indicado.
     * - idExcluir: id de una reserva que no se debe comparar (la que se está
     * modificando).
     * Al crear o consultar disponibilidad se pasa null.
     * Devuelve true si hay choque.
     */
    private boolean existeChoqueHorario(Long mesaId, LocalDate fecha, LocalTime hora, Long idExcluir) {
        // Se usa LocalDateTime (fecha + hora) para que las reservas que pasan
        // la medianoche (ej: 23:00 a 01:00) se comparen bien
        LocalDateTime inicioNuevaReserva = LocalDateTime.of(fecha, hora);
        LocalDateTime finNuevaReserva = inicioNuevaReserva.plusMinutes(DURACION_MINUTOS);

        List<Reserva> reservas = reservaRepository.findAll();

        for (Reserva r : reservas) {
            // Aplicamos guard clauses: se saltan las reservas que no pueden generar choque,
            // así la comparación de horario solo se aplica a las que importan.

            // Una reserva de otra mesa no ocupa esta mesa
            if (!Objects.equals(r.getMesaId(), mesaId)) {
                continue;
            }

            // Una reserva cancelada ya no ocupa la mesa
            if (r.getEstado() != EstadoReserva.ACTIVA) {
                continue;
            }

            // La reserva que se está modificando no se compara consigo misma
            if (Objects.equals(r.getId(), idExcluir)) {
                continue;
            }
            LocalDateTime inicioReservaExistente = LocalDateTime.of(r.getFecha(), r.getHora());
            LocalDateTime finReservaExistente = inicioReservaExistente.plusMinutes(DURACION_MINUTOS);

            // Dos reservas chocan si cada una empieza antes de que la otra termine.
            // Esto cubre cualquier cruce: parcial, mismo horario o una dentro de la otra.
            // Si una termina justo cuando empieza la otra (ej: 20:00-22:00 y 22:00-00:00),
            // no hay choque.
            if (inicioNuevaReserva.isBefore(finReservaExistente) && inicioReservaExistente.isBefore(finNuevaReserva)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reglas de negocio comunes al crear y al modificar una reserva.
     * Si alguna no se cumple, lanza la excepción correspondiente y la API responde
     * sola:
     * - mesa o cliente inexistente: 404
     * - mesa inactiva o capacidad superada: 400
     * - choque de horario con otra reserva activa: 409
     * idExcluir: null al crear; el id de la reserva al modificar.
     */
    private void validarReserva(Reserva reserva, Long idExcluir) {
        // Se guarda la mesa porque después se necesitan sus datos (activa y capacidad)
        Mesa mesaConsultada = mesaRepository.findById(reserva.getMesaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La mesa con id " + reserva.getMesaId() + " no existe en los registros"));

        // Del cliente solo importa que exista, por eso no se guarda en una variable
        clienteRepository.findById(reserva.getClienteId()).orElseThrow(() -> new RecursoNoEncontradoException(
                "El cliente con id " + reserva.getClienteId() + " no existe en los registros"));

        // La mesa debe estar activa (ej: una mesa en reparación no se puede reservar)
        if (!mesaConsultada.isActiva()) {
            throw new DatosInvalidosException(
                    "La mesa con id " + mesaConsultada.getId() + " no está disponible para reservas.");
        }
        // La cantidad de personas no puede superar la capacidad de la mesa
        if (reserva.getCantidadPersonas() > mesaConsultada.getCapacidad()) {
            throw new DatosInvalidosException("La mesa con id " + mesaConsultada.getId() + " admite un máximo de "
                    + mesaConsultada.getCapacidad() + " personas.");
        }

        // La mesa no puede tener otra reserva activa que se cruce en ese horario
        if (existeChoqueHorario(reserva.getMesaId(), reserva.getFecha(), reserva.getHora(), idExcluir)) {
            throw new ConflictoException("La mesa solicitada ya tiene una reserva en ese horario.");
        }
    }
}
