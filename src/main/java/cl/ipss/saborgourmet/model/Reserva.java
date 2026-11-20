package cl.ipss.saborgourmet.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una reserva de mesa.
 * Se relaciona con Cliente y Mesa guardando solo sus ids (clienteId, mesaId).
 */
public class Reserva {

    // Identificador único. Lo asigna el sistema, no el usuario.
    private Long id;

    // Id del cliente que hace la reserva (relación con Cliente).
    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    // Id de la mesa reservada (relación con Mesa).
    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;

    // Día de la reserva. En JSON se escribe así: "2026-12-05"
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    // Hora de inicio. En JSON se escribe así: "20:00"
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    // Cuántas personas asistirán. No puede superar la capacidad de la mesa.
    @NotNull(message = "La cantidad de personas es obligatoria")
    @Positive(message = "La cantidad de personas debe ser mayor a 0")
    private Integer cantidadPersonas;

    // Toda reserva nueva parte ACTIVA. Al cancelarla pasa a CANCELADA.
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    // Constructor vacío: Jackson lo necesita para convertir JSON en objeto.
    public Reserva() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
}