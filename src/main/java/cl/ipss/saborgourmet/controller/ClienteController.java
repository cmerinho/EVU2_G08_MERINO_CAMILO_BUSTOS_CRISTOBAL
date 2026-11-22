package cl.ipss.saborgourmet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.service.ClienteService;
import jakarta.validation.Valid;

/**
 * Controlador REST de clientes: recibe las solicitudes HTTP en /api/clientes.
 * No contiene reglas de negocio: llama a ClienteService y devuelve la respuesta
 * envuelta en ResponseEntity (JSON + código HTTP).
 * Los errores (400, 404, 409) los lanza el Service o @Valid,
 * y los responde ManejadorGlobalErrores.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    // Spring entrega el ClienteService automáticamente (inyección por constructor)
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * GET /api/clientes
     * Devuelve todos los clientes. Responde 200 (lista vacía si no hay).
     */
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listar());
    }

    /**
     * GET /api/clientes/{id}
     * Devuelve un cliente por su id. Responde 200, o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    /**
     * POST /api/clientes
     * Crea un cliente con los datos del body. Responde 201 con el cliente creado.
     * @Valid revisa campos obligatorios y formato de email (400 si fallan).
     */
    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody Cliente cliente) {
        Cliente clienteCreado = clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
    }

    /**
     * PUT /api/clientes/{id}
     * Reemplaza los datos de un cliente. El id se toma de la URL, no del body.
     * Responde 200 con el cliente actualizado; 404 si no existe; 400 si los datos no son válidos.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id,
                                                     @Valid @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.actualizar(id, cliente));
    }

    /**
     * DELETE /api/clientes/{id}
     * Elimina un cliente. Responde 204 (sin cuerpo) si se eliminó;
     * 404 si no existe; 409 si tiene reservas asociadas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}