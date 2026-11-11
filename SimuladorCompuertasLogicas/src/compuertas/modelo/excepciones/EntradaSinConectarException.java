package compuertas.modelo.excepciones;

/** Se intentó evaluar algo que tiene una entrada sin conectar. */
public class EntradaSinConectarException extends CircuitoException {

    private static final long serialVersionUID = 1L;

    public EntradaSinConectarException(String mensaje) {
        super(mensaje);
    }

    public EntradaSinConectarException(String componente, int indice) {
        super("La compuerta «" + componente + "» tiene la entrada " + (indice + 1) + " sin conectar.");
    }
}
