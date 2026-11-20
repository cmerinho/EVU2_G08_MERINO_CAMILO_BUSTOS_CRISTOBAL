package cl.ipss.saborgourmet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el contenido completo del archivo datos.json.
 *
 * Tiene la misma forma que el archivo: tres listas (clientes, mesas y reservas).
 * Jackson convierte el archivo en un objeto Datos al leerlo,
 * y convierte este objeto en JSON al guardarlo.
 */
public class Datos {

    // Lista de todos los clientes registrados
    private List<Cliente> clientes = new ArrayList<>();

    // Lista de todas las mesas del restaurante
    private List<Mesa> mesas = new ArrayList<>();

    // Lista de todas las reservas (activas y canceladas)
    private List<Reserva> reservas = new ArrayList<>();

    // Constructor vacío: Jackson lo necesita para crear el objeto al leer el archivo.
    public Datos() {
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    // Si el archivo trae la lista en null, se reemplaza por una lista vacía
    // para evitar errores al recorrerla o agregarle elementos.
    public void setClientes(List<Cliente> clientes) {
        this.clientes = (clientes != null) ? clientes : new ArrayList<>();
    }

    public List<Mesa> getMesas() {
        return mesas;
    }

    public void setMesas(List<Mesa> mesas) {
        this.mesas = (mesas != null) ? mesas : new ArrayList<>();
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = (reservas != null) ? reservas : new ArrayList<>();
    }
}