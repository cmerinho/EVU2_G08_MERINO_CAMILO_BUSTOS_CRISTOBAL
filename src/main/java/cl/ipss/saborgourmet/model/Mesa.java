package cl.ipss.saborgourmet.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Representa una mesa del restaurante.
 * El personal puede cambiar su configuración o desactivarla.
 */

public class Mesa {

    // Identificador único. Lo asigna el sistema, no el usuario.
    private Long id;

    // Número visible de la mesa en el salón (ej: mesa 4).
    @NotNull(message = "El número de mesa es obligatorio")
    @Positive(message = "El número de mesa debe ser mayor a 0")
    private Integer numero;

    // Cantidad de personas que se pueden sentar en una mesa
    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    // Sector donde está ubicada la mesa
    private String ubicacion;

    // Si es false, la mesa no se puede reservar (ej: mesa en reparación).
    // Toda mesa nueva parte activa.
    private boolean activa = true;

    public Mesa() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}
