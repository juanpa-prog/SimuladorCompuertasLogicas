package compuertas.modelo.excepciones;

/** Error de dominio del simulador. Los mensajes están pensados para mostrarse al usuario. */
public class CircuitoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CircuitoException(String mensaje) {
        super(mensaje);
    }
}
