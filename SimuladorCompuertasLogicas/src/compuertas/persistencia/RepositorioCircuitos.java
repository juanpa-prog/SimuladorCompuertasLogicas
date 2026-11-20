package compuertas.persistencia;

import compuertas.modelo.Circuito;
import compuertas.modelo.excepciones.FormatoCircuitoException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Contrato de persistencia de circuitos. El modelo no sabe nada de archivos:
 * quien guarda y carga es una implementación de esta interfaz (DIP), y el
 * controlador depende solo de este contrato.
 *
 * Se distinguen dos tipos de falla:
 *  - {@link IOException}: no se pudo acceder al archivo (no existe, sin permisos...).
 *  - {@link FormatoCircuitoException}: el archivo se abrió, pero su contenido es inválido.
 */
public interface RepositorioCircuitos {

    /**
     * Guarda el circuito en la ruta indicada.
     *
     * @param posiciones posiciones de los componentes en el lienzo (puede ser null)
     * @throws IOException si no se puede escribir el archivo
     */
    void guardar(Circuito circuito, PosicionesCircuito posiciones, Path ruta) throws IOException;

    /**
     * Lee un circuito completo y lo valida. Devuelve un circuito NUEVO: nunca modifica
     * el circuito que la aplicación esté usando.
     *
     * @throws IOException              si no se puede leer el archivo
     * @throws FormatoCircuitoException si el contenido es inválido
     */
    CircuitoCargado cargar(Path ruta) throws IOException, FormatoCircuitoException;
}
