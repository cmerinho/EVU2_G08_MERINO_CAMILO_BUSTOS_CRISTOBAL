package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.exception.PersistenciaException;
import cl.ipss.saborgourmet.model.Datos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Única clase que toca el archivo datos.json.
 * Los repositorios (Cliente, Mesa, Reserva) la usan para leer y guardar,
 * así el manejo del archivo está en un solo lugar y no se repite.
 */
@Component
public class AlmacenJson {

    // Herramienta de Jackson que convierte JSON <-> objetos Java
    private final ObjectMapper objectMapper;

    // Ruta completa del archivo datos.json
    private final Path rutaArchivo;

    /**
     * Spring llama a este constructor al arrancar la app y le entrega:
     * - el ObjectMapper (inyección de dependencias)
     * - el valor de "app.datos.ruta" desde application.properties
     */
    public AlmacenJson(ObjectMapper objectMapper,
                       @Value("${app.datos.ruta}") String ruta) {
        this.objectMapper = objectMapper;
        this.rutaArchivo = Path.of(ruta).toAbsolutePath();
        inicializarArchivo();
    }

    /**
     * Si datos.json no existe, lo crea con las tres listas vacías.
     * Si ya existe, no lo toca.
     */
    private void inicializarArchivo() {
        if (Files.notExists(rutaArchivo)) {
            guardar(new Datos());
        }
    }

    /**
     * Lee el archivo completo y lo convierte en un objeto Datos.
     * synchronized: si llegan dos peticiones al mismo tiempo, atiende una a la vez.
     */
    public synchronized Datos leer() {
        // Si alguien borró el archivo con la app corriendo, partimos con datos vacíos
        if (Files.notExists(rutaArchivo)) {
            return new Datos();
        }
        try {
            String contenido = Files.readString(rutaArchivo);
            if (contenido.isBlank()) {
                return new Datos(); // archivo vacío: no hay datos que perder
            }
            return objectMapper.readValue(contenido, Datos.class);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo de datos.", e);
        } catch (JacksonException e) {
            // JSON mal escrito: avisamos y NO sobrescribimos el archivo
            throw new PersistenciaException(
                    "El archivo de datos tiene un formato JSON inválido. No se modificó.", e);
        }
    }

    /**
     * Convierte el objeto Datos en JSON y lo guarda en el archivo.
     * Primero escribe un archivo temporal y después reemplaza el original:
     * si algo falla a mitad de camino, el datos.json anterior queda intacto.
     */
    public synchronized void guardar(Datos datos) {
        Path archivoTemporal = rutaArchivo.resolveSibling(rutaArchivo.getFileName() + ".tmp");
        try {
            // Convierte el objeto en texto JSON con saltos de línea (legible)
            String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(datos);

            Files.writeString(archivoTemporal, json);
            Files.move(archivoTemporal, rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | JacksonException e) {
            throw new PersistenciaException(
                    "No se pudo guardar el archivo de datos. Los datos anteriores se conservan.", e);
        }
    }
}