package compuertas.modelo.excepciones;

/** Se intentó una conexión que rompe las reglas del circuito (ciclo, índice, elemento ajeno...). */
public class ConexionInvalidaException extends CircuitoException {

    private static final long serialVersionUID = 1L;

    public ConexionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
