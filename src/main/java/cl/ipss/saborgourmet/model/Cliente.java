package cl.ipss.saborgourmet.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa a un cliente del restaurante (la persona que reserva).
 * Guarda su identificación (RUT, nombre) y su contacto (correo, teléfono).
 */
public class Cliente {

    // Identificador único. Lo asigna el sistema, no el usuario.
    private Long id;

    // Las anotaciones validan los datos cuando llegan desde Postman.
    // Si no se cumplen, la API responde 400 (datos inválidos).
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // Constructor vacío: Jackson lo necesita para convertir JSON en objeto.
    public Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}

