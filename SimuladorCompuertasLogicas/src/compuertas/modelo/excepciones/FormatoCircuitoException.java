package compuertas.modelo.excepciones;

/*
 El contenido de un archivo de circuito es ilegible o inválido 
 */
public class FormatoCircuitoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int linea;

    /** Error sin línea concreta (por ejemplo, archivo vacío). */
    public FormatoCircuitoException(String mensaje) {
        this(mensaje, 0);
    }

    /** @param linea número de línea del archivo (desde 1); 0 si no aplica. */
    public FormatoCircuitoException(String mensaje, int linea) {
        super(componer(mensaje, linea));
        this.linea = linea;
    }

    public FormatoCircuitoException(String mensaje, int linea, Throwable causa) {
        super(componer(mensaje, linea), causa);
        this.linea = linea;
    }

    /** @return la línea con el problema, o 0 si no se conoce. */
    public int getLinea() {
        return linea;
    }

    private static String componer(String mensaje, int linea) {
        return linea > 0 ? "Línea " + linea + ": " + mensaje : mensaje;
    }
}
