package compuertas.modelo.excepciones;

/**
 * Error de dominio del simulador. Los mensajes están pensados para mostrarse al usuario.
 *
 * Es NO VERIFICADA (RuntimeException): representa un uso incorrecto del modelo
 * (conexión inválida, nombre duplicado, entrada sin conectar), es decir, la violación
 * de una precondición. El archivo corrupto, en cambio, se modela con la excepción
 * VERIFICADA {@link FormatoCircuitoException}, porque el llamador puede recuperarse
 * (por ejemplo, pidiendo otro archivo).
 */
public class CircuitoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CircuitoException(String mensaje) {
        super(mensaje);
    }

    /** Conserva la causa original al traducir otra excepción a una del dominio. */
    public CircuitoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
