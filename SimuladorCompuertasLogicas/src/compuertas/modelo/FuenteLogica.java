package compuertas.modelo;

/**
 * Contrato común de todo lo que entrega un valor booleano: una entrada
 * lógica o la salida de una compuerta. Gracias a este contrato, la salida
 * de una compuerta puede alimentar la entrada de otra (circuito como red
 * de objetos).
 */
public interface FuenteLogica {

    /** @return el valor lógico actual de esta fuente. */
    boolean getValor();

    /**
     * Indica si esta fuente depende (directa o indirectamente) de otra.
     * Se usa para impedir ciclos al conectar. Por defecto una fuente solo
     * depende de sí misma; las compuertas lo sobrescriben.
     */
    default boolean dependeDe(FuenteLogica otra) {
        return this == otra;
    }
}
